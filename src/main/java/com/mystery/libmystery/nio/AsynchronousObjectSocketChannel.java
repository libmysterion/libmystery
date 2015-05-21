package com.mystery.libmystery.nio;

import com.mystery.libmystery.event.Handler;
import com.mystery.libmystery.event.WeakDualHandler;
import com.mystery.libmystery.event.DualHandler;
import com.mystery.libmystery.event.WeakHandler;
import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import com.mystery.libmystery.bytes.MultiDeserialiser;
import com.mystery.libmystery.event.EventEmitter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsynchronousObjectSocketChannel {

    public static final Logger logger = LoggerFactory.getLogger(AsynchronousObjectSocketChannel.class);

    private final int BUFFER_SIZE;
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer readBuffer;
    private byte[] readData;
    private int readDataPointer = 0;

    private final IObjectDeserialiser deserialiser;
    private final IObjectSerialiser serialiser;

    private final ExecutorService executor;

    private EventEmitter emitter = new EventEmitter();
    private Errback exceptionHandler;

    private final ArrayDeque<PendingWrite> writeQueue = new ArrayDeque<>();

    public AsynchronousObjectSocketChannel(ExecutorService executor, AsynchronousSocketChannel channel, IObjectSerialiser serialiser, IObjectDeserialiser deserialiser, int readBufferSize) {
        this.channel = channel;
        this.serialiser = serialiser;
        this.deserialiser = deserialiser;
        this.executor = executor;
        this.BUFFER_SIZE = readBufferSize;
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public AsynchronousObjectSocketChannel(ExecutorService executor, AsynchronousSocketChannel channel, IObjectSerialiser serialiser, IObjectDeserialiser deserialiser) {
        this(executor, channel, serialiser, deserialiser, 4096);    // 4kb is default for buffer size
    }

    private void nextWrite() {
        this.executor.submit(() -> {
            PendingWrite write;
            synchronized (writeQueue) {
                write = writeQueue.peek();  // this simply cant be null its 1 to 1 with the adding of tasks
                if (write.inProgress) {
                    return;
                }
                write.inProgress = true;
            }
            byte[] serialised = serialiser.serialise(write.getMessage());
            final ByteBuffer byteBuffer = ByteBuffer.wrap(serialised);
            try {
                channel.write(byteBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer result, Void attachment) {
                        // todo... apparently this can be a parial write completed
                        // i.e. 
                        if (result != serialised.length) {
                            logger.warn("partial write detected");
                        }

                        //System.out.println("write complete");
                        synchronized (writeQueue) {
                            writeQueue.remove();
                            //  before it will pick up the next task there needs to have been a thread available to do the removing
                            // thats maybe better than syncing on the jvm callback thread
                        }
                        nextWrite();
                        write.getCallbacks().doSuccess();

                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        logger.debug("write failed", exc);
                        synchronized (writeQueue) {
                            writeQueue.remove();
                        }
                        nextWrite();
                        write.getCallbacks().doError(exc);
                    }
                });
            } catch (RuntimeException e) {
                logger.error("exception writing to channel", e);
            }
        });
    }

    public CallbackErrbackTuple send(Serializable message) {
        CallbackErrbackTuple rv = new CallbackErrbackTuple();
        PendingWrite pw = new PendingWrite(message, rv);
        synchronized (writeQueue) {
            writeQueue.add(pw);
        }
        nextWrite();
        return rv;
    }

    private synchronized void readObjects(int len) {// throws IOException{
        try {
            if (len > 0) {
                byte[] array = readBuffer.array();
                byte[] combinedData = new byte[readDataPointer + len];
                if (this.readData != null) {
                    System.arraycopy(readData, 0, combinedData, 0, readDataPointer);
                }
                System.arraycopy(array, 0, combinedData, readDataPointer, len);
                readBuffer.clear();

                MultiDeserialiser multiDeserialiser = new MultiDeserialiser(deserialiser);
                multiDeserialiser.deserialise(combinedData);
                List<Serializable> objects = multiDeserialiser.getObjects();

//                System.out.println("read " + objects.size() + " objects");
                if (!objects.isEmpty()) {
                    objects.forEach((msg) -> {
                        this.emitter.emit(msg.getClass(), this, msg);
                        this.emitter.emit(msg.getClass(), msg);
                    });

                }

                if (multiDeserialiser.getRemaining() > 0) {
                    this.readData = multiDeserialiser.getSpares();
                    //System.out.println("spares=" + this.readData.length);
                    // todo...use this for a progress updater
                } else {
                    this.readData = null;
                }

//                System.out.println("pointer=" + readDataPointer);
                readDataPointer = multiDeserialiser.getRemaining(); // could simply be lenght of read data...

            }
        } catch (ClassNotFoundException e) {
            logger.error("Could not deserialise message", e);
            // todo callout to some kindof erback which we can atach to client/server
        } catch (Exception e) {
            logger.error("exception reading objects", e);
        }

    }

    public void onDisconnect(WeakHandler<AsynchronousObjectSocketChannel> r) {
        this.emitter.on("dc", r);
    }

    public void onDisconnect(DisconnectHandler r) {
        this.emitter.on("dc", r);
    }

    public void offDisconnect(DisconnectHandler r) {
        this.emitter.off("dc", r);
    }

    private <T extends Serializable> void _onMessage(Class<T> clazz, Handler<T> handler) {
        this.emitter.on(clazz, handler);
    }

    private <T extends Serializable> void _onMessage(Class<T> clazz, DualHandler<AsynchronousObjectSocketChannel, T> handler) {
        this.emitter.on(clazz, handler);
    }

    public <T extends Serializable> void onMessage(Class<T> clazz, WeakHandler<T> handler) {
        this._onMessage(clazz, handler);
    }

    public <T extends Serializable> void onMessage(Class<T> clazz, MessageHandler<T> handler) {
        this._onMessage(clazz, handler);
    }

    public <T extends Serializable> void onMessage(Class<T> clazz, WeakDualHandler<AsynchronousObjectSocketChannel, T> handler) {
        this._onMessage(clazz, handler);
    }

    public <T extends Serializable> void onMessage(Class<T> clazz, ClientMessageHandler<T> handler) {
        this._onMessage(clazz, handler);
    }

    private String hostName = null;
    void startReading() {
        this.executor.submit(() -> {
            String hostString = null;
            try {
                InetSocketAddress remoteAddress = (InetSocketAddress) this.channel.getRemoteAddress();
                hostString = remoteAddress.getHostString();
            } catch (IOException ex) {
                hostString = "unknown-host";
            }
            hostName = hostString;
        });

        this.executor.submit(() -> {
            try {
                channel.read(readBuffer, this, new CompletionHandler<Integer, AsynchronousObjectSocketChannel>() {
                    @Override
                    public void completed(Integer result, AsynchronousObjectSocketChannel attachment) {
                        if (result != -1) {
                            readObjects(result);
                            startReading();
                        } else {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                logger.warn("exception closing channel", e);
                            }
                            emitter.emit("dc", attachment);// if closed by remote
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousObjectSocketChannel attachment) { // if closed locally 
                        emitter.emit("dc", attachment);
                        try {
                            AsynchronousObjectSocketChannel.this.close();
                        } catch (Exception ex) {
                            logger.warn("exception closing AsynchronousObjectSocketChannel", ex);
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("exception reading from channel", e);
            }

        });

    }

    public String getHostName() {
       return hostName;
    }

    void close() throws Exception {
        this.channel.close();
    }

    void setEmitter(EventEmitter e) {
        this.emitter = e;
    }

}

class PendingWrite {

    private Serializable message;
    private CallbackErrbackTuple callbacks;

    boolean inProgress;

    public PendingWrite(Serializable message, CallbackErrbackTuple callbacks) {
        this.message = message;
        this.callbacks = callbacks;
    }

    Serializable getMessage() {
        return message;
    }

    CallbackErrbackTuple getCallbacks() {
        return callbacks;
    }

}

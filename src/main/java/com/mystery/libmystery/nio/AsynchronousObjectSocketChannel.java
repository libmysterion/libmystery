package com.mystery.libmystery.nio;

import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import com.mystery.libmystery.bytes.MultiDeserialiser;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsynchronousObjectSocketChannel {

    private final int BUFFER_SIZE;
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer readBuffer;
    private byte[] readData;
    private int readDataPointer = 0;

    private final IObjectDeserialiser deserialiser;
    private final IObjectSerialiser serialiser;

    private final ExecutorService executor;

    private final HashMap<Class<? extends Serializable>, List<MessageHandler>> handlers = new HashMap<>();
    private Errback exceptionHandler;

    private final ArrayDeque<PendingWrite> writeQueue = new ArrayDeque<>();
    private final List<Runnable> disconnectHandlers = new ArrayList<>();

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
                            try{
                                throw new Exception("partial wirte detecgted");
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                        
                        //System.out.println("write complete");
                        executor.submit(() -> {
                            synchronized (writeQueue) {
                                writeQueue.remove();
                                //  before it will pick up the next task there needs to have been a thread available to do the removing
                                // thats maybe better than syncing on the jvm callback thread
                            }
                            nextWrite();
                            write.getCallbacks().doSuccess();
                        });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        //System.out.println("write failed");
                        executor.submit(() -> {
                            synchronized (writeQueue) {
                                writeQueue.remove();
                            }
                            nextWrite();
                            write.getCallbacks().doError(exc);
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
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
                        List<MessageHandler> handlerList = this.getHandlerList(msg.getClass());
                        if (handlerList.isEmpty()) {
                            throw new IllegalStateException("No handler specified for recieved message of class " + msg.getClass());
                        } else {
                            handlerList.forEach((h) -> h.handleMessage(msg));
                        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<MessageHandler> getHandlerList(Class clazz) {
        if (this.handlers.get(clazz) == null) {
            this.handlers.put(clazz, new ArrayList<>());
        }
        return this.handlers.get(clazz);
    }

    void onDisconnect(Runnable r){
        this.disconnectHandlers.add(r);
    }
    
    public <T extends Serializable> void onMessage(Class<T> clazz, MessageHandler<T> handler) {
        getHandlerList(clazz).add(handler);
    }

    void startReading() {
        this.executor.submit(() -> {
            try {
                channel.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer result, Void attachment) {
                        executor.submit(() -> {
                            if(result != -1){
                                readObjects(result);
                                startReading();
                            } else {
                                try {
                                    channel.close();
                                } catch (IOException ignore) {
                                }
                                disconnectHandlers.forEach((h) -> h.run());
                            }
                        });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        if (exceptionHandler != null) {
                            startReading(); // TODO...i should perhaps stop trying to read here since it will probably go nuts
                            exceptionHandler.onFailure(exc);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    
    void close() throws Exception {
        this.channel.close();
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

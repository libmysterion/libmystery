package com.mystery.libmystery.nio;

import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import com.mystery.libmystery.event.EventEmitter;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioClient implements AutoCloseable {


    public static final Logger logger = LoggerFactory.getLogger(NioClient.class);

    private AsynchronousSocketChannel channel;
    private ExecutorService executor;
    private AsynchronousObjectSocketChannel server;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private final int readBufferSize;

    private final EventEmitter emitter = new EventEmitter();
    

    public NioClient() {
        this(Executors.newCachedThreadPool(), DEFAULT_BUFFER_SIZE);
    }

    public NioClient(ExecutorService executor) {
        this(executor, DEFAULT_BUFFER_SIZE);
    }

    public NioClient(int bufferSize) {
        this(Executors.newCachedThreadPool(), bufferSize);
    }

    public NioClient(ExecutorService executor, int bufferSize) {
        this.executor = executor;
        this.readBufferSize = bufferSize;
    }

    public <T extends Serializable> void onMessage(Class<T> clazz, MessageHandler<T> handler) {
        emitter.on(clazz, handler);
    }

    public void onDisconnect(DisconnectHandler handler) {
        emitter.on("dc", handler);
    }

    public CallbackErrbackTuple connect(InetSocketAddress socketAddress) {
        CallbackErrbackTuple rv = new CallbackErrbackTuple();
        executor.submit(() -> {
            try {
                if (this.channel != null && this.channel.isOpen()) {
                  logger.warn("NioClient is already connected, will autoclose channel and connect to" + socketAddress);
                  this.channel.close(); // auto disconnect if already connected
                }
        
                AsynchronousChannelProvider defaultProvider = AsynchronousChannelProvider.provider();
                AsynchronousChannelGroup group = defaultProvider.openAsynchronousChannelGroup(executor, 0);
                this.channel = AsynchronousSocketChannel.open(group);
                this.server = new AsynchronousObjectSocketChannel(executor, channel, IObjectSerialiser.simple, IObjectDeserialiser.simple);
                server.setEmitter(emitter); // todo ..probs ought to be a constructor arg!
                channel.connect(socketAddress, null, new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void attachment) {
                        logger.debug("connection established with " + socketAddress);
                        server.startReading();
                        rv.doSuccess();
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        logger.debug("failed to establish connection with " + socketAddress);
                        rv.doError(exc);
                    }
                });
            } catch (Exception e) {
                logger.error("exception attempting connection", e);
                rv.doError(e);
            }
        });
        return rv;
    }

    public CallbackErrbackTuple send(Serializable obj) {
        // todo enque these messages and send them once a connection is established
        // we will just NPE like this
        return this.server.send(obj);
    }

    public CallbackErrbackTuple connect(String serverAddress, int port) throws IOException {
        return this.connect(new InetSocketAddress(serverAddress, port));
    }

    @Override
    public void close() throws Exception {
        this.channel.close();
    }

}

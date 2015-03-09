package com.mystery.libmystery.nio;

import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioClient implements AutoCloseable {

    private AsynchronousSocketChannel channel;
    private ExecutorService executor;
    private AsynchronousObjectSocketChannel server;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private final int readBufferSize;
    private final Object handlerMonitor = new Object();
    private final HashMap<Class, List<MessageHandler>> pendingHandlers = new HashMap<>();
    private final List<Runnable> disconnectHandlers = new ArrayList<>();

    
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
        synchronized (handlerMonitor) {
            List<MessageHandler> handlerList = pendingHandlers.get(clazz);
            if (handlerList == null) {
                handlerList = new ArrayList<>();
                pendingHandlers.put(clazz, handlerList);
            }
            handlerList.add(handler);

            if (this.server != null) {
                this.server.onMessage(clazz, handler);
            }
        }
    }

    public void onDisconnect(Runnable callback) {
        disconnectHandlers.add(callback);
        
        if(this.server !=null){
            this.server.onDisconnect(callback);
        }
    }

    public CallbackErrbackTuple connect(InetSocketAddress socketAddress) {
        CallbackErrbackTuple rv = new CallbackErrbackTuple();
        executor.submit(() -> {
            try {
                if (this.channel != null && this.channel.isOpen()) {
                    this.channel.close(); // auto disconnect if already connected
                }
                AsynchronousChannelGroup g;
                AsynchronousChannelProvider.provider();
                
                this.channel = AsynchronousSocketChannel.open();
                this.server = new AsynchronousObjectSocketChannel(executor, channel, IObjectSerialiser.simple, IObjectDeserialiser.simple);
                disconnectHandlers.forEach( (r) -> this.server.onDisconnect(r));
                channel.connect(socketAddress, null, new CompletionHandler<Void, Void>() {
                    @Override
                    public void completed(Void result, Void attachment) {
                       
                        executor.submit(() -> {
                            synchronized (handlerMonitor) {
                                pendingHandlers.forEach((k, v) -> {
                                    v.forEach((h) -> server.onMessage(k, h));   // apply any message handlers
                                });
                            }
                            
                            server.startReading();
                            executor.submit(() -> {
                                rv.doSuccess();
                            });

                        });
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        executor.submit(() -> {
                            rv.doError(exc);
                        });
                    }
                });
            } catch (Exception e) {
                rv.doError(e);
            }
        });
        return rv;
    }

    public CallbackErrbackTuple send(Serializable obj){
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

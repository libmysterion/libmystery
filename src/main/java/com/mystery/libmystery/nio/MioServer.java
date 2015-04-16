package com.mystery.libmystery.nio;

import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MioServer implements AutoCloseable {

    private AsynchronousServerSocketChannel channel;
    private ExecutorService executor;
    private final ConnectionHandlerList connectionHandlers = new ConnectionHandlerList();
    private final ErrorHandlerList errorHandlers = new ErrorHandlerList();
    private final List<AsynchronousObjectSocketChannel> clients = new ArrayList<>();
    private final int clientBufferSize;
    private static final int DEFAULT_CLIENT_BUFFER_SIZE = 4096;

    private boolean manageExecutors = false;

    public MioServer() {
        this(Executors.newCachedThreadPool(MioServer::createThread), DEFAULT_CLIENT_BUFFER_SIZE);
        this.manageExecutors = true;
    }

    private static int threadCount;

    private static Thread createThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("MioServer-" + (threadCount++));
        return t;
    }

    public MioServer(ExecutorService executor) {
        this(executor, DEFAULT_CLIENT_BUFFER_SIZE);
    }

    public MioServer(int clientBufferSize) {
        this(Executors.newCachedThreadPool(MioServer::createThread), clientBufferSize);
        this.manageExecutors = true;
    }

    public MioServer(ExecutorService executor, int clientBufferSize) {
        this.executor = executor;
        this.clientBufferSize = clientBufferSize;
    }

    public void listen(InetSocketAddress socketAddress) throws IOException {
        if (this.channel != null) {
            throw new IllegalStateException("server is already started");
        }
        this.channel = AsynchronousServerSocketChannel.open().bind(socketAddress);
        this.acceptConnection();
    }

    public void listen(int port) throws IOException {
        this.listen("0.0.0.0", port);
    }

    public void listen(String boundAddress, int port) throws IOException {
        this.listen(new InetSocketAddress(boundAddress, port));
    }

    public MioServer onConnection(ConnectionHandler handler) {
        connectionHandlers.put(handler);
        return this;
    }

    // this is really just for disconnect events
    public MioServer onError(ErrorHandler handler) {
        errorHandlers.put(handler);
        return this;
    }

    private AsynchronousObjectSocketChannel addClientChannel(AsynchronousSocketChannel clientChannel) {
        AsynchronousObjectSocketChannel client = new AsynchronousObjectSocketChannel(this.executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple, this.clientBufferSize);

        synchronized (this.clients) {
            this.clients.add(client);
        }
        return client;
    }

    private void dispatchConnectionEvent(AsynchronousObjectSocketChannel client) {
        this.connectionHandlers.handle(client);
    }

    private void beginSocketReading(AsynchronousObjectSocketChannel client) {
        client.startReading();
    }

    public Stream<AsynchronousObjectSocketChannel> getClients() {
        return clients.stream();
    }

    private void onClientDisconnect(AsynchronousObjectSocketChannel client) {
        synchronized (this.clients) {
            this.clients.remove(client);
        }
    }

    private void acceptConnection() {

        channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                acceptConnection();
                AsynchronousObjectSocketChannel client = addClientChannel(channel);
                client.onDisconnect(MioServer.this::onClientDisconnect);
                dispatchConnectionEvent(client);
                beginSocketReading(client); /// now that the message handlers are added
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                errorHandlers.handle(exc);
            }
        });
    }

    @Override
    public void close() throws Exception {
        this.channel.close();
        // disconenect any connected clients
        synchronized (this.clients) {
            for (AsynchronousObjectSocketChannel ch : this.clients) {
                ch.close();
            }
        }

        if (manageExecutors) {
            this.executor.shutdown();
        }
    }

}

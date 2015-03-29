/*

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mystery.libmystery.nio.autojoin;

import com.mystery.libmystery.nio.Callback;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class TcpPortScanningAutoJoinClient {

    private final ExecutorService executor = new ThreadPoolExecutor(0, 24, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private int port;
    private Callback<InetSocketAddress> callback;
    private AsynchronousChannelGroup group;

    public TcpPortScanningAutoJoinClient(int port, Callback<InetSocketAddress> callback) {
        this.port = port;
        this.callback = callback;
        
    }

    public void start() throws IOException {
        List<String> subnetAddresses = getSubnetAddresses();
        
        AsynchronousChannelProvider p = AsynchronousChannelProvider.provider();
        group = p.openAsynchronousChannelGroup(executor, 0);

        subnetAddresses.stream().forEach((a) -> attempt(a));
    }

    private String getMyAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private List<String> getSubnetAddresses() throws UnknownHostException {
        List<String> rv = new ArrayList<>();
        String myAddress = getMyAddress();
        String[] octets = myAddress.split("\\.");

        for (int i = 0; i < 256; i++) {
            String possible = octets[0] + "." + octets[1] + "." + octets[2] + "." + i;
            if (!possible.equals(myAddress)) {
                rv.add(possible);
            }
        }
        return rv;
    }

    private void attempt(String address) {
        executor.submit(() -> {
            try {
                AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
                System.out.println("connecting to " + address + ":" + port);
                InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

                channel.connect(inetSocketAddress, inetSocketAddress, new CompletionHandler<Void, InetSocketAddress>() {

                    @Override
                    public void completed(Void v, InetSocketAddress a) {
                        callback.onSuccess(inetSocketAddress);
                        close();
                    }

                    @Override
                    public void failed(Throwable thrwbl, InetSocketAddress a) {
                        System.out.println("failed: " + address);
                        close();
                    }

                    private void close() {
                        try {
                            channel.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new TcpPortScanningAutoJoinClient(80, (addy) -> {
            System.out.println("found:" + addy);
        }).start();
    }
}

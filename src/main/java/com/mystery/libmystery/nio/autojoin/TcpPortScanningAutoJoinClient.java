package com.mystery.libmystery.nio.autojoin;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.NioClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpPortScanningAutoJoinClient {

    public static final Logger logger = LoggerFactory.getLogger(TcpPortScanningAutoJoinClient.class);

    private final ExecutorService executor = new ThreadPoolExecutor(0, 24, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private int port;
    private Callback<NioClient> callback;

    private CountDownLatch failureLatch;
    private Thread retryThread;
    
    public TcpPortScanningAutoJoinClient(int port, Callback<NioClient> callback) {
        this.port = port;
        this.callback = callback;
    }

    public void start() throws IOException {
        List<String> subnetAddresses = getSubnetAddresses();
        failureLatch = new CountDownLatch(subnetAddresses.size());
        subnetAddresses.stream().forEach((a) -> attempt(a));
        retryThread = new Thread(()-> {
            try {
                
                failureLatch.await();// this gets blocked forever unless all fail
                
                // all have failed so retry
                logger.debug("all connections failed - retry");
                start();            
            } catch (InterruptedException ex) {
                // we interupt if there is a success
                logger.debug("retry thread interrupted - ending");
            } catch (IOException ex) {
                // wierd!
                logger.error("error getting subnet addresses", ex);
            }
        });
        retryThread.start();
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

            logger.debug("connecting to " + address + ":" + port);

            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

            NioClient maybeClient = new NioClient(executor);
            maybeClient.connect(inetSocketAddress).onSucess(() -> {
                logger.debug("connection established with " + address + ":" + port);
                callback.onSuccess(maybeClient);
                retryThread.interrupt();
            }).onError((e) -> {
                
                failureLatch.countDown();
                
                try {
                    maybeClient.close();
                } catch (Exception ex) {
                    logger.warn("exception closing autojoin nioclient", ex);
                }
            });
        });
    }
    
    // use this before any connection has been found, before we hand off the executor to the client
    public void stop() {
        executor.shutdownNow();
        retryThread.interrupt();
    }
}

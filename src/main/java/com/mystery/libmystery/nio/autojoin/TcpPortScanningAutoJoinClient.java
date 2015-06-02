package com.mystery.libmystery.nio.autojoin;

import com.mystery.libmystery.nio.Callback;
import com.mystery.libmystery.nio.NioClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private CountDownLatch latch;
    private Thread rescanThread;
    
    private Map<String, Boolean> connectedMap = new ConcurrentHashMap<>();
    
    public TcpPortScanningAutoJoinClient(int port, Callback<NioClient> callback) {
        this.port = port;
        this.callback = callback;
    }

    
    public void start() throws IOException {
        List<String> subnetAddresses = getSubnetAddresses();
        subnetAddresses.stream().forEach((ip) -> connectedMap.put(ip, false));
        performScan();
    }
    
    private void performScan(){
    
        latch = new CountDownLatch(connectedMap.size());
        connectedMap.entrySet().stream()
                .filter((e) -> !e.getValue())
                .forEach((e) -> attempt(e.getKey()));
       
        // once all have been processed then start again
        // but do not attempt to connect to ones which we got a connection already
        rescanThread = new Thread(()-> {
            try {
                latch.await();// this gets blocked until all have succeeded or failed
                logger.debug("retry port scan");
                performScan();            
            } catch (InterruptedException ex) {
                logger.debug("retry thread interrupted - ending");
            }
        });
        rescanThread.start();
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

            logger.trace("connecting to " + address + ":" + port);

            InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

            NioClient maybeClient = new NioClient(executor);
            maybeClient.connect(inetSocketAddress).onSucess(() -> {
                logger.debug("connection established with " + address + ":" + port);
                callback.onSuccess(maybeClient);
                connectedMap.put(address, Boolean.TRUE);
                latch.countDown();
                maybeClient.onDisconnect((c)-> {
                    connectedMap.put(address, Boolean.FALSE);
                });
            }).onError((e) -> {

                connectedMap.put(address, Boolean.FALSE);
                latch.countDown();
                
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
        rescanThread.interrupt();
    }
}

package com.mystery.libmystery.nio.autojoin;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AutoJoinerServer implements Runnable {
    
    private static final int PORT = 4446;
    private boolean stop;
    private final int port;
    private final ExecutorService executor;
    private static final int INTERVAL = 1500;
    public static final String SEP = ":#";
     
    private String appId;
    
    public AutoJoinerServer(String appId, int port) {
        this.appId = appId;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    protected final void broadcast(String message){
        try(MulticastSocket socket = new MulticastSocket(PORT)){
            byte[] buf = message.getBytes();
            InetAddress group = InetAddress.getByName("228.5.6.7");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void run(){

        String message = appId + SEP + port;
        broadcast(message);

        try {
            Thread.sleep(INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        if(!this.stop){
            executor.submit(this);
        }
    }
    
    
    public void stop(){
        this.stop = true;
        this.executor.shutdown();
    }
    
    public void start(){
        this.executor.submit(this);
    }
    
 
    
    public static void main(String[] args) {
        new AutoJoinerServer("myAPp", 4124).start();
    }
}

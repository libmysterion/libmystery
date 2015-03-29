package com.mystery.libmystery.nio.autojoin;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UDPMulticastAutoJoinerServer implements Runnable {
    
    private static final int PORT = 4446;
    private boolean stop;
    private final int appPort;
    private final ExecutorService executor;
    private static final int INTERVAL = 1500;
    public static final String SEP = ":#";
     
    private String appId;
    
    public UDPMulticastAutoJoinerServer(String appId, int appPort) {
        this.appId = appId;
        this.appPort = appPort;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    protected final void broadcast(String message){
        try(DatagramSocket socket = new DatagramSocket(PORT)){
            byte[] buf = message.getBytes();
            InetAddress group = InetAddress.getByName("224.0.0.3");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
            
            System.out.println("sent: " + message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void run(){

        System.out.println("running autojoin loop...");
        String message = appId + SEP + appPort;
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
        new UDPMulticastAutoJoinerServer("test", 4124).start();
    }
}

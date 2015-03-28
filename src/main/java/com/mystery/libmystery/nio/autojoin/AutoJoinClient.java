package com.mystery.libmystery.nio.autojoin;

import com.mystery.libmystery.nio.Callback;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class AutoJoinClient extends Thread {

    boolean stop = false;

    private String app;
    private Callback<InetSocketAddress> callback;

    public AutoJoinClient(String app, Callback<InetSocketAddress> callback) {
        this.app = app;
        this.callback = callback;
    }    
    
    public void stopAutoJoin() {
        stop = true;
    }

    @Override
    public void run() {
        do {
            try {
                MulticastSocket socket = new MulticastSocket(4446);
                InetAddress group = InetAddress.getByName("228.5.6.7");
                socket.joinGroup(group);
                
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData());
                
                String[] split = received.split(AutoJoinerServer.SEP);
                if (split.length==2) {
                    String msgApp = split[0];
                    
                    if(app.equals(msgApp)){
                        String portRaw = split[1];
                        int port = Integer.parseInt(portRaw);
                        this.callback.onSuccess(new InetSocketAddress(packet.getAddress(), port));                        
                        stop = true;
                    }
                }      
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } while (!stop);
    }
}

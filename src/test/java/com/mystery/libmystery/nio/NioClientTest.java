package com.mystery.libmystery.nio;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NioClientTest {

    public NioClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    int count;

    @Before
    public void setUp() {
        count = 0;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConnectingSuccess() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        try (MioServer server = new MioServer();
                NioClient client = new NioClient();) {
            server.listen(port);
            client.connect("localhost", port).onSucess(() -> {
                synchronized (monitor) {
                    monitor.notify();
                }
            });
            synchronized (monitor) {
                monitor.wait();
            }
        }
    }

    @Test
    public void testConnectingFailure() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        try (NioClient client = new NioClient();) {
            client.connect("localhost", port).onError((a) -> {
                synchronized (monitor) {
                    monitor.notify();
                }
            });
            synchronized (monitor) {
                monitor.wait();
            }
        }
    }

    @Test
    public void testConnectingDisconnecting() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        MioServer server = new MioServer();
        NioClient client = new NioClient();
        server.listen(port);

        client.connect("localhost", port)
                .onSucess(() -> {
                    System.out.println("connect success");

                    synchronized (monitor) {
                        monitor.notify();
                    }
                });

        client.onDisconnect(() -> {
            synchronized (monitor) {
                monitor.notify();
            }
        });

        synchronized (monitor) {
            monitor.wait(); // wait for connect success
        }

        server.close();

        synchronized (monitor) {
            monitor.wait(); // wait for disconnect event
        }
    }

    @Test
    public void testPingPongClient() throws Exception {
        System.out.println("testPingPongClient");
        final Object monitor = new Object();
        int port = 1009;
        try (MioServer server = new MioServer();
                NioClient cli = new NioClient();) {
            int msgCount = 5000;
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    client.send(msg);
                });
            });
            server.listen(port);
            cli.onMessage(TestMessage.class, (msg) -> {
                cli.send(msg);
                synchronized(monitor){
                    if (count++ == msgCount) {
                        monitor.notify();
                    }
                }
            });

            cli.connect("localhost", port).onSucess(() -> {
                cli.send(new TestMessage(2, 7));
            });

            long start = System.currentTimeMillis();
            synchronized (monitor) {
                monitor.wait();
            }
            long fin = System.currentTimeMillis();
            System.out.println("pingpong client test takes " + (fin - start) + "ms for " + msgCount + " ping pongs (round trips i.e. 2 messages sent) so " + (msgCount * 2) + " messages were sent....get it?");
            // pingpong client test takes 3265ms for 5000 ping pongs (round trips i.e. 2 messages sent) so 10000 messages were sent....get it?
            // pingpong client test takes 3347ms for 5000 ping pongs (round trips i.e. 2 messages sent) so 10000 messages were sent....get it?
        }

       // todo auto reconnect option on NioClient
        // needs lots of tests to make sure everything still works after reconnecting
        // enqueu messages if attempt to send before connection is established
    }
}

package com.mystery.libmystery.nio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.fail;

public class NioClientTest {

     public static final Logger logger = LoggerFactory.getLogger(NioClientTest.class);

    private static ExecutorService executor;

    public NioClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        executor = Executors.newCachedThreadPool((r)->{
            return new Thread(r, "NioClientTest");
        });
    }

    @AfterClass
    public static void tearDownClass() {
        executor.shutdown();
    }

    int count;

    @Before
    public void setUp() {
        count = 0;
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 5000)
    public void testConnectingSuccess() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        try (MioServer server = new MioServer(executor);
                NioClient client = new NioClient(executor);) {
            server.listen(port);
            client.connect("localhost", port).onSucess(() -> {
                synchronized (monitor) {
                    monitor.notify();
                }
            }).onError((e) ->{
                logger.error("testConnectingSuccess has failed", e);
                fail("onError should not be called here");
            });
            synchronized (monitor) {
                monitor.wait();
            }
        }
    }

    @Test(timeout = 3500)
    public void testConnectingFailure() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        try (NioClient client = new NioClient(executor);) {
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

    @Test(timeout = 5000)
    public void testConnectingDisconnecting() throws Exception {
        final Object monitor = new Object();
        int port = 999;
        MioServer server = new MioServer(executor);
        NioClient client = new NioClient(executor);
        server.listen(port);

        client.onDisconnect((c) -> {
            logger.debug("client.onDisconnect");
            synchronized (monitor) {
                monitor.notify();
            }
        });

        client.connect("localhost", port)
                .onSucess(() -> {
                     logger.debug("client.connect.onSuccess");
                    synchronized (monitor) {
                        monitor.notify();
                    }
                });

        synchronized (monitor) {
            monitor.wait(); // wait for connect success
        }

        Thread.sleep(1000); // I have no idea why i need this sleep...I really shouldnt, but seems to make this test stable, and I just dont care anymore
        server.close();
   
        synchronized (monitor) {
            monitor.wait(); // wait for disconnect event
        }
    }

    @Test(timeout = 3500)
    public void testPingPongClient() throws Exception {
        System.out.println("testPingPongClient");
        final Object monitor = new Object();
        int port = 1009;
        try (MioServer server = new MioServer(executor);
                NioClient cli = new NioClient(executor);) {
            int msgCount = 5000;
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    client.send(msg);
                });
            });
            server.listen(port);
            cli.onMessage(TestMessage.class, (msg) -> {
                cli.send(msg);
                synchronized (monitor) {
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
            // after some tweaks.... prebably due to removal of all the thread switching
            // pingpong client test takes 954ms for 5000 ping pongs (round trips i.e. 2 messages sent) so 10000 messages were sent....get it?
            // pingpong client test takes 792ms for 5000 ping pongs (round trips i.e. 2 messages sent) so 10000 messages were sent....get it?
        }

        // todo auto reconnect option on NioClient
        // needs lots of tests to make sure everything still works after reconnecting
        // enqueu messages if attempt to send before connection is established
    }
}

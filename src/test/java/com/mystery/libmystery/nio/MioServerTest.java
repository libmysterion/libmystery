package com.mystery.libmystery.nio;

import com.mystery.libmystery.bytes.IObjectDeserialiser;
import com.mystery.libmystery.bytes.IObjectSerialiser;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MioServerTest {

    public MioServerTest() {
    }

    static ExecutorService executor;

    @BeforeClass
    public static void setUpClass() {
        executor = Executors.newCachedThreadPool((r) -> {
            return new Thread(r, "MioServerTest");
        });
    }

    @AfterClass
    public static void tearDownClass() {
        executor.shutdownNow();
    }

    @Before
    public void setUp() {
        count = 0;
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 5000)
    public void testServerErrorEvent() throws Exception {
        System.out.println("testServerErrorEvent");
        final Object monitor = new Object();
        int port = 1000;
        try (MioServer server = new MioServer(executor);) {
            server.onConnection((client) -> {

            }).onError((ex) -> {
                assertThat(Thread.currentThread().getName(), is("MioServerTest"));
                assertNotNull(ex);
                synchronized (monitor) {
                    monitor.notify();
                }
            });
            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
        }// IMPORTANT - AutoCloseable.close is invoked here
        // when that happens the pending accept callsback with failed cos the channel closed

        synchronized (monitor) {
            monitor.wait();
        }

    }

    /**
     * Test of onConnection method, of class MioServer.
     */
    @Test(timeout = 3500)
    public void testConnectingSingleClient() throws Exception {
        System.out.println("testConnectingSingleClient");
        final Object monitor = new Object();
        int port = 1000;
        try (MioServer server = new MioServer(executor);) {
            server.onConnection((client) -> {

                assertThat(Thread.currentThread().getName(), is("MioServerTest"));

                synchronized (monitor) {
                    monitor.notify();
                }
            });
            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            synchronized (monitor) {
                monitor.wait();
            }

        }

    }

    int count = 0;

    @Test(timeout = 3500)
    public void testConnecting_10_Clients() throws Exception {
        System.out.println("testConnecting_10_Clients");
        final Object monitor = new Object();
        int port = 1001;
        int clientCount = 10;
        try (MioServer server = new MioServer(executor);) {
            server.onConnection((client) -> {
                synchronized (monitor) {
                    if (clientCount == ++count) {
                        monitor.notify();
                    }
                }
            });
            server.listen(port);

            for (int i = 0; i < clientCount; i++) {
                AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
                Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            }

            synchronized (monitor) {
                monitor.wait();
            }
        }
    }

    @Test(timeout = 3500)
    // this test seems to hang at around 9700 connection if the buffer size is not specified
    // based on that ....probs a memeory issue
    //im worried thate error is never reported....seems i might be easting it somewhere
    public void testConnecting_10000_Clients() throws Exception {
        System.out.println("testConnecting_10000_Clients");
        final Object monitor = new Object();
        int port = 1002;
        int clientCount = 10000;
        try (MioServer server = new MioServer(executor, 512);) {
            server.onConnection((client) -> {
                synchronized (monitor) {
                    if (clientCount == ++count) {
                        monitor.notify();
                    }
                }
            });
            server.listen(port);

            long st = System.currentTimeMillis();

            for (int i = 0; i < clientCount; i++) {
                AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
                Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            }

            synchronized (monitor) {
                monitor.wait();
            }
            long fin = System.currentTimeMillis();
            System.out.println(clientCount + " connections made in " + (fin - st) + "ms");
        }
    }

    @Test(timeout = 3500)
    public void testSendingSingleMessage() throws Exception {
        System.out.println("testSendingSingleMessage");
        final Object monitor = new Object();
        int port = 1003;
        try (MioServer server = new MioServer(executor);) {
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    assertThat(Thread.currentThread().getName(), is("MioServerTest"));

                    synchronized (monitor) {
                        monitor.notify();
                    }
                });
            });

            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, client, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.send(new TestMessage(5, 8));

            synchronized (monitor) {
                monitor.wait();
            }
        }

    }

    @Test(timeout = 3500)
    public void testSendingSingle_Large_Message() throws Exception {
        System.out.println("testSendingSingle_Large_Message");
        final Object monitor = new Object();
        int port = 1004;
        byte[] b = new byte[1024 * 1024 * 8]; //8Mb message
        java.util.Random r = new java.util.Random();
        r.nextBytes(b);

        try (MioServer server = new MioServer(executor, 1024 * 64);) { // give this server a 64kb buffer
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    assertArrayEquals(msg.b, b);
                    synchronized (monitor) {
                        monitor.notify();
                    }
                });
            });

            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, client, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            TestMessage t = new TestMessage(5, 8);
            t.b = b;
            clientObjectChannel.send(t);

            long start = System.currentTimeMillis();

            synchronized (monitor) {
                monitor.wait();
            }
            long fin = System.currentTimeMillis();
            System.out.println("sent " + (b.length / 1024 / 1024) + " Mb message in " + (fin - start) + "ms");
        }

    }

    @Test(timeout = 3500)
    public void testSending_10_Message() throws Exception {
        System.out.println("testSending_10_Message");
        final Object monitor = new Object();
        int port = 1005;
        try (MioServer server = new MioServer(executor);) {
            int msgCount = 10;
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> { // there is a reace here...if the server is really busy
                    // then a connection can actually come after a message read
                    synchronized (monitor) {
                        count++;
                        if (count == msgCount) {
                            monitor.notify();
                        }
                    }
                });
            });

            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, client, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            for (int i = 0; i < msgCount; i++) {
                clientObjectChannel.send(new TestMessage(5, 8));
            }

            synchronized (monitor) {
                monitor.wait();
            }

        }

    }

    @Test
    public void testSending_1000_Message() throws Exception {
        System.out.println("testSending_1000_Message");
        final Object monitor = new Object();
        int port = 1006;
        try (MioServer server = new MioServer(executor);) {
            int msgCount = 1000;
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    synchronized (monitor) {
                        count++;
                        //System.out.println("server got msg " + count);
                        if (count == msgCount) {
                            monitor.notify();
                        }
                    }
                });
            });

            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, client, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            for (int i = 0; i < msgCount; i++) {
                clientObjectChannel.send(new TestMessage(5, 8));
            }

            synchronized (monitor) {
                monitor.wait();
            }
        }

    }

    @Test(timeout = 3500)
    public void testSending_10_000_Message() throws Exception {
        System.out.println("testSending_10_000_Message");
        final Object monitor = new Object();
        int port = 1007;
        try (MioServer server = new MioServer(executor);) {
            int msgCount = 10000;
            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    synchronized (monitor) {
                        count++;
//                    System.out.println("server got msg " + count);
                        if (count == msgCount) {
                            monitor.notify();
                        }
                    }
                });
            });

            server.listen(port);
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

            Future<Void> connect = client.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, client, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            long start = System.currentTimeMillis();

            for (int i = 0; i < msgCount; i++) {
                clientObjectChannel.send(new TestMessage(5, 8));
            }

            synchronized (monitor) {
                monitor.wait();
            }
            long fin = System.currentTimeMillis();
            System.out.println("sent " + msgCount + " messages in " + (fin - start) + "ms");

        }

    }

    @Test(timeout = 3500)
    public void testClientResponding() throws Exception {
        System.out.println("testClientResponding");
        final Object monitor = new Object();
        int port = 1008;
        try (MioServer server = new MioServer(executor);) {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    System.out.println("gets the message");
                    client.send(msg);
                });
            });

            server.listen(port);
            Future<Void> connect = clientChannel.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.onMessage(TestMessage.class, (m) -> {
                synchronized (monitor) {
                    monitor.notify();
                }
            });

            clientObjectChannel.startReading(); // d'oh again...

            clientObjectChannel.send(new TestMessage(5, 8));

            synchronized (monitor) {
                monitor.wait();
            }
        }

    }

    @Test(timeout = 3500)
    public void testPingPongServer() throws Exception {
        System.out.println("testPingPongServer");
        final Object monitor = new Object();
        int port = 1009;
        try (MioServer server = new MioServer(executor);) {
            int msgCount = 1000;

            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {
                    //System.out.println("ping");
                    client.send(msg);
                });
            });

            server.listen(port);
            Future<Void> connect = clientChannel.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.onMessage(TestMessage.class, (m) -> {
                //System.out.println("pong");
                clientObjectChannel.send(m);

                synchronized (monitor) {
                    if (count++ == msgCount) {
                        monitor.notify();
                    }
                }

            });

            clientObjectChannel.startReading(); // d'oh again...

            clientObjectChannel.send(new TestMessage(5, 8));

            long start = System.currentTimeMillis();

            synchronized (monitor) {
                monitor.wait();
            }

            long fin = System.currentTimeMillis();

            System.out.println("pingpong test takes " + (fin - start) + "ms for " + msgCount + " ping pongs (round trips i.e. 2 messages sent) so " + (msgCount * 2) + " messages were sent....get it?");

        }

    }

    @Test(timeout = 3500)
    public void testClientDisconnecting() throws Exception {
        System.out.println("testClientDisconnecting");
        final Object monitor = new Object();
        int port = 1010;
        try (MioServer server = new MioServer(executor);) {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            server.onConnection((client) -> {
                client.onMessage(TestMessage.class, (msg) -> {

                    System.out.println("gets the message");

                    try {
                        clientChannel.close();
//                    Thread.currentThread().sleep(400);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // ioexception might actually be expected... maybe
                    }

                    client.send(msg).onError((throwable) -> {
                        System.out.println("it fails");
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    });
                });
            });

            server.listen(port);
            Future<Void> connect = clientChannel.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.onMessage(TestMessage.class, (m) -> {
                System.out.println("THIS SHOULD NOT PRINT"); // BUT I CANT THINK HOW TO FAIL THE SPEC
            });

            clientObjectChannel.startReading();
            clientObjectChannel.send(new TestMessage(5, 8));

//        .onSucess((s)->{
//            System.out.println("client sent first message");
//        });
            synchronized (monitor) {
                monitor.wait();
            }
        }

    }

    @Test(timeout = 3500)
    public void testOnDisconnect() throws Exception {
        System.out.println("testOnDisconnect");
        final Object monitor = new Object();
        int port = 1010;
        try (MioServer server = new MioServer(executor);) {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            server.onConnection((client) -> {

                client.onDisconnect((c) -> {

                    assertThat(Thread.currentThread().getName(), is("MioServerTest"));

                    synchronized (monitor) {
                        monitor.notify();
                    }
                });

            });

            server.listen(port);
            Future<Void> connect = clientChannel.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            Thread.sleep(500);
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.startReading();
            clientObjectChannel.send(new TestMessage(5, 8));

            clientChannel.close();

            synchronized (monitor) {
                monitor.wait();
            }
        }
    }

    @Test(timeout = 3500)
    public void testdisconnectClient() throws Exception {
        System.out.println("testDisconnectClient");
        final Object monitor = new Object();
        int port = 1010;
        try (MioServer server = new MioServer(executor);) {
            AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open();

            server.onConnection((client) -> {

                client.onMessage(TestMessage.class, (m) -> {
                    try {
                        server.disconnectClient(client);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                client.onDisconnect((c) -> {
                    System.out.println("server disconnect event");
                    synchronized (monitor) {
                        monitor.notify();
                    }

                });

            });

            server.listen(port);
            Future<Void> connect = clientChannel.connect(new InetSocketAddress("localhost", port));
            Void get = connect.get(); // block here until connection is all done
            Thread.sleep(500);
            AsynchronousObjectSocketChannel clientObjectChannel = new AsynchronousObjectSocketChannel(executor, clientChannel, IObjectSerialiser.simple, IObjectDeserialiser.simple);

            clientObjectChannel.onDisconnect((e) -> {
                System.out.println("client disconnect event");
                synchronized (monitor) {
                    monitor.notify();
                }
            });

            clientObjectChannel.startReading();
            clientObjectChannel.send(new TestMessage(5, 8));

            synchronized (monitor) {
                monitor.wait(); // wait once for client event
            }

            synchronized (monitor) {
                monitor.wait(); // and again for server event
            }
        }
    }

    // todo
    // add tests for lots of users * lots of messages
    // add the mioclient with the nice API where you get a server passed to your onconnect callback
    // stick nio onto github
    // add ability to tell server to send a message to a specific client
    // implement synergy in java
    // add broadcast function to server
    // add key exchange logic + encryption serialisation
}

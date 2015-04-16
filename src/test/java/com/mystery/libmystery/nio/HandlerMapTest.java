package com.mystery.libmystery.nio;

import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HandlerMapTest {

    private boolean callbackCalled = false;

    @Before
    public void setUp() {
        callbackCalled = false;
    }

    @Test
    public void message_handlers_should_handle_a_message() {
        HandlerMap map = new HandlerMap();
        TestMessage testMessage = new TestMessage(5, 8);
        map.put(TestMessage.class, (t) -> {
            assertThat(t, is(testMessage));
            callbackCalled = true;
        });
        map.handle(testMessage);
        assertThat(callbackCalled, is(true));
    }
    
    @Test
    public void weak_message_handlers_should_handle_a_message_if_not_collected() {
        HandlerMap map = new HandlerMap();
        TestMessage testMessage = new TestMessage(5, 8);
        map.put(TestMessage.class, new WeakHandler((t) -> {
            assertThat(t, is(testMessage));
            callbackCalled = true;
        }));
        map.handle(testMessage);
        assertThat(callbackCalled, is(true));
    }

    @Test
    public void message_handlers_should_not_handle_a_message_once_removed() {
        HandlerMap map = new HandlerMap();
        MessageHandler<TestMessage> handler = (t) -> {
            callbackCalled = true;
        };
        map.put(TestMessage.class, handler);
        map.remove(handler);
        map.handle(new TestMessage(5, 8));
        assertThat(callbackCalled, is(false));
    }

    private void attachCollectableHandler(HandlerMap map) {
        MessageHandler<TestMessage> handler = (t) -> {
            callbackCalled = true;
        };
        map.put(TestMessage.class, new WeakHandler(handler));
    }

    @Test
    public void weak_message_handlers_should_remove_itself() {
        HandlerMap map = new HandlerMap();
        attachCollectableHandler(map);
        System.gc();    // we need to force the gc to collect the handler
        map.handle(new TestMessage(5, 8));
        
        assertThat(callbackCalled, is(false));
    }

}

package com.mystery.libmystery.event;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class EventEmmitterTest {
    
    boolean called;
    @Before
    public void setUp() {
        called = false;
    }
   
    @Test
    public void testOnString_should_call_the_callback() {
        EventEmitter e = new EventEmitter();
        e.on("myEvent", (arg) -> {
            called = true;
        });
        e.emit("myEvent", null);
        assertTrue(called);
    }


    @Test
    public void testOnString_should_pass_null_argument() {
        EventEmitter e = new EventEmitter();
        e.on("myEvent", (arg) -> {
            assertNull(arg);
        });
        e.emit("myEvent", null);
    }

    class TestClass {
        int x = 3;
    }
    
    @Test
    public void testOnString_should_pass__not_null_argument() {
        EventEmitter e = new EventEmitter();
        e.on("myEvent", (TestClass arg) -> {
            assertNotNull(arg);
            assertEquals(3, arg.x);
        });
        e.emit("myEvent", new TestClass());
    }
    
    @Test
    public void testOnClass_should_call_the_callback() {
        EventEmitter e = new EventEmitter();
        e.on(TestClass.class, (arg) -> {
            called = true;
        });
        e.emit(new TestClass().getClass(), null);
        assertTrue(called);
    }


    @Test
    public void testOnClass_should_pass_null_argument() {
        EventEmitter e = new EventEmitter();
        e.on(TestClass.class, (arg) -> {
            assertNull(arg);
        });
        e.emit(TestClass.class, null);
    }
    
    @Test
    public void testOnClass_should_pass__not_null_argument() {
        EventEmitter e = new EventEmitter();
        e.on(TestClass.class, (TestClass arg) -> {
            assertNotNull(arg);
            assertEquals(3, arg.x);
        });
        Object msg = new TestClass();
        e.emit(msg.getClass(), msg);
    }

    private void addCollectableWeakHandler(EventEmitter emitter){
        emitter.on(TestClass.class, new WeakHandler<>((TestClass arg) -> {
            called = true;
        }));
    }
    
    @Test
    public void should_remove_weak_handlers() {
        EventEmitter e = new EventEmitter();
        addCollectableWeakHandler(e);
        System.gc();
        Object msg = new TestClass();
        e.emit(msg.getClass(), msg);    // would go into HandlerList an HandlerList would remove the collected handler
        e.emit(msg.getClass(), msg);    // would eval true on the isEmpty block
        e.emit(msg.getClass(), msg);    // would eval true on the == null block and do nothing
        
        assertFalse(called);
    }
    
    
    @Test
    public void testOnString_should_call_the_dual_callback() {
        EventEmitter e = new EventEmitter();
        e.on("myEvent", (arg1, arg2) -> {
            called = true;
        });
        e.emit("myEvent", null, null);
        assertTrue(called);
    }
    

    @Test
    public void testOnString_should_pass_the_arguments() {
        EventEmitter e = new EventEmitter();
        e.on("myEvent", (TestClass arg1, TestClass arg2) -> {
            assertNotNull(arg1);
            assertNotNull(arg2);
        });
        e.emit("myEvent", new TestClass(), new TestClass());
    }
    
     private void addCollectableWeakDualHandler(EventEmitter emitter){
        emitter.on(TestClass.class, new WeakDualHandler<>((TestClass arg1, TestClass arg2) -> {
            called = true;
        }));
    }
     
    @Test
    public void should_remove_weak_dual_handlers() {
        EventEmitter e = new EventEmitter();
        addCollectableWeakDualHandler(e);
        System.gc();
        Object msg = new TestClass();
        e.emit(msg.getClass(), msg);    // would go into HandlerList an HandlerList would remove the collected handler
        e.emit(msg.getClass(), msg);    // would eval true on the isEmpty block
        e.emit(msg.getClass(), msg);    // would eval true on the == null block and do nothing
        
        assertFalse(called);
    }

    
    
}

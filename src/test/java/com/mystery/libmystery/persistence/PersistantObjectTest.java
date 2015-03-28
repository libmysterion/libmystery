
package com.mystery.libmystery.persistence;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class PersistantObjectTest {
    
    public PersistantObjectTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of save method, of class PersistantObject.
     */
    @Test
    public void testSave() throws Exception {
        
        TestPersistantObject t = new TestPersistantObject();
        
        t.setField1("hello");
        t.setField2("world");
        
        t.setNumber(568);
        
        ArrayList<String> l = new ArrayList<>();
        
        l.add("one");
        l.add("two");
        l.add("three");
        l.add("phoar");
        
        t.setItems(l);
        
        
        t.save();
       
        TestPersistantObject read = new TestPersistantObject();
        
       
        assertEquals("hello", read.getField1());
        assertEquals("world", read.getField2());
        
        assertEquals(568, read.getNumber());
        assertEquals("one", read.getItems().get(0));
        assertEquals("two", read.getItems().get(1));
        assertEquals("three", read.getItems().get(2));
        assertEquals("phoar", read.getItems().get(3));
       
    }
    
}

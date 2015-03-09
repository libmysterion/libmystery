
package com.mystery.libmystery.bytes;

import java.io.Serializable;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;


public class ByteFunctionsTest {
    
    public ByteFunctionsTest() {
    }
    
    
    /**
     * Test of serialize method, of class ByteFunctions.
     */
    @Test
    public void testSerialize() throws Exception {
        System.out.println("serialize");
        TestMessage obj = new TestMessage(1, 2, 3434L, 5.8d);
  
        byte[] result = ByteFunctions.serialize(obj);
        TestMessage deSerialize = ByteFunctions.deSerialize(result, TestMessage.class);
        
        assertEquals(obj, deSerialize);
        
        
    }
    
    @Test
    public void testSerializeAByteArray() throws Exception {
        System.out.println("serialize");
        int len = 230;
        byte[] ar = new byte[len];
        for(int i = 0; i < len;i++){
            ar[i] = (byte)i;
        }
        byte[] result = ByteFunctions.serialize(ar);
        
        byte[] deSerialize = ByteFunctions.deSerialize(result, null);
        
        Assert.assertArrayEquals(ar, deSerialize);
        
        
    }


    

    /**
     * Test of integerToBytes method, of class ByteFunctions.
     */
    @Test
    public void testIntegerToBytes() {
        int a = 8;
        byte[] result = ByteFunctions.integerToBytes(a);
        int val = ByteFunctions.bytesToInteger(result);
        assertEquals(val, a);
        
    }


    /**
     * Test of longToBytes method, of class ByteFunctions.
     */
    @Test
    public void testLongToBytes() {
        long a = 8;
        byte[] result = ByteFunctions.longToBytes(a);
        long val = ByteFunctions.bytesToLong(result);
        assertEquals(val, a);
    }

    /**
     * Test of shortToBytes method, of class ByteFunctions.
     */
    @Test
    public void testShortToBytes() {
        short a = 8;
        byte[] result = ByteFunctions.shortToBytes(a);
        short val = ByteFunctions.bytesToShort(result);
        assertEquals(val, a);
    }

    /**
     * Test of doubleToBytes method, of class ByteFunctions.
     */
    @Test
    public void testDoubleToBytes() {
        double a = 8.678d;
        byte[] result = ByteFunctions.doubleToBytes(a);
        double val = ByteFunctions.bytesToDouble(result);
        assertEquals(val, a, 0);
    }
    
}


class TestMessage implements Serializable {

    int x;
    int y;
    long dong;
    double precision;

    public TestMessage() {
    }

    public TestMessage(int x, int y, long dong, double precision) {
        this.x = x;
        this.y = y;
        this.dong = dong;
        this.precision = precision;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getDong() {
        return dong;
    }

    public void setDong(long dong) {
        this.dong = dong;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestMessage other = (TestMessage) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.dong != other.dong) {
            return false;
        }
        if (Double.doubleToLongBits(this.precision) != Double.doubleToLongBits(other.precision)) {
            return false;
        }
        return true;
    }
    
    
    
    

}
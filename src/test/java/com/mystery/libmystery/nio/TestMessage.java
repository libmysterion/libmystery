package com.mystery.libmystery.nio;

import java.io.Serializable;

public class TestMessage implements Serializable {

    public TestMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public byte[] b;
    public int x;
    public int y;
}

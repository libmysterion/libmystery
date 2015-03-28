
package com.mystery.libmystery.bytes;

import static com.mystery.libmystery.bytes.ByteFunctions.bytesToInteger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MultiDeserialiser {

    private IObjectDeserialiser deserialiser;

    private int remaining;
    private byte[] spares;
    private final List<Serializable> objects = new ArrayList<>();

    public MultiDeserialiser(IObjectDeserialiser deseraliser){
        this.deserialiser = deseraliser;
    }

    public int getRemaining() {
        return remaining;
    }

    public byte[] getSpares() {
        return spares;
    }

    public List<Serializable> getObjects() {
        return objects;
    }

    public void deserialise(byte[] bytes) throws ClassNotFoundException{
        int pos = 0;
        while (pos != bytes.length) {

            if (pos + 4 > bytes.length) {
                remaining = bytes.length - pos;
                spares = new byte[remaining];
                System.arraycopy(bytes, pos, spares, 0, spares.length);
                return;
            }

            byte[] lenBytes = new byte[4];
            System.arraycopy(bytes, pos, lenBytes, 0, 4);
            pos += 4;
            int len = bytesToInteger(lenBytes);

            if (pos + len > bytes.length) {
                pos -= 4; // i need those len bytes
                remaining = bytes.length - pos;
                spares = new byte[remaining];
                System.arraycopy(bytes, pos, spares, 0, spares.length);
                return;
            }
            byte[] objectBytes = new byte[len];
            System.arraycopy(bytes, pos, objectBytes, 0, len);
            pos += len;

            Serializable read = this.deserialiser.deserialise(objectBytes);
            objects.add(read);
        }
    }

}

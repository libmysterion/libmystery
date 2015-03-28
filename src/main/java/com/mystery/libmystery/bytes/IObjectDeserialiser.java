package com.mystery.libmystery.bytes;

import java.io.Serializable;

public interface IObjectDeserialiser {
    
    public Serializable deserialise(byte[] objectByes) throws ClassNotFoundException;

    public static IObjectDeserialiser simple = (byte[] objectBytes) -> ByteFunctions.deSerialize(objectBytes, Serializable.class);
    
}

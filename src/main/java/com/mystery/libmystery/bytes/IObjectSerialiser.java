
package com.mystery.libmystery.bytes;

import java.io.Serializable;


@FunctionalInterface
public interface IObjectSerialiser {
    
    public byte[] serialise(Serializable object);
    
    public static IObjectSerialiser simple = (Serializable object) -> ByteFunctions.serialize(object, true);
    
}

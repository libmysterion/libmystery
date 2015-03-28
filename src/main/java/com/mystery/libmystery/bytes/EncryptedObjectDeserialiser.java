
package com.mystery.libmystery.bytes;

import static com.mystery.libmystery.bytes.ByteFunctions.deSerialize;
import com.mystery.libmystery.encrypt.EncryptionException;
import com.mystery.libmystery.encrypt.SymmetricDecrypter;
import java.io.Serializable;
import javax.crypto.SecretKey;


public class EncryptedObjectDeserialiser implements IObjectDeserialiser {

    private SecretKey key;

    public EncryptedObjectDeserialiser(SecretKey key) {
        this.key = key;
    }
    
    
    @Override
    public Serializable deserialise(byte[] objectByes){
        return null;
//        try {
//            SymmetricDecrypter decrypter = new SymmetricDecrypter(key);
//            byte[] objectBytes = decrypter.decrypt(objectByes);
//            return deSerialize(objectBytes, Serializable.class);
//        } catch (EncryptionException ex) {
//            ex.printStackTrace();
//            return null;
//            // fuck it...it will happen but not on happy path
//        }
    }
    
}

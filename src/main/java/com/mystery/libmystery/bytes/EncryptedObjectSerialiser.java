package com.mystery.libmystery.bytes;

import com.mystery.libmystery.encrypt.EncryptionException;
import com.mystery.libmystery.encrypt.SymmetricEncrypter;
import java.io.Serializable;
import javax.crypto.SecretKey;


public class EncryptedObjectSerialiser implements IObjectSerialiser {

    private SecretKey key;

    public EncryptedObjectSerialiser(SecretKey key) {
        this.key = key;
    }

    @Override
    public byte[] serialise(Serializable object) {
        try {
            byte[] serialize = ByteFunctions.serialize(object);
            SymmetricEncrypter encrypter = new SymmetricEncrypter(key);
            byte[] encrypt = encrypter.encrypt(serialize);
            return ByteFunctions.join(ByteFunctions.integerToBytes(encrypt.length), encrypt);
        } catch (EncryptionException ex) {
            throw new RuntimeException(ex);
        }
    }

}

package com.mystery.libmystery.encrypt;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyGenerator {

    public static SecretKey getAESKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static SecretKey getAESKey(byte[] bytes) {
        return new SecretKeySpec(bytes, 0, bytes.length, "AES"); 
    }
}

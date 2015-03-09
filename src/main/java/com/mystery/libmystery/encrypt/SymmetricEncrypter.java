package com.mystery.libmystery.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class SymmetricEncrypter extends Encryption {

    public SymmetricEncrypter(SecretKey key) {
        super(key, Cipher.ENCRYPT_MODE, "AES/ECB/PKCS5Padding");
    }
    
    public SymmetricEncrypter() {
        this(AESKeyGenerator.getAESKey());
    }

    public byte[] encrypt(byte[] inpBytes) throws EncryptionException {
        return super.crypt(inpBytes);
    }
   
    public SecretKey getKey(){
        return (SecretKey)super.key;
    }
    
}

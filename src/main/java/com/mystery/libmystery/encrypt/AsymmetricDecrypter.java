package com.mystery.libmystery.encrypt;

import java.security.PrivateKey;


public class AsymmetricDecrypter extends Encryption{
    
    public AsymmetricDecrypter(PrivateKey key){
       super(key, "RSA/ECB/PKCS1Padding");
    }

    public byte[] decrypt(byte[] inpBytes) throws EncryptionException {
        return super.crypt(inpBytes);
    }

}
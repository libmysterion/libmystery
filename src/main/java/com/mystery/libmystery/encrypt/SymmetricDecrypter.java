package com.mystery.libmystery.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class SymmetricDecrypter extends Encryption {

    public SymmetricDecrypter(SecretKey key){
        super(key, Cipher.DECRYPT_MODE, "AES/ECB/PKCS5Padding");
    }
    
    public byte[] decrypt(byte[] inpBytes) throws EncryptionException {
        return super.crypt(inpBytes);
    }
}

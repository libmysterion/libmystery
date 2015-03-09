
package com.mystery.libmystery.encrypt;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

class Encryption {

    private final String xForm;
    final Key key;
    private final int mode;

    Encryption(PrivateKey privateKey, String xForm) {
        this.key = privateKey;
        this.mode = Cipher.DECRYPT_MODE;
        this.xForm = xForm;
    }

    Encryption(PublicKey publicKey, String xForm) {
        this.key = publicKey;
        this.mode = Cipher.ENCRYPT_MODE;
        this.xForm = xForm;
    }
    
    Encryption(SecretKey key, int mode, String xForm) {
        this.key = key;
        this.mode = mode;
        this.xForm =xForm;
    }

    byte[] crypt(byte[] inpBytes) throws EncryptionException {
        try {
            Cipher cipher = Cipher.getInstance(xForm);
            cipher.init(this.mode, this.key);
            return cipher.doFinal(inpBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            // todo log the error
            throw new EncryptionException(String.format("Exception in Asymmetric %s", this.mode == Cipher.DECRYPT_MODE ? "Decryption" : "Encryption"), e);
        }
    }

}

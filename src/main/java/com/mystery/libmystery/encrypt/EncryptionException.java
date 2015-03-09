package com.mystery.libmystery.encrypt;

import java.security.GeneralSecurityException;

public class EncryptionException extends Exception {

    public EncryptionException(String message, GeneralSecurityException e) {
       super(message, e);
    }
    
}

package com.mystery.libmystery.encrypt;

import java.security.PublicKey;

public class AsymmetricEncrypter extends Encryption {

  public AsymmetricEncrypter(PublicKey key){
    super(key, "RSA/ECB/PKCS1Padding");
  }
  
  public byte[] encrypt(byte[] inpBytes) throws EncryptionException{
      return super.crypt(inpBytes);
  }
}

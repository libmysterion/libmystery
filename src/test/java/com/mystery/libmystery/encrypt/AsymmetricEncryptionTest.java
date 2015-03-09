package com.mystery.libmystery.encrypt;

import java.security.KeyPair;
import java.util.Random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

public class AsymmetricEncryptionTest {

    public void testRun(byte[] bytes) throws EncryptionException {
        try {

            long s = System.currentTimeMillis();

            KeyPair keyPair = RSAKeyPairGenerator.generateRSAKeyPair();
            AsymmetricEncrypter encrypter = new AsymmetricEncrypter(keyPair.getPublic());
            byte[] encrypted = encrypter.encrypt(bytes);
            
           
          
            AsymmetricDecrypter decrypter = new AsymmetricDecrypter(keyPair.getPrivate());
            byte[] decrypted = decrypter.decrypt(encrypted);

            long f = System.currentTimeMillis();

            System.out.println(bytes.length + " bytes takes " + (f - s) + " ms");
            
            assertThat(bytes, not(equalTo(encrypted)));
            Assert.assertArrayEquals(bytes, decrypted);
            
        } catch (Exception e) {
            System.out.println("failed on " + bytes.length);
            throw e;
        }
    }

    @Test
    public void testSimple() throws Exception {
        byte[] simpleBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        testRun(simpleBytes);
    }

    @Test
    public void testUpTo53() throws Exception {

        Random ramdom = new Random();

        int maxLen = 54;
        for (int i = 0; i < maxLen; i+=2) {
            byte[] bytes = new byte[i];
            ramdom.nextBytes(bytes);
            testRun(bytes);
        }

    }

    @Test
    public void test54WithException() throws Exception {
        boolean thrown = false;
        try {
            Random ramdom = new Random();
            byte[] bytes = new byte[54];
            ramdom.nextBytes(bytes);
            testRun(bytes);
        } catch (EncryptionException e) {
            assertEquals("Exception in Asymmetric Encryption", e.getMessage());
            thrown = true;
        }
        
        assertTrue(thrown);
    }

}

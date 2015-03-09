package com.mystery.libmystery.encrypt;

import java.util.Random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

public class SymmetricEncryptionTest {

    public void testRun(byte[] bytes) throws EncryptionException {
        try {

            long s = System.currentTimeMillis();

            SymmetricEncrypter encrypter = new SymmetricEncrypter();
            byte[] encrypted = encrypter.encrypt(bytes);
         
            SymmetricDecrypter decrypter = new SymmetricDecrypter(encrypter.getKey());
            byte[] decrypted = decrypter.decrypt(encrypted);

            long f = System.currentTimeMillis();

            //System.out.println(bytes.length + " bytes takes " + (f - s) + " ms");
            
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
    public void testUpTo1K() throws Exception {

        Random ramdom = new Random();

        int maxLen = 1024;
        for (int i = 0; i < maxLen; i+=3) {
            byte[] bytes = new byte[i];
            ramdom.nextBytes(bytes);
            testRun(bytes);
        }

    }
    
    
    @Test
    public void testUpTo1M() throws Exception {

        Random ramdom = new Random();

        int minLen = 1024;
        
        for (int i = 1024 * 1024; i > minLen; i/=2) {
            byte[] bytes = new byte[i];
            ramdom.nextBytes(bytes);
            testRun(bytes);
        }

    }

}

package com.example.Authentication.UTIL;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class MyEncryptionUtil {
    private static final String SECRET_KEY = "MySecret12345678"; // 16-char for AES

    public static String encrypt(String input) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] enc = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String input) {
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dec = cipher.doFinal(Base64.getDecoder().decode(input));
            return new String(dec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt permission: " + input, e);
        }
    }
}

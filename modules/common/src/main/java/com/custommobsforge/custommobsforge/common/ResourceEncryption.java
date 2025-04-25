package com.custommobsforge.custommobsforge.common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class ResourceEncryption {
    private static final String KEY = "MySecretKey12345"; // TODO: Сделать конфигурируемым
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    public static String encrypt(String data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data to encrypt cannot be null");
        }
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null) {
            throw new IllegalArgumentException("Encrypted data cannot be null");
        }
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        if (combined.length < IV_LENGTH) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }
        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
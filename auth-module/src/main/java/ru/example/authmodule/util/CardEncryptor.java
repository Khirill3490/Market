package ru.example.authmodule.util;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CardEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    // Ключ должен быть 16, 24 или 32 байта (AES-128, AES-192, AES-256)
    private static final String SECRET_KEY = "1234567890123456"; // 16 байт

    // Генератор случайного IV (инициализационного вектора)
    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // 16 байт для AES
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    @SneakyThrows
    public static String encrypt(String plainText) {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        IvParameterSpec iv = generateIv();
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        // Присоединяем IV к зашифрованному тексту (IV + encryptedData)
        byte[] encryptedWithIv = new byte[iv.getIV().length + encrypted.length];
        System.arraycopy(iv.getIV(), 0, encryptedWithIv, 0, iv.getIV().length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.getIV().length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decrypt(String encryptedText) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedText);

        // Извлекаем IV из начала
        byte[] ivBytes = new byte[16];
        System.arraycopy(decoded, 0, ivBytes, 0, 16);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Оставшиеся байты — это зашифрованные данные
        byte[] encryptedBytes = new byte[decoded.length - 16];
        System.arraycopy(decoded, 16, encryptedBytes, 0, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        byte[] decrypted = cipher.doFinal(encryptedBytes);
        return new String(decrypted);
    }
}


package ru.example.authmodule.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class test {
    public static void main(String[] args) throws Exception {
        System.out.println(generateKey());
        System.out.println("Привет");

    }

    // Генерация ключа (вызовите 1 раз и сохраните ключ!)
    public static String generateKey() throws Exception {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        return base64Key;
    }
}

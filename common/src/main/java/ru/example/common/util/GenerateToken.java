package ru.example.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class GenerateToken {
    private static final SecureRandom RNG = new SecureRandom();
    public static String newOpaqueToken() {
        byte[] b = new byte[32]; // 256 бит
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}

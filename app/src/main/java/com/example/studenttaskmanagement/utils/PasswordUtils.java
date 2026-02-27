package com.example.studenttaskmanagement.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String sha256(String plainText) {
        if (plainText == null) return "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

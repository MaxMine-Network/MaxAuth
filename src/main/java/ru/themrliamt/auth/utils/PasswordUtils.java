package ru.themrliamt.auth.utils;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class PasswordUtils {
    public static String hash(String password) {
        return Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
    }
}
package com.example.Backend.util;

import java.util.concurrent.ThreadLocalRandom;

public final class PasswordGenerator {

    private PasswordGenerator() {
    }

    public static String generateNumericPassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            password.append(ThreadLocalRandom.current().nextInt(10));
        }
        return password.toString();
    }
}

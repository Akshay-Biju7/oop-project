package com.pgtr.util;

import java.util.regex.Pattern;

/**
 * Validates user inputs across registration, workouts, nutrition, and health tracking.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.length() >= 6;
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isPositive(double number) {
        return number > 0;
    }

    public static boolean isNonNegative(double number) {
        return number >= 0;
    }

    public static boolean isInRange(double val, double min, double max) {
        return val >= min && val <= max;
    }
}

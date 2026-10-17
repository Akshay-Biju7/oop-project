package com.pgtr.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for secure password hashing and verification using BCrypt algorithm with salt.
 * Ensures plaintext passwords are never stored in the database.
 */
public class PasswordHasher {

    private static final int LOG_ROUNDS = 12;

    /**
     * Hashes a plaintext password using BCrypt with automatic salting.
     */
    public static String hash(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a candidate plaintext password against a stored BCrypt hash.
     */
    public static boolean verify(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

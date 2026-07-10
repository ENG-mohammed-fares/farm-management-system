package com.smartfarm.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private static final String BCRYPT_PREFIX = "$2";

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verify(String password, String storedPassword) {
        if (password == null || storedPassword == null) {
            return false;
        }

        if (isHashed(storedPassword)) {
            return BCrypt.checkpw(password, storedPassword);
        }

        return storedPassword.equals(password);
    }

    public static boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(BCRYPT_PREFIX);
    }
}

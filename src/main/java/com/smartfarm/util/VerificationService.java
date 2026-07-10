package com.smartfarm.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class VerificationService {

    private static final int CODE_LENGTH = 5;
    private static final int EXPIRY_SECONDS = 300;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, String> activeCodes = new HashMap<>();
    private static final Map<String, String> lastCodes = new HashMap<>();
    private static final Map<String, LocalDateTime> codeExpiry = new HashMap<>();

    public static String generateUniqueCode(String key) {
        String newCode;
        String previousCode = lastCodes.get(key);

        do {
            newCode = generateRandomCode();
        } while (newCode.equals(previousCode));

        activeCodes.put(key, newCode);
        lastCodes.put(key, newCode);
        codeExpiry.put(key, LocalDateTime.now().plusSeconds(EXPIRY_SECONDS));

        return newCode;
    }

    private static String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static boolean verifyCode(String key, String inputCode) {
        String stored = activeCodes.get(key);
        LocalDateTime expiry = codeExpiry.get(key);

        if (stored == null || expiry == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(expiry)) {
            activeCodes.remove(key);
            codeExpiry.remove(key);
            return false;
        }

        boolean valid = stored.equals(inputCode);
        if (valid) {
            activeCodes.remove(key);
            codeExpiry.remove(key);
        }
        return valid;
    }

    public static void clearCode(String key) {
        activeCodes.remove(key);
        codeExpiry.remove(key);
    }

    public static boolean sendEmailCode(String email, String code) {
        return EmailService.sendVerificationCode(email, code);
    }

    public static String simulateSmsCode(String phone, String code) {
        return "SMS Simulation — sent to " + phone
                + "\n\nYour verification code is:\n\n" + code
                + "\n\n(Fake SMS for this university project — no real SMS is sent.)";
    }
}
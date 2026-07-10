package com.smartfarm.service;

import java.sql.SQLException;
import java.util.regex.Pattern;

import com.smartfarm.dao.FarmWorkerDAO;
import com.smartfarm.dao.UserDAO;
import com.smartfarm.util.SessionManager;

public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final boolean isAdmin;

        public AuthResult(boolean success, String message, boolean isAdmin) {
            this.success = success;
            this.message = message;
            this.isAdmin = isAdmin;
        }
    }

    public static AuthResult login(String emailOrPhone, String password) {
        if (emailOrPhone == null || emailOrPhone.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your email or phone", false);
        }
        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your password", false);
        }

        try {
            int[] result = UserDAO.login(emailOrPhone.trim(), password);

            if (result == null) {
                return new AuthResult(false, "Invalid email/phone or password", false);
            }

            int userId = result[0];
            boolean isAdmin = result[1] == 1;
            String name = UserDAO.getUserName(userId);

            if (!isAdmin) {
                int fwId = FarmWorkerDAO.getFwId(userId);
                if (fwId == -1) {
                    return new AuthResult(false, "This account is not assigned as an active worker", false);
                }
                SessionManager.login(userId, name, false);
                SessionManager.setFwId(fwId);
            } else {
                SessionManager.login(userId, name, true);
            }

            return new AuthResult(true, "Login successful", isAdmin);

        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthResult(false, "Connection error", false);
        }
    }

    public static AuthResult registerWorker(String name, String email, String phone, String password) {
        if (name == null || name.trim().isEmpty()) {
            return new AuthResult(false, "Please enter your full name", false);
        }

        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            return new AuthResult(false, "Email must look like: name123@domain.com", false);
        }

        String passwordError = validatePasswordStrength(password);
        if (passwordError != null) {
            return new AuthResult(false, passwordError, false);
        }

        try {
            if (email != null && !email.isEmpty() && UserDAO.emailExists(email)) {
                return new AuthResult(false, "This email is already registered", false);
            }
            if (phone != null && !phone.isEmpty() && UserDAO.phoneExists(phone)) {
                return new AuthResult(false, "This phone number is already registered", false);
            }
            if (UserDAO.nameExists(name)) {
                return new AuthResult(false, "This username is already taken", false);
            }

            int userId = UserDAO.createWorker(name, email, phone, password);
            FarmWorkerDAO.assignWorker(userId, "HARVESTER", 8.0, "kg");

            return new AuthResult(true, "Account created successfully", false);

        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthResult(false, "Error: " + e.getMessage(), false);
        }
    }

    public static String validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Password cannot be empty or spaces only";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return "Password must contain at least one special character";
        }
        return null;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static AuthResult resetPassword(String emailOrPhone, String newPassword) {
        String passwordError = validatePasswordStrength(newPassword);
        if (passwordError != null) {
            return new AuthResult(false, passwordError, false);
        }

        try {
            if (!UserDAO.accountExists(emailOrPhone)) {
                return new AuthResult(false, "No account found with this email or phone", false);
            }

            boolean updated = UserDAO.resetPassword(emailOrPhone, newPassword);
            return updated
                    ? new AuthResult(true, "Password reset successfully", false)
                    : new AuthResult(false, "Something went wrong. Try again.", false);

        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthResult(false, "Connection error", false);
        }
    }

    public static com.smartfarm.model.User getUserById(int userId) {
        try {
            return UserDAO.getUserById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static AuthResult updateProfile(int userId, String name, String email, String phone) {
        if (name == null || name.trim().isEmpty()) {
            return new AuthResult(false, "Name cannot be empty", false);
        }
        if (email != null && !email.isEmpty() && !isValidEmail(email)) {
            return new AuthResult(false, "Email must look like: name123@domain.com", false);
        }

        try {
            com.smartfarm.model.User current = UserDAO.getUserById(userId);
            if (current == null) {
                return new AuthResult(false, "User not found", false);
            }

            if (!name.trim().equalsIgnoreCase(current.getName()) && UserDAO.nameExists(name.trim())) {
                return new AuthResult(false, "This username is already taken", false);
            }
            if (email != null && !email.isEmpty()
                    && (current.getEmail() == null || !email.equals(current.getEmail()))
                    && UserDAO.emailExists(email)) {
                return new AuthResult(false, "This email is already registered", false);
            }
            if (phone != null && !phone.isEmpty()
                    && (current.getPhone() == null || !phone.equals(current.getPhone()))
                    && UserDAO.phoneExists(phone)) {
                return new AuthResult(false, "This phone number is already registered", false);
            }

            boolean updated = UserDAO.updateProfile(userId, name.trim(), email, phone);
            return updated
                    ? new AuthResult(true, "Profile updated successfully", false)
                    : new AuthResult(false, "Update failed", false);

        } catch (SQLException e) {
            e.printStackTrace();
            return new AuthResult(false, "Database error", false);
        }
    }

    public static void logout() {
        SessionManager.logout();
    }
}
package com.smartfarm.dao;

import java.sql.*;

import com.smartfarm.util.DatabaseConnection;
import com.smartfarm.util.PasswordHasher;

public class UserDAO {

    public static int createWorker(String name, String email, String phone, String password) throws SQLException {
        String sql = "INSERT INTO Users (name, email, phone, password, role) VALUES (?, ?, ?, ?, 'WORKER')";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, email != null && !email.isEmpty() ? email : null);
        ps.setString(3, phone != null && !phone.isEmpty() ? phone : null);
        ps.setString(4, PasswordHasher.hash(password));
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        ps.close();
        throw new SQLException("Failed to retrieve generated user_id");
    }

    public static int[] login(String input, String password) throws SQLException {
        String sql = "SELECT user_id, password, role FROM Users WHERE  email = ? OR phone = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, input);
        ps.setString(2, input);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String storedPassword = rs.getString("password");
            String storedRole = rs.getString("role");
            int rawUserId = rs.getInt("user_id");

            if (PasswordHasher.verify(password, storedPassword)) {
                int userId = rawUserId;
                int role = storedRole != null && "ADMIN".equalsIgnoreCase(storedRole.trim()) ? 1 : 0;
                rs.close(); ps.close();
                migratePlainTextPassword(userId, password, storedPassword);
                return new int[]{userId, role};
            }
        }
        rs.close(); ps.close();
        return null;
    }

    private static void migratePlainTextPassword(int userId, String password, String storedPassword) throws SQLException {
        if (PasswordHasher.isHashed(storedPassword)) {
            return;
        }

        String sql = "UPDATE Users SET password = ? WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, PasswordHasher.hash(password));
        ps.setInt(2, userId);
        ps.executeUpdate();
        ps.close();
    }

    public static boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close(); ps.close();
        return exists;
    }

    public static boolean phoneExists(String phone) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE phone = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, phone);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close(); ps.close();
        return exists;
    }

    public static boolean nameExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE LOWER(name) = LOWER(?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close(); ps.close();
        return exists;
    }

    public static boolean accountExists(String emailOrPhone) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE email = ? OR phone = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, emailOrPhone);
        ps.setString(2, emailOrPhone);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close(); ps.close();
        return exists;
    }

    public static boolean resetPassword(String emailOrPhone, String newPassword) throws SQLException {
        String sql = "UPDATE Users SET password = ? WHERE email = ? OR phone = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, PasswordHasher.hash(newPassword));
        ps.setString(2, emailOrPhone);
        ps.setString(3, emailOrPhone);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean updateProfile(int userId, String name, String email, String phone) throws SQLException {
        String sql = "UPDATE Users SET name = ?, email = ?, phone = ? WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email != null && !email.isEmpty() ? email : null);
        ps.setString(3, phone != null && !phone.isEmpty() ? phone : null);
        ps.setInt(4, userId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static com.smartfarm.model.User findUserByIdentifier(String identifier) throws SQLException {
        String sql = "SELECT * FROM Users WHERE LOWER(name) = LOWER(?) OR email = ? OR phone = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, identifier);
        ps.setString(2, identifier);
        ps.setString(3, identifier);
        ResultSet rs = ps.executeQuery();

        com.smartfarm.model.User user = null;
        if (rs.next()) {
            user = new com.smartfarm.model.User();
            user.setUserId(rs.getInt("user_id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
        }
        rs.close(); ps.close();
        return user;
    }

    public static com.smartfarm.model.User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        com.smartfarm.model.User user = null;
        if (rs.next()) {
            user = new com.smartfarm.model.User();
            user.setUserId(rs.getInt("user_id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setRole(rs.getString("role"));
        }
        rs.close(); ps.close();
        return user;
    }

    public static String getUserName(int userId) throws SQLException {
        String sql = "SELECT name FROM Users WHERE user_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        String name = rs.next() ? rs.getString("name") : "";
        rs.close(); ps.close();
        return name;
    }
}

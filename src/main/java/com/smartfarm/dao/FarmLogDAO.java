package com.smartfarm.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.FarmLog;
import com.smartfarm.util.DatabaseConnection;

public class FarmLogDAO {

    public static List<FarmLog> getAllLogs() throws SQLException {
        String sql = "SELECT l.*, f.name AS field_name, u.name AS worker_name " +
                "FROM Farm_Logs l " +
                "JOIN Fields f ON l.field_id = f.field_id " +
                "JOIN Farm_Workers fw ON l.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE f.farm_id = 1 ORDER BY l.log_date DESC, l.log_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<FarmLog> logs = new ArrayList<>();
        while (rs.next()) {
            logs.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return logs;
    }

    public static List<FarmLog> getLogsByType(String logType) throws SQLException {
        String sql = "SELECT l.*, f.name AS field_name, u.name AS worker_name " +
                "FROM Farm_Logs l " +
                "JOIN Fields f ON l.field_id = f.field_id " +
                "JOIN Farm_Workers fw ON l.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE f.farm_id = 1 AND l.log_type = ? ORDER BY l.log_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, logType);
        ResultSet rs = ps.executeQuery();

        List<FarmLog> logs = new ArrayList<>();
        while (rs.next()) {
            logs.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return logs;
    }

    public static List<FarmLog> getLogsByFieldAndDate(int fieldId, LocalDate date, String logType) throws SQLException {
        String sql = "SELECT l.*, f.name AS field_name, u.name AS worker_name " +
                "FROM Farm_Logs l " +
                "JOIN Fields f ON l.field_id = f.field_id " +
                "JOIN Farm_Workers fw ON l.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE l.field_id = ? AND l.log_date = ? AND l.log_type = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fieldId);
        ps.setDate(2, Date.valueOf(date));
        ps.setString(3, logType);
        ResultSet rs = ps.executeQuery();

        List<FarmLog> logs = new ArrayList<>();
        while (rs.next()) {
            logs.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return logs;
    }

    public static int addLog(int fieldId, int fwId, String logType, String description, Double quantity) throws SQLException {
        String sql = "INSERT INTO Farm_Logs (field_id, fw_id, log_type, description, quantity, log_date) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_DATE)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, fieldId);
        ps.setInt(2, fwId);
        ps.setString(3, logType);
        ps.setString(4, description);
        if (quantity != null) {
            ps.setDouble(5, quantity);
        } else {
            ps.setNull(5, Types.DECIMAL);
        }
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        ps.close();
        throw new SQLException("Failed to retrieve generated log_id");
    }

    public static boolean deleteLog(int logId) throws SQLException {
        String sql = "DELETE FROM Farm_Logs WHERE log_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, logId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    private static FarmLog mapRow(ResultSet rs) throws SQLException {
        FarmLog l = new FarmLog();
        l.setLogId(rs.getInt("log_id"));
        l.setFieldId(rs.getInt("field_id"));
        l.setFieldName(rs.getString("field_name"));
        l.setFwId(rs.getInt("fw_id"));
        l.setWorkerName(rs.getString("worker_name"));
        l.setLogType(rs.getString("log_type"));
        l.setDescription(rs.getString("description"));
        double qty = rs.getDouble("quantity");
        l.setQuantity(rs.wasNull() ? null : qty);
        Date date = rs.getDate("log_date");
        l.setLogDate(date != null ? date.toLocalDate() : null);
        return l;
    }
}
package com.smartfarm.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.FarmWorker;
import com.smartfarm.util.DatabaseConnection;

public class FarmWorkerDAO {

    public static int assignWorker(int userId, String jobType, double wagePerUnit, String wageUnit) throws SQLException {
        String sql = "INSERT INTO Farm_Workers (farm_id, user_id, job_type, wage_per_unit, wage_unit) VALUES (1, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, userId);
        ps.setString(2, jobType);
        ps.setDouble(3, wagePerUnit);
        ps.setString(4, wageUnit);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        ps.close();
        throw new SQLException("Failed to retrieve generated fw_id");
    }

    public static int getFwId(int userId) throws SQLException {
        String sql = "SELECT fw_id FROM Farm_Workers WHERE user_id = ? AND farm_id = 1 AND status = 'ACTIVE'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int fwId = rs.next() ? rs.getInt("fw_id") : -1;
        rs.close(); ps.close();
        return fwId;
    }

    /** Returns fw_id for this user on farm 1 regardless of ACTIVE/INACTIVE status. */
    public static int getAnyFwId(int userId) throws SQLException {
        String sql = "SELECT fw_id FROM Farm_Workers WHERE user_id = ? AND farm_id = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int fwId = rs.next() ? rs.getInt("fw_id") : -1;
        rs.close(); ps.close();
        return fwId;
    }

    public static String getJobType(int fwId) throws SQLException {
        String sql = "SELECT job_type FROM Farm_Workers WHERE fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();
        String job = rs.next() ? rs.getString("job_type") : "";
        rs.close(); ps.close();
        return job;
    }

    public static double getWagePerUnit(int fwId) throws SQLException {
        String sql = "SELECT wage_per_unit FROM Farm_Workers WHERE fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();
        double wage = rs.next() ? rs.getDouble("wage_per_unit") : 0;
        rs.close(); ps.close();
        return wage;
    }

    public static List<FarmWorker> getAllWorkers() throws SQLException {
        String sql = "SELECT fw.*, u.name AS user_name FROM Farm_Workers fw " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE fw.farm_id = 1 ORDER BY fw.fw_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<FarmWorker> workers = new ArrayList<>();
        while (rs.next()) {
            workers.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return workers;
    }

    public static FarmWorker getWorkerByFwId(int fwId) throws SQLException {
        String sql = "SELECT fw.*, u.name AS user_name FROM Farm_Workers fw " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE fw.fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();

        FarmWorker worker = rs.next() ? mapRow(rs) : null;
        rs.close(); ps.close();
        return worker;
    }

    public static boolean updateWorker(int fwId, String jobType, double wagePerUnit, String wageUnit, String status) throws SQLException {
        String sql = "UPDATE Farm_Workers SET job_type = ?, wage_per_unit = ?, wage_unit = ?, status = ? WHERE fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, jobType);
        ps.setDouble(2, wagePerUnit);
        ps.setString(3, wageUnit);
        ps.setString(4, status);
        ps.setInt(5, fwId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean setWorkerStatus(int fwId, String status) throws SQLException {
        String sql = "UPDATE Farm_Workers SET status = ? WHERE fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, fwId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean deleteWorker(int fwId) throws SQLException {
        String sql = "DELETE FROM Farm_Workers WHERE fw_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static int getTotalWorkersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Farm_Workers WHERE farm_id = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    public static int getActiveWorkersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Farm_Workers WHERE farm_id = 1 AND status = 'ACTIVE'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    private static FarmWorker mapRow(ResultSet rs) throws SQLException {
        FarmWorker w = new FarmWorker();
        w.setFwId(rs.getInt("fw_id"));
        w.setFarmId(rs.getInt("farm_id"));
        w.setUserId(rs.getInt("user_id"));
        w.setUserName(rs.getString("user_name"));
        w.setJobType(rs.getString("job_type"));
        w.setWagePerUnit(rs.getDouble("wage_per_unit"));
        w.setWageUnit(rs.getString("wage_unit"));
        w.setStatus(rs.getString("status"));
        Date hired = rs.getDate("hired_at");
        w.setHiredAt(hired != null ? hired.toLocalDate() : null);
        return w;
    }
}
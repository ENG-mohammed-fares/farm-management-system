package com.smartfarm.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.Harvest;
import com.smartfarm.util.DatabaseConnection;

public class HarvestDAO {

    public static List<Harvest> getAllHarvests() throws SQLException {
        String sql = "SELECT h.*, f.name AS field_name, c.name AS crop_name, u.name AS worker_name " +
                "FROM Harvests h " +
                "JOIN Fields f ON h.field_id = f.field_id " +
                "JOIN Crops c ON h.crop_id = c.crop_id " +
                "JOIN Farm_Workers fw ON h.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE f.farm_id = 1 ORDER BY h.harvest_date DESC, h.harvest_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Harvest> harvests = new ArrayList<>();
        while (rs.next()) {
            harvests.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return harvests;
    }

    public static List<Harvest> getHarvestsByStatus(String status) throws SQLException {
        String sql = "SELECT h.*, f.name AS field_name, c.name AS crop_name, u.name AS worker_name " +
                "FROM Harvests h " +
                "JOIN Fields f ON h.field_id = f.field_id " +
                "JOIN Crops c ON h.crop_id = c.crop_id " +
                "JOIN Farm_Workers fw ON h.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE f.farm_id = 1 AND h.status = ? ORDER BY h.harvest_date DESC, h.harvest_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();

        List<Harvest> harvests = new ArrayList<>();
        while (rs.next()) {
            harvests.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return harvests;
    }

    public static List<Harvest> getHarvestsByWorker(int fwId) throws SQLException {
        String sql = "SELECT h.*, f.name AS field_name, c.name AS crop_name, u.name AS worker_name " +
                "FROM Harvests h " +
                "JOIN Fields f ON h.field_id = f.field_id " +
                "JOIN Crops c ON h.crop_id = c.crop_id " +
                "JOIN Farm_Workers fw ON h.fw_id = fw.fw_id " +
                "JOIN Users u ON fw.user_id = u.user_id " +
                "WHERE h.fw_id = ? ORDER BY h.harvest_date DESC, h.harvest_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();

        List<Harvest> harvests = new ArrayList<>();
        while (rs.next()) {
            harvests.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return harvests;
    }

    public static int addHarvest(int fieldId, int cropId, int fwId, double quantityGood,
                                 double quantityDamaged, String unit, String notes) throws SQLException {
        String sql = "INSERT INTO Harvests (field_id, crop_id, fw_id, quantity_good, quantity_damaged, unit, harvest_date, notes, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, ?, 'PENDING') RETURNING harvest_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fieldId);
        ps.setInt(2, cropId);
        ps.setInt(3, fwId);
        ps.setDouble(4, quantityGood);
        ps.setDouble(5, quantityDamaged);
        ps.setString(6, unit);
        ps.setString(7, notes);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int harvestId = rs.getInt(1);
        rs.close(); ps.close();
        return harvestId;
    }

    public static boolean updateStatus(int harvestId, String status) throws SQLException {
        String sql = "UPDATE Harvests SET status = ? WHERE harvest_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, harvestId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean deleteHarvest(int harvestId) throws SQLException {
        String sql = "DELETE FROM Harvests WHERE harvest_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, harvestId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static int getTotalHarvestsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Harvests h JOIN Fields f ON h.field_id = f.field_id WHERE f.farm_id = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    public static int getPendingCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Harvests h JOIN Fields f ON h.field_id = f.field_id " +
                "WHERE f.farm_id = 1 AND h.status = 'PENDING'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    public static double getTotalGoodQuantity() throws SQLException {
        String sql = "SELECT COALESCE(SUM(h.quantity_good), 0) FROM Harvests h " +
                "JOIN Fields f ON h.field_id = f.field_id WHERE f.farm_id = 1 AND h.status = 'APPROVED'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        double total = rs.next() ? rs.getDouble(1) : 0;
        rs.close(); ps.close();
        return total;
    }

    public static double getWorkerTotalQuantity(int fwId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity_good), 0) FROM Harvests WHERE fw_id = ? AND status = 'APPROVED'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();
        double total = rs.next() ? rs.getDouble(1) : 0;
        rs.close(); ps.close();
        return total;
    }

    public static double getWorkerTotalEarnings(int fwId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(h.quantity_good * fw.wage_per_unit), 0) " +
                "FROM Harvests h JOIN Farm_Workers fw ON h.fw_id = fw.fw_id " +
                "WHERE h.fw_id = ? AND h.status = 'APPROVED'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fwId);
        ResultSet rs = ps.executeQuery();
        double total = rs.next() ? rs.getDouble(1) : 0;
        rs.close(); ps.close();
        return total;
    }

    private static Harvest mapRow(ResultSet rs) throws SQLException {
        Harvest h = new Harvest();
        h.setHarvestId(rs.getInt("harvest_id"));
        h.setFieldId(rs.getInt("field_id"));
        h.setFieldName(rs.getString("field_name"));
        h.setCropId(rs.getInt("crop_id"));
        h.setCropName(rs.getString("crop_name"));
        h.setFwId(rs.getInt("fw_id"));
        h.setWorkerName(rs.getString("worker_name"));
        h.setQuantityGood(rs.getDouble("quantity_good"));
        h.setQuantityDamaged(rs.getDouble("quantity_damaged"));
        h.setUnit(rs.getString("unit"));
        Date date = rs.getDate("harvest_date");
        h.setHarvestDate(date != null ? date.toLocalDate() : null);
        h.setNotes(rs.getString("notes"));
        h.setStatus(rs.getString("status"));
        return h;
    }
}
package com.smartfarm.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.Crop;
import com.smartfarm.util.DatabaseConnection;

public class CropDAO {

    public static List<Crop> getAllCrops() throws SQLException {
        String sql = "SELECT c.*, f.name AS field_name FROM Crops c " +
                "JOIN Fields f ON c.field_id = f.field_id " +
                "WHERE f.farm_id = 1 ORDER BY c.crop_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Crop> crops = new ArrayList<>();
        while (rs.next()) {
            crops.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return crops;
    }

    public static List<Crop> getCropsByField(int fieldId) throws SQLException {
        String sql = "SELECT c.*, f.name AS field_name FROM Crops c " +
                "JOIN Fields f ON c.field_id = f.field_id " +
                "WHERE c.field_id = ? ORDER BY c.crop_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fieldId);
        ResultSet rs = ps.executeQuery();

        List<Crop> crops = new ArrayList<>();
        while (rs.next()) {
            crops.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return crops;
    }

    public static Crop getCropById(int cropId) throws SQLException {
        String sql = "SELECT c.*, f.name AS field_name FROM Crops c " +
                "JOIN Fields f ON c.field_id = f.field_id " +
                "WHERE c.crop_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cropId);
        ResultSet rs = ps.executeQuery();

        Crop crop = rs.next() ? mapRow(rs) : null;
        rs.close(); ps.close();
        return crop;
    }

    public static int addCrop(int fieldId, String name, String type, LocalDate plantedDate, String quantity) throws SQLException {
        String sql = "INSERT INTO Crops (field_id, name, type, planted_date, quantity, status) " +
                "VALUES (?, ?, ?, ?, ?, 'GROWING')";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, fieldId);
        ps.setString(2, name);
        ps.setString(3, type);
        ps.setDate(4, plantedDate != null ? Date.valueOf(plantedDate) : null);
        ps.setString(5, quantity);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        ps.close();
        throw new SQLException("Failed to retrieve generated crop_id");
    }

    public static boolean updateCropStatus(int cropId, String status) throws SQLException {
        String sql = "UPDATE Crops SET status = ? WHERE crop_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setInt(2, cropId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean deleteCrop(int cropId) throws SQLException {
        String sql = "DELETE FROM Crops WHERE crop_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cropId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static int getActiveCropsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Crops c JOIN Fields f ON c.field_id = f.field_id " +
                "WHERE f.farm_id = 1 AND c.status != 'HARVESTED'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    private static Crop mapRow(ResultSet rs) throws SQLException {
        Crop c = new Crop();
        c.setCropId(rs.getInt("crop_id"));
        c.setFieldId(rs.getInt("field_id"));
        c.setFieldName(rs.getString("field_name"));
        c.setName(rs.getString("name"));
        c.setType(rs.getString("type"));
        Date planted = rs.getDate("planted_date");
        c.setPlantedDate(planted != null ? planted.toLocalDate() : null);
        c.setQuantity(rs.getString("quantity"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
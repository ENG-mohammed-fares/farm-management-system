package com.smartfarm.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.FertilizerMedicine;
import com.smartfarm.util.DatabaseConnection;

public class FertilizerMedicineDAO {

    private static final double LOW_STOCK_THRESHOLD = 20.0;

    public static List<FertilizerMedicine> getAllItems() throws SQLException {
        String sql = "SELECT fm.*, f.name AS field_name FROM Fertilizers_Medicines fm " +
                "LEFT JOIN Fields f ON fm.field_id = f.field_id " +
                "WHERE f.farm_id = 1 OR fm.field_id IS NULL " +
                "ORDER BY fm.fm_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<FertilizerMedicine> items = new ArrayList<>();
        while (rs.next()) {
            items.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return items;
    }

    public static List<FertilizerMedicine> getItemsByType(String type) throws SQLException {
        String sql = "SELECT fm.*, f.name AS field_name FROM Fertilizers_Medicines fm " +
                "LEFT JOIN Fields f ON fm.field_id = f.field_id " +
                "WHERE fm.type = ? ORDER BY fm.fm_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, type);
        ResultSet rs = ps.executeQuery();

        List<FertilizerMedicine> items = new ArrayList<>();
        while (rs.next()) {
            items.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return items;
    }

    public static int addItem(Integer fieldId, String name, String type, String composition,
                              String activeIngredient, String targetDisease, double quantity,
                              String unit, boolean isOrganic, String notes) throws SQLException {
        String sql = "INSERT INTO Fertilizers_Medicines " +
                "(field_id, name, type, composition, active_ingredient, target_disease, quantity, unit, is_organic, applied_date, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?) RETURNING fm_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        if (fieldId != null) ps.setInt(1, fieldId); else ps.setNull(1, Types.INTEGER);
        ps.setString(2, name);
        ps.setString(3, type);
        ps.setString(4, composition);
        ps.setString(5, activeIngredient);
        ps.setString(6, targetDisease);
        ps.setDouble(7, quantity);
        ps.setString(8, unit);
        ps.setBoolean(9, isOrganic);
        ps.setString(10, notes);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int fmId = rs.getInt(1);
        rs.close(); ps.close();
        return fmId;
    }

    public static boolean updateQuantity(int fmId, double newQuantity) throws SQLException {
        String sql = "UPDATE Fertilizers_Medicines SET quantity = ? WHERE fm_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, newQuantity);
        ps.setInt(2, fmId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean deleteItem(int fmId) throws SQLException {
        String sql = "DELETE FROM Fertilizers_Medicines WHERE fm_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fmId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static int getTotalItemsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Fertilizers_Medicines";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    public static int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Fertilizers_Medicines WHERE quantity < ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, LOW_STOCK_THRESHOLD);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    private static FertilizerMedicine mapRow(ResultSet rs) throws SQLException {
        FertilizerMedicine fm = new FertilizerMedicine();
        fm.setFmId(rs.getInt("fm_id"));
        int fieldId = rs.getInt("field_id");
        fm.setFieldId(rs.wasNull() ? null : fieldId);
        fm.setFieldName(rs.getString("field_name"));
        fm.setName(rs.getString("name"));
        fm.setType(rs.getString("type"));
        fm.setComposition(rs.getString("composition"));
        fm.setActiveIngredient(rs.getString("active_ingredient"));
        fm.setTargetDisease(rs.getString("target_disease"));
        fm.setQuantity(rs.getDouble("quantity"));
        fm.setUnit(rs.getString("unit"));
        fm.setOrganic(rs.getBoolean("is_organic"));
        Date date = rs.getDate("applied_date");
        fm.setAppliedDate(date != null ? date.toLocalDate() : null);
        fm.setNotes(rs.getString("notes"));
        fm.setLowStock(fm.getQuantity() < LOW_STOCK_THRESHOLD);
        return fm;
    }
}
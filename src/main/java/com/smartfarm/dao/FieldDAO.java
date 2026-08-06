package com.smartfarm.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.Field;
import com.smartfarm.util.DatabaseConnection;

public class FieldDAO {

    public static List<Field> getAllFields() throws SQLException {
        String sql = "SELECT * FROM Fields WHERE farm_id = 1 ORDER BY field_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Field> fields = new ArrayList<>();
        while (rs.next()) {
            fields.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return fields;
    }

    public static Field getFieldById(int fieldId) throws SQLException {
        String sql = "SELECT * FROM Fields WHERE field_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fieldId);
        ResultSet rs = ps.executeQuery();

        Field field = rs.next() ? mapRow(rs) : null;
        rs.close(); ps.close();
        return field;
    }

    public static int addField(String name, double sizeDunums, String location) throws SQLException {
        String sql = "INSERT INTO Fields (farm_id, name, size_dunums, location) VALUES (1, ?, ?, ?) RETURNING field_id";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setDouble(2, sizeDunums);
        ps.setString(3, location);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int fieldId = rs.getInt(1);
        rs.close(); ps.close();
        return fieldId;
    }

    public static boolean updateField(int fieldId, String name, double sizeDunums, String location) throws SQLException {
        String sql = "UPDATE Fields SET name = ?, size_dunums = ?, location = ? WHERE field_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setDouble(2, sizeDunums);
        ps.setString(3, location);
        ps.setInt(4, fieldId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static boolean deleteField(int fieldId) throws SQLException {
        String sql = "DELETE FROM Fields WHERE field_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, fieldId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static int getTotalFieldsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Fields WHERE farm_id = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close(); ps.close();
        return count;
    }

    public static double getTotalDunums() throws SQLException {
        String sql = "SELECT COALESCE(SUM(size_dunums), 0) FROM Fields WHERE farm_id = 1";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        double total = rs.next() ? rs.getDouble(1) : 0;
        rs.close(); ps.close();
        return total;
    }

    private static Field mapRow(ResultSet rs) throws SQLException {
        Field f = new Field();
        f.setFieldId(rs.getInt("field_id"));
        f.setFarmId(rs.getInt("farm_id"));
        f.setName(rs.getString("name"));
        f.setSizeDunums(rs.getDouble("size_dunums"));
        f.setLocation(rs.getString("location"));
        return f;
    }
}
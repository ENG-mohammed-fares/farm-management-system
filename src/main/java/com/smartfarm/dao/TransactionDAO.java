package com.smartfarm.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.smartfarm.model.Transaction;
import com.smartfarm.util.DatabaseConnection;

public class TransactionDAO {

    public static List<Transaction> getAllTransactions() throws SQLException {
        String sql = "SELECT t.*, u.name AS worker_name FROM Transactions t " +
                "LEFT JOIN Users u ON t.related_user_id = u.user_id " +
                "ORDER BY t.transaction_date DESC, t.transaction_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return transactions;
    }

    public static List<Transaction> getTransactionsByType(String type) throws SQLException {
        String sql = "SELECT t.*, u.name AS worker_name FROM Transactions t " +
                "LEFT JOIN Users u ON t.related_user_id = u.user_id " +
                "WHERE t.type = ? ORDER BY t.transaction_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, type);
        ResultSet rs = ps.executeQuery();

        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return transactions;
    }

    public static List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        String sql = "SELECT t.*, u.name AS worker_name FROM Transactions t " +
                "LEFT JOIN Users u ON t.related_user_id = u.user_id " +
                "WHERE t.related_user_id = ? ORDER BY t.transaction_date DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        List<Transaction> transactions = new ArrayList<>();
        while (rs.next()) {
            transactions.add(mapRow(rs));
        }
        rs.close(); ps.close();
        return transactions;
    }

    public static int addTransaction(String type, double amount, String description,
                                     Integer relatedHarvestId, Integer relatedUserId) throws SQLException {
        String sql = "INSERT INTO Transactions (type, amount, description, related_harvest_id, related_user_id, transaction_date) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_DATE)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, type);
        ps.setDouble(2, amount);
        ps.setString(3, description);
        if (relatedHarvestId != null) ps.setInt(4, relatedHarvestId); else ps.setNull(4, Types.INTEGER);
        if (relatedUserId != null) ps.setInt(5, relatedUserId); else ps.setNull(5, Types.INTEGER);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        ps.close();
        throw new SQLException("Failed to retrieve generated transaction_id");
    }

    public static boolean deleteTransaction(int transactionId) throws SQLException {
        String sql = "DELETE FROM Transactions WHERE transaction_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, transactionId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public static double getTotalRevenue() throws SQLException {
        return getSumByType("SALE");
    }

    public static double getTotalExpenses() throws SQLException {
        double purchases = getSumByType("PURCHASE");
        double payments = getSumByType("PAYMENT");
        return purchases + payments;
    }

    public static double getNetProfit() throws SQLException {
        return getTotalRevenue() - getTotalExpenses();
    }

    private static double getSumByType(String type) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM Transactions WHERE type = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, type);
        ResultSet rs = ps.executeQuery();
        double total = rs.next() ? rs.getDouble(1) : 0;
        rs.close(); ps.close();
        return total;
    }

    private static Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getDouble("amount"));
        t.setDescription(rs.getString("description"));
        int harvestId = rs.getInt("related_harvest_id");
        t.setRelatedHarvestId(rs.wasNull() ? null : harvestId);
        int userId = rs.getInt("related_user_id");
        t.setRelatedUserId(rs.wasNull() ? null : userId);
        t.setWorkerName(rs.getString("worker_name"));
        Date date = rs.getDate("transaction_date");
        t.setTransactionDate(date != null ? date.toLocalDate() : null);
        return t;
    }
}
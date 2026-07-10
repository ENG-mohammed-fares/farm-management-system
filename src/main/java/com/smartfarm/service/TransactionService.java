package com.smartfarm.service;

import java.sql.SQLException;
import java.util.List;

import com.smartfarm.dao.TransactionDAO;
import com.smartfarm.model.Transaction;

public class TransactionService {

    public static class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class FinancialSummary {
        public final double revenue;
        public final double expenses;
        public final double netProfit;

        public FinancialSummary(double revenue, double expenses, double netProfit) {
            this.revenue = revenue;
            this.expenses = expenses;
            this.netProfit = netProfit;
        }
    }

    public static List<Transaction> getAllTransactions() {
        try {
            return TransactionDAO.getAllTransactions();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Transaction> getTransactionsByType(String type) {
        try {
            return "ALL".equals(type) ? TransactionDAO.getAllTransactions() : TransactionDAO.getTransactionsByType(type);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Transaction> getTransactionsByUser(int userId) {
        try {
            return TransactionDAO.getTransactionsByUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Result recordSale(double amount, String description, Integer relatedHarvestId) {
        if (amount <= 0) {
            return new Result(false, "Amount must be greater than 0");
        }
        try {
            TransactionDAO.addTransaction("SALE", amount, description, relatedHarvestId, null);
            return new Result(true, "Sale recorded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result recordPurchase(double amount, String description) {
        if (amount <= 0) {
            return new Result(false, "Amount must be greater than 0");
        }
        try {
            TransactionDAO.addTransaction("PURCHASE", amount, description, null, null);
            return new Result(true, "Purchase recorded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result recordPayment(double amount, String description, int workerUserId) {
        if (amount <= 0) {
            return new Result(false, "Amount must be greater than 0");
        }
        try {
            TransactionDAO.addTransaction("PAYMENT", amount, description, null, workerUserId);
            return new Result(true, "Payment recorded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result recordWaterCost(double cubicMeters) {
        double cost = cubicMeters * 4.0;
        return recordPurchase(cost, "Water cost: " + cubicMeters + " m3 x 4 NIS");
    }

    public static Result deleteTransaction(int transactionId) {
        try {
            boolean deleted = TransactionDAO.deleteTransaction(transactionId);
            return deleted ? new Result(true, "Transaction deleted") : new Result(false, "Transaction not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static FinancialSummary getFinancialSummary() {
        try {
            double revenue = TransactionDAO.getTotalRevenue();
            double expenses = TransactionDAO.getTotalExpenses();
            double netProfit = revenue - expenses;
            return new FinancialSummary(revenue, expenses, netProfit);
        } catch (SQLException e) {
            e.printStackTrace();
            return new FinancialSummary(0, 0, 0);
        }
    }
}
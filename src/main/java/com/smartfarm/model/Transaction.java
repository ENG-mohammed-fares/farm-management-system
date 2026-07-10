package com.smartfarm.model;

import java.time.LocalDate;

public class Transaction {

    private int transactionId;
    private String type;
    private double amount;
    private String description;
    private Integer relatedHarvestId;
    private Integer relatedUserId;
    private String workerName;
    private LocalDate transactionDate;

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRelatedHarvestId() { return relatedHarvestId; }
    public void setRelatedHarvestId(Integer relatedHarvestId) { this.relatedHarvestId = relatedHarvestId; }

    public Integer getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Integer relatedUserId) { this.relatedUserId = relatedUserId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
}
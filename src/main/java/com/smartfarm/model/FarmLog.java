package com.smartfarm.model;

import java.time.LocalDate;

public class FarmLog {

    private int logId;
    private int fieldId;
    private String fieldName;
    private int fwId;
    private String workerName;
    private String logType;
    private String description;
    private Double quantity;
    private LocalDate logDate;

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public int getFwId() { return fwId; }
    public void setFwId(int fwId) { this.fwId = fwId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
}
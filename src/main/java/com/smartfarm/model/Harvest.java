package com.smartfarm.model;

import java.time.LocalDate;

public class Harvest {

    private int harvestId;
    private int fieldId;
    private String fieldName;
    private int cropId;
    private String cropName;
    private int fwId;
    private String workerName;
    private double quantityGood;
    private double quantityDamaged;
    private String unit;
    private LocalDate harvestDate;
    private String notes;
    private String status;

    public int getHarvestId() { return harvestId; }
    public void setHarvestId(int harvestId) { this.harvestId = harvestId; }

    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public int getCropId() { return cropId; }
    public void setCropId(int cropId) { this.cropId = cropId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public int getFwId() { return fwId; }
    public void setFwId(int fwId) { this.fwId = fwId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public double getQuantityGood() { return quantityGood; }
    public void setQuantityGood(double quantityGood) { this.quantityGood = quantityGood; }

    public double getQuantityDamaged() { return quantityDamaged; }
    public void setQuantityDamaged(double quantityDamaged) { this.quantityDamaged = quantityDamaged; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
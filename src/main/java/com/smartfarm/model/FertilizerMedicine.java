package com.smartfarm.model;

import java.time.LocalDate;

public class FertilizerMedicine {

    private int fmId;
    private Integer fieldId;
    private String fieldName;
    private String name;
    private String type;
    private String composition;
    private String activeIngredient;
    private String targetDisease;
    private double quantity;
    private String unit;
    private boolean isOrganic;
    private LocalDate appliedDate;
    private String notes;
    private boolean lowStock;

    public int getFmId() { return fmId; }
    public void setFmId(int fmId) { this.fmId = fmId; }

    public Integer getFieldId() { return fieldId; }
    public void setFieldId(Integer fieldId) { this.fieldId = fieldId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getComposition() { return composition; }
    public void setComposition(String composition) { this.composition = composition; }

    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }

    public String getTargetDisease() { return targetDisease; }
    public void setTargetDisease(String targetDisease) { this.targetDisease = targetDisease; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public boolean isOrganic() { return isOrganic; }
    public void setOrganic(boolean organic) { isOrganic = organic; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isLowStock() { return lowStock; }
    public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
}
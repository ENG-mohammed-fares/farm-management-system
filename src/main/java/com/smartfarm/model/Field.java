package com.smartfarm.model;

public class Field {

    private int fieldId;
    private int farmId;
    private String name;
    private double sizeDunums;
    private String location;

    public int getFieldId() { return fieldId; }
    public void setFieldId(int fieldId) { this.fieldId = fieldId; }

    public int getFarmId() { return farmId; }
    public void setFarmId(int farmId) { this.farmId = farmId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSizeDunums() { return sizeDunums; }
    public void setSizeDunums(double sizeDunums) { this.sizeDunums = sizeDunums; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
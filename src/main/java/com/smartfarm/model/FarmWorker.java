package com.smartfarm.model;

import java.time.LocalDate;

public class FarmWorker {

    private int fwId;
    private int farmId;
    private int userId;
    private String userName;
    private String jobType;
    private double wagePerUnit;
    private String wageUnit;
    private String status;
    private LocalDate hiredAt;

    public int getFwId() { return fwId; }
    public void setFwId(int fwId) { this.fwId = fwId; }

    public int getFarmId() { return farmId; }
    public void setFarmId(int farmId) { this.farmId = farmId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public double getWagePerUnit() { return wagePerUnit; }
    public void setWagePerUnit(double wagePerUnit) { this.wagePerUnit = wagePerUnit; }

    public String getWageUnit() { return wageUnit; }
    public void setWageUnit(String wageUnit) { this.wageUnit = wageUnit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return "ACTIVE".equals(status); }

    public LocalDate getHiredAt() { return hiredAt; }
    public void setHiredAt(LocalDate hiredAt) { this.hiredAt = hiredAt; }
}
package com.smartfarm.service;

import java.sql.SQLException;
import java.util.List;

import com.smartfarm.dao.FarmLogDAO;
import com.smartfarm.dao.FarmWorkerDAO;
import com.smartfarm.dao.HarvestDAO;
import com.smartfarm.model.FarmLog;
import com.smartfarm.model.FarmWorker;
import com.smartfarm.model.Harvest;

public class WorkerService {

    public static class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class EarningsSummary {
        public final double totalGoodQuantity;
        public final double totalEarned;

        public EarningsSummary(double totalGoodQuantity, double totalEarned) {
            this.totalGoodQuantity = totalGoodQuantity;
            this.totalEarned = totalEarned;
        }
    }

    private static boolean isValidWageUnit(String jobType, String wageUnit) {
        if (jobType == null || wageUnit == null) return false;
        switch (jobType) {
            case "IRRIGATOR": return "cup".equals(wageUnit);
            case "HARVESTER": return "kg".equals(wageUnit) ;
            case "PLOWER": return "dunum".equals(wageUnit);
            default: return "kg".equals(wageUnit) || "cup".equals(wageUnit) || "dunum".equals(wageUnit) ;
        }
    }

    // ===================== WORKER MANAGEMENT =====================

    public static List<FarmWorker> getAllWorkers() {
        try {
            return FarmWorkerDAO.getAllWorkers();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static FarmWorker getWorkerByFwId(int fwId) {
        try {
            return FarmWorkerDAO.getWorkerByFwId(fwId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Result updateWorker(int fwId, String jobType, String wageText, String wageUnit, String status) {
        double wage;
        try {
            wage = Double.parseDouble(wageText.trim());
            if (wage <= 0) return new Result(false, "Wage must be greater than 0");
        } catch (Exception e) {
            return new Result(false, "Wage must be a valid number");
        }

        if (!isValidWageUnit(jobType, wageUnit)) {
            return new Result(false, "Selected wage unit is not valid for this job type");
        }
        try {
            boolean updated = FarmWorkerDAO.updateWorker(fwId, jobType, wage, wageUnit, status);
            return updated ? new Result(true, "Worker updated successfully") : new Result(false, "Worker not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result deactivateWorker(int fwId) {
        try {
            boolean updated = FarmWorkerDAO.setWorkerStatus(fwId, "INACTIVE");
            return updated ? new Result(true, "Worker deactivated") : new Result(false, "Worker not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result deleteWorker(int fwId) {
        try {
            boolean deleted = FarmWorkerDAO.deleteWorker(fwId);
            return deleted ? new Result(true, "Worker removed successfully") : new Result(false, "Worker not found");
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("foreign key") || msg.contains("violates")) {
                return new Result(false, "Cannot delete: this worker has existing harvests or logs. Deactivate instead.");
            }
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result activateWorker(int fwId) {
        try {
            boolean updated = FarmWorkerDAO.setWorkerStatus(fwId, "ACTIVE");
            return updated ? new Result(true, "Worker activated") : new Result(false, "Worker not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static int getTotalWorkersCount() {
        try {
            return FarmWorkerDAO.getTotalWorkersCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static int getActiveWorkersCount() {
        try {
            return FarmWorkerDAO.getActiveWorkersCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static Result assignNewWorker(String identifier, String jobType, String wageText, String wageUnit) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return new Result(false, "Please enter a name, email, or phone");
        }

        double wage;
        try {
            wage = Double.parseDouble(wageText.trim());
            if (wage <= 0) return new Result(false, "Wage must be greater than 0");
        } catch (Exception e) {
            return new Result(false, "Wage must be a valid number");
        }

        if (!isValidWageUnit(jobType, wageUnit)) {
            return new Result(false, "Selected wage unit is not valid for this job type");
        }

        try {
            com.smartfarm.model.User user = com.smartfarm.dao.UserDAO.findUserByIdentifier(identifier.trim());
            if (user == null) {
                return new Result(false, "No registered user found with this name, email, or phone");
            }
            if (user.isAdmin()) {
                return new Result(false, "Cannot assign the Admin account as a worker");
            }

            int existingFwId = FarmWorkerDAO.getFwId(user.getUserId());
            if (existingFwId != -1) {
                return new Result(false, user.getName() + " is already an active worker");
            }

            int inactiveFwId = FarmWorkerDAO.getAnyFwId(user.getUserId());
            if (inactiveFwId != -1) {
                boolean updated = FarmWorkerDAO.updateWorker(inactiveFwId, jobType, wage, wageUnit, "ACTIVE");
                return updated
                        ? new Result(true, user.getName() + " has been reactivated as a worker")
                        : new Result(false, "Could not reactivate worker");
            }

            FarmWorkerDAO.assignWorker(user.getUserId(), jobType, wage, wageUnit);
            return new Result(true, user.getName() + " has been added as a worker");

        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    // ===================== HARVESTS & WAGE CALCULATION =====================

    public static List<Harvest> getAllHarvests() {
        try {
            return HarvestDAO.getAllHarvests();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Harvest> getHarvestsByWorker(int fwId) {
        try {
            return HarvestDAO.getHarvestsByWorker(fwId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<FarmLog> getLogsByWorker(int fwId) {
        try {
            return FarmLogDAO.getLogsByWorker(fwId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Maps IRRIGATOR→IRRIGATION, PLOWER→PLOWING; null for HARVESTER/unknown. */
    public static String logTypeForJob(String jobType) {
        if ("IRRIGATOR".equals(jobType)) return "IRRIGATION";
        if ("PLOWER".equals(jobType)) return "PLOWING";
        return null;
    }

    public static List<Harvest> getHarvestsByStatus(String status) {
        try {
            return "ALL".equals(status) ? HarvestDAO.getAllHarvests() : HarvestDAO.getHarvestsByStatus(status);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static int getPendingHarvestsCount() {
        try {
            return HarvestDAO.getPendingCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static Result approveHarvest(int harvestId) {
        try {
            boolean updated = HarvestDAO.updateStatus(harvestId, "APPROVED");
            return updated ? new Result(true, "Harvest approved") : new Result(false, "Harvest not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result rejectHarvest(int harvestId) {
        try {
            boolean updated = HarvestDAO.updateStatus(harvestId, "REJECTED");
            return updated ? new Result(true, "Harvest rejected") : new Result(false, "Harvest not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result submitHarvest(int fieldId, int cropId, int fwId, String goodQtyText,
                                       String damagedQtyText, String unit, String notes) {
        FarmWorker worker = getWorkerByFwId(fwId);
        if (worker == null || !worker.isActive()) {
            return new Result(false, "Your account is inactive. Contact your Admin.");
        }

        double goodQty;
        double damagedQty;

        try {
            goodQty = Double.parseDouble(goodQtyText.trim());
            if (goodQty < 0) return new Result(false, "Good quantity cannot be negative");
        } catch (Exception e) {
            return new Result(false, "Good quantity must be a valid number");
        }

        try {
            damagedQty = damagedQtyText == null || damagedQtyText.trim().isEmpty()
                    ? 0 : Double.parseDouble(damagedQtyText.trim());
            if (damagedQty < 0) return new Result(false, "Damaged quantity cannot be negative");
        } catch (Exception e) {
            return new Result(false, "Damaged quantity must be a valid number");
        }

        if (goodQty == 0 && damagedQty == 0) {
            return new Result(false, "Please enter at least a good or damaged quantity");
        }

        try {
            HarvestDAO.addHarvest(fieldId, cropId, fwId, goodQty, damagedQty, unit, notes);
            double estimatedWage = calculateWage(fwId, goodQty);
            return new Result(true, String.format(java.util.Locale.US, "Harvest submitted! Pending approval. Estimated wage: %.2f NIS", estimatedWage));
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static double calculateWage(int fwId, double quantityGood) {
        try {
            double wagePerUnit = FarmWorkerDAO.getWagePerUnit(fwId);
            return quantityGood * wagePerUnit;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static EarningsSummary getWorkerEarnings(int fwId) {
        try {
            FarmWorker worker = FarmWorkerDAO.getWorkerByFwId(fwId);
            if (worker == null) {
                return new EarningsSummary(0, 0);
            }

            String logType = logTypeForJob(worker.getJobType());
            if (logType != null) {
                double totalQty = FarmLogDAO.getWorkerTotalQuantity(fwId, logType);
                double totalEarned = FarmLogDAO.getWorkerTotalEarnings(fwId, logType);
                return new EarningsSummary(totalQty, totalEarned);
            }

            double totalQty = HarvestDAO.getWorkerTotalQuantity(fwId);
            double totalEarned = HarvestDAO.getWorkerTotalEarnings(fwId);
            return new EarningsSummary(totalQty, totalEarned);
        } catch (SQLException e) {
            e.printStackTrace();
            return new EarningsSummary(0, 0);
        }
    }

    public static int getTotalHarvestsCount() {
        try {
            return HarvestDAO.getTotalHarvestsCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static double getTotalGoodQuantity() {
        try {
            return HarvestDAO.getTotalGoodQuantity();
        } catch (SQLException e) {
            return 0;
        }
    }
}
package com.smartfarm.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.smartfarm.dao.CropDAO;
import com.smartfarm.dao.FarmLogDAO;
import com.smartfarm.dao.FertilizerMedicineDAO;
import com.smartfarm.dao.FieldDAO;
import com.smartfarm.model.Crop;
import com.smartfarm.model.FarmLog;
import com.smartfarm.model.Field;
import com.smartfarm.model.FertilizerMedicine;

public class FarmService {

    public static class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    // ===================== FIELDS =====================

    public static List<Field> getAllFields() {
        try {
            return FieldDAO.getAllFields();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static boolean isValidItemUnit(String type, String unit) {
        if (type == null || unit == null) return false;
        if ("MEDICINE".equals(type)) {
            return "liter/cup".equals(unit) || "liter/dunum".equals(unit);
        }
        return "kg/dunum".equals(unit) || "kg/cup".equals(unit);
    }

    public static Result addField(String name, String sizeText, String unit, String location) {
        if (name == null || name.trim().isEmpty()) {
            return new Result(false, "Field name is required");
        }
        double sizeInput;
        try {
            sizeInput = Double.parseDouble(sizeText.trim());
            if (sizeInput <= 0) return new Result(false, "Size must be greater than 0");
        } catch (Exception e) {
            return new Result(false, "Size must be a valid number");
        }

        double sizeInSquareMeters = com.smartfarm.util.AreaUnitConverter.toSquareMeters(sizeInput, unit);

        try {
            FieldDAO.addField(name.trim(), sizeInSquareMeters, location != null ? location.trim() : "");
            return new Result(true, "Field added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error: " + e.getMessage());
        }
    }

    public static Result updateField(int fieldId, String name, double size, String location) {
        try {
            boolean updated = FieldDAO.updateField(fieldId, name, size, location);
            return updated ? new Result(true, "Field updated") : new Result(false, "Field not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result deleteField(int fieldId) {
        try {
            List<Crop> crops = CropDAO.getCropsByField(fieldId);
            if (!crops.isEmpty()) {
                return new Result(false, "Cannot delete field with active crops");
            }
            boolean deleted = FieldDAO.deleteField(fieldId);
            return deleted ? new Result(true, "Field deleted") : new Result(false, "Field not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static int getTotalFieldsCount() {
        try {
            return FieldDAO.getTotalFieldsCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static double getTotalDunums() {
        try {
            return FieldDAO.getTotalDunums();
        } catch (SQLException e) {
            return 0;
        }
    }

    // ===================== CROPS =====================

    public static List<Crop> getAllCrops() {
        try {
            return CropDAO.getAllCrops();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<Crop> getCropsByField(int fieldId) {
        try {
            return CropDAO.getCropsByField(fieldId);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Result addCrop(int fieldId, String name, String type, LocalDate plantedDate, String quantity) {
        if (name == null || name.trim().isEmpty()) {
            return new Result(false, "Crop name is required");
        }
        try {
            CropDAO.addCrop(fieldId, name.trim(), type, plantedDate, quantity);
            return new Result(true, "Crop added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result updateCropStatus(int cropId, String status) {
        try {
            boolean updated = CropDAO.updateCropStatus(cropId, status);
            return updated ? new Result(true, "Status updated") : new Result(false, "Crop not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static Result deleteCrop(int cropId) {
        try {
            boolean deleted = CropDAO.deleteCrop(cropId);
            return deleted ? new Result(true, "Crop deleted") : new Result(false, "Crop not found");
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("foreign key") || msg.contains("violates")) {
                return new Result(false, "Cannot delete: this crop is linked to harvests");
            }
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static int getActiveCropsCount() {
        try {
            return CropDAO.getActiveCropsCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    // ===================== FARM LOGS =====================

    public static List<FarmLog> getAllLogs() {
        try {
            return FarmLogDAO.getAllLogs();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<FarmLog> getLogsByType(String logType) {
        try {
            return "ALL".equals(logType) ? FarmLogDAO.getAllLogs() : FarmLogDAO.getLogsByType(logType);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Result addLog(int fieldId, int fwId, String logType, String description, Double quantity) {
        try {
            FarmLogDAO.addLog(fieldId, fwId, logType, description, quantity);
            return new Result(true, "Activity logged successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static boolean hasLogToday(int fieldId, String logType) {
        try {
            List<FarmLog> logs = FarmLogDAO.getLogsByFieldAndDate(fieldId, LocalDate.now(), logType);
            return !logs.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===================== FERTILIZERS & MEDICINES =====================

    public static List<FertilizerMedicine> getAllFertilizersAndMedicines() {
        try {
            return FertilizerMedicineDAO.getAllItems();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<FertilizerMedicine> getItemsByType(String type) {
        try {
            return "ALL".equals(type) ? FertilizerMedicineDAO.getAllItems() : FertilizerMedicineDAO.getItemsByType(type);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static Result addFertilizerOrMedicine(Integer fieldId, String name, String type, String composition,
                                                 String activeIngredient, String targetDisease, double quantity,
                                                 String unit, boolean isOrganic, String notes) {
        if (name == null || name.trim().isEmpty()) {
            return new Result(false, "Name is required");
        }
        if (!isValidItemUnit(type, unit)) {
            return new Result(false, "Unit is not valid for the selected item type");
        }
        try {
            FertilizerMedicineDAO.addItem(fieldId, name.trim(), type, composition, activeIngredient,
                    targetDisease, quantity, unit, isOrganic, notes);
            return new Result(true, "Item added successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }

    public static int getTotalItemsCount() {
        try {
            return FertilizerMedicineDAO.getTotalItemsCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static int getLowStockCount() {
        try {
            return FertilizerMedicineDAO.getLowStockCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    public static Result deleteFertilizerOrMedicine(int fmId) {
        try {
            boolean deleted = FertilizerMedicineDAO.deleteItem(fmId);
            return deleted ? new Result(true, "Item deleted") : new Result(false, "Item not found");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Result(false, "Database error");
        }
    }
}
package com.smartfarm.util;

public class AreaUnitConverter {

    public static final String SQUARE_METER = "Square Meter (m\u00B2)";
    public static final String DUNUM = "Dunum";
    public static final String HECTARE = "Hectare";
    public static final String ACRE = "Acre";

    public static final String[] ALL_UNITS = { SQUARE_METER, DUNUM, HECTARE, ACRE };

    public static double toSquareMeters(double value, String unit) {
        switch (unit) {
            case DUNUM: return value * 1000.0;
            case HECTARE: return value * 10000.0;
            case ACRE: return value * 4046.86;
            case SQUARE_METER:
            default: return value;
        }
    }

    public static double fromSquareMeters(double squareMeters, String unit) {
        switch (unit) {
            case DUNUM: return squareMeters / 1000.0;
            case HECTARE: return squareMeters / 10000.0;
            case ACRE: return squareMeters / 4046.86;
            case SQUARE_METER:
            default: return squareMeters;
        }
    }

    public static String formatWithUnit(double squareMeters, String displayUnit) {
        double converted = fromSquareMeters(squareMeters, displayUnit);
        String unitLabel = getShortLabel(displayUnit);
        return String.format("%,.2f %s", converted, unitLabel);
    }

    public static String getShortLabel(String unit) {
        switch (unit) {
            case DUNUM: return "dunum";
            case HECTARE: return "ha";
            case ACRE: return "acre";
            case SQUARE_METER:
            default: return "m\u00B2";
        }
    }
}
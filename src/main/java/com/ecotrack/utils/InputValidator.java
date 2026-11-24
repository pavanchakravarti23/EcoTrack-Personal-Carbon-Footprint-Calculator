package com.ecotrack.utils;

/**
 * Input validation helpers ensuring safe parsing, range checks, and clear error messages.
 */
public final class InputValidator {

    public static final double MAX_REASONABLE_VALUE = 1_000_000_000.0;

    private InputValidator() {
    }

    public static double parseNonNegativeDouble(String rawText, String fieldName) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        double value;
        try {
            value = Double.parseDouble(rawText.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(fieldName + " must be a finite number.");
        }
        if (value < 0.0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        if (value > MAX_REASONABLE_VALUE) {
            throw new IllegalArgumentException(fieldName + " exceeds the supported upper limit (" + MAX_REASONABLE_VALUE + ").");
        }
        return value;
    }

    public static double parsePositiveDouble(String rawText, String fieldName) {
        double value = parseNonNegativeDouble(rawText, fieldName);
        if (value <= 0.0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    public static double parsePercentage(String rawText, String fieldName) {
        double value = parseNonNegativeDouble(rawText, fieldName);
        if (value > 100.0) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 100%.");
        }
        return value;
    }

    public static int parseNonNegativeInt(String rawText, String fieldName, int maxValue) {
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        int value;
        try {
            value = Integer.parseInt(rawText.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative.");
        }
        if (value > maxValue) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxValue + ".");
        }
        return value;
    }

    public static int parsePositiveInt(String rawText, String fieldName, int maxValue) {
        int value = parseNonNegativeInt(rawText, fieldName, maxValue);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be at least 1.");
        }
        return value;
    }
}

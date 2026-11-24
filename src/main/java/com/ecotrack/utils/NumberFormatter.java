package com.ecotrack.utils;

import java.text.DecimalFormat;

/**
 * Consistent number formatting utilities for emissions, percentages, and distances.
 */
public final class NumberFormatter {

    private static final DecimalFormat KG_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat FACTOR_FORMAT = new DecimalFormat("0.###");

    private NumberFormatter() {
    }

    public static String formatKg(double kg) {
        if (Double.isNaN(kg) || Double.isInfinite(kg)) {
            return "0.00 kg CO2e";
        }
        return KG_FORMAT.format(kg) + " kg CO2e";
    }

    public static String formatKgCompact(double kg) {
        if (Double.isNaN(kg) || Double.isInfinite(kg)) {
            return "0.00";
        }
        if (Math.abs(kg) >= 10000.0) {
            return new DecimalFormat("#,##0.0").format(kg / 1000.0) + " t";
        }
        return KG_FORMAT.format(kg);
    }

    public static String formatNumber(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0.00";
        }
        return KG_FORMAT.format(value);
    }

    public static String formatPercent(double pct) {
        if (Double.isNaN(pct) || Double.isInfinite(pct)) {
            return "0.0%";
        }
        return PCT_FORMAT.format(pct) + "%";
    }

    public static String formatFactor(double factor) {
        return FACTOR_FORMAT.format(factor);
    }
}

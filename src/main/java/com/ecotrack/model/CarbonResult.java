package com.ecotrack.model;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/**
 * Aggregated carbon footprint calculation output for the active period and annual projection.
 */
public class CarbonResult {
    private final ActivityData.CalculationPeriod period;
    private final Map<EmissionCategory, Double> categoryEmissions;
    private final double totalPeriodEmissionsKg;
    private final double monthlyEquivalentKg;
    private final double annualProjectionKg;
    private final EmissionCategory highestCategory;
    private final LocalDateTime calculatedAt;
    private final boolean hasAnyData;
    private final String summaryNote;

    public CarbonResult(
            ActivityData.CalculationPeriod period,
            Map<EmissionCategory, Double> categoryEmissions,
            double totalPeriodEmissionsKg,
            double monthlyEquivalentKg,
            double annualProjectionKg,
            EmissionCategory highestCategory,
            boolean hasAnyData,
            String summaryNote) {
        this.period = period;
        this.categoryEmissions = new EnumMap<>(EmissionCategory.class);
        for (EmissionCategory cat : EmissionCategory.values()) {
            this.categoryEmissions.put(cat, categoryEmissions.getOrDefault(cat, 0.0));
        }
        this.totalPeriodEmissionsKg = totalPeriodEmissionsKg;
        this.monthlyEquivalentKg = monthlyEquivalentKg;
        this.annualProjectionKg = annualProjectionKg;
        this.highestCategory = highestCategory;
        this.calculatedAt = LocalDateTime.now();
        this.hasAnyData = hasAnyData;
        this.summaryNote = summaryNote;
    }

    public ActivityData.CalculationPeriod getPeriod() {
        return period;
    }

    public Map<EmissionCategory, Double> getCategoryEmissions() {
        return categoryEmissions;
    }

    public double getEmission(EmissionCategory category) {
        return categoryEmissions.getOrDefault(category, 0.0);
    }

    public double getPercentage(EmissionCategory category) {
        if (totalPeriodEmissionsKg <= 0.0) {
            return 0.0;
        }
        return (getEmission(category) / totalPeriodEmissionsKg) * 100.0;
    }

    public double getTotalPeriodEmissionsKg() {
        return totalPeriodEmissionsKg;
    }

    public double getMonthlyEquivalentKg() {
        return monthlyEquivalentKg;
    }

    public double getAnnualProjectionKg() {
        return annualProjectionKg;
    }

    public EmissionCategory getHighestCategory() {
        return highestCategory;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public boolean isHasAnyData() {
        return hasAnyData;
    }

    public String getSummaryNote() {
        return summaryNote;
    }
}

package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;

import java.util.EnumMap;
import java.util.Map;

/**
 * Calculates household waste emissions across six streams without double-counting:
 * each kilogram of generated waste is partitioned cleanly into diverted (recycled/composted)
 * vs. residual disposed mass before applying distinct factors.
 */
public class WasteCalculator {

    public static class StreamBreakdown {
        private final double weeklyTotalKg;
        private final double weeklyDivertedKg;
        private final double weeklyDisposedKg;
        private final double monthlyEmissionsKg;

        public StreamBreakdown(
                double weeklyTotalKg,
                double weeklyDivertedKg,
                double weeklyDisposedKg,
                double monthlyEmissionsKg) {
            this.weeklyTotalKg = weeklyTotalKg;
            this.weeklyDivertedKg = weeklyDivertedKg;
            this.weeklyDisposedKg = weeklyDisposedKg;
            this.monthlyEmissionsKg = monthlyEmissionsKg;
        }

        public double getWeeklyTotalKg() {
            return weeklyTotalKg;
        }

        public double getWeeklyDivertedKg() {
            return weeklyDivertedKg;
        }

        public double getWeeklyDisposedKg() {
            return weeklyDisposedKg;
        }

        public double getMonthlyEmissionsKg() {
            return monthlyEmissionsKg;
        }
    }

    private final EmissionFactors factors;

    public WasteCalculator(EmissionFactors factors) {
        this.factors = factors;
    }

    public Map<ActivityData.WasteStream, StreamBreakdown> calculateStreamBreakdowns(ActivityData data) {
        Map<ActivityData.WasteStream, StreamBreakdown> map = new EnumMap<>(ActivityData.WasteStream.class);
        if (data == null || !data.isWasteEntered()) {
            for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
                map.put(stream, new StreamBreakdown(0.0, 0.0, 0.0, 0.0));
            }
            return map;
        }

        for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
            double weeklyKg = Math.max(0.0, data.getWeeklyWasteKg().getOrDefault(stream, 0.0));
            double recyclingPct = (stream == ActivityData.WasteStream.GENERAL)
                    ? 0.0
                    : Math.max(0.0, Math.min(100.0, data.getRecyclingPercentages().getOrDefault(stream, 0.0)));

            double divertedKg = weeklyKg * (recyclingPct / 100.0);
            double disposedKg = weeklyKg - divertedKg;

            double weeklyEmissions = (disposedKg * factors.getDisposalFactor(stream))
                    + (divertedKg * factors.getDiversionFactor(stream));
            double monthlyEmissions = weeklyEmissions * EmissionFactors.WEEKS_PER_MONTH;

            map.put(stream, new StreamBreakdown(weeklyKg, divertedKg, disposedKg, monthlyEmissions));
        }
        return map;
    }

    public double calculateMonthlyEmissionsKg(ActivityData data) {
        if (data == null || !data.isWasteEntered()) {
            return 0.0;
        }
        double total = 0.0;
        for (StreamBreakdown breakdown : calculateStreamBreakdowns(data).values()) {
            total += breakdown.getMonthlyEmissionsKg();
        }
        return total;
    }

    public double calculateForPeriodKg(ActivityData data) {
        return calculateMonthlyEmissionsKg(data) * data.getCalculationPeriod().getMultiplierFromMonthly();
    }

    public double calculateOverallDiversionPercentage(ActivityData data) {
        if (data == null || !data.isWasteEntered()) {
            return 0.0;
        }
        double totalKg = 0.0;
        double divertedKg = 0.0;
        for (StreamBreakdown b : calculateStreamBreakdowns(data).values()) {
            totalKg += b.getWeeklyTotalKg();
            divertedKg += b.getWeeklyDivertedKg();
        }
        if (totalKg <= 0.0) {
            return 0.0;
        }
        return (divertedKg / totalKg) * 100.0;
    }
}

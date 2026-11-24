package com.ecotrack.service;

import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prepares structured series data for Analytics charts:
 * category breakdown, monthly comparison benchmarks, and 12-month cumulative/annual projections.
 */
public class AnalyticsService {

    /**
     * Illustrative reference benchmark (NOT an official national standard) representing
     * a moderate low-carbon lifestyle monthly target (~250 kg CO2e/month = 3.0 t/year).
     */
    public static final double ILLUSTRATIVE_LOW_CARBON_MONTHLY_TARGET_KG = 250.0;

    public Map<String, Double> buildMonthlyComparisonSeries(CarbonResult result) {
        Map<String, Double> series = new LinkedHashMap<>();
        if (result == null || !result.isHasAnyData()) {
            return series;
        }
        double currentMonthly = result.getMonthlyEquivalentKg();
        series.put("Your Monthly Footprint", currentMonthly);
        series.put("Transport + Electricity",
                (result.getEmission(EmissionCategory.TRANSPORT) + result.getEmission(EmissionCategory.ELECTRICITY))
                        / result.getPeriod().getMultiplierFromMonthly());
        series.put("Food + Waste",
                (result.getEmission(EmissionCategory.FOOD) + result.getEmission(EmissionCategory.WASTE))
                        / result.getPeriod().getMultiplierFromMonthly());
        series.put("Illustrative Target (250 kg)", ILLUSTRATIVE_LOW_CARBON_MONTHLY_TARGET_KG);
        return series;
    }

    /**
     * Builds a 12-month cumulative projection comparing baseline current trajectory
     * against a 20% reduction trajectory.
     */
    public List<Double> buildCumulativeBaselineProjection(CarbonResult result) {
        List<Double> points = new ArrayList<>(12);
        double monthly = (result == null || !result.isHasAnyData()) ? 0.0 : result.getMonthlyEquivalentKg();
        for (int month = 1; month <= 12; month++) {
            points.add(monthly * month);
        }
        return points;
    }

    public List<Double> buildCumulativeReducedProjection(CarbonResult result, double reductionFraction) {
        List<Double> points = new ArrayList<>(12);
        double monthly = (result == null || !result.isHasAnyData()) ? 0.0 : result.getMonthlyEquivalentKg();
        double clamped = Math.max(0.0, Math.min(0.95, reductionFraction));
        double reducedMonthly = monthly * (1.0 - clamped);
        for (int month = 1; month <= 12; month++) {
            points.add(reducedMonthly * month);
        }
        return points;
    }
}

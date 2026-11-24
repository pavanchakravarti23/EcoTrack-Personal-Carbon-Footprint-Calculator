package com.ecotrack.model;

/**
 * Immutable output comparing original vs simulated carbon footprint without mutating original state.
 */
public class SimulationResult {
    private final CarbonResult originalResult;
    private final CarbonResult simulatedResult;
    private final ActivityData simulatedActivityData;
    private final double differenceKgCo2e; // positive means reduction
    private final double percentageReduction;

    public SimulationResult(
            CarbonResult originalResult,
            CarbonResult simulatedResult,
            ActivityData simulatedActivityData) {
        this.originalResult = originalResult;
        this.simulatedResult = simulatedResult;
        this.simulatedActivityData = simulatedActivityData;
        double orig = originalResult.getTotalPeriodEmissionsKg();
        double sim = simulatedResult.getTotalPeriodEmissionsKg();
        this.differenceKgCo2e = orig - sim;
        this.percentageReduction = (orig > 0.0) ? ((orig - sim) / orig) * 100.0 : 0.0;
    }

    public CarbonResult getOriginalResult() {
        return originalResult;
    }

    public CarbonResult getSimulatedResult() {
        return simulatedResult;
    }

    public ActivityData getSimulatedActivityData() {
        return simulatedActivityData;
    }

    public double getDifferenceKgCo2e() {
        return differenceKgCo2e;
    }

    public double getPercentageReduction() {
        return percentageReduction;
    }
}

package com.ecotrack.model;

/**
 * Parameterized lifestyle adjustment scenario for the What-If Simulator.
 */
public class SimulationScenario {
    private double travelDistanceReductionPct = 0.0; // 0 to 100%
    private ActivityData.TransportMode overrideTransportMode = null; // null keeps original
    private double electricityReductionPct = 0.0; // 0 to 100%
    private ActivityData.DietType overrideDietType = null; // null keeps original
    private double recyclingBoostPercentagePoints = 0.0; // 0 to 100 points added (capped at 100%)

    public double getTravelDistanceReductionPct() {
        return travelDistanceReductionPct;
    }

    public void setTravelDistanceReductionPct(double travelDistanceReductionPct) {
        this.travelDistanceReductionPct = Math.max(0.0, Math.min(100.0, travelDistanceReductionPct));
    }

    public ActivityData.TransportMode getOverrideTransportMode() {
        return overrideTransportMode;
    }

    public void setOverrideTransportMode(ActivityData.TransportMode overrideTransportMode) {
        this.overrideTransportMode = overrideTransportMode;
    }

    public double getElectricityReductionPct() {
        return electricityReductionPct;
    }

    public void setElectricityReductionPct(double electricityReductionPct) {
        this.electricityReductionPct = Math.max(0.0, Math.min(100.0, electricityReductionPct));
    }

    public ActivityData.DietType getOverrideDietType() {
        return overrideDietType;
    }

    public void setOverrideDietType(ActivityData.DietType overrideDietType) {
        this.overrideDietType = overrideDietType;
    }

    public double getRecyclingBoostPercentagePoints() {
        return recyclingBoostPercentagePoints;
    }

    public void setRecyclingBoostPercentagePoints(double recyclingBoostPercentagePoints) {
        this.recyclingBoostPercentagePoints = Math.max(0.0, Math.min(100.0, recyclingBoostPercentagePoints));
    }
}

package com.ecotrack.service;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.SimulationResult;
import com.ecotrack.model.SimulationScenario;

/**
 * Evaluates hypothetical "What-If" lifestyle scenarios on a deep copy of ActivityData
 * without mutating the user's active calculation unless explicitly applied.
 */
public class SimulationService {

    private final CarbonCalculator calculator;

    public SimulationService(CarbonCalculator calculator) {
        this.calculator = calculator;
    }

    public SimulationResult simulate(ActivityData originalData, SimulationScenario scenario) {
        if (originalData == null || scenario == null) {
            throw new IllegalArgumentException("Original data and scenario must not be null.");
        }

        CarbonResult originalResult = calculator.calculate(originalData);
        ActivityData simData = originalData.copy();

        if (simData.isTransportEntered()) {
            double remainingFraction = 1.0 - (scenario.getTravelDistanceReductionPct() / 100.0);
            simData.setDistancePerTrip(Math.max(0.0, simData.getDistancePerTrip() * remainingFraction));
            if (scenario.getOverrideTransportMode() != null) {
                simData.setTransportMode(scenario.getOverrideTransportMode());
                if (scenario.getOverrideTransportMode() == ActivityData.TransportMode.PETROL_CAR
                        || scenario.getOverrideTransportMode() == ActivityData.TransportMode.DIESEL_CAR
                        || scenario.getOverrideTransportMode() == ActivityData.TransportMode.MOTORCYCLE) {
                    simData.setFuelEfficiencyKmPerLitre(
                            com.ecotrack.calculation.TransportCalculator
                                    .getDefaultEfficiencyOrConsumption(scenario.getOverrideTransportMode()));
                }
            }
        }

        if (simData.isElectricityEntered()) {
            double remainingFraction = 1.0 - (scenario.getElectricityReductionPct() / 100.0);
            simData.setMonthlyElectricityKwh(Math.max(0.0, simData.getMonthlyElectricityKwh() * remainingFraction));
        }

        if (simData.isFoodEntered() && scenario.getOverrideDietType() != null) {
            simData.setDietType(scenario.getOverrideDietType());
            simData.setUseCustomMealFrequency(false);
        }

        if (simData.isWasteEntered() && scenario.getRecyclingBoostPercentagePoints() > 0.0) {
            for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
                if (stream == ActivityData.WasteStream.GENERAL) {
                    continue;
                }
                double currentPct = simData.getRecyclingPercentages().getOrDefault(stream, 0.0);
                double boosted = Math.min(100.0, currentPct + scenario.getRecyclingBoostPercentagePoints());
                simData.getRecyclingPercentages().put(stream, boosted);
            }
        }

        CarbonResult simulatedResult = calculator.calculate(simData);
        return new SimulationResult(originalResult, simulatedResult, simData);
    }
}

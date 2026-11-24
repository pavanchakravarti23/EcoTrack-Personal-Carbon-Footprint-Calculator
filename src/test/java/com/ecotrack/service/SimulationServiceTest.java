package com.ecotrack.service;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.SimulationResult;
import com.ecotrack.model.SimulationScenario;
import org.junit.Test;

import static org.junit.Assert.*;

public class SimulationServiceTest {

    @Test
    public void simulationComputesSavingsWithoutMutatingOriginalActivityData() {
        CarbonCalculator calc = new CarbonCalculator();
        SimulationService simService = new SimulationService(calc);

        ActivityData original = new ActivityData();
        original.setTransportEntered(true);
        original.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
        original.setDistancePerTrip(20.0);
        original.setFuelEfficiencyKmPerLitre(15.0);
        original.setElectricityEntered(true);
        original.setMonthlyElectricityKwh(300.0);
        original.setElectricityEmissionFactor(0.50);

        SimulationScenario scenario = new SimulationScenario();
        scenario.setTravelDistanceReductionPct(50.0);
        scenario.setElectricityReductionPct(20.0);

        SimulationResult res = simService.simulate(original, scenario);

        // Original remains unchanged
        assertEquals(20.0, original.getDistancePerTrip(), 0.0001);
        assertEquals(300.0, original.getMonthlyElectricityKwh(), 0.0001);

        // Simulated reflects reductions
        assertTrue(res.getDifferenceKgCo2e() > 0.0);
        assertTrue(res.getPercentageReduction() > 0.0);
        assertEquals(10.0, res.getSimulatedActivityData().getDistancePerTrip(), 0.0001);
        assertEquals(240.0, res.getSimulatedActivityData().getMonthlyElectricityKwh(), 0.0001);
    }
}

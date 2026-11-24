package com.ecotrack.service;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.model.Recommendation;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RecommendationServiceTest {

    @Test
    public void prioritizesHighestEmissionCategoryWithRuleBasedGuidance() {
        CarbonCalculator calc = new CarbonCalculator();
        RecommendationService service = new RecommendationService(calc.getWasteCalculator());

        ActivityData data = new ActivityData();
        data.setTransportEntered(true);
        data.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
        data.setDistancePerTrip(30.0);
        data.setTravelFrequency(ActivityData.TravelFrequency.DAILY);
        data.setFuelEfficiencyKmPerLitre(12.0);
        data.setVehicleOccupancy(1);

        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(80.0);
        data.setElectricityEmissionFactor(0.45);

        CarbonResult result = calc.calculate(data);
        List<Recommendation> recs = service.generateRecommendations(data, result);

        assertFalse(recs.isEmpty());
        assertEquals(EmissionCategory.TRANSPORT, recs.get(0).getCategory());
        assertEquals(Recommendation.Priority.HIGH, recs.get(0).getPriority());
    }
}

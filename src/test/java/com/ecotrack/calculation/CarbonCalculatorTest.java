package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import org.junit.Test;

import static org.junit.Assert.*;

public class CarbonCalculatorTest {

    @Test
    public void emptySessionHasZeroTotalAndFalseHasDataFlag() {
        CarbonCalculator calc = new CarbonCalculator();
        ActivityData data = new ActivityData();
        CarbonResult res = calc.calculate(data);

        assertFalse(res.isHasAnyData());
        assertEquals(0.0, res.getTotalPeriodEmissionsKg(), 0.0001);
        assertNull(res.getHighestCategory());
    }

    @Test
    public void aggregatesAllFourCategoriesAndIdentifiesHighestContributor() {
        CarbonCalculator calc = new CarbonCalculator();
        ActivityData data = new ActivityData();

        data.setTransportEntered(true);
        data.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
        data.setDistancePerTrip(20.0);
        data.setTravelFrequency(ActivityData.TravelFrequency.DAILY);
        data.setFuelEfficiencyKmPerLitre(15.0);

        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(400.0);
        data.setElectricityEmissionFactor(0.82); // 328 kg CO2e/month -> highest

        data.setFoodEntered(true);
        data.setDietType(ActivityData.DietType.MIXED); // 168 kg CO2e/month

        data.setWasteEntered(true);
        data.setWasteEntry(ActivityData.WasteStream.GENERAL, 4.0, 0.0);

        CarbonResult monthly = calc.calculate(data);
        assertTrue(monthly.isHasAnyData());
        assertEquals(EmissionCategory.ELECTRICITY, monthly.getHighestCategory());

        double sum = monthly.getEmission(EmissionCategory.TRANSPORT)
                + monthly.getEmission(EmissionCategory.ELECTRICITY)
                + monthly.getEmission(EmissionCategory.FOOD)
                + monthly.getEmission(EmissionCategory.WASTE);
        assertEquals(sum, monthly.getTotalPeriodEmissionsKg(), 0.001);
        assertEquals(monthly.getMonthlyEquivalentKg() * 12.0, monthly.getAnnualProjectionKg(), 0.001);

        // Period scaling check
        data.setCalculationPeriod(ActivityData.CalculationPeriod.ANNUAL);
        CarbonResult annual = calc.calculate(data);
        assertEquals(monthly.getMonthlyEquivalentKg() * 12.0, annual.getTotalPeriodEmissionsKg(), 0.01);
    }
}

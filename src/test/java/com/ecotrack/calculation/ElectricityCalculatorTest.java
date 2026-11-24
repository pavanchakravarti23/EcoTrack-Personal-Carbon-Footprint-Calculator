package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElectricityCalculatorTest {

    @Test
    public void calculatesMonthlyAndAnnualProjectionCorrectly() {
        ElectricityCalculator calc = new ElectricityCalculator();
        ActivityData data = new ActivityData();
        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(320.0);
        data.setElectricityEmissionFactor(0.45);

        assertEquals(144.0, calc.calculateMonthlyEmissionsKg(data), 0.001);
        assertEquals(1728.0, calc.calculateAnnualProjectionKg(data), 0.001);
    }

    @Test
    public void zeroUsageReturnsZero() {
        ElectricityCalculator calc = new ElectricityCalculator();
        ActivityData data = new ActivityData();
        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(0.0);
        data.setElectricityEmissionFactor(0.82);

        assertEquals(0.0, calc.calculateMonthlyEmissionsKg(data), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeKwh() {
        ElectricityCalculator calc = new ElectricityCalculator();
        ActivityData data = new ActivityData();
        data.setElectricityEntered(true);
        data.setMonthlyElectricityKwh(-10.0);
        calc.calculateMonthlyEmissionsKg(data);
    }
}

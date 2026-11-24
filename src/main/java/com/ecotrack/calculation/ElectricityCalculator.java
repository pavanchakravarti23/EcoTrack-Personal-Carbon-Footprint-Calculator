package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;

/**
 * Calculates household electricity emissions from monthly kWh consumption
 * and a user-selected or user-entered grid emission factor (kg CO2e/kWh).
 */
public class ElectricityCalculator {

    public double calculateMonthlyEmissionsKg(ActivityData data) {
        if (data == null || !data.isElectricityEntered()) {
            return 0.0;
        }
        double kwh = data.getMonthlyElectricityKwh();
        double factor = data.getElectricityEmissionFactor();
        if (kwh < 0.0 || factor < 0.0) {
            throw new IllegalArgumentException("Electricity kWh and emission factor must be non-negative.");
        }
        return kwh * factor;
    }

    public double calculateAnnualProjectionKg(ActivityData data) {
        return calculateMonthlyEmissionsKg(data) * EmissionFactors.MONTHS_PER_YEAR;
    }

    public double calculateForPeriodKg(ActivityData data) {
        return calculateMonthlyEmissionsKg(data) * data.getCalculationPeriod().getMultiplierFromMonthly();
    }
}

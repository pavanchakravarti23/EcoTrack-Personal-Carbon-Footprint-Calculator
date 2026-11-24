package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;

/**
 * Estimates dietary greenhouse gas emissions using either representative daily
 * dietary baselines or user-entered weekly meal frequencies.
 *
 * All outputs are explicitly documented as dietary approximations.
 */
public class FoodCalculator {

    private final EmissionFactors factors;

    public FoodCalculator(EmissionFactors factors) {
        this.factors = factors;
    }

    public double calculateMonthlyEmissionsKg(ActivityData data) {
        if (data == null || !data.isFoodEntered()) {
            return 0.0;
        }

        if (!data.isUseCustomMealFrequency()) {
            double dailyBaseline = factors.getDietDailyBaseline(data.getDietType());
            return dailyBaseline * EmissionFactors.DAYS_PER_MONTH;
        }

        int redMeat = Math.max(0, data.getRedMeatMealsPerWeek());
        int dairyPoultry = Math.max(0, data.getDairyPoultryMealsPerWeek());
        int plant = Math.max(0, data.getPlantMealsPerWeek());

        double weeklyKg = (redMeat * factors.getValue(EmissionFactors.FOOD_MEAL_RED_MEAT))
                + (dairyPoultry * factors.getValue(EmissionFactors.FOOD_MEAL_DAIRY_POULTRY))
                + (plant * factors.getValue(EmissionFactors.FOOD_MEAL_PLANT));

        return weeklyKg * EmissionFactors.WEEKS_PER_MONTH;
    }

    public double calculateForPeriodKg(ActivityData data) {
        return calculateMonthlyEmissionsKg(data) * data.getCalculationPeriod().getMultiplierFromMonthly();
    }
}

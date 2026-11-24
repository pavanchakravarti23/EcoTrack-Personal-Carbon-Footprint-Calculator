package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import org.junit.Test;

import static org.junit.Assert.*;

public class FoodCalculatorTest {

    @Test
    public void dietBaselinesScaleCleanlyByMonthlyDays() {
        EmissionFactors factors = new EmissionFactors();
        FoodCalculator calc = new FoodCalculator(factors);
        ActivityData data = new ActivityData();
        data.setFoodEntered(true);
        data.setUseCustomMealFrequency(false);

        data.setDietType(ActivityData.DietType.PLANT_BASED);
        double plant = calc.calculateMonthlyEmissionsKg(data);
        assertEquals(2.50 * 30.0, plant, 0.001);

        data.setDietType(ActivityData.DietType.MEAT_HEAVY);
        double meat = calc.calculateMonthlyEmissionsKg(data);
        assertEquals(8.20 * 30.0, meat, 0.001);
        assertTrue(meat > plant);
    }

    @Test
    public void customMealFrequencyCalculatesFromWeeklyServings() {
        EmissionFactors factors = new EmissionFactors();
        FoodCalculator calc = new FoodCalculator(factors);
        ActivityData data = new ActivityData();
        data.setFoodEntered(true);
        data.setUseCustomMealFrequency(true);
        data.setRedMeatMealsPerWeek(2);
        data.setDairyPoultryMealsPerWeek(5);
        data.setPlantMealsPerWeek(14);

        double weekly = 2 * 3.20 + 5 * 1.40 + 14 * 0.65;
        double expectedMonthly = weekly * EmissionFactors.WEEKS_PER_MONTH;
        assertEquals(expectedMonthly, calc.calculateMonthlyEmissionsKg(data), 0.001);
    }
}

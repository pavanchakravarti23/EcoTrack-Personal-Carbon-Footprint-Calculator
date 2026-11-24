package com.ecotrack.service;

import com.ecotrack.calculation.WasteCalculator;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.model.Recommendation;
import com.ecotrack.utils.NumberFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic rule-based recommendation engine grounded strictly in the user's
 * entered activity data and calculated category shares.
 * (No artificial intelligence claims; transparent threshold and ranking rules.)
 */
public class RecommendationService {

    private final WasteCalculator wasteCalculator;

    public RecommendationService(WasteCalculator wasteCalculator) {
        this.wasteCalculator = wasteCalculator;
    }

    public List<Recommendation> generateRecommendations(ActivityData data, CarbonResult result) {
        List<Recommendation> list = new ArrayList<>();
        if (data == null || result == null || !result.isHasAnyData() || result.getTotalPeriodEmissionsKg() <= 0.0) {
            return list;
        }

        EmissionCategory highest = result.getHighestCategory();
        double total = result.getTotalPeriodEmissionsKg();

        // 1. Transportation rules
        double transportKg = result.getEmission(EmissionCategory.TRANSPORT);
        if (data.isTransportEntered() && transportKg > 0.0) {
            double pct = result.getPercentage(EmissionCategory.TRANSPORT);
            Recommendation.Priority prio = (highest == EmissionCategory.TRANSPORT || pct >= 35.0)
                    ? Recommendation.Priority.HIGH
                    : Recommendation.Priority.MEDIUM;

            ActivityData.TransportMode mode = data.getTransportMode();
            if (mode == ActivityData.TransportMode.PETROL_CAR || mode == ActivityData.TransportMode.DIESEL_CAR) {
                if (data.getVehicleOccupancy() <= 1) {
                    list.add(new Recommendation(
                            EmissionCategory.TRANSPORT,
                            prio,
                            "Shift Solo Car Commutes to Carpooling or Public Transit",
                            "Carpooling with 1 co-passenger halves per-person tailpipe emissions; switching selected trips to bus or rail cuts per-km impact significantly.",
                            "Transportation accounts for " + NumberFormatter.formatPercent(pct)
                                    + " (" + NumberFormatter.formatKg(transportKg) + ") using a single-occupant " + mode.getDisplayName() + ".",
                            "Up to 45-70% transport reduction on shifted trips"));
                } else {
                    list.add(new Recommendation(
                            EmissionCategory.TRANSPORT,
                            prio,
                            "Replace Short Vehicle Trips with Rail, Bus, or Active Travel",
                            "Consolidating errands and shifting 2 trips/week to metro, bus, cycling, or walking reduces fuel combustion.",
                            "Transportation contributes " + NumberFormatter.formatPercent(pct) + " of your current footprint.",
                            "15-30% transport reduction"));
                }
            } else if (mode == ActivityData.TransportMode.MOTORCYCLE) {
                list.add(new Recommendation(
                        EmissionCategory.TRANSPORT,
                        prio,
                        "Combine Daily Two-Wheeler Trips & Maintain Engine Tuning",
                        "Maintain tyre pressure and regular servicing, and substitute trips under 3 km with walking or cycling.",
                        "Motorcycle travel currently generates " + NumberFormatter.formatKg(transportKg) + " (" + NumberFormatter.formatPercent(pct) + ").",
                        "10-25% transport reduction"));
            } else if (mode == ActivityData.TransportMode.ELECTRIC_VEHICLE) {
                list.add(new Recommendation(
                        EmissionCategory.TRANSPORT,
                        prio,
                        "Charge Electric Vehicle During Off-Peak or Renewable Hours",
                        "Because EV operational footprint tracks your local grid factor ("
                                + NumberFormatter.formatFactor(data.getElectricityEmissionFactor()) + " kg CO2e/kWh), pairing charging with rooftop solar lowers emissions further.",
                        "EV charging represents " + NumberFormatter.formatPercent(pct) + " of your current total.",
                        "Depends on cleaner electricity sourcing"));
            } else {
                list.add(new Recommendation(
                        EmissionCategory.TRANSPORT,
                        Recommendation.Priority.LOW,
                        "Sustain Low-Emission Public Transit Habits",
                        "Continue prioritizing bus/rail transit and use cycling or walking for short last-mile legs.",
                        "Public transit accounts for " + NumberFormatter.formatKg(transportKg) + " (" + NumberFormatter.formatPercent(pct) + ").",
                        "Maintains low per-km footprint"));
            }
        }

        // 2. Electricity rules
        double elecKg = result.getEmission(EmissionCategory.ELECTRICITY);
        if (data.isElectricityEntered() && elecKg > 0.0) {
            double pct = result.getPercentage(EmissionCategory.ELECTRICITY);
            Recommendation.Priority prio = (highest == EmissionCategory.ELECTRICITY || pct >= 35.0)
                    ? Recommendation.Priority.HIGH
                    : Recommendation.Priority.MEDIUM;

            if (data.getMonthlyElectricityKwh() >= 250.0) {
                list.add(new Recommendation(
                        EmissionCategory.ELECTRICITY,
                        prio,
                        "Reduce High-Load Cooling, Heating & Standby Power",
                        "Set air conditioning 1-2 C higher, switch off appliances at the wall to remove standby draw, and replace remaining legacy bulbs with LEDs.",
                        "Electricity usage (" + NumberFormatter.formatNumber(data.getMonthlyElectricityKwh())
                                + " kWh/month) contributes " + NumberFormatter.formatPercent(pct)
                                + " (" + NumberFormatter.formatKg(elecKg) + ").",
                        "10-25% electricity savings"));
            } else {
                list.add(new Recommendation(
                        EmissionCategory.ELECTRICITY,
                        prio,
                        "Optimize Appliance Efficiency & Daylight Utilization",
                        "Use star-rated energy-efficient appliances, run washing machines on cold cycles, and maximize natural ventilation.",
                        "Electricity accounts for " + NumberFormatter.formatPercent(pct) + " (" + NumberFormatter.formatKg(elecKg) + ").",
                        "5-15% electricity savings"));
            }
        }

        // 3. Food rules
        double foodKg = result.getEmission(EmissionCategory.FOOD);
        if (data.isFoodEntered() && foodKg > 0.0) {
            double pct = result.getPercentage(EmissionCategory.FOOD);
            Recommendation.Priority prio = (highest == EmissionCategory.FOOD || pct >= 35.0)
                    ? Recommendation.Priority.HIGH
                    : Recommendation.Priority.MEDIUM;

            if (data.getDietType() == ActivityData.DietType.MEAT_HEAVY || data.getDietType() == ActivityData.DietType.MIXED
                    || (data.isUseCustomMealFrequency() && data.getRedMeatMealsPerWeek() > 2)) {
                list.add(new Recommendation(
                        EmissionCategory.FOOD,
                        prio,
                        "Introduce More Plant-Based Meals Each Week",
                        "Swapping 2-4 meat-centered meals per week for legumes, grains, and seasonal vegetables meaningfully lowers estimated dietary emissions.",
                        "Food habits contribute " + NumberFormatter.formatPercent(pct)
                                + " (" + NumberFormatter.formatKg(foodKg) + ") under illustrative LCA estimates.",
                        "15-35% dietary reduction"));
            } else {
                list.add(new Recommendation(
                        EmissionCategory.FOOD,
                        Recommendation.Priority.LOW,
                        "Minimize Household Food Spoilage & Plate Waste",
                        "Plan weekly grocery quantities carefully and store perishables properly so low-carbon plant/vegetarian meals aren't wasted.",
                        "Your dietary footprint is " + NumberFormatter.formatKg(foodKg) + " (" + NumberFormatter.formatPercent(pct) + ").",
                        "5-10% food-chain savings"));
            }
        }

        // 4. Waste rules
        double wasteKg = result.getEmission(EmissionCategory.WASTE);
        if (data.isWasteEntered() && wasteKg > 0.0) {
            double pct = result.getPercentage(EmissionCategory.WASTE);
            double diversionPct = wasteCalculator.calculateOverallDiversionPercentage(data);
            Recommendation.Priority prio = (highest == EmissionCategory.WASTE || diversionPct < 30.0)
                    ? Recommendation.Priority.HIGH
                    : Recommendation.Priority.MEDIUM;

            if (diversionPct < 50.0) {
                list.add(new Recommendation(
                        EmissionCategory.WASTE,
                        prio,
                        "Increase Source Segregation, Composting & Recycling",
                        "Current overall waste diversion is " + NumberFormatter.formatPercent(diversionPct)
                                + ". Composting organic kitchen waste and separating clean paper, plastic, metal, and glass avoids methane-intensive disposal.",
                        "Waste currently produces " + NumberFormatter.formatKg(wasteKg)
                                + " (" + NumberFormatter.formatPercent(pct) + " of total).",
                        "25-55% net waste reduction"));
            } else {
                list.add(new Recommendation(
                        EmissionCategory.WASTE,
                        Recommendation.Priority.LOW,
                        "Focus on Source Reduction & Reusable Packaging",
                        "With " + NumberFormatter.formatPercent(diversionPct)
                                + " already diverted, the next step is cutting single-use packaging before it enters the waste stream.",
                        "Waste accounts for " + NumberFormatter.formatPercent(pct) + " (" + NumberFormatter.formatKg(wasteKg) + ").",
                        "10-20% material reduction"));
            }
        }

        // Sort HIGH -> MEDIUM -> LOW
        list.sort((a, b) -> Integer.compare(a.getPriority().ordinal(), b.getPriority().ordinal()));
        return list;
    }
}

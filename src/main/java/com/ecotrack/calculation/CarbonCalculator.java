package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized calculation coordinator aggregating emissions across Transport,
 * Electricity, Food, and Waste in double precision with consistent period scaling.
 */
public class CarbonCalculator {

    private final EmissionFactors emissionFactors;
    private final TransportCalculator transportCalculator;
    private final ElectricityCalculator electricityCalculator;
    private final FoodCalculator foodCalculator;
    private final WasteCalculator wasteCalculator;

    public CarbonCalculator() {
        this(new EmissionFactors());
    }

    public CarbonCalculator(EmissionFactors emissionFactors) {
        this.emissionFactors = emissionFactors;
        this.transportCalculator = new TransportCalculator(emissionFactors);
        this.electricityCalculator = new ElectricityCalculator();
        this.foodCalculator = new FoodCalculator(emissionFactors);
        this.wasteCalculator = new WasteCalculator(emissionFactors);
    }

    public EmissionFactors getEmissionFactors() {
        return emissionFactors;
    }

    public TransportCalculator getTransportCalculator() {
        return transportCalculator;
    }

    public ElectricityCalculator getElectricityCalculator() {
        return electricityCalculator;
    }

    public FoodCalculator getFoodCalculator() {
        return foodCalculator;
    }

    public WasteCalculator getWasteCalculator() {
        return wasteCalculator;
    }

    public CarbonResult calculate(ActivityData data) {
        if (data == null) {
            throw new IllegalArgumentException("ActivityData must not be null.");
        }

        double monthlyTransport = transportCalculator.calculateMonthlyEmissionsKg(data);
        double monthlyElectricity = electricityCalculator.calculateMonthlyEmissionsKg(data);
        double monthlyFood = foodCalculator.calculateMonthlyEmissionsKg(data);
        double monthlyWaste = wasteCalculator.calculateMonthlyEmissionsKg(data);

        double monthlyTotal = monthlyTransport + monthlyElectricity + monthlyFood + monthlyWaste;
        double periodScale = data.getCalculationPeriod().getMultiplierFromMonthly();

        Map<EmissionCategory, Double> periodMap = new EnumMap<>(EmissionCategory.class);
        periodMap.put(EmissionCategory.TRANSPORT, monthlyTransport * periodScale);
        periodMap.put(EmissionCategory.ELECTRICITY, monthlyElectricity * periodScale);
        periodMap.put(EmissionCategory.FOOD, monthlyFood * periodScale);
        periodMap.put(EmissionCategory.WASTE, monthlyWaste * periodScale);

        double periodTotal = monthlyTotal * periodScale;
        double annualProjection = monthlyTotal * EmissionFactors.MONTHS_PER_YEAR;

        EmissionCategory highest = null;
        double maxVal = 0.0;
        for (Map.Entry<EmissionCategory, Double> entry : periodMap.entrySet()) {
            if (entry.getValue() > maxVal) {
                maxVal = entry.getValue();
                highest = entry.getKey();
            }
        }

        boolean hasAnyData = data.hasAnyDataEntered();
        String summaryNote;
        if (!hasAnyData) {
            summaryNote = "No activity inputs entered yet. Complete any calculator module to generate an estimate.";
        } else if (periodTotal <= 0.0) {
            summaryNote = "Current entered activities result in 0.00 kg CO2e direct operational emissions.";
        } else {
            summaryNote = String.format(
                    "Primary emission source: %s (%.1f%% of total %s footprint).",
                    highest.getDisplayName(),
                    (maxVal / periodTotal) * 100.0,
                    data.getCalculationPeriod().getDisplayName().toLowerCase());
        }

        return new CarbonResult(
                data.getCalculationPeriod(),
                periodMap,
                periodTotal,
                monthlyTotal,
                annualProjection,
                highest,
                hasAnyData,
                summaryNote);
    }
}

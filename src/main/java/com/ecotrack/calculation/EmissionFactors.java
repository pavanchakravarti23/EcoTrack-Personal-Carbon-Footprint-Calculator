package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import com.ecotrack.model.EmissionFactorInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized, configurable registry of emission factors with source, year,
 * geographical coverage, and methodology documentation.
 *
 * All default factors that represent generalized literature or cross-regional
 * reference values are explicitly marked as illustrative configurable assumptions
 * rather than claimed as official local measurements.
 */
public class EmissionFactors {

    public static final double DAYS_PER_MONTH = 30.0;
    public static final double WEEKS_PER_MONTH = 52.0 / 12.0;
    public static final double MONTHS_PER_YEAR = 12.0;

    // Factor registry keys
    public static final String TRANSPORT_PETROL_FUEL = "transport.petrol.fuel_per_litre";
    public static final String TRANSPORT_DIESEL_FUEL = "transport.diesel.fuel_per_litre";
    public static final String TRANSPORT_BUS_PKM = "transport.bus.passenger_km";
    public static final String TRANSPORT_TRAIN_PKM = "transport.train.passenger_km";
    public static final String TRANSPORT_ACTIVE_ZERO = "transport.active.zero";

    public static final String ELECTRICITY_PRESET_COAL = "electricity.preset.coal_heavy";
    public static final String ELECTRICITY_PRESET_MIXED = "electricity.preset.mixed";
    public static final String ELECTRICITY_PRESET_LOW_CARBON = "electricity.preset.low_carbon";

    public static final String FOOD_DIET_PLANT_BASED = "food.diet.plant_based";
    public static final String FOOD_DIET_VEGETARIAN = "food.diet.vegetarian";
    public static final String FOOD_DIET_MIXED = "food.diet.mixed";
    public static final String FOOD_DIET_MEAT_HEAVY = "food.diet.meat_heavy";
    public static final String FOOD_MEAL_RED_MEAT = "food.meal.red_meat";
    public static final String FOOD_MEAL_DAIRY_POULTRY = "food.meal.dairy_poultry";
    public static final String FOOD_MEAL_PLANT = "food.meal.plant";

    public static final String WASTE_ORGANIC_DISPOSAL = "waste.organic.disposal";
    public static final String WASTE_ORGANIC_COMPOST = "waste.organic.compost";
    public static final String WASTE_PAPER_DISPOSAL = "waste.paper.disposal";
    public static final String WASTE_PAPER_RECYCLE = "waste.paper.recycle";
    public static final String WASTE_PLASTIC_DISPOSAL = "waste.plastic.disposal";
    public static final String WASTE_PLASTIC_RECYCLE = "waste.plastic.recycle";
    public static final String WASTE_METAL_DISPOSAL = "waste.metal.disposal";
    public static final String WASTE_METAL_RECYCLE = "waste.metal.recycle";
    public static final String WASTE_GLASS_DISPOSAL = "waste.glass.disposal";
    public static final String WASTE_GLASS_RECYCLE = "waste.glass.recycle";
    public static final String WASTE_GENERAL_DISPOSAL = "waste.general.disposal";

    private final Map<String, EmissionFactorInfo> factors = new LinkedHashMap<>();

    public EmissionFactors() {
        resetToDefaults();
    }

    public final void resetToDefaults() {
        factors.clear();

        // Transport - Fuel Combustion (IPCC 2006 Guidelines Vol. 2, Ch. 3 mobile combustion basis; configurable)
        register(new EmissionFactorInfo(
                TRANSPORT_PETROL_FUEL,
                "Petrol (Gasoline) Fuel Combustion",
                2.31,
                "kg CO2e / litre",
                "Tailpipe Fuel Combustion (Volume-based)",
                "IPCC 2006 Guidelines (Vol. 2, Mobile Combustion) representative density & carbon factor",
                "2006 (2019 Refinement context)",
                "Global generic fuel stoichiometry (configurable)",
                true));

        register(new EmissionFactorInfo(
                TRANSPORT_DIESEL_FUEL,
                "Diesel Fuel Combustion",
                2.68,
                "kg CO2e / litre",
                "Tailpipe Fuel Combustion (Volume-based)",
                "IPCC 2006 Guidelines (Vol. 2, Mobile Combustion) representative density & carbon factor",
                "2006 (2019 Refinement context)",
                "Global generic fuel stoichiometry (configurable)",
                true));

        register(new EmissionFactorInfo(
                TRANSPORT_BUS_PKM,
                "Public Bus Transit",
                0.089,
                "kg CO2e / passenger-km",
                "Passenger-km Occupancy-Averaged Transit",
                "Illustrative urban bus passenger-km reference assumption",
                "2024",
                "Illustrative global urban transit benchmark (configurable)",
                true));

        register(new EmissionFactorInfo(
                TRANSPORT_TRAIN_PKM,
                "Rail / Metro Transit",
                0.041,
                "kg CO2e / passenger-km",
                "Passenger-km Occupancy-Averaged Transit",
                "Illustrative commuter rail/metro passenger-km reference assumption",
                "2024",
                "Illustrative mixed-traction rail benchmark (configurable)",
                true));

        register(new EmissionFactorInfo(
                TRANSPORT_ACTIVE_ZERO,
                "Bicycle & Walking (Active Mobility)",
                0.0,
                "kg CO2e / km",
                "Zero Direct Operational Tailpipe Emissions",
                "Direct operational combustion boundary",
                "N/A",
                "Universal",
                false));

        // Electricity - Explicitly labelled configurable presets (never misrepresented as official local grid factors)
        register(new EmissionFactorInfo(
                ELECTRICITY_PRESET_COAL,
                "Coal-Heavy Grid Preset (Illustrative)",
                0.82,
                "kg CO2e / kWh",
                "Grid Generation Intensity",
                "Illustrative configurable preset (NOT an official national/Indian grid factor; verify local utility/CEA/EPA data)",
                "Illustrative Reference",
                "Thermal/coal-dominated grid scenario (configurable)",
                true));

        register(new EmissionFactorInfo(
                ELECTRICITY_PRESET_MIXED,
                "Mixed Generation Grid Preset (Illustrative)",
                0.45,
                "kg CO2e / kWh",
                "Grid Generation Intensity",
                "Illustrative configurable preset (balanced fossil and renewable generation)",
                "Illustrative Reference",
                "Mixed-generation grid scenario (configurable)",
                true));

        register(new EmissionFactorInfo(
                ELECTRICITY_PRESET_LOW_CARBON,
                "Low-Carbon Grid Preset (Illustrative)",
                0.15,
                "kg CO2e / kWh",
                "Grid Generation Intensity",
                "Illustrative configurable preset (high hydro/nuclear/solar/wind share)",
                "Illustrative Reference",
                "Renewable/low-carbon grid scenario (configurable)",
                true));

        // Food - Representative dietary LCA approximations
        register(new EmissionFactorInfo(
                FOOD_DIET_PLANT_BASED,
                "Plant-Based Diet Daily Baseline",
                2.50,
                "kg CO2e / day",
                "Representative Dietary LCA Approximation",
                "Illustrative dietary LCA approximation (Poore & Nemecek / IPCC AR6 dietary range context)",
                "Illustrative Reference",
                "Generalized per-capita dietary pattern",
                true));

        register(new EmissionFactorInfo(
                FOOD_DIET_VEGETARIAN,
                "Vegetarian Diet Daily Baseline",
                3.80,
                "kg CO2e / day",
                "Representative Dietary LCA Approximation",
                "Illustrative dietary LCA approximation (includes dairy/eggs)",
                "Illustrative Reference",
                "Generalized per-capita dietary pattern",
                true));

        register(new EmissionFactorInfo(
                FOOD_DIET_MIXED,
                "Mixed Diet Daily Baseline",
                5.60,
                "kg CO2e / day",
                "Representative Dietary LCA Approximation",
                "Illustrative dietary LCA approximation (moderate animal-product intake)",
                "Illustrative Reference",
                "Generalized per-capita dietary pattern",
                true));

        register(new EmissionFactorInfo(
                FOOD_DIET_MEAT_HEAVY,
                "Meat-Heavy Diet Daily Baseline",
                8.20,
                "kg CO2e / day",
                "Representative Dietary LCA Approximation",
                "Illustrative dietary LCA approximation (frequent ruminant/meat intake)",
                "Illustrative Reference",
                "Generalized per-capita dietary pattern",
                true));

        register(new EmissionFactorInfo(
                FOOD_MEAL_RED_MEAT,
                "High-Impact Meat Meal",
                3.20,
                "kg CO2e / meal",
                "Meal-Frequency LCA Approximation",
                "Illustrative meal-level dietary approximation",
                "Illustrative Reference",
                "Generalized serving approximation",
                true));

        register(new EmissionFactorInfo(
                FOOD_MEAL_DAIRY_POULTRY,
                "Poultry / Fish / Dairy-Rich Meal",
                1.40,
                "kg CO2e / meal",
                "Meal-Frequency LCA Approximation",
                "Illustrative meal-level dietary approximation",
                "Illustrative Reference",
                "Generalized serving approximation",
                true));

        register(new EmissionFactorInfo(
                FOOD_MEAL_PLANT,
                "Plant-Based Meal",
                0.65,
                "kg CO2e / meal",
                "Meal-Frequency LCA Approximation",
                "Illustrative meal-level dietary approximation",
                "Illustrative Reference",
                "Generalized serving approximation",
                true));

        // Waste - Partitioned net disposal vs diversion/recycling factors (avoids double counting)
        register(new EmissionFactorInfo(
                WASTE_ORGANIC_DISPOSAL,
                "Organic Waste (Landfill Disposal)",
                0.58,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Methane/CO2e Factor",
                "IPCC Waste Model concept / illustrative landfill organic degradation factor",
                "Illustrative Reference",
                "Generalized municipal landfill assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_ORGANIC_COMPOST,
                "Organic Waste (Composting Process)",
                0.05,
                "kg CO2e / kg diverted",
                "Diverted Stream Processing Factor",
                "Illustrative aerobic composting process emission factor",
                "Illustrative Reference",
                "Generalized composting assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_PAPER_DISPOSAL,
                "Paper & Cardboard (Landfill Disposal)",
                1.04,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Methane/CO2e Factor",
                "Illustrative cellulosic landfill degradation factor",
                "Illustrative Reference",
                "Generalized municipal landfill assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_PAPER_RECYCLE,
                "Paper & Cardboard (Recycling Process)",
                0.12,
                "kg CO2e / kg diverted",
                "Diverted Stream Processing Factor",
                "Illustrative sorting & pulping process factor (no double-counting with disposal)",
                "Illustrative Reference",
                "Generalized MRF/reprocessing assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_PLASTIC_DISPOSAL,
                "Plastic (Residual Disposal)",
                0.35,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Factor",
                "Illustrative mixed residual landfill/thermal disposal assumption",
                "Illustrative Reference",
                "Generalized municipal disposal assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_PLASTIC_RECYCLE,
                "Plastic (Recycling Process)",
                0.18,
                "kg CO2e / kg diverted",
                "Diverted Stream Processing Factor",
                "Illustrative sorting & pelletizing process factor",
                "Illustrative Reference",
                "Generalized reprocessing assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_METAL_DISPOSAL,
                "Metal (Residual Disposal)",
                0.04,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Factor",
                "Illustrative inert landfill handling factor",
                "Illustrative Reference",
                "Generalized municipal landfill assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_METAL_RECYCLE,
                "Metal (Recycling Process)",
                0.15,
                "kg CO2e / kg diverted",
                "Diverted Stream Processing Factor",
                "Illustrative scrap collection/remelting process factor",
                "Illustrative Reference",
                "Generalized reprocessing assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_GLASS_DISPOSAL,
                "Glass (Residual Disposal)",
                0.03,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Factor",
                "Illustrative inert landfill handling factor",
                "Illustrative Reference",
                "Generalized municipal landfill assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_GLASS_RECYCLE,
                "Glass (Recycling Process)",
                0.08,
                "kg CO2e / kg diverted",
                "Diverted Stream Processing Factor",
                "Illustrative cullet sorting & processing factor",
                "Illustrative Reference",
                "Generalized reprocessing assumption",
                true));

        register(new EmissionFactorInfo(
                WASTE_GENERAL_DISPOSAL,
                "General Residual Waste (Disposal)",
                0.65,
                "kg CO2e / kg disposed",
                "Net Disposed-Waste Factor",
                "Illustrative mixed municipal residual waste factor",
                "Illustrative Reference",
                "Generalized municipal landfill assumption",
                true));
    }

    private void register(EmissionFactorInfo info) {
        factors.put(info.getKey(), info);
    }

    public EmissionFactorInfo getInfo(String key) {
        return factors.get(key);
    }

    public double getValue(String key) {
        EmissionFactorInfo info = factors.get(key);
        if (info == null) {
            throw new IllegalArgumentException("Unknown emission factor key: " + key);
        }
        return info.getValue();
    }

    public void setValue(String key, double newValue) {
        if (newValue < 0.0 || Double.isNaN(newValue) || Double.isInfinite(newValue)) {
            throw new IllegalArgumentException("Emission factor must be a non-negative finite number.");
        }
        EmissionFactorInfo info = factors.get(key);
        if (info == null) {
            throw new IllegalArgumentException("Unknown emission factor key: " + key);
        }
        info.setValue(newValue);
    }

    public Map<String, EmissionFactorInfo> getAllFactors() {
        return Collections.unmodifiableMap(factors);
    }

    public double getDisposalFactor(ActivityData.WasteStream stream) {
        return switch (stream) {
            case ORGANIC -> getValue(WASTE_ORGANIC_DISPOSAL);
            case PAPER -> getValue(WASTE_PAPER_DISPOSAL);
            case PLASTIC -> getValue(WASTE_PLASTIC_DISPOSAL);
            case METAL -> getValue(WASTE_METAL_DISPOSAL);
            case GLASS -> getValue(WASTE_GLASS_DISPOSAL);
            case GENERAL -> getValue(WASTE_GENERAL_DISPOSAL);
        };
    }

    public double getDiversionFactor(ActivityData.WasteStream stream) {
        return switch (stream) {
            case ORGANIC -> getValue(WASTE_ORGANIC_COMPOST);
            case PAPER -> getValue(WASTE_PAPER_RECYCLE);
            case PLASTIC -> getValue(WASTE_PLASTIC_RECYCLE);
            case METAL -> getValue(WASTE_METAL_RECYCLE);
            case GLASS -> getValue(WASTE_GLASS_RECYCLE);
            case GENERAL -> 0.0;
        };
    }

    public double getDietDailyBaseline(ActivityData.DietType dietType) {
        return switch (dietType) {
            case PLANT_BASED -> getValue(FOOD_DIET_PLANT_BASED);
            case VEGETARIAN -> getValue(FOOD_DIET_VEGETARIAN);
            case MIXED -> getValue(FOOD_DIET_MIXED);
            case MEAT_HEAVY -> getValue(FOOD_DIET_MEAT_HEAVY);
        };
    }
}

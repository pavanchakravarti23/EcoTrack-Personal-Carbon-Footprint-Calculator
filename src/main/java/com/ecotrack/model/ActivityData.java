package com.ecotrack.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stores all user-entered activity data for the active EcoTrack session in memory.
 */
public class ActivityData {

    public enum TransportMode {
        PETROL_CAR("Petrol Car", CalculationMethod.FUEL_COMBUSTION),
        DIESEL_CAR("Diesel Car", CalculationMethod.FUEL_COMBUSTION),
        MOTORCYCLE("Motorcycle", CalculationMethod.FUEL_COMBUSTION),
        BUS("Bus", CalculationMethod.PASSENGER_KM),
        TRAIN("Train", CalculationMethod.PASSENGER_KM),
        ELECTRIC_VEHICLE("Electric Vehicle", CalculationMethod.ELECTRIC_GRID),
        BICYCLE("Bicycle", CalculationMethod.ZERO_OPERATIONAL),
        WALKING("Walking", CalculationMethod.ZERO_OPERATIONAL);

        public enum CalculationMethod {
            FUEL_COMBUSTION,
            PASSENGER_KM,
            ELECTRIC_GRID,
            ZERO_OPERATIONAL
        }

        private final String displayName;
        private final CalculationMethod method;

        TransportMode(String displayName, CalculationMethod method) {
            this.displayName = displayName;
            this.method = method;
        }

        public String getDisplayName() {
            return displayName;
        }

        public CalculationMethod getMethod() {
            return method;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum DistanceUnit {
        KILOMETERS("km", 1.0),
        MILES("miles", 1.60934);

        private final String label;
        private final double toKmFactor;

        DistanceUnit(String label, double toKmFactor) {
            this.label = label;
            this.toKmFactor = toKmFactor;
        }

        public String getLabel() {
            return label;
        }

        public double toKilometers(double value) {
            return value * toKmFactor;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum TravelFrequency {
        DAILY("Daily (30 trips/month)", 30.0),
        WEEKDAYS("Weekdays (22 trips/month)", 22.0),
        WEEKLY("Weekly (4.33 trips/month)", 52.0 / 12.0),
        MONTHLY("Monthly (1 trip/month)", 1.0);

        private final String displayName;
        private final double tripsPerMonth;

        TravelFrequency(String displayName, double tripsPerMonth) {
            this.displayName = displayName;
            this.tripsPerMonth = tripsPerMonth;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getTripsPerMonth() {
            return tripsPerMonth;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum CalculationPeriod {
        WEEKLY("Weekly", 12.0 / 52.0),
        MONTHLY("Monthly", 1.0),
        ANNUAL("Annual", 12.0);

        private final String displayName;
        private final double monthlyMultiplier;

        CalculationPeriod(String displayName, double monthlyMultiplier) {
            this.displayName = displayName;
            this.monthlyMultiplier = monthlyMultiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        /** Multiplier converting a monthly value into this calculation period. */
        public double getMultiplierFromMonthly() {
            return monthlyMultiplier;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum DietType {
        PLANT_BASED("Plant-based diet", 2.50, 0, 2, 19),
        VEGETARIAN("Vegetarian", 3.80, 0, 7, 14),
        MIXED("Mixed diet", 5.60, 5, 7, 9),
        MEAT_HEAVY("Meat-heavy diet", 8.20, 11, 6, 4);

        private final String displayName;
        private final double representativeDailyKgCo2e;
        private final int defaultRedMeatMealsPerWeek;
        private final int defaultDairyOrPoultryMealsPerWeek;
        private final int defaultPlantMealsPerWeek;

        DietType(
                String displayName,
                double representativeDailyKgCo2e,
                int defaultRedMeatMealsPerWeek,
                int defaultDairyOrPoultryMealsPerWeek,
                int defaultPlantMealsPerWeek) {
            this.displayName = displayName;
            this.representativeDailyKgCo2e = representativeDailyKgCo2e;
            this.defaultRedMeatMealsPerWeek = defaultRedMeatMealsPerWeek;
            this.defaultDairyOrPoultryMealsPerWeek = defaultDairyOrPoultryMealsPerWeek;
            this.defaultPlantMealsPerWeek = defaultPlantMealsPerWeek;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getRepresentativeDailyKgCo2e() {
            return representativeDailyKgCo2e;
        }

        public int getDefaultRedMeatMealsPerWeek() {
            return defaultRedMeatMealsPerWeek;
        }

        public int getDefaultDairyOrPoultryMealsPerWeek() {
            return defaultDairyOrPoultryMealsPerWeek;
        }

        public int getDefaultPlantMealsPerWeek() {
            return defaultPlantMealsPerWeek;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum WasteStream {
        ORGANIC("Organic Waste"),
        PAPER("Paper & Cardboard"),
        PLASTIC("Plastic"),
        METAL("Metal"),
        GLASS("Glass"),
        GENERAL("General Residual Waste");

        private final String displayName;

        WasteStream(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Period & tracking flags
    private CalculationPeriod calculationPeriod = CalculationPeriod.MONTHLY;
    private boolean transportEntered = false;
    private boolean electricityEntered = false;
    private boolean foodEntered = false;
    private boolean wasteEntered = false;

    // Transportation fields
    private TransportMode transportMode = TransportMode.PETROL_CAR;
    private double distancePerTrip = 0.0;
    private DistanceUnit distanceUnit = DistanceUnit.KILOMETERS;
    private TravelFrequency travelFrequency = TravelFrequency.DAILY;
    private double fuelEfficiencyKmPerLitre = 15.0;
    private double evKwhPer100Km = 16.0;
    private int vehicleOccupancy = 1;

    // Electricity fields
    private double monthlyElectricityKwh = 0.0;
    private double electricityEmissionFactor = 0.45; // Illustrative mixed grid preset default
    private String electricityFactorSourceLabel = "Configurable Reference — Mixed Grid Preset (Illustrative)";

    // Food fields
    private DietType dietType = DietType.MIXED;
    private boolean useCustomMealFrequency = false;
    private int redMeatMealsPerWeek = DietType.MIXED.getDefaultRedMeatMealsPerWeek();
    private int dairyPoultryMealsPerWeek = DietType.MIXED.getDefaultDairyOrPoultryMealsPerWeek();
    private int plantMealsPerWeek = DietType.MIXED.getDefaultPlantMealsPerWeek();

    // Waste fields (weekly kg and recycling/composting percentage 0-100)
    private final Map<WasteStream, Double> weeklyWasteKg = new EnumMap<>(WasteStream.class);
    private final Map<WasteStream, Double> recyclingPercentages = new EnumMap<>(WasteStream.class);

    public ActivityData() {
        for (WasteStream stream : WasteStream.values()) {
            weeklyWasteKg.put(stream, 0.0);
            recyclingPercentages.put(stream, 0.0);
        }
    }

    public ActivityData copy() {
        ActivityData clone = new ActivityData();
        clone.calculationPeriod = this.calculationPeriod;
        clone.transportEntered = this.transportEntered;
        clone.electricityEntered = this.electricityEntered;
        clone.foodEntered = this.foodEntered;
        clone.wasteEntered = this.wasteEntered;

        clone.transportMode = this.transportMode;
        clone.distancePerTrip = this.distancePerTrip;
        clone.distanceUnit = this.distanceUnit;
        clone.travelFrequency = this.travelFrequency;
        clone.fuelEfficiencyKmPerLitre = this.fuelEfficiencyKmPerLitre;
        clone.evKwhPer100Km = this.evKwhPer100Km;
        clone.vehicleOccupancy = this.vehicleOccupancy;

        clone.monthlyElectricityKwh = this.monthlyElectricityKwh;
        clone.electricityEmissionFactor = this.electricityEmissionFactor;
        clone.electricityFactorSourceLabel = this.electricityFactorSourceLabel;

        clone.dietType = this.dietType;
        clone.useCustomMealFrequency = this.useCustomMealFrequency;
        clone.redMeatMealsPerWeek = this.redMeatMealsPerWeek;
        clone.dairyPoultryMealsPerWeek = this.dairyPoultryMealsPerWeek;
        clone.plantMealsPerWeek = this.plantMealsPerWeek;

        for (WasteStream stream : WasteStream.values()) {
            clone.weeklyWasteKg.put(stream, this.weeklyWasteKg.get(stream));
            clone.recyclingPercentages.put(stream, this.recyclingPercentages.get(stream));
        }
        return clone;
    }

    public void resetAll() {
        calculationPeriod = CalculationPeriod.MONTHLY;
        transportEntered = false;
        electricityEntered = false;
        foodEntered = false;
        wasteEntered = false;

        transportMode = TransportMode.PETROL_CAR;
        distancePerTrip = 0.0;
        distanceUnit = DistanceUnit.KILOMETERS;
        travelFrequency = TravelFrequency.DAILY;
        fuelEfficiencyKmPerLitre = 15.0;
        evKwhPer100Km = 16.0;
        vehicleOccupancy = 1;

        monthlyElectricityKwh = 0.0;
        electricityEmissionFactor = 0.45;
        electricityFactorSourceLabel = "Configurable Reference — Mixed Grid Preset (Illustrative)";

        dietType = DietType.MIXED;
        useCustomMealFrequency = false;
        redMeatMealsPerWeek = DietType.MIXED.getDefaultRedMeatMealsPerWeek();
        dairyPoultryMealsPerWeek = DietType.MIXED.getDefaultDairyOrPoultryMealsPerWeek();
        plantMealsPerWeek = DietType.MIXED.getDefaultPlantMealsPerWeek();

        for (WasteStream stream : WasteStream.values()) {
            weeklyWasteKg.put(stream, 0.0);
            recyclingPercentages.put(stream, 0.0);
        }
    }

    public boolean hasAnyDataEntered() {
        return transportEntered || electricityEntered || foodEntered || wasteEntered;
    }

    // Getters and setters
    public CalculationPeriod getCalculationPeriod() {
        return calculationPeriod;
    }

    public void setCalculationPeriod(CalculationPeriod calculationPeriod) {
        this.calculationPeriod = calculationPeriod;
    }

    public boolean isTransportEntered() {
        return transportEntered;
    }

    public void setTransportEntered(boolean transportEntered) {
        this.transportEntered = transportEntered;
    }

    public boolean isElectricityEntered() {
        return electricityEntered;
    }

    public void setElectricityEntered(boolean electricityEntered) {
        this.electricityEntered = electricityEntered;
    }

    public boolean isFoodEntered() {
        return foodEntered;
    }

    public void setFoodEntered(boolean foodEntered) {
        this.foodEntered = foodEntered;
    }

    public boolean isWasteEntered() {
        return wasteEntered;
    }

    public void setWasteEntered(boolean wasteEntered) {
        this.wasteEntered = wasteEntered;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }

    public double getDistancePerTrip() {
        return distancePerTrip;
    }

    public void setDistancePerTrip(double distancePerTrip) {
        this.distancePerTrip = distancePerTrip;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public TravelFrequency getTravelFrequency() {
        return travelFrequency;
    }

    public void setTravelFrequency(TravelFrequency travelFrequency) {
        this.travelFrequency = travelFrequency;
    }

    public double getFuelEfficiencyKmPerLitre() {
        return fuelEfficiencyKmPerLitre;
    }

    public void setFuelEfficiencyKmPerLitre(double fuelEfficiencyKmPerLitre) {
        this.fuelEfficiencyKmPerLitre = fuelEfficiencyKmPerLitre;
    }

    public double getEvKwhPer100Km() {
        return evKwhPer100Km;
    }

    public void setEvKwhPer100Km(double evKwhPer100Km) {
        this.evKwhPer100Km = evKwhPer100Km;
    }

    public int getVehicleOccupancy() {
        return vehicleOccupancy;
    }

    public void setVehicleOccupancy(int vehicleOccupancy) {
        this.vehicleOccupancy = vehicleOccupancy;
    }

    public double getMonthlyElectricityKwh() {
        return monthlyElectricityKwh;
    }

    public void setMonthlyElectricityKwh(double monthlyElectricityKwh) {
        this.monthlyElectricityKwh = monthlyElectricityKwh;
    }

    public double getElectricityEmissionFactor() {
        return electricityEmissionFactor;
    }

    public void setElectricityEmissionFactor(double electricityEmissionFactor) {
        this.electricityEmissionFactor = electricityEmissionFactor;
    }

    public String getElectricityFactorSourceLabel() {
        return electricityFactorSourceLabel;
    }

    public void setElectricityFactorSourceLabel(String electricityFactorSourceLabel) {
        this.electricityFactorSourceLabel = electricityFactorSourceLabel;
    }

    public DietType getDietType() {
        return dietType;
    }

    public void setDietType(DietType dietType) {
        this.dietType = dietType;
    }

    public boolean isUseCustomMealFrequency() {
        return useCustomMealFrequency;
    }

    public void setUseCustomMealFrequency(boolean useCustomMealFrequency) {
        this.useCustomMealFrequency = useCustomMealFrequency;
    }

    public int getRedMeatMealsPerWeek() {
        return redMeatMealsPerWeek;
    }

    public void setRedMeatMealsPerWeek(int redMeatMealsPerWeek) {
        this.redMeatMealsPerWeek = redMeatMealsPerWeek;
    }

    public int getDairyPoultryMealsPerWeek() {
        return dairyPoultryMealsPerWeek;
    }

    public void setDairyPoultryMealsPerWeek(int dairyPoultryMealsPerWeek) {
        this.dairyPoultryMealsPerWeek = dairyPoultryMealsPerWeek;
    }

    public int getPlantMealsPerWeek() {
        return plantMealsPerWeek;
    }

    public void setPlantMealsPerWeek(int plantMealsPerWeek) {
        this.plantMealsPerWeek = plantMealsPerWeek;
    }

    public Map<WasteStream, Double> getWeeklyWasteKg() {
        return weeklyWasteKg;
    }

    public Map<WasteStream, Double> getRecyclingPercentages() {
        return recyclingPercentages;
    }

    public void setWasteEntry(WasteStream stream, double kgPerWeek, double recyclingPct) {
        weeklyWasteKg.put(stream, kgPerWeek);
        recyclingPercentages.put(stream, recyclingPct);
    }
}

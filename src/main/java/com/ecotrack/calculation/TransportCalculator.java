package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;

/**
 * Calculates transportation emissions using distinct methodologies per transport mode:
 * - Fuel-based combustion (Petrol Car, Diesel Car, Motorcycle): distance -> litres -> fuel factor / occupancy
 * - Passenger-kilometre transit (Bus, Train): distance * passenger-km factor
 * - Grid-coupled Electric Vehicle: distance -> kWh -> electricity grid factor / occupancy
 * - Active zero-tailpipe modes (Bicycle, Walking): 0 kg CO2e
 */
public class TransportCalculator {

    private final EmissionFactors factors;

    public TransportCalculator(EmissionFactors factors) {
        this.factors = factors;
    }

    /**
     * Computes monthly equivalent distance in kilometres.
     */
    public double calculateMonthlyDistanceKm(ActivityData data) {
        if (data == null || data.getDistancePerTrip() <= 0.0) {
            return 0.0;
        }
        double singleTripKm = data.getDistanceUnit().toKilometers(data.getDistancePerTrip());
        return singleTripKm * data.getTravelFrequency().getTripsPerMonth();
    }

    /**
     * Computes monthly equivalent transportation emissions in kg CO2e.
     */
    public double calculateMonthlyEmissionsKg(ActivityData data) {
        if (data == null || !data.isTransportEntered()) {
            return 0.0;
        }
        double monthlyKm = calculateMonthlyDistanceKm(data);
        if (monthlyKm <= 0.0) {
            return 0.0;
        }

        int occupancy = Math.max(1, data.getVehicleOccupancy());
        ActivityData.TransportMode mode = data.getTransportMode();

        return switch (mode) {
            case PETROL_CAR, MOTORCYCLE -> {
                double kmPerLitre = data.getFuelEfficiencyKmPerLitre();
                if (kmPerLitre <= 0.0) {
                    throw new IllegalArgumentException("Fuel efficiency (km/L) must be greater than zero.");
                }
                double litres = monthlyKm / kmPerLitre;
                yield (litres * factors.getValue(EmissionFactors.TRANSPORT_PETROL_FUEL)) / occupancy;
            }
            case DIESEL_CAR -> {
                double kmPerLitre = data.getFuelEfficiencyKmPerLitre();
                if (kmPerLitre <= 0.0) {
                    throw new IllegalArgumentException("Fuel efficiency (km/L) must be greater than zero.");
                }
                double litres = monthlyKm / kmPerLitre;
                yield (litres * factors.getValue(EmissionFactors.TRANSPORT_DIESEL_FUEL)) / occupancy;
            }
            case BUS -> monthlyKm * factors.getValue(EmissionFactors.TRANSPORT_BUS_PKM);
            case TRAIN -> monthlyKm * factors.getValue(EmissionFactors.TRANSPORT_TRAIN_PKM);
            case ELECTRIC_VEHICLE -> {
                double kwhPer100Km = Math.max(0.0, data.getEvKwhPer100Km());
                double kwhUsed = (monthlyKm / 100.0) * kwhPer100Km;
                double gridFactor = Math.max(0.0, data.getElectricityEmissionFactor());
                yield (kwhUsed * gridFactor) / occupancy;
            }
            case BICYCLE, WALKING -> 0.0;
        };
    }

    public double calculateForPeriodKg(ActivityData data) {
        return calculateMonthlyEmissionsKg(data) * data.getCalculationPeriod().getMultiplierFromMonthly();
    }

    public static double getDefaultEfficiencyOrConsumption(ActivityData.TransportMode mode) {
        return switch (mode) {
            case PETROL_CAR -> 15.0; // km/L
            case DIESEL_CAR -> 18.0; // km/L
            case MOTORCYCLE -> 45.0; // km/L
            case ELECTRIC_VEHICLE -> 16.0; // kWh/100km
            default -> 0.0;
        };
    }
}

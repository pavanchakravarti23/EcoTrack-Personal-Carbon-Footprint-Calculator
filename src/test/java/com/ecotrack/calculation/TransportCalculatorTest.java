package com.ecotrack.calculation;

import com.ecotrack.model.ActivityData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransportCalculatorTest {

    private EmissionFactors factors;
    private TransportCalculator calculator;
    private ActivityData data;

    @Before
    public void setUp() {
        factors = new EmissionFactors();
        calculator = new TransportCalculator(factors);
        data = new ActivityData();
        data.setTransportEntered(true);
    }

    @Test
    public void petrolCarDailyUsesFuelCombustionAndOccupancy() {
        data.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
        data.setDistancePerTrip(15.0);
        data.setDistanceUnit(ActivityData.DistanceUnit.KILOMETERS);
        data.setTravelFrequency(ActivityData.TravelFrequency.DAILY); // 30 trips/month -> 450 km
        data.setFuelEfficiencyKmPerLitre(15.0); // 30 litres
        data.setVehicleOccupancy(1);

        // 30 litres * 2.31 kg CO2e/L = 69.30 kg CO2e/month
        double monthly = calculator.calculateMonthlyEmissionsKg(data);
        assertEquals(69.30, monthly, 0.001);

        // Carpooling with 2 people halves per-person share
        data.setVehicleOccupancy(2);
        assertEquals(34.65, calculator.calculateMonthlyEmissionsKg(data), 0.001);
    }

    @Test
    public void dieselCarAndMilesUnitConversion() {
        data.setTransportMode(ActivityData.TransportMode.DIESEL_CAR);
        data.setDistancePerTrip(10.0);
        data.setDistanceUnit(ActivityData.DistanceUnit.MILES); // 16.0934 km/trip
        data.setTravelFrequency(ActivityData.TravelFrequency.MONTHLY); // 1 trip/month
        data.setFuelEfficiencyKmPerLitre(18.0);
        data.setVehicleOccupancy(1);

        double expectedKm = 10.0 * 1.60934;
        assertEquals(expectedKm, calculator.calculateMonthlyDistanceKm(data), 0.0001);

        double expectedKg = (expectedKm / 18.0) * factors.getValue(EmissionFactors.TRANSPORT_DIESEL_FUEL);
        assertEquals(expectedKg, calculator.calculateMonthlyEmissionsKg(data), 0.001);
    }

    @Test
    public void busAndTrainUsePassengerKmFormula() {
        data.setDistancePerTrip(20.0);
        data.setDistanceUnit(ActivityData.DistanceUnit.KILOMETERS);
        data.setTravelFrequency(ActivityData.TravelFrequency.WEEKDAYS); // 22 trips -> 440 km

        data.setTransportMode(ActivityData.TransportMode.BUS);
        assertEquals(440.0 * 0.089, calculator.calculateMonthlyEmissionsKg(data), 0.001);

        data.setTransportMode(ActivityData.TransportMode.TRAIN);
        assertEquals(440.0 * 0.041, calculator.calculateMonthlyEmissionsKg(data), 0.001);
    }

    @Test
    public void electricVehicleCouplesWithGridFactor() {
        data.setTransportMode(ActivityData.TransportMode.ELECTRIC_VEHICLE);
        data.setDistancePerTrip(100.0);
        data.setDistanceUnit(ActivityData.DistanceUnit.KILOMETERS);
        data.setTravelFrequency(ActivityData.TravelFrequency.MONTHLY); // 100 km
        data.setEvKwhPer100Km(16.0); // 16 kWh
        data.setElectricityEmissionFactor(0.50);
        data.setVehicleOccupancy(1);

        assertEquals(8.0, calculator.calculateMonthlyEmissionsKg(data), 0.001);
    }

    @Test
    public void bicycleAndWalkingHaveZeroDirectEmissions() {
        data.setDistancePerTrip(12.0);
        data.setTravelFrequency(ActivityData.TravelFrequency.DAILY);

        data.setTransportMode(ActivityData.TransportMode.BICYCLE);
        assertEquals(0.0, calculator.calculateMonthlyEmissionsKg(data), 0.0001);

        data.setTransportMode(ActivityData.TransportMode.WALKING);
        assertEquals(0.0, calculator.calculateMonthlyEmissionsKg(data), 0.0001);
    }

    @Test
    public void zeroDistanceAndUnenteredFlagReturnZero() {
        data.setDistancePerTrip(0.0);
        assertEquals(0.0, calculator.calculateMonthlyEmissionsKg(data), 0.0001);

        data.setDistancePerTrip(25.0);
        data.setTransportEntered(false);
        assertEquals(0.0, calculator.calculateMonthlyEmissionsKg(data), 0.0001);
    }
}

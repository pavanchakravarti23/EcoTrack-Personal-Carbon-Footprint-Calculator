package com.ecotrack.ui;

import com.ecotrack.components.Sidebar;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.utils.ThemeManager;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

public class MainFrameNavigationTest {

    @Test
    public void navigatesAllPanelsAndRecalculatesAcrossAllModules() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ThemeManager.applyGlobalDefaults();
            MainFrame frame = new MainFrame();
            try {
                // Initially empty state
                assertFalse(frame.getLatestResult().isHasAnyData());
                assertEquals(0.0, frame.getLatestResult().getTotalPeriodEmissionsKg(), 0.0001);

                // Navigate through every sidebar view
                String[] views = {
                        Sidebar.NAV_DASHBOARD,
                        Sidebar.NAV_TRANSPORT,
                        Sidebar.NAV_ELECTRICITY,
                        Sidebar.NAV_FOOD,
                        Sidebar.NAV_WASTE,
                        Sidebar.NAV_ANALYTICS,
                        Sidebar.NAV_RECOMMENDATIONS,
                        Sidebar.NAV_SIMULATOR,
                        Sidebar.NAV_SETTINGS,
                        Sidebar.NAV_ABOUT
                };
                for (String view : views) {
                    frame.navigateTo(view);
                }

                // Populate all 4 modules in session and trigger recalculation
                ActivityData data = frame.getActivityData();
                data.setTransportEntered(true);
                data.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
                data.setDistancePerTrip(12.0);
                data.setTravelFrequency(ActivityData.TravelFrequency.WEEKDAYS);
                data.setFuelEfficiencyKmPerLitre(15.0);

                data.setElectricityEntered(true);
                data.setMonthlyElectricityKwh(240.0);
                data.setElectricityEmissionFactor(0.45);

                data.setFoodEntered(true);
                data.setDietType(ActivityData.DietType.MIXED);

                data.setWasteEntered(true);
                data.setWasteEntry(ActivityData.WasteStream.ORGANIC, 4.0, 50.0);
                data.setWasteEntry(ActivityData.WasteStream.GENERAL, 3.0, 0.0);

                frame.getTransportPanel().syncFromModel();
                frame.getElectricityPanel().syncFromModel();
                frame.getFoodPanel().syncFromModel();
                frame.getWastePanel().syncFromModel();
                frame.recalculateAndRefreshAll();

                CarbonResult result = frame.getLatestResult();
                assertTrue(result.isHasAnyData());
                assertTrue(result.getEmission(EmissionCategory.TRANSPORT) > 0.0);
                assertTrue(result.getEmission(EmissionCategory.ELECTRICITY) > 0.0);
                assertTrue(result.getEmission(EmissionCategory.FOOD) > 0.0);
                assertTrue(result.getEmission(EmissionCategory.WASTE) > 0.0);
                assertTrue(result.getTotalPeriodEmissionsKg() > 0.0);

                frame.navigateTo(Sidebar.NAV_DASHBOARD);
                frame.navigateTo(Sidebar.NAV_ANALYTICS);
            } finally {
                frame.dispose();
            }
        });
    }
}

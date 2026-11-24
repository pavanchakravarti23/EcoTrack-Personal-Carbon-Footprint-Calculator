package com.ecotrack.ui;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.components.HeaderPanel;
import com.ecotrack.components.Sidebar;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.Recommendation;
import com.ecotrack.model.UserProfile;
import com.ecotrack.service.AnalyticsService;
import com.ecotrack.service.RecommendationService;
import com.ecotrack.service.ReportService;
import com.ecotrack.service.SimulationService;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Main application window coordinating the Sidebar, HeaderPanel, CardLayout content views,
 * centralized calculation recalculation, and non-blocking PDF/TXT export.
 */
public class MainFrame extends JFrame {

    private final ActivityData activityData = new ActivityData();
    private final UserProfile userProfile = new UserProfile();
    private final CarbonCalculator calculator = new CarbonCalculator();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final RecommendationService recommendationService = new RecommendationService(calculator.getWasteCalculator());
    private final SimulationService simulationService = new SimulationService(calculator);
    private final ReportService reportService = new ReportService();

    private final Sidebar sidebar;
    private final HeaderPanel headerPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentDeck = new JPanel(cardLayout);

    private final DashboardPanel dashboardPanel;
    private final TransportPanel transportPanel;
    private final ElectricityPanel electricityPanel;
    private final FoodPanel foodPanel;
    private final WastePanel wastePanel;
    private final AnalyticsPanel analyticsPanel;
    private final RecommendationsPanel recommendationsPanel;
    private final SimulatorPanel simulatorPanel;
    private final SettingsPanel settingsPanel;
    private final AboutPanel aboutPanel;

    private CarbonResult latestResult;
    private List<Recommendation> latestRecommendations;

    public MainFrame() {
        setTitle("EcoTrack – Personal Carbon Footprint Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1060, 680));
        setSize(1340, 840);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeManager.BG_MAIN);
        setLayout(new BorderLayout());

        sidebar = new Sidebar(this::navigateTo);
        headerPanel = new HeaderPanel(
                this::onPeriodChanged,
                () -> triggerExport(true),
                () -> triggerExport(false));

        dashboardPanel = new DashboardPanel(this::navigateTo);
        transportPanel = new TransportPanel(activityData, calculator, this::recalculateAndRefreshAll, this::onPeriodChanged);
        electricityPanel = new ElectricityPanel(activityData, calculator, this::recalculateAndRefreshAll);
        foodPanel = new FoodPanel(activityData, calculator, this::recalculateAndRefreshAll);
        wastePanel = new WastePanel(activityData, calculator, this::recalculateAndRefreshAll);
        analyticsPanel = new AnalyticsPanel(analyticsService);
        recommendationsPanel = new RecommendationsPanel();
        simulatorPanel = new SimulatorPanel(activityData, simulationService, this::applySimulatedActivityData);
        settingsPanel = new SettingsPanel(userProfile, calculator, this::recalculateAndRefreshAll, this::resetSession);
        aboutPanel = new AboutPanel();

        contentDeck.setBackground(ThemeManager.BG_MAIN);
        contentDeck.add(wrapScrollable(dashboardPanel), Sidebar.NAV_DASHBOARD);
        contentDeck.add(wrapScrollable(transportPanel), Sidebar.NAV_TRANSPORT);
        contentDeck.add(wrapScrollable(electricityPanel), Sidebar.NAV_ELECTRICITY);
        contentDeck.add(wrapScrollable(foodPanel), Sidebar.NAV_FOOD);
        contentDeck.add(wrapScrollable(wastePanel), Sidebar.NAV_WASTE);
        contentDeck.add(wrapScrollable(analyticsPanel), Sidebar.NAV_ANALYTICS);
        contentDeck.add(wrapScrollable(recommendationsPanel), Sidebar.NAV_RECOMMENDATIONS);
        contentDeck.add(wrapScrollable(simulatorPanel), Sidebar.NAV_SIMULATOR);
        contentDeck.add(wrapScrollable(settingsPanel), Sidebar.NAV_SETTINGS);
        contentDeck.add(wrapScrollable(aboutPanel), Sidebar.NAV_ABOUT);

        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setBackground(ThemeManager.BG_MAIN);
        rightColumn.add(headerPanel, BorderLayout.NORTH);
        rightColumn.add(contentDeck, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(rightColumn, BorderLayout.CENTER);

        recalculateAndRefreshAll();
        navigateTo(Sidebar.NAV_DASHBOARD);
    }

    private JScrollPane wrapScrollable(JPanel panel) {
        ScrollableHost host = new ScrollableHost(panel);
        JScrollPane scroll = new JScrollPane(host);
        ThemeManager.styleScrollPane(scroll);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private static class ScrollableHost extends JPanel implements Scrollable {
        ScrollableHost(JPanel content) {
            super(new BorderLayout());
            setBackground(ThemeManager.BG_MAIN);
            add(content, BorderLayout.NORTH);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 60;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public void navigateTo(String navKey) {
        sidebar.setActiveItem(navKey);
        cardLayout.show(contentDeck, navKey);
        switch (navKey) {
            case Sidebar.NAV_DASHBOARD ->
                    headerPanel.setHeaderContext("Dashboard Overview", "Session carbon footprint summary & quick actions");
            case Sidebar.NAV_TRANSPORT ->
                    headerPanel.setHeaderContext("Transportation Calculator", "Fuel combustion, passenger-km transit, EV & active modes");
            case Sidebar.NAV_ELECTRICITY ->
                    headerPanel.setHeaderContext("Electricity Calculator", "Monthly kWh usage & configurable grid emission factors");
            case Sidebar.NAV_FOOD ->
                    headerPanel.setHeaderContext("Food Footprint Calculator", "Dietary profile & meal-frequency approximations");
            case Sidebar.NAV_WASTE ->
                    headerPanel.setHeaderContext("Waste & Recycling Calculator", "Six waste streams with diversion & disposal partitioning");
            case Sidebar.NAV_ANALYTICS ->
                    headerPanel.setHeaderContext("Analytics Dashboard", "Custom Java Graphics2D charts & 12-month projections");
            case Sidebar.NAV_RECOMMENDATIONS ->
                    headerPanel.setHeaderContext("Smart Recommendations", "Deterministic rule-based actions grounded in your inputs");
            case Sidebar.NAV_SIMULATOR ->
                    headerPanel.setHeaderContext("What-If Simulator", "Test hypothetical lifestyle reductions before applying");
            case Sidebar.NAV_SETTINGS ->
                    headerPanel.setHeaderContext("Settings & Emission Factors", "Session preferences, factor registry & session reset");
            case Sidebar.NAV_ABOUT ->
                    headerPanel.setHeaderContext("About & Methodology", "Academic project details, formulas & scientific transparency");
        }
    }

    private void onPeriodChanged(ActivityData.CalculationPeriod period) {
        activityData.setCalculationPeriod(period);
        headerPanel.setSelectedPeriod(period);
        recalculateAndRefreshAll();
    }

    public void recalculateAndRefreshAll() {
        latestResult = calculator.calculate(activityData);
        latestRecommendations = recommendationService.generateRecommendations(activityData, latestResult);

        dashboardPanel.updateState(userProfile, activityData, latestResult, latestRecommendations);
        transportPanel.refreshResults(latestResult);
        electricityPanel.refreshResults(latestResult);
        foodPanel.refreshResults(latestResult);
        wastePanel.refreshResults(latestResult);
        analyticsPanel.refreshCharts(latestResult);
        recommendationsPanel.updateRecommendations(latestResult, latestRecommendations);
        simulatorPanel.runSimulation();
    }

    private void applySimulatedActivityData(ActivityData simulated) {
        activityData.setTransportMode(simulated.getTransportMode());
        activityData.setDistancePerTrip(simulated.getDistancePerTrip());
        activityData.setFuelEfficiencyKmPerLitre(simulated.getFuelEfficiencyKmPerLitre());
        activityData.setMonthlyElectricityKwh(simulated.getMonthlyElectricityKwh());
        activityData.setDietType(simulated.getDietType());
        activityData.setUseCustomMealFrequency(simulated.isUseCustomMealFrequency());
        for (ActivityData.WasteStream s : ActivityData.WasteStream.values()) {
            activityData.setWasteEntry(s,
                    simulated.getWeeklyWasteKg().getOrDefault(s, 0.0),
                    simulated.getRecyclingPercentages().getOrDefault(s, 0.0));
        }
        transportPanel.syncFromModel();
        electricityPanel.syncFromModel();
        foodPanel.syncFromModel();
        wastePanel.syncFromModel();
        recalculateAndRefreshAll();
    }

    private void resetSession() {
        activityData.resetAll();
        headerPanel.setSelectedPeriod(activityData.getCalculationPeriod());
        transportPanel.syncFromModel();
        electricityPanel.syncFromModel();
        foodPanel.syncFromModel();
        wastePanel.syncFromModel();
        recalculateAndRefreshAll();
        navigateTo(Sidebar.NAV_DASHBOARD);
    }

    private void triggerExport(boolean isPdf) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(isPdf ? "Save EcoTrack PDF Report" : "Save EcoTrack TXT Report");
        String ext = isPdf ? "pdf" : "txt";
        chooser.setFileFilter(new FileNameExtensionFilter(
                isPdf ? "PDF Documents (*.pdf)" : "Text Files (*.txt)", ext));
        chooser.setSelectedFile(new File("EcoTrack_Carbon_Report." + ext));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase().endsWith("." + ext)) {
            target = new File(target.getParentFile(), target.getName() + "." + ext);
        }

        final File finalTarget = target;
        final ActivityData snapshotData = activityData.copy();
        final CarbonResult snapshotResult = latestResult;
        final List<Recommendation> snapshotRecs = latestRecommendations;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (isPdf) {
                    reportService.exportPdfReport(
                            finalTarget, userProfile, snapshotData, snapshotResult,
                            snapshotRecs, calculator.getEmissionFactors());
                } else {
                    reportService.exportTxtReport(
                            finalTarget, userProfile, snapshotData, snapshotResult,
                            snapshotRecs, calculator.getEmissionFactors());
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            (isPdf ? "PDF" : "TXT") + " report saved successfully to:\n" + finalTarget.getAbsolutePath(),
                            "Export Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "Failed to export report: " + ex.getMessage(),
                            "Export Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Package-accessible getters for automated UI verification and screenshot capture
    public ActivityData getActivityData() {
        return activityData;
    }

    public CarbonResult getLatestResult() {
        return latestResult;
    }

    public TransportPanel getTransportPanel() {
        return transportPanel;
    }

    public ElectricityPanel getElectricityPanel() {
        return electricityPanel;
    }

    public FoodPanel getFoodPanel() {
        return foodPanel;
    }

    public WastePanel getWastePanel() {
        return wastePanel;
    }
}

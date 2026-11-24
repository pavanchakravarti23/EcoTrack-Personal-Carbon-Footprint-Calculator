package com.ecotrack.ui;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.calculation.EmissionFactors;
import com.ecotrack.calculation.TransportCalculator;
import com.ecotrack.components.CustomButton;
import com.ecotrack.components.CustomProgressBar;
import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.utils.InputValidator;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Module 2: Transportation Carbon Calculator supporting fuel-based combustion,
 * passenger-km transit, grid-coupled electric vehicles, and zero-tailpipe active travel.
 */
public class TransportPanel extends JPanel {

    private final ActivityData activityData;
    private final CarbonCalculator calculator;
    private final Runnable onDataUpdated;
    private final Consumer<ActivityData.CalculationPeriod> onPeriodChanged;

    private final JComboBox<ActivityData.TransportMode> modeCombo;
    private final JTextField distanceField;
    private final JComboBox<ActivityData.DistanceUnit> unitCombo;
    private final JComboBox<ActivityData.TravelFrequency> frequencyCombo;
    private final JComboBox<ActivityData.CalculationPeriod> periodCombo;
    private final JTextField efficiencyField;
    private final JLabel efficiencyLabel;
    private final JSpinner occupancySpinner;
    private final JLabel occupancyLabel;
    private final JLabel methodNoteLabel;

    private final JLabel resultValueLabel;
    private final JLabel monthlyDistanceLabel;
    private final JLabel annualProjectionLabel;
    private final JLabel contributionLabel;
    private final CustomProgressBar contributionBar;
    private final JTextArea formulaArea;
    private final JLabel statusLabel;

    public TransportPanel(
            ActivityData activityData,
            CarbonCalculator calculator,
            Runnable onDataUpdated,
            Consumer<ActivityData.CalculationPeriod> onPeriodChanged) {
        this.activityData = activityData;
        this.calculator = calculator;
        this.onDataUpdated = onDataUpdated;
        this.onPeriodChanged = onPeriodChanged;

        setBackground(ThemeManager.BG_MAIN);
        setLayout(new GridLayout(1, 2, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Left: Input Form Card
        StyledCardPanel formCard = new StyledCardPanel();
        formCard.setLayout(new BorderLayout(0, 14));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 4));
        headerPanel.setOpaque(false);
        headerPanel.add(ThemeManager.createSectionTitle("Transportation Inputs"), BorderLayout.NORTH);
        headerPanel.add(ThemeManager.createMutedLabel(
                "Distinct formulas for fuel combustion, passenger-km transit, EV grid use, and active modes."),
                BorderLayout.SOUTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.weightx = 1.0;

        modeCombo = new JComboBox<>(ActivityData.TransportMode.values());
        ThemeManager.styleComboBox(modeCombo);

        distanceField = new JTextField("15.0");
        ThemeManager.styleTextField(distanceField);

        unitCombo = new JComboBox<>(ActivityData.DistanceUnit.values());
        ThemeManager.styleComboBox(unitCombo);

        frequencyCombo = new JComboBox<>(ActivityData.TravelFrequency.values());
        ThemeManager.styleComboBox(frequencyCombo);

        periodCombo = new JComboBox<>(ActivityData.CalculationPeriod.values());
        ThemeManager.styleComboBox(periodCombo);

        efficiencyLabel = new JLabel("Fuel Efficiency (km / litre):");
        efficiencyLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        efficiencyField = new JTextField("15.0");
        ThemeManager.styleTextField(efficiencyField);

        occupancyLabel = new JLabel("Passengers Sharing Vehicle:");
        occupancyLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        occupancySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        ThemeManager.styleSpinner(occupancySpinner);

        methodNoteLabel = new JLabel();
        methodNoteLabel.setFont(ThemeManager.FONT_SMALL);
        methodNoteLabel.setForeground(ThemeManager.EMERALD);

        int row = 0;
        addFormRow(fieldsPanel, gbc, row++, "Transportation Mode:", modeCombo);

        // Distance + Unit pair
        JPanel distRow = new JPanel(new BorderLayout(8, 0));
        distRow.setOpaque(false);
        distRow.add(distanceField, BorderLayout.CENTER);
        unitCombo.setPreferredSize(new Dimension(95, 36));
        distRow.add(unitCombo, BorderLayout.EAST);
        addFormRow(fieldsPanel, gbc, row++, "Distance Per Trip:", distRow);

        addFormRow(fieldsPanel, gbc, row++, "Travel Frequency:", frequencyCombo);
        addFormRow(fieldsPanel, gbc, row++, "Calculation Period:", periodCombo);
        addFormRow(fieldsPanel, gbc, row++, efficiencyLabel, efficiencyField);
        addFormRow(fieldsPanel, gbc, row++, occupancyLabel, occupancySpinner);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        fieldsPanel.add(methodNoteLabel, gbc);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        CustomButton calculateBtn = new CustomButton("Calculate Transportation", CustomButton.Variant.PRIMARY);
        CustomButton resetBtn = new CustomButton("Reset Module", CustomButton.Variant.SECONDARY);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.STATUS_SUCCESS);

        calculateBtn.addActionListener(e -> handleCalculate());
        resetBtn.addActionListener(e -> handleReset());
        buttonBar.add(calculateBtn);
        buttonBar.add(resetBtn);
        buttonBar.add(statusLabel);

        formCard.add(headerPanel, BorderLayout.NORTH);
        formCard.add(fieldsPanel, BorderLayout.CENTER);
        formCard.add(buttonBar, BorderLayout.SOUTH);

        // Right: Result & Methodology Card
        StyledCardPanel resultCard = new StyledCardPanel();
        resultCard.setLayout(new BorderLayout(0, 14));
        resultCard.add(ThemeManager.createSectionTitle("Transportation Emissions Result"), BorderLayout.NORTH);

        JPanel resultBody = new JPanel();
        resultBody.setOpaque(false);
        resultBody.setLayout(new BoxLayout(resultBody, BoxLayout.Y_AXIS));

        StyledCardPanel highlightBox = new StyledCardPanel(ThemeManager.BG_CARD_ALT, ThemeManager.BORDER_SUBTLE, 12);
        highlightBox.setLayout(new BoxLayout(highlightBox, BoxLayout.Y_AXIS));
        highlightBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel resTag = ThemeManager.createMutedLabel("ESTIMATED TRANSPORT EMISSIONS");
        resultValueLabel = new JLabel("0.00 kg CO2e");
        resultValueLabel.setFont(ThemeManager.FONT_HERO);
        resultValueLabel.setForeground(ThemeManager.EMERALD);

        monthlyDistanceLabel = ThemeManager.createMutedLabel("Monthly Travel Distance: 0.00 km");
        annualProjectionLabel = ThemeManager.createMutedLabel("Annual Projection: 0.00 kg CO2e");

        highlightBox.add(resTag);
        highlightBox.add(Box.createVerticalStrut(6));
        highlightBox.add(resultValueLabel);
        highlightBox.add(Box.createVerticalStrut(8));
        highlightBox.add(monthlyDistanceLabel);
        highlightBox.add(Box.createVerticalStrut(4));
        highlightBox.add(annualProjectionLabel);

        JPanel sharePanel = new JPanel(new BorderLayout(0, 6));
        sharePanel.setOpaque(false);
        sharePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contributionLabel = new JLabel("Contribution to Total Footprint: 0.0%");
        contributionLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        contributionLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        contributionBar = new CustomProgressBar();
        contributionBar.setProgress(0.0, ThemeManager.COLOR_TRANSPORT);
        sharePanel.add(contributionLabel, BorderLayout.NORTH);
        sharePanel.add(contributionBar, BorderLayout.SOUTH);

        formulaArea = new JTextArea(8, 30);
        formulaArea.setEditable(false);
        formulaArea.setLineWrap(true);
        formulaArea.setWrapStyleWord(true);
        formulaArea.setFont(ThemeManager.FONT_SMALL);
        formulaArea.setBackground(ThemeManager.BG_CARD_ALT);
        formulaArea.setForeground(ThemeManager.TEXT_SECONDARY);
        formulaArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        formulaArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultBody.add(highlightBox);
        resultBody.add(Box.createVerticalStrut(14));
        resultBody.add(sharePanel);
        resultBody.add(Box.createVerticalStrut(14));
        resultBody.add(formulaArea);

        resultCard.add(resultBody, BorderLayout.CENTER);

        modeCombo.addActionListener(e -> onModeChanged());
        periodCombo.addActionListener(e -> {
            ActivityData.CalculationPeriod p = (ActivityData.CalculationPeriod) periodCombo.getSelectedItem();
            if (p != null && p != activityData.getCalculationPeriod() && onPeriodChanged != null) {
                onPeriodChanged.accept(p);
            }
        });

        add(formCard);
        add(resultCard);

        syncFromModel();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        addFormRow(panel, gbc, row, lbl, comp);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JLabel labelComp, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.42;
        panel.add(labelComp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.58;
        panel.add(comp, gbc);
    }

    private void onModeChanged() {
        ActivityData.TransportMode mode = (ActivityData.TransportMode) modeCombo.getSelectedItem();
        if (mode == null) return;

        switch (mode.getMethod()) {
            case FUEL_COMBUSTION -> {
                efficiencyLabel.setText("Fuel Efficiency (km / litre):");
                efficiencyField.setEnabled(true);
                efficiencyField.setText(String.valueOf(TransportCalculator.getDefaultEfficiencyOrConsumption(mode)));
                occupancyLabel.setEnabled(true);
                occupancySpinner.setEnabled(true);
                methodNoteLabel.setText("Method: Vehicle fuel combustion (distance / efficiency x fuel factor / occupancy)");
            }
            case ELECTRIC_GRID -> {
                efficiencyLabel.setText("EV Consumption (kWh / 100 km):");
                efficiencyField.setEnabled(true);
                efficiencyField.setText(String.valueOf(TransportCalculator.getDefaultEfficiencyOrConsumption(mode)));
                occupancyLabel.setEnabled(true);
                occupancySpinner.setEnabled(true);
                methodNoteLabel.setText("Method: Grid-coupled EV (kWh used x configured grid factor / occupancy)");
            }
            case PASSENGER_KM -> {
                efficiencyLabel.setText("Efficiency (Not applicable):");
                efficiencyField.setEnabled(false);
                efficiencyField.setText("--");
                occupancyLabel.setEnabled(false);
                occupancySpinner.setEnabled(false);
                methodNoteLabel.setText("Method: Passenger-km transit factor (illustrative occupancy-averaged benchmark)");
            }
            case ZERO_OPERATIONAL -> {
                efficiencyLabel.setText("Efficiency (Not applicable):");
                efficiencyField.setEnabled(false);
                efficiencyField.setText("0");
                occupancyLabel.setEnabled(false);
                occupancySpinner.setEnabled(false);
                methodNoteLabel.setText("Method: Active mobility (0.00 kg CO2e direct operational tailpipe emissions)");
            }
        }
    }

    private void handleCalculate() {
        try {
            ActivityData.TransportMode mode = (ActivityData.TransportMode) modeCombo.getSelectedItem();
            double distance = InputValidator.parseNonNegativeDouble(distanceField.getText(), "Distance per trip");
            ActivityData.DistanceUnit unit = (ActivityData.DistanceUnit) unitCombo.getSelectedItem();
            ActivityData.TravelFrequency freq = (ActivityData.TravelFrequency) frequencyCombo.getSelectedItem();
            ActivityData.CalculationPeriod period = (ActivityData.CalculationPeriod) periodCombo.getSelectedItem();

            if (mode.getMethod() == ActivityData.TransportMode.CalculationMethod.FUEL_COMBUSTION) {
                double eff = InputValidator.parsePositiveDouble(efficiencyField.getText(), "Fuel efficiency (km/L)");
                activityData.setFuelEfficiencyKmPerLitre(eff);
                activityData.setVehicleOccupancy((Integer) occupancySpinner.getValue());
            } else if (mode.getMethod() == ActivityData.TransportMode.CalculationMethod.ELECTRIC_GRID) {
                double evKwh = InputValidator.parsePositiveDouble(efficiencyField.getText(), "EV consumption (kWh/100 km)");
                activityData.setEvKwhPer100Km(evKwh);
                activityData.setVehicleOccupancy((Integer) occupancySpinner.getValue());
            }

            activityData.setTransportMode(mode);
            activityData.setDistancePerTrip(distance);
            activityData.setDistanceUnit(unit);
            activityData.setTravelFrequency(freq);
            if (period != null) {
                activityData.setCalculationPeriod(period);
            }
            activityData.setTransportEntered(true);

            statusLabel.setForeground(ThemeManager.STATUS_SUCCESS);
            statusLabel.setText("Updated!");
            if (onDataUpdated != null) {
                onDataUpdated.run();
            }
        } catch (IllegalArgumentException ex) {
            statusLabel.setForeground(ThemeManager.STATUS_ERROR);
            statusLabel.setText("Invalid input");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleReset() {
        activityData.setTransportEntered(false);
        activityData.setTransportMode(ActivityData.TransportMode.PETROL_CAR);
        activityData.setDistancePerTrip(0.0);
        activityData.setDistanceUnit(ActivityData.DistanceUnit.KILOMETERS);
        activityData.setTravelFrequency(ActivityData.TravelFrequency.DAILY);
        activityData.setFuelEfficiencyKmPerLitre(15.0);
        activityData.setEvKwhPer100Km(16.0);
        activityData.setVehicleOccupancy(1);
        syncFromModel();
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setText("Reset");
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
    }

    public final void syncFromModel() {
        modeCombo.setSelectedItem(activityData.getTransportMode());
        distanceField.setText(activityData.isTransportEntered() ? String.valueOf(activityData.getDistancePerTrip()) : "");
        unitCombo.setSelectedItem(activityData.getDistanceUnit());
        frequencyCombo.setSelectedItem(activityData.getTravelFrequency());
        periodCombo.setSelectedItem(activityData.getCalculationPeriod());
        onModeChanged();

        if (activityData.getTransportMode().getMethod() == ActivityData.TransportMode.CalculationMethod.FUEL_COMBUSTION) {
            efficiencyField.setText(String.valueOf(activityData.getFuelEfficiencyKmPerLitre()));
            occupancySpinner.setValue(activityData.getVehicleOccupancy());
        } else if (activityData.getTransportMode().getMethod() == ActivityData.TransportMode.CalculationMethod.ELECTRIC_GRID) {
            efficiencyField.setText(String.valueOf(activityData.getEvKwhPer100Km()));
            occupancySpinner.setValue(activityData.getVehicleOccupancy());
        }
    }

    public void refreshResults(CarbonResult result) {
        periodCombo.setSelectedItem(activityData.getCalculationPeriod());
        double periodEmissions = result.getEmission(EmissionCategory.TRANSPORT);
        double monthlyDist = calculator.getTransportCalculator().calculateMonthlyDistanceKm(activityData);
        double monthlyEmissions = calculator.getTransportCalculator().calculateMonthlyEmissionsKg(activityData);
        double pct = result.getPercentage(EmissionCategory.TRANSPORT);

        resultValueLabel.setText(NumberFormatter.formatKg(periodEmissions) + " (" + result.getPeriod().getDisplayName() + ")");
        monthlyDistanceLabel.setText("Monthly Equivalent Distance: " + NumberFormatter.formatNumber(monthlyDist) + " km");
        annualProjectionLabel.setText("Annual Transport Projection: "
                + NumberFormatter.formatKg(monthlyEmissions * EmissionFactors.MONTHS_PER_YEAR));
        contributionLabel.setText("Contribution to Total Footprint: " + NumberFormatter.formatPercent(pct));
        contributionBar.setProgress(pct, ThemeManager.COLOR_TRANSPORT);

        formulaArea.setText(buildFormulaExplanation(monthlyDist, monthlyEmissions));
    }

    private String buildFormulaExplanation(double monthlyDistKm, double monthlyEmissions) {
        if (!activityData.isTransportEntered()) {
            return "No transportation inputs saved yet.\n"
                    + "Enter distance, select a transport mode, and click 'Calculate Transportation' to evaluate.";
        }
        ActivityData.TransportMode mode = activityData.getTransportMode();
        EmissionFactors ef = calculator.getEmissionFactors();
        return switch (mode.getMethod()) {
            case FUEL_COMBUSTION -> {
                String fuelKey = (mode == ActivityData.TransportMode.DIESEL_CAR)
                        ? EmissionFactors.TRANSPORT_DIESEL_FUEL
                        : EmissionFactors.TRANSPORT_PETROL_FUEL;
                double fuelFactor = ef.getValue(fuelKey);
                double litres = monthlyDistKm / activityData.getFuelEfficiencyKmPerLitre();
                yield "Methodology: Tailpipe Fuel Combustion\n"
                        + "• Monthly distance: " + NumberFormatter.formatNumber(monthlyDistKm) + " km\n"
                        + "• Estimated fuel used: " + NumberFormatter.formatNumber(litres)
                        + " L (at " + NumberFormatter.formatNumber(activityData.getFuelEfficiencyKmPerLitre()) + " km/L)\n"
                        + "• Fuel factor: " + NumberFormatter.formatFactor(fuelFactor) + " kg CO2e/L (configurable IPCC-based default)\n"
                        + "• Occupancy divisor: " + activityData.getVehicleOccupancy() + " person(s)\n"
                        + "• Monthly result: " + NumberFormatter.formatKg(monthlyEmissions);
            }
            case PASSENGER_KM -> {
                double pkm = (mode == ActivityData.TransportMode.BUS)
                        ? ef.getValue(EmissionFactors.TRANSPORT_BUS_PKM)
                        : ef.getValue(EmissionFactors.TRANSPORT_TRAIN_PKM);
                yield "Methodology: Passenger-Kilometre Transit Factor\n"
                        + "• Monthly distance: " + NumberFormatter.formatNumber(monthlyDistKm) + " passenger-km\n"
                        + "• Applied factor: " + NumberFormatter.formatFactor(pkm)
                        + " kg CO2e/passenger-km (illustrative configurable transit assumption)\n"
                        + "• Monthly result: " + NumberFormatter.formatKg(monthlyEmissions);
            }
            case ELECTRIC_GRID -> "Methodology: Grid-Coupled Electric Vehicle\n"
                    + "• Monthly distance: " + NumberFormatter.formatNumber(monthlyDistKm) + " km\n"
                    + "• Energy draw: " + NumberFormatter.formatNumber(activityData.getEvKwhPer100Km()) + " kWh/100 km\n"
                    + "• Active grid factor: " + NumberFormatter.formatFactor(activityData.getElectricityEmissionFactor())
                    + " kg CO2e/kWh (" + activityData.getElectricityFactorSourceLabel() + ")\n"
                    + "• Occupancy divisor: " + activityData.getVehicleOccupancy() + "\n"
                    + "• Monthly result: " + NumberFormatter.formatKg(monthlyEmissions);
            case ZERO_OPERATIONAL -> "Methodology: Active Human-Powered Mobility\n"
                    + "• Monthly distance: " + NumberFormatter.formatNumber(monthlyDistKm) + " km\n"
                    + "• Direct operational tailpipe emissions = 0.00 kg CO2e.";
        };
    }
}

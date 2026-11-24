package com.ecotrack.ui;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.calculation.EmissionFactors;
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

/**
 * Module 3: Household Electricity Calculator with configurable grid factor presets,
 * custom emission factor support, and transparent source documentation.
 */
public class ElectricityPanel extends JPanel {

    private static class GridPresetItem {
        final String label;
        final double factor; // negative indicates custom input mode
        final String sourceDescription;

        GridPresetItem(String label, double factor, String sourceDescription) {
            this.label = label;
            this.factor = factor;
            this.sourceDescription = sourceDescription;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final ActivityData activityData;
    private final CarbonCalculator calculator;
    private final Runnable onDataUpdated;

    private final JTextField kwhField;
    private final JComboBox<GridPresetItem> presetCombo;
    private final JTextField factorField;
    private final JTextArea sourceDocArea;
    private final JLabel statusLabel;

    private final JLabel monthlyValueLabel;
    private final JLabel periodValueLabel;
    private final JLabel annualValueLabel;
    private final JLabel contributionLabel;
    private final CustomProgressBar contributionBar;

    public ElectricityPanel(
            ActivityData activityData,
            CarbonCalculator calculator,
            Runnable onDataUpdated) {
        this.activityData = activityData;
        this.calculator = calculator;
        this.onDataUpdated = onDataUpdated;

        setBackground(ThemeManager.BG_MAIN);
        setLayout(new GridLayout(1, 2, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Left Form
        StyledCardPanel formCard = new StyledCardPanel();
        formCard.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(ThemeManager.createSectionTitle("Household Electricity Consumption"), BorderLayout.NORTH);
        header.add(ThemeManager.createMutedLabel(
                "Enter monthly electricity bill usage (kWh) and select or enter your grid factor."),
                BorderLayout.SOUTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 12);

        kwhField = new JTextField();
        ThemeManager.styleTextField(kwhField);

        EmissionFactors ef = calculator.getEmissionFactors();
        presetCombo = new JComboBox<>(new GridPresetItem[]{
                new GridPresetItem(
                        "Mixed Generation Grid Preset (0.45 kg CO2e/kWh - Illustrative)",
                        ef.getValue(EmissionFactors.ELECTRICITY_PRESET_MIXED),
                        ef.getInfo(EmissionFactors.ELECTRICITY_PRESET_MIXED).getSource()),
                new GridPresetItem(
                        "Coal-Heavy Grid Preset (0.82 kg CO2e/kWh - Illustrative)",
                        ef.getValue(EmissionFactors.ELECTRICITY_PRESET_COAL),
                        ef.getInfo(EmissionFactors.ELECTRICITY_PRESET_COAL).getSource()),
                new GridPresetItem(
                        "Low-Carbon Grid Preset (0.15 kg CO2e/kWh - Illustrative)",
                        ef.getValue(EmissionFactors.ELECTRICITY_PRESET_LOW_CARBON),
                        ef.getInfo(EmissionFactors.ELECTRICITY_PRESET_LOW_CARBON).getSource()),
                new GridPresetItem(
                        "Custom User-Entered Grid Factor (kg CO2e/kWh)",
                        -1.0,
                        "User-supplied local utility, national authority (e.g., CEA / EPA / DEFRA), or renewable tariff factor.")
        });
        ThemeManager.styleComboBox(presetCombo);

        factorField = new JTextField("0.45");
        ThemeManager.styleTextField(factorField);

        sourceDocArea = new JTextArea(4, 30);
        sourceDocArea.setEditable(false);
        sourceDocArea.setLineWrap(true);
        sourceDocArea.setWrapStyleWord(true);
        sourceDocArea.setFont(ThemeManager.FONT_SMALL);
        sourceDocArea.setBackground(ThemeManager.BG_CARD_ALT);
        sourceDocArea.setForeground(ThemeManager.TEXT_SECONDARY);
        sourceDocArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        addRow(fields, gbc, 0, "Monthly Consumption (kWh):", kwhField);
        addRow(fields, gbc, 1, "Grid Emission Factor Preset:", presetCombo);
        addRow(fields, gbc, 2, "Factor Value (kg CO2e/kWh):", factorField);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        fields.add(sourceDocArea, gbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        CustomButton calcBtn = new CustomButton("Calculate Electricity", CustomButton.Variant.PRIMARY);
        CustomButton resetBtn = new CustomButton("Reset Module", CustomButton.Variant.SECONDARY);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.STATUS_SUCCESS);

        calcBtn.addActionListener(e -> handleCalculate());
        resetBtn.addActionListener(e -> handleReset());
        btnBar.add(calcBtn);
        btnBar.add(resetBtn);
        btnBar.add(statusLabel);

        formCard.add(header, BorderLayout.NORTH);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(btnBar, BorderLayout.SOUTH);

        // Right Results
        StyledCardPanel resultCard = new StyledCardPanel();
        resultCard.setLayout(new BorderLayout(0, 14));
        resultCard.add(ThemeManager.createSectionTitle("Electricity Emissions & Projections"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        StyledCardPanel monthlyBox = new StyledCardPanel(ThemeManager.BG_CARD_ALT, ThemeManager.BORDER_SUBTLE, 12);
        monthlyBox.setLayout(new BoxLayout(monthlyBox, BoxLayout.Y_AXIS));
        monthlyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthlyBox.add(ThemeManager.createMutedLabel("MONTHLY ELECTRICITY EMISSIONS"));
        monthlyValueLabel = new JLabel("0.00 kg CO2e");
        monthlyValueLabel.setFont(ThemeManager.FONT_HERO);
        monthlyValueLabel.setForeground(ThemeManager.COLOR_ELECTRICITY);
        periodValueLabel = ThemeManager.createMutedLabel("Active Period Equivalent: 0.00 kg CO2e");
        annualValueLabel = ThemeManager.createMutedLabel("Annual Projection (12 months): 0.00 kg CO2e");

        monthlyBox.add(Box.createVerticalStrut(6));
        monthlyBox.add(monthlyValueLabel);
        monthlyBox.add(Box.createVerticalStrut(8));
        monthlyBox.add(periodValueLabel);
        monthlyBox.add(Box.createVerticalStrut(4));
        monthlyBox.add(annualValueLabel);

        JPanel sharePanel = new JPanel(new BorderLayout(0, 6));
        sharePanel.setOpaque(false);
        sharePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contributionLabel = new JLabel("Contribution to Total Footprint: 0.0%");
        contributionLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        contributionLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        contributionBar = new CustomProgressBar();
        contributionBar.setProgress(0.0, ThemeManager.COLOR_ELECTRICITY);
        sharePanel.add(contributionLabel, BorderLayout.NORTH);
        sharePanel.add(contributionBar, BorderLayout.SOUTH);

        JTextArea noteArea = new JTextArea(
                "Transparency Notice:\n"
                        + "• Formula: Monthly Emissions = Monthly Consumption (kWh) x Grid Factor (kg CO2e/kWh).\n"
                        + "• Default presets are illustrative configurable scenarios for educational comparison.\n"
                        + "• They are NOT presented as an official Indian national grid emission factor. "
                        + "For official regional accounting, enter a published CEA/utility/EPA value using Custom mode.");
        noteArea.setEditable(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(ThemeManager.FONT_SMALL);
        noteArea.setBackground(ThemeManager.BG_CARD_ALT);
        noteArea.setForeground(ThemeManager.TEXT_SECONDARY);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        noteArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(monthlyBox);
        body.add(Box.createVerticalStrut(14));
        body.add(sharePanel);
        body.add(Box.createVerticalStrut(14));
        body.add(noteArea);

        resultCard.add(body, BorderLayout.CENTER);

        presetCombo.addActionListener(e -> onPresetChanged());

        add(formCard);
        add(resultCard);
        syncFromModel();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.42;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.58;
        panel.add(comp, gbc);
    }

    private void onPresetChanged() {
        GridPresetItem item = (GridPresetItem) presetCombo.getSelectedItem();
        if (item == null) return;
        if (item.factor >= 0.0) {
            factorField.setText(NumberFormatter.formatFactor(item.factor));
            factorField.setEditable(false);
        } else {
            factorField.setEditable(true);
            factorField.requestFocusInWindow();
        }
        sourceDocArea.setText("Source & Applicability:\n" + item.sourceDescription);
    }

    private void handleCalculate() {
        try {
            double kwh = InputValidator.parseNonNegativeDouble(kwhField.getText(), "Monthly electricity (kWh)");
            double factor = InputValidator.parseNonNegativeDouble(factorField.getText(), "Grid emission factor");
            GridPresetItem selected = (GridPresetItem) presetCombo.getSelectedItem();

            activityData.setMonthlyElectricityKwh(kwh);
            activityData.setElectricityEmissionFactor(factor);
            activityData.setElectricityFactorSourceLabel(selected != null ? selected.label : "Custom Grid Factor");
            activityData.setElectricityEntered(true);

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
        activityData.setElectricityEntered(false);
        activityData.setMonthlyElectricityKwh(0.0);
        activityData.setElectricityEmissionFactor(0.45);
        activityData.setElectricityFactorSourceLabel("Mixed Generation Grid Preset (0.45 kg CO2e/kWh - Illustrative)");
        presetCombo.setSelectedIndex(0);
        syncFromModel();
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setText("Reset");
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
    }

    public final void syncFromModel() {
        kwhField.setText(activityData.isElectricityEntered()
                ? String.valueOf(activityData.getMonthlyElectricityKwh()) : "");
        factorField.setText(NumberFormatter.formatFactor(activityData.getElectricityEmissionFactor()));
        onPresetChanged();
    }

    public void refreshResults(CarbonResult result) {
        double monthly = calculator.getElectricityCalculator().calculateMonthlyEmissionsKg(activityData);
        double period = result.getEmission(EmissionCategory.ELECTRICITY);
        double annual = calculator.getElectricityCalculator().calculateAnnualProjectionKg(activityData);
        double pct = result.getPercentage(EmissionCategory.ELECTRICITY);

        monthlyValueLabel.setText(NumberFormatter.formatKg(monthly));
        periodValueLabel.setText("Active Period (" + result.getPeriod().getDisplayName() + "): "
                + NumberFormatter.formatKg(period));
        annualValueLabel.setText("Annual Projection (12 months): " + NumberFormatter.formatKg(annual));
        contributionLabel.setText("Contribution to Total Footprint: " + NumberFormatter.formatPercent(pct));
        contributionBar.setProgress(pct, ThemeManager.COLOR_ELECTRICITY);
    }
}

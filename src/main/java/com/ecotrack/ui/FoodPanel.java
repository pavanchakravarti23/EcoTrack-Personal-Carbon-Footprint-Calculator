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
 * Module 4: Food Footprint Calculator supporting dietary profiles and optional
 * weekly meal-frequency breakdown with transparent approximation disclaimers.
 */
public class FoodPanel extends JPanel {

    private final ActivityData activityData;
    private final CarbonCalculator calculator;
    private final Runnable onDataUpdated;

    private final JComboBox<ActivityData.DietType> dietCombo;
    private final JCheckBox customMealsCheck;
    private final JTextField redMeatMealsField;
    private final JTextField dairyPoultryMealsField;
    private final JTextField plantMealsField;
    private final JLabel statusLabel;

    private final JLabel resultValueLabel;
    private final JLabel monthlyValueLabel;
    private final JLabel annualValueLabel;
    private final JLabel contributionLabel;
    private final CustomProgressBar contributionBar;
    private final JTextArea methodologyArea;

    public FoodPanel(
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
        header.add(ThemeManager.createSectionTitle("Food Consumption Habits"), BorderLayout.NORTH);
        header.add(ThemeManager.createMutedLabel(
                "Select a representative dietary profile or customize weekly meal frequencies."),
                BorderLayout.SOUTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 0, 7, 12);

        dietCombo = new JComboBox<>(ActivityData.DietType.values());
        ThemeManager.styleComboBox(dietCombo);

        customMealsCheck = new JCheckBox("Use custom weekly meal frequencies instead of daily baseline");
        customMealsCheck.setOpaque(false);
        customMealsCheck.setForeground(ThemeManager.EMERALD);
        customMealsCheck.setFont(ThemeManager.FONT_SMALL_BOLD);

        redMeatMealsField = new JTextField();
        dairyPoultryMealsField = new JTextField();
        plantMealsField = new JTextField();
        ThemeManager.styleTextField(redMeatMealsField);
        ThemeManager.styleTextField(dairyPoultryMealsField);
        ThemeManager.styleTextField(plantMealsField);

        addRow(fields, gbc, 0, "Dietary Profile:", dietCombo);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        fields.add(customMealsCheck, gbc);

        addRow(fields, gbc, 2, "High-Impact Meat Meals / Week:", redMeatMealsField);
        addRow(fields, gbc, 3, "Dairy / Poultry / Fish Meals / Week:", dairyPoultryMealsField);
        addRow(fields, gbc, 4, "Plant-Based Meals / Week:", plantMealsField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        CustomButton calcBtn = new CustomButton("Calculate Food Footprint", CustomButton.Variant.PRIMARY);
        CustomButton resetBtn = new CustomButton("Reset Module", CustomButton.Variant.SECONDARY);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);

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
        resultCard.add(ThemeManager.createSectionTitle("Estimated Food Footprint"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        StyledCardPanel box = new StyledCardPanel(ThemeManager.BG_CARD_ALT, ThemeManager.BORDER_SUBTLE, 12);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ThemeManager.createMutedLabel("ESTIMATED FOOD EMISSIONS"));

        resultValueLabel = new JLabel("0.00 kg CO2e");
        resultValueLabel.setFont(ThemeManager.FONT_HERO);
        resultValueLabel.setForeground(ThemeManager.COLOR_FOOD);
        monthlyValueLabel = ThemeManager.createMutedLabel("Monthly Equivalent: 0.00 kg CO2e");
        annualValueLabel = ThemeManager.createMutedLabel("Annual Projection: 0.00 kg CO2e");

        box.add(Box.createVerticalStrut(6));
        box.add(resultValueLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(monthlyValueLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(annualValueLabel);

        JPanel sharePanel = new JPanel(new BorderLayout(0, 6));
        sharePanel.setOpaque(false);
        sharePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contributionLabel = new JLabel("Contribution to Total Footprint: 0.0%");
        contributionLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        contributionLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        contributionBar = new CustomProgressBar();
        contributionBar.setProgress(0.0, ThemeManager.COLOR_FOOD);
        sharePanel.add(contributionLabel, BorderLayout.NORTH);
        sharePanel.add(contributionBar, BorderLayout.SOUTH);

        methodologyArea = new JTextArea(7, 30);
        methodologyArea.setEditable(false);
        methodologyArea.setLineWrap(true);
        methodologyArea.setWrapStyleWord(true);
        methodologyArea.setFont(ThemeManager.FONT_SMALL);
        methodologyArea.setBackground(ThemeManager.BG_CARD_ALT);
        methodologyArea.setForeground(ThemeManager.TEXT_SECONDARY);
        methodologyArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        methodologyArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(box);
        body.add(Box.createVerticalStrut(14));
        body.add(sharePanel);
        body.add(Box.createVerticalStrut(14));
        body.add(methodologyArea);

        resultCard.add(body, BorderLayout.CENTER);

        dietCombo.addActionListener(e -> populateMealDefaultsFromDiet());
        customMealsCheck.addActionListener(e -> updateMealFieldEnabledState());

        add(formCard);
        add(resultCard);
        syncFromModel();
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.52;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.48;
        panel.add(comp, gbc);
    }

    private void populateMealDefaultsFromDiet() {
        ActivityData.DietType dt = (ActivityData.DietType) dietCombo.getSelectedItem();
        if (dt == null) return;
        redMeatMealsField.setText(String.valueOf(dt.getDefaultRedMeatMealsPerWeek()));
        dairyPoultryMealsField.setText(String.valueOf(dt.getDefaultDairyOrPoultryMealsPerWeek()));
        plantMealsField.setText(String.valueOf(dt.getDefaultPlantMealsPerWeek()));
    }

    private void updateMealFieldEnabledState() {
        boolean custom = customMealsCheck.isSelected();
        redMeatMealsField.setEnabled(custom);
        dairyPoultryMealsField.setEnabled(custom);
        plantMealsField.setEnabled(custom);
    }

    private void handleCalculate() {
        try {
            ActivityData.DietType diet = (ActivityData.DietType) dietCombo.getSelectedItem();
            boolean useCustom = customMealsCheck.isSelected();
            int redMeat = InputValidator.parseNonNegativeInt(redMeatMealsField.getText(), "High-impact meat meals/week", 70);
            int dairy = InputValidator.parseNonNegativeInt(dairyPoultryMealsField.getText(), "Dairy/poultry meals/week", 70);
            int plant = InputValidator.parseNonNegativeInt(plantMealsField.getText(), "Plant-based meals/week", 70);

            activityData.setDietType(diet);
            activityData.setUseCustomMealFrequency(useCustom);
            activityData.setRedMeatMealsPerWeek(redMeat);
            activityData.setDairyPoultryMealsPerWeek(dairy);
            activityData.setPlantMealsPerWeek(plant);
            activityData.setFoodEntered(true);

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
        activityData.setFoodEntered(false);
        activityData.setDietType(ActivityData.DietType.MIXED);
        activityData.setUseCustomMealFrequency(false);
        activityData.setRedMeatMealsPerWeek(ActivityData.DietType.MIXED.getDefaultRedMeatMealsPerWeek());
        activityData.setDairyPoultryMealsPerWeek(ActivityData.DietType.MIXED.getDefaultDairyOrPoultryMealsPerWeek());
        activityData.setPlantMealsPerWeek(ActivityData.DietType.MIXED.getDefaultPlantMealsPerWeek());
        syncFromModel();
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setText("Reset");
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
    }

    public final void syncFromModel() {
        dietCombo.setSelectedItem(activityData.getDietType());
        customMealsCheck.setSelected(activityData.isUseCustomMealFrequency());
        redMeatMealsField.setText(String.valueOf(activityData.getRedMeatMealsPerWeek()));
        dairyPoultryMealsField.setText(String.valueOf(activityData.getDairyPoultryMealsPerWeek()));
        plantMealsField.setText(String.valueOf(activityData.getPlantMealsPerWeek()));
        updateMealFieldEnabledState();
    }

    public void refreshResults(CarbonResult result) {
        double period = result.getEmission(EmissionCategory.FOOD);
        double monthly = calculator.getFoodCalculator().calculateMonthlyEmissionsKg(activityData);
        double annual = monthly * EmissionFactors.MONTHS_PER_YEAR;
        double pct = result.getPercentage(EmissionCategory.FOOD);

        resultValueLabel.setText(NumberFormatter.formatKg(period) + " (" + result.getPeriod().getDisplayName() + ")");
        monthlyValueLabel.setText("Monthly Equivalent: " + NumberFormatter.formatKg(monthly));
        annualValueLabel.setText("Annual Projection: " + NumberFormatter.formatKg(annual));
        contributionLabel.setText("Contribution to Total Footprint: " + NumberFormatter.formatPercent(pct));
        contributionBar.setProgress(pct, ThemeManager.COLOR_FOOD);

        EmissionFactors ef = calculator.getEmissionFactors();
        String modeDesc = activityData.isUseCustomMealFrequency()
                ? "Custom Weekly Meal Frequency (" + activityData.getRedMeatMealsPerWeek() + " meat, "
                + activityData.getDairyPoultryMealsPerWeek() + " dairy/poultry, "
                + activityData.getPlantMealsPerWeek() + " plant meals/week)"
                : "Representative Daily Baseline (" + activityData.getDietType().getDisplayName()
                + " = " + NumberFormatter.formatFactor(ef.getDietDailyBaseline(activityData.getDietType()))
                + " kg CO2e/day)";

        methodologyArea.setText("Approximation Notice:\n"
                + "• Active Mode: " + modeDesc + "\n"
                + "• Real-world food emissions vary substantially by agricultural practices, transport distance, "
                + "seasonality, and food waste. These values are clearly documented illustrative approximations, "
                + "not exact nutritional or supply-chain measurements.");
    }
}

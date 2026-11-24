package com.ecotrack.ui;

import com.ecotrack.components.CustomBarChart;
import com.ecotrack.components.CustomButton;
import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.SimulationResult;
import com.ecotrack.model.SimulationScenario;
import com.ecotrack.service.SimulationService;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Module 8: What-If Lifestyle Simulator allowing non-destructive experimentation
 * with travel distance, transport mode, electricity savings, dietary choices, and recycling rates.
 */
public class SimulatorPanel extends JPanel {

    private final ActivityData activityData;
    private final SimulationService simulationService;
    private final Consumer<ActivityData> onApplySimulatedData;

    private final JSlider travelReductionSlider;
    private final JLabel travelSliderValLabel;
    private final JComboBox<Object> modeOverrideCombo;
    private final JSlider elecReductionSlider;
    private final JLabel elecSliderValLabel;
    private final JComboBox<Object> dietOverrideCombo;
    private final JSlider recycleBoostSlider;
    private final JLabel recycleSliderValLabel;

    private final JLabel origValueLabel;
    private final JLabel simValueLabel;
    private final JLabel diffValueLabel;
    private final JLabel pctValueLabel;
    private final JLabel emptyNoteLabel;
    private final CustomBarChart comparisonChart;
    private final CustomButton applyBtn;

    private SimulationResult latestSimulation;

    public SimulatorPanel(
            ActivityData activityData,
            SimulationService simulationService,
            Consumer<ActivityData> onApplySimulatedData) {
        this.activityData = activityData;
        this.simulationService = simulationService;
        this.onApplySimulatedData = onApplySimulatedData;

        setBackground(ThemeManager.BG_MAIN);
        setLayout(new GridLayout(1, 2, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Left: Controls
        StyledCardPanel controlsCard = new StyledCardPanel();
        controlsCard.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(ThemeManager.createSectionTitle("What-If Scenario Controls"), BorderLayout.NORTH);
        header.add(ThemeManager.createMutedLabel(
                "Changes here do not overwrite your session unless you click 'Apply Changes to Session'."),
                BorderLayout.SOUTH);

        JPanel slidersPanel = new JPanel();
        slidersPanel.setOpaque(false);
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.Y_AXIS));

        travelReductionSlider = createSlider(0, 100, 0);
        travelSliderValLabel = new JLabel("0%");
        slidersPanel.add(makeSliderRow("Reduce Travel Distance (%):", travelReductionSlider, travelSliderValLabel));
        slidersPanel.add(Box.createVerticalStrut(10));

        modeOverrideCombo = new JComboBox<>();
        modeOverrideCombo.addItem("Keep Current Transport Mode");
        for (ActivityData.TransportMode m : ActivityData.TransportMode.values()) {
            modeOverrideCombo.addItem(m);
        }
        ThemeManager.styleComboBox(modeOverrideCombo);
        slidersPanel.add(makeComboRow("Hypothetical Transport Mode:", modeOverrideCombo));
        slidersPanel.add(Box.createVerticalStrut(12));

        elecReductionSlider = createSlider(0, 100, 0);
        elecSliderValLabel = new JLabel("0%");
        slidersPanel.add(makeSliderRow("Reduce Electricity Consumption (%):", elecReductionSlider, elecSliderValLabel));
        slidersPanel.add(Box.createVerticalStrut(10));

        dietOverrideCombo = new JComboBox<>();
        dietOverrideCombo.addItem("Keep Current Dietary Habit");
        for (ActivityData.DietType d : ActivityData.DietType.values()) {
            dietOverrideCombo.addItem(d);
        }
        ThemeManager.styleComboBox(dietOverrideCombo);
        slidersPanel.add(makeComboRow("Hypothetical Dietary Habit:", dietOverrideCombo));
        slidersPanel.add(Box.createVerticalStrut(12));

        recycleBoostSlider = createSlider(0, 100, 0);
        recycleSliderValLabel = new JLabel("+0%");
        slidersPanel.add(makeSliderRow("Increase Recycling Rate (+ pts):", recycleBoostSlider, recycleSliderValLabel));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        applyBtn = new CustomButton("Apply Changes to Session", CustomButton.Variant.PRIMARY);
        CustomButton resetBtn = new CustomButton("Reset Sliders", CustomButton.Variant.SECONDARY);
        applyBtn.addActionListener(e -> handleApply());
        resetBtn.addActionListener(e -> resetSliders());
        btnBar.add(applyBtn);
        btnBar.add(resetBtn);

        controlsCard.add(header, BorderLayout.NORTH);
        controlsCard.add(slidersPanel, BorderLayout.CENTER);
        controlsCard.add(btnBar, BorderLayout.SOUTH);

        // Right: Comparison Output
        StyledCardPanel outputCard = new StyledCardPanel();
        outputCard.setLayout(new BorderLayout(0, 14));
        outputCard.add(ThemeManager.createSectionTitle("Simulation Comparison Results"), BorderLayout.NORTH);

        JPanel topMetrics = new JPanel(new GridLayout(2, 2, 12, 12));
        topMetrics.setOpaque(false);
        origValueLabel = new JLabel("0.00 kg CO2e");
        simValueLabel = new JLabel("0.00 kg CO2e");
        diffValueLabel = new JLabel("0.00 kg CO2e");
        pctValueLabel = new JLabel("0.0%");

        topMetrics.add(makeMiniStat("ORIGINAL FOOTPRINT", origValueLabel, ThemeManager.TEXT_PRIMARY));
        topMetrics.add(makeMiniStat("SIMULATED FOOTPRINT", simValueLabel, ThemeManager.EMERALD));
        topMetrics.add(makeMiniStat("ESTIMATED REDUCTION", diffValueLabel, ThemeManager.EMERALD));
        topMetrics.add(makeMiniStat("PERCENTAGE CHANGE", pctValueLabel, ThemeManager.COLOR_ELECTRICITY));

        emptyNoteLabel = ThemeManager.createMutedLabel("");

        comparisonChart = new CustomBarChart("Original vs. Simulated Emissions");

        JPanel rightCenter = new JPanel(new BorderLayout(0, 12));
        rightCenter.setOpaque(false);
        JPanel metricsAndNote = new JPanel(new BorderLayout(0, 8));
        metricsAndNote.setOpaque(false);
        metricsAndNote.add(topMetrics, BorderLayout.CENTER);
        metricsAndNote.add(emptyNoteLabel, BorderLayout.SOUTH);

        rightCenter.add(metricsAndNote, BorderLayout.NORTH);
        rightCenter.add(comparisonChart, BorderLayout.CENTER);

        outputCard.add(rightCenter, BorderLayout.CENTER);

        // Listeners
        travelReductionSlider.addChangeListener(e -> {
            travelSliderValLabel.setText(travelReductionSlider.getValue() + "%");
            runSimulation();
        });
        elecReductionSlider.addChangeListener(e -> {
            elecSliderValLabel.setText(elecReductionSlider.getValue() + "%");
            runSimulation();
        });
        recycleBoostSlider.addChangeListener(e -> {
            recycleSliderValLabel.setText("+" + recycleBoostSlider.getValue() + "%");
            runSimulation();
        });
        modeOverrideCombo.addActionListener(e -> runSimulation());
        dietOverrideCombo.addActionListener(e -> runSimulation());

        add(controlsCard);
        add(outputCard);
        runSimulation();
    }

    private JSlider createSlider(int min, int max, int val) {
        JSlider slider = new JSlider(min, max, val);
        slider.setOpaque(false);
        slider.setForeground(ThemeManager.EMERALD);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        return slider;
    }

    private JPanel makeSliderRow(String title, JSlider slider, JLabel valueLbl) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.FONT_BODY);
        t.setForeground(ThemeManager.TEXT_PRIMARY);
        valueLbl.setFont(ThemeManager.FONT_BODY_BOLD);
        valueLbl.setForeground(ThemeManager.EMERALD);
        top.add(t, BorderLayout.CENTER);
        top.add(valueLbl, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeComboRow(String title, JComboBox<?> combo) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel(title);
        t.setFont(ThemeManager.FONT_BODY);
        t.setForeground(ThemeManager.TEXT_PRIMARY);
        panel.add(t, BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        return panel;
    }

    private StyledCardPanel makeMiniStat(String title, JLabel valLabel, Color valColor) {
        StyledCardPanel card = new StyledCardPanel(ThemeManager.BG_CARD_ALT, ThemeManager.BORDER_SUBTLE, 10);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = ThemeManager.createMutedLabel(title);
        valLabel.setFont(new Font(ThemeManager.FONT_TITLE.getFamily(), Font.BOLD, 18));
        valLabel.setForeground(valColor);
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(valLabel);
        return card;
    }

    public void runSimulation() {
        SimulationScenario scenario = new SimulationScenario();
        scenario.setTravelDistanceReductionPct(travelReductionSlider.getValue());
        scenario.setElectricityReductionPct(elecReductionSlider.getValue());
        scenario.setRecyclingBoostPercentagePoints(recycleBoostSlider.getValue());

        Object modeSel = modeOverrideCombo.getSelectedItem();
        if (modeSel instanceof ActivityData.TransportMode tm) {
            scenario.setOverrideTransportMode(tm);
        }
        Object dietSel = dietOverrideCombo.getSelectedItem();
        if (dietSel instanceof ActivityData.DietType dt) {
            scenario.setOverrideDietType(dt);
        }

        latestSimulation = simulationService.simulate(activityData, scenario);
        boolean hasData = latestSimulation.getOriginalResult().isHasAnyData();
        applyBtn.setEnabled(hasData);

        if (!hasData) {
            origValueLabel.setText("--");
            simValueLabel.setText("--");
            diffValueLabel.setText("--");
            pctValueLabel.setText("--");
            emptyNoteLabel.setText("Enter activity data in at least one calculator module to simulate lifestyle changes.");
            comparisonChart.setSeriesData("Unit: kg CO2e", null, null);
            return;
        }

        double orig = latestSimulation.getOriginalResult().getTotalPeriodEmissionsKg();
        double sim = latestSimulation.getSimulatedResult().getTotalPeriodEmissionsKg();
        double diff = latestSimulation.getDifferenceKgCo2e();
        double pct = latestSimulation.getPercentageReduction();

        origValueLabel.setText(NumberFormatter.formatKg(orig));
        simValueLabel.setText(NumberFormatter.formatKg(sim));
        diffValueLabel.setText((diff >= 0 ? "-" : "+") + NumberFormatter.formatKg(Math.abs(diff)));
        diffValueLabel.setForeground(diff >= 0 ? ThemeManager.EMERALD : ThemeManager.STATUS_WARNING);
        pctValueLabel.setText((pct >= 0 ? "-" : "+") + NumberFormatter.formatPercent(Math.abs(pct)));
        pctValueLabel.setForeground(pct >= 0 ? ThemeManager.EMERALD : ThemeManager.STATUS_WARNING);

        emptyNoteLabel.setText("Period: " + activityData.getCalculationPeriod().getDisplayName()
                + " | Original baseline remains untouched until you click Apply.");

        Map<String, Double> series = new LinkedHashMap<>();
        series.put("Original Total", orig);
        series.put("Simulated Total", sim);
        series.put("Net Change", Math.max(0.0, diff));
        Color[] palette = {ThemeManager.COLOR_FOOD, ThemeManager.EMERALD, ThemeManager.COLOR_ELECTRICITY};
        comparisonChart.setSeriesData("Unit: kg CO2e (" + activityData.getCalculationPeriod().getDisplayName() + ")",
                series, palette);
    }

    private void resetSliders() {
        travelReductionSlider.setValue(0);
        elecReductionSlider.setValue(0);
        recycleBoostSlider.setValue(0);
        modeOverrideCombo.setSelectedIndex(0);
        dietOverrideCombo.setSelectedIndex(0);
        runSimulation();
    }

    private void handleApply() {
        if (latestSimulation == null || !latestSimulation.getOriginalResult().isHasAnyData()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apply simulated changes to your active session?\nThis will update your current activity inputs.",
                "Apply What-If Scenario",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION && onApplySimulatedData != null) {
            onApplySimulatedData.accept(latestSimulation.getSimulatedActivityData());
            resetSliders();
            JOptionPane.showMessageDialog(this, "Simulated scenario applied to current session.",
                    "Scenario Applied", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

package com.ecotrack.ui;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.calculation.EmissionFactors;
import com.ecotrack.calculation.WasteCalculator;
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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Module 5: Household Waste & Recycling Calculator covering 6 waste streams
 * with strict partitioning between diverted and residual disposed mass to prevent double counting.
 */
public class WastePanel extends JPanel {

    private final ActivityData activityData;
    private final CarbonCalculator calculator;
    private final Runnable onDataUpdated;

    private final Map<ActivityData.WasteStream, JTextField> kgFields = new EnumMap<>(ActivityData.WasteStream.class);
    private final Map<ActivityData.WasteStream, JTextField> recycleFields = new EnumMap<>(ActivityData.WasteStream.class);
    private final JLabel statusLabel;

    private final JLabel resultValueLabel;
    private final JLabel diversionLabel;
    private final JLabel annualValueLabel;
    private final JLabel contributionLabel;
    private final CustomProgressBar contributionBar;
    private final DefaultTableModel breakdownModel;

    public WastePanel(
            ActivityData activityData,
            CarbonCalculator calculator,
            Runnable onDataUpdated) {
        this.activityData = activityData;
        this.calculator = calculator;
        this.onDataUpdated = onDataUpdated;

        setBackground(ThemeManager.BG_MAIN);
        setLayout(new GridLayout(1, 2, 18, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Left: Stream Input Grid
        StyledCardPanel formCard = new StyledCardPanel();
        formCard.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(ThemeManager.createSectionTitle("Weekly Waste Generation & Recycling"), BorderLayout.NORTH);
        header.add(ThemeManager.createMutedLabel(
                "Enter weekly waste mass (kg/week) and recycling/composting % (0-100%)."),
                BorderLayout.SOUTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 8);

        addGridHeader(grid, gbc, 0, "Waste Stream", 0.44);
        addGridHeader(grid, gbc, 1, "Weekly Qty (kg)", 0.28);
        addGridHeader(grid, gbc, 2, "Recycled (%)", 0.28);

        int row = 1;
        for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
            gbc.gridy = row++;
            gbc.gridx = 0;
            gbc.weightx = 0.44;
            JLabel lbl = new JLabel(stream.getDisplayName());
            lbl.setFont(ThemeManager.FONT_BODY);
            lbl.setForeground(ThemeManager.TEXT_PRIMARY);
            grid.add(lbl, gbc);

            JTextField kgField = new JTextField("0.0");
            ThemeManager.styleTextField(kgField);
            kgFields.put(stream, kgField);
            gbc.gridx = 1;
            gbc.weightx = 0.28;
            grid.add(kgField, gbc);

            JTextField pctField = new JTextField("0");
            ThemeManager.styleTextField(pctField);
            if (stream == ActivityData.WasteStream.GENERAL) {
                pctField.setText("0");
                pctField.setEnabled(false);
                pctField.setToolTipText("General mixed residual waste is sent to disposal (0% recycling).");
            }
            recycleFields.put(stream, pctField);
            gbc.gridx = 2;
            gbc.weightx = 0.28;
            grid.add(pctField, gbc);
        }

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);
        CustomButton calcBtn = new CustomButton("Calculate Waste", CustomButton.Variant.PRIMARY);
        CustomButton resetBtn = new CustomButton("Reset Module", CustomButton.Variant.SECONDARY);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ThemeManager.FONT_SMALL);

        calcBtn.addActionListener(e -> handleCalculate());
        resetBtn.addActionListener(e -> handleReset());
        btnBar.add(calcBtn);
        btnBar.add(resetBtn);
        btnBar.add(statusLabel);

        formCard.add(header, BorderLayout.NORTH);
        formCard.add(grid, BorderLayout.CENTER);
        formCard.add(btnBar, BorderLayout.SOUTH);

        // Right: Breakdown & Anti-Double-Counting Summary
        StyledCardPanel resultCard = new StyledCardPanel();
        resultCard.setLayout(new BorderLayout(0, 14));
        resultCard.add(ThemeManager.createSectionTitle("Net Waste Emissions Breakdown"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        StyledCardPanel box = new StyledCardPanel(ThemeManager.BG_CARD_ALT, ThemeManager.BORDER_SUBTLE, 12);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(ThemeManager.createMutedLabel("NET WASTE EMISSIONS"));

        resultValueLabel = new JLabel("0.00 kg CO2e");
        resultValueLabel.setFont(ThemeManager.FONT_HERO);
        resultValueLabel.setForeground(ThemeManager.COLOR_WASTE);
        diversionLabel = ThemeManager.createMutedLabel("Overall Diversion Rate: 0.0%");
        annualValueLabel = ThemeManager.createMutedLabel("Annual Projection: 0.00 kg CO2e");

        box.add(Box.createVerticalStrut(6));
        box.add(resultValueLabel);
        box.add(Box.createVerticalStrut(8));
        box.add(diversionLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(annualValueLabel);

        JPanel sharePanel = new JPanel(new BorderLayout(0, 6));
        sharePanel.setOpaque(false);
        sharePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contributionLabel = new JLabel("Contribution to Total Footprint: 0.0%");
        contributionLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        contributionLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        contributionBar = new CustomProgressBar();
        contributionBar.setProgress(0.0, ThemeManager.COLOR_WASTE);
        sharePanel.add(contributionLabel, BorderLayout.NORTH);
        sharePanel.add(contributionBar, BorderLayout.SOUTH);

        String[] cols = {"Stream", "Total (kg/wk)", "Diverted", "Disposed", "Monthly kg CO2e"};
        breakdownModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(breakdownModel);
        table.setBackground(ThemeManager.BG_CARD_ALT);
        table.setForeground(ThemeManager.TEXT_PRIMARY);
        table.setGridColor(ThemeManager.BORDER_SUBTLE);
        table.setRowHeight(26);
        table.setFont(ThemeManager.FONT_SMALL);
        table.getTableHeader().setBackground(ThemeManager.BG_SIDEBAR);
        table.getTableHeader().setForeground(ThemeManager.TEXT_SECONDARY);
        table.getTableHeader().setFont(ThemeManager.FONT_SMALL_BOLD);

        JScrollPane tableScroll = new JScrollPane(table);
        ThemeManager.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(400, 195));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel noteLbl = ThemeManager.createMutedLabel(
                "Note: Total = Diverted + Disposed. Separate factors are applied to avoid double-counting.");
        noteLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(box);
        body.add(Box.createVerticalStrut(12));
        body.add(sharePanel);
        body.add(Box.createVerticalStrut(12));
        body.add(tableScroll);
        body.add(Box.createVerticalStrut(8));
        body.add(noteLbl);

        resultCard.add(body, BorderLayout.CENTER);

        add(formCard);
        add(resultCard);
        syncFromModel();
    }

    private void addGridHeader(JPanel grid, GridBagConstraints gbc, int col, String text, double weight) {
        gbc.gridy = 0;
        gbc.gridx = col;
        gbc.weightx = weight;
        JLabel lbl = new JLabel(text);
        lbl.setFont(ThemeManager.FONT_SMALL_BOLD);
        lbl.setForeground(ThemeManager.TEXT_SECONDARY);
        grid.add(lbl, gbc);
    }

    private void handleCalculate() {
        try {
            Map<ActivityData.WasteStream, Double> kgs = new EnumMap<>(ActivityData.WasteStream.class);
            Map<ActivityData.WasteStream, Double> pcts = new EnumMap<>(ActivityData.WasteStream.class);

            for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
                double kg = InputValidator.parseNonNegativeDouble(
                        kgFields.get(stream).getText(), stream.getDisplayName() + " weekly kg");
                double pct = (stream == ActivityData.WasteStream.GENERAL)
                        ? 0.0
                        : InputValidator.parsePercentage(
                                recycleFields.get(stream).getText(), stream.getDisplayName() + " recycling %");
                kgs.put(stream, kg);
                pcts.put(stream, pct);
            }

            for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
                activityData.setWasteEntry(stream, kgs.get(stream), pcts.get(stream));
            }
            activityData.setWasteEntered(true);

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
        activityData.setWasteEntered(false);
        for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
            activityData.setWasteEntry(stream, 0.0, 0.0);
        }
        syncFromModel();
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusLabel.setText("Reset");
        if (onDataUpdated != null) {
            onDataUpdated.run();
        }
    }

    public final void syncFromModel() {
        for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
            double kg = activityData.getWeeklyWasteKg().getOrDefault(stream, 0.0);
            double pct = activityData.getRecyclingPercentages().getOrDefault(stream, 0.0);
            kgFields.get(stream).setText(activityData.isWasteEntered() ? String.valueOf(kg) : "0.0");
            recycleFields.get(stream).setText(activityData.isWasteEntered() ? String.valueOf(pct) : "0");
        }
    }

    public void refreshResults(CarbonResult result) {
        double period = result.getEmission(EmissionCategory.WASTE);
        WasteCalculator wc = calculator.getWasteCalculator();
        double monthly = wc.calculateMonthlyEmissionsKg(activityData);
        double diversion = wc.calculateOverallDiversionPercentage(activityData);
        double pct = result.getPercentage(EmissionCategory.WASTE);

        resultValueLabel.setText(NumberFormatter.formatKg(period) + " (" + result.getPeriod().getDisplayName() + ")");
        diversionLabel.setText("Overall Diversion Rate: " + NumberFormatter.formatPercent(diversion));
        annualValueLabel.setText("Annual Projection: "
                + NumberFormatter.formatKg(monthly * EmissionFactors.MONTHS_PER_YEAR));
        contributionLabel.setText("Contribution to Total Footprint: " + NumberFormatter.formatPercent(pct));
        contributionBar.setProgress(pct, ThemeManager.COLOR_WASTE);

        breakdownModel.setRowCount(0);
        Map<ActivityData.WasteStream, WasteCalculator.StreamBreakdown> map = wc.calculateStreamBreakdowns(activityData);
        for (ActivityData.WasteStream stream : ActivityData.WasteStream.values()) {
            WasteCalculator.StreamBreakdown b = map.get(stream);
            breakdownModel.addRow(new Object[]{
                    stream.getDisplayName(),
                    NumberFormatter.formatNumber(b.getWeeklyTotalKg()),
                    NumberFormatter.formatNumber(b.getWeeklyDivertedKg()),
                    NumberFormatter.formatNumber(b.getWeeklyDisposedKg()),
                    NumberFormatter.formatNumber(b.getMonthlyEmissionsKg())
            });
        }
    }
}

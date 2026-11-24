package com.ecotrack.ui;

import com.ecotrack.components.CustomBarChart;
import com.ecotrack.components.CustomPieChart;
import com.ecotrack.components.CustomProjectionChart;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.service.AnalyticsService;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Module 6: Analytics Dashboard containing four interactive custom Java Graphics2D charts:
 * 1. Carbon emissions pie chart
 * 2. Category comparison bar chart
 * 3. Monthly emission comparison chart
 * 4. 12-month annual projection chart
 */
public class AnalyticsPanel extends JPanel {

    private final AnalyticsService analyticsService;
    private final CustomPieChart pieChart;
    private final CustomBarChart categoryBarChart;
    private final CustomBarChart monthlyComparisonChart;
    private final CustomProjectionChart annualProjectionChart;
    private final JLabel statusInfoLabel;

    public AnalyticsPanel(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        setBackground(ThemeManager.BG_MAIN);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.add(ThemeManager.createSectionTitle("Interactive Emissions Analytics (Java Graphics2D)"), BorderLayout.CENTER);
        statusInfoLabel = ThemeManager.createMutedLabel("Hover over slices, bars, or monthly points for exact values.");
        topRow.add(statusInfoLabel, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        pieChart = new CustomPieChart("1. Carbon Emissions Breakdown");
        categoryBarChart = new CustomBarChart("2. Category Comparison");
        monthlyComparisonChart = new CustomBarChart("3. Monthly Emission Comparison");
        annualProjectionChart = new CustomProjectionChart("4. Annual Projection (12 Months)");

        grid.add(pieChart);
        grid.add(categoryBarChart);
        grid.add(monthlyComparisonChart);
        grid.add(annualProjectionChart);

        add(topRow, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    public void refreshCharts(CarbonResult result) {
        pieChart.setCarbonResult(result);
        categoryBarChart.setCategoryData(result);

        if (result == null || !result.isHasAnyData()) {
            monthlyComparisonChart.setSeriesData("Unit: kg CO2e / month", null, null);
            annualProjectionChart.setProjectionData(false, null, null);
            statusInfoLabel.setText("Empty session: enter calculator inputs to activate live charts.");
            return;
        }

        Map<String, Double> monthlySeries = analyticsService.buildMonthlyComparisonSeries(result);
        Color[] comparisonPalette = {
                ThemeManager.EMERALD,
                ThemeManager.COLOR_ELECTRICITY,
                ThemeManager.COLOR_FOOD,
                ThemeManager.TEXT_MUTED
        };
        monthlyComparisonChart.setSeriesData(
                "Unit: kg CO2e / month (Target 250 kg is illustrative)",
                monthlySeries,
                comparisonPalette);

        annualProjectionChart.setProjectionData(
                true,
                analyticsService.buildCumulativeBaselineProjection(result),
                analyticsService.buildCumulativeReducedProjection(result, 0.20));

        statusInfoLabel.setText("Charts synchronized with " + result.getPeriod().getDisplayName()
                + " footprint (" + com.ecotrack.utils.NumberFormatter.formatKg(result.getTotalPeriodEmissionsKg()) + ").");
    }
}

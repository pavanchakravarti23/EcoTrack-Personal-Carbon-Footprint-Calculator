package com.ecotrack.ui;

import com.ecotrack.components.CustomButton;
import com.ecotrack.components.CustomPieChart;
import com.ecotrack.components.CustomProgressBar;
import com.ecotrack.components.Sidebar;
import com.ecotrack.components.StatCard;
import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.model.ActivityData;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.model.Recommendation;
import com.ecotrack.model.UserProfile;
import com.ecotrack.utils.IconFactory;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Module 1: Home Dashboard showing KPI cards, distribution chart, summary status,
 * quick navigation buttons, environmental tips, and an honest empty state.
 */
public class DashboardPanel extends JPanel {

    private final Consumer<String> navigator;

    private final JLabel welcomeLabel;
    private final JLabel completionStatusLabel;
    private final JLabel emptyStateBanner;

    private final StatCard totalCard;
    private final StatCard transportCard;
    private final StatCard electricityCard;
    private final StatCard foodCard;
    private final StatCard wasteCard;

    private final CustomPieChart pieChart;
    private final Map<EmissionCategory, CustomProgressBar> shareBars = new EnumMap<>(EmissionCategory.class);
    private final Map<EmissionCategory, JLabel> shareValueLabels = new EnumMap<>(EmissionCategory.class);
    private final JTextArea summaryArea;
    private final JTextArea tipsArea;

    public DashboardPanel(Consumer<String> navigator) {
        this.navigator = navigator;
        setBackground(ThemeManager.BG_MAIN);
        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Top Welcome + Quick Navigation Card
        StyledCardPanel heroCard = new StyledCardPanel(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        heroCard.setLayout(new BorderLayout(16, 12));

        JPanel welcomeTextPanel = new JPanel();
        welcomeTextPanel.setOpaque(false);
        welcomeTextPanel.setLayout(new BoxLayout(welcomeTextPanel, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Welcome to EcoTrack");
        welcomeLabel.setFont(ThemeManager.FONT_TITLE);
        welcomeLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        completionStatusLabel = new JLabel("0 of 4 activity modules completed in this session");
        completionStatusLabel.setFont(ThemeManager.FONT_BODY);
        completionStatusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        completionStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        emptyStateBanner = new JLabel(
                "No carbon data entered yet. Start with Transportation, Electricity, Food, or Waste below to compute your estimate.");
        emptyStateBanner.setFont(ThemeManager.FONT_SMALL_BOLD);
        emptyStateBanner.setForeground(ThemeManager.EMERALD);

        welcomeTextPanel.add(welcomeLabel);
        welcomeTextPanel.add(Box.createVerticalStrut(4));
        welcomeTextPanel.add(completionStatusLabel);
        welcomeTextPanel.add(Box.createVerticalStrut(6));
        welcomeTextPanel.add(emptyStateBanner);

        JPanel quickButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        quickButtons.setOpaque(false);
        quickButtons.add(makeQuickButton("Transport", Sidebar.NAV_TRANSPORT, CustomButton.Variant.SECONDARY));
        quickButtons.add(makeQuickButton("Electricity", Sidebar.NAV_ELECTRICITY, CustomButton.Variant.SECONDARY));
        quickButtons.add(makeQuickButton("Food", Sidebar.NAV_FOOD, CustomButton.Variant.SECONDARY));
        quickButtons.add(makeQuickButton("Waste", Sidebar.NAV_WASTE, CustomButton.Variant.SECONDARY));
        quickButtons.add(makeQuickButton("What-If Simulator", Sidebar.NAV_SIMULATOR, CustomButton.Variant.PRIMARY));

        heroCard.add(welcomeTextPanel, BorderLayout.CENTER);
        heroCard.add(quickButtons, BorderLayout.SOUTH);

        // KPI Stat Grid (5 cards)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 14, 14));
        kpiGrid.setOpaque(false);
        totalCard = new StatCard("TOTAL FOOTPRINT",
                IconFactory.create(IconFactory.Kind.LEAF, 18, ThemeManager.EMERALD), ThemeManager.EMERALD);
        transportCard = new StatCard("TRANSPORTATION",
                IconFactory.create(IconFactory.Kind.TRANSPORT, 18, ThemeManager.COLOR_TRANSPORT), ThemeManager.COLOR_TRANSPORT);
        electricityCard = new StatCard("ELECTRICITY",
                IconFactory.create(IconFactory.Kind.ELECTRICITY, 18, ThemeManager.COLOR_ELECTRICITY), ThemeManager.COLOR_ELECTRICITY);
        foodCard = new StatCard("FOOD HABITS",
                IconFactory.create(IconFactory.Kind.FOOD, 18, ThemeManager.COLOR_FOOD), ThemeManager.COLOR_FOOD);
        wasteCard = new StatCard("WASTE & RECYCLING",
                IconFactory.create(IconFactory.Kind.WASTE, 18, ThemeManager.COLOR_WASTE), ThemeManager.COLOR_WASTE);

        kpiGrid.add(totalCard);
        kpiGrid.add(transportCard);
        kpiGrid.add(electricityCard);
        kpiGrid.add(foodCard);
        kpiGrid.add(wasteCard);

        // Middle Split: Pie Chart + Category Distribution Bars
        JPanel middleGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        middleGrid.setOpaque(false);

        pieChart = new CustomPieChart("Category-Wise Emission Distribution");

        StyledCardPanel distributionCard = new StyledCardPanel(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        distributionCard.setLayout(new BorderLayout(0, 14));
        JLabel distHeader = ThemeManager.createSectionTitle("Emission Share & Latest Summary");
        distributionCard.add(distHeader, BorderLayout.NORTH);

        JPanel barsPanel = new JPanel();
        barsPanel.setOpaque(false);
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));

        for (EmissionCategory cat : EmissionCategory.values()) {
            JPanel row = new JPanel(new BorderLayout(0, 5));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            JPanel labelRow = new JPanel(new BorderLayout());
            labelRow.setOpaque(false);
            JLabel catLbl = new JLabel(cat.getDisplayName());
            catLbl.setFont(ThemeManager.FONT_SMALL_BOLD);
            catLbl.setForeground(ThemeManager.TEXT_PRIMARY);

            JLabel valLbl = new JLabel("0.00 kg CO2e (0.0%)");
            valLbl.setFont(ThemeManager.FONT_SMALL);
            valLbl.setForeground(ThemeManager.TEXT_SECONDARY);
            shareValueLabels.put(cat, valLbl);

            labelRow.add(catLbl, BorderLayout.WEST);
            labelRow.add(valLbl, BorderLayout.EAST);

            CustomProgressBar bar = new CustomProgressBar();
            bar.setProgress(0.0, ThemeManager.getCategoryColor(cat));
            shareBars.put(cat, bar);

            row.add(labelRow, BorderLayout.NORTH);
            row.add(bar, BorderLayout.SOUTH);
            barsPanel.add(row);
        }

        summaryArea = new JTextArea(4, 30);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBackground(ThemeManager.BG_CARD_ALT);
        summaryArea.setForeground(ThemeManager.TEXT_PRIMARY);
        summaryArea.setFont(ThemeManager.FONT_SMALL);
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        distributionCard.add(barsPanel, BorderLayout.CENTER);
        distributionCard.add(summaryArea, BorderLayout.SOUTH);

        middleGrid.add(pieChart);
        middleGrid.add(distributionCard);

        // Bottom Tips Card
        StyledCardPanel tipsCard = new StyledCardPanel(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        tipsCard.setLayout(new BorderLayout(12, 10));

        JPanel tipsHeader = new JPanel(new BorderLayout());
        tipsHeader.setOpaque(false);
        tipsHeader.add(ThemeManager.createSectionTitle("Environmental Tips & Action Highlights"), BorderLayout.WEST);
        CustomButton viewAllRecBtn = makeQuickButton("Open Recommendations", Sidebar.NAV_RECOMMENDATIONS, CustomButton.Variant.SECONDARY);
        tipsHeader.add(viewAllRecBtn, BorderLayout.EAST);

        tipsArea = new JTextArea(4, 40);
        tipsArea.setEditable(false);
        tipsArea.setLineWrap(true);
        tipsArea.setWrapStyleWord(true);
        tipsArea.setBackground(ThemeManager.BG_CARD);
        tipsArea.setForeground(ThemeManager.TEXT_SECONDARY);
        tipsArea.setFont(ThemeManager.FONT_BODY);
        tipsArea.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));

        tipsCard.add(tipsHeader, BorderLayout.NORTH);
        tipsCard.add(tipsArea, BorderLayout.CENTER);

        JPanel bodyStack = new JPanel();
        bodyStack.setOpaque(false);
        bodyStack.setLayout(new BoxLayout(bodyStack, BoxLayout.Y_AXIS));
        heroCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        middleGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        bodyStack.add(heroCard);
        bodyStack.add(Box.createVerticalStrut(16));
        bodyStack.add(kpiGrid);
        bodyStack.add(Box.createVerticalStrut(16));
        bodyStack.add(middleGrid);
        bodyStack.add(Box.createVerticalStrut(16));
        bodyStack.add(tipsCard);

        add(bodyStack, BorderLayout.NORTH);
    }

    private CustomButton makeQuickButton(String text, String navTarget, CustomButton.Variant variant) {
        CustomButton btn = new CustomButton(text, variant);
        btn.setPreferredSize(new Dimension(Math.max(110, text.length() * 9 + 30), 34));
        btn.addActionListener(e -> {
            if (navigator != null) {
                navigator.accept(navTarget);
            }
        });
        return btn;
    }

    public void updateState(
            UserProfile profile,
            ActivityData data,
            CarbonResult result,
            List<Recommendation> recommendations) {
        String name = (profile != null) ? profile.getDisplayName() : "EcoTrack User";
        welcomeLabel.setText("Welcome back, " + name);

        int completedCount = 0;
        if (data.isTransportEntered()) completedCount++;
        if (data.isElectricityEntered()) completedCount++;
        if (data.isFoodEntered()) completedCount++;
        if (data.isWasteEntered()) completedCount++;
        completionStatusLabel.setText(completedCount + " of 4 activity modules entered  |  Active period: "
                + result.getPeriod().getDisplayName());

        if (!result.isHasAnyData()) {
            emptyStateBanner.setVisible(true);
            totalCard.updateMetric("--", "No data entered yet", 0.0);
            transportCard.updateMetric("--", "Not entered", 0.0);
            electricityCard.updateMetric("--", "Not entered", 0.0);
            foodCard.updateMetric("--", "Not entered", 0.0);
            wasteCard.updateMetric("--", "Not entered", 0.0);

            for (EmissionCategory cat : EmissionCategory.values()) {
                shareBars.get(cat).setProgress(0.0, ThemeManager.getCategoryColor(cat));
                shareValueLabels.get(cat).setText("0.00 kg CO2e (0.0%)");
            }
            summaryArea.setText("Status: Empty session. No fabricated figures are shown.\n"
                    + "Use the quick navigation buttons above or the sidebar to enter Transportation, Electricity, Food, or Waste data.");
            tipsArea.setText("General Sustainability Tips:\n"
                    + "• Track your daily commute distance and mode to identify fuel vs. transit impacts.\n"
                    + "• Check your monthly utility bill (kWh) and select an appropriate local grid emission factor.\n"
                    + "• Segregate organic and recyclable streams to prevent landfill methane generation.");
        } else {
            emptyStateBanner.setVisible(false);
            totalCard.updateMetric(
                    NumberFormatter.formatKg(result.getTotalPeriodEmissionsKg()),
                    "Annual projection: " + NumberFormatter.formatKg(result.getAnnualProjectionKg()),
                    100.0);

            updateCategoryCard(transportCard, result, EmissionCategory.TRANSPORT,
                    data.isTransportEntered() ? data.getTransportMode().getDisplayName() : "Not entered");
            updateCategoryCard(electricityCard, result, EmissionCategory.ELECTRICITY,
                    data.isElectricityEntered()
                            ? NumberFormatter.formatNumber(data.getMonthlyElectricityKwh()) + " kWh/mo"
                            : "Not entered");
            updateCategoryCard(foodCard, result, EmissionCategory.FOOD,
                    data.isFoodEntered() ? data.getDietType().getDisplayName() : "Not entered");
            updateCategoryCard(wasteCard, result, EmissionCategory.WASTE,
                    data.isWasteEntered() ? "6-stream waste profile" : "Not entered");

            for (EmissionCategory cat : EmissionCategory.values()) {
                double val = result.getEmission(cat);
                double pct = result.getPercentage(cat);
                shareBars.get(cat).setProgress(pct, ThemeManager.getCategoryColor(cat));
                shareValueLabels.get(cat).setText(
                        NumberFormatter.formatKg(val) + " (" + NumberFormatter.formatPercent(pct) + ")");
            }

            String timestamp = result.getCalculatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            summaryArea.setText("Latest Calculation (" + timestamp + "):\n"
                    + result.getSummaryNote() + "\n"
                    + "Monthly Equivalent: " + NumberFormatter.formatKg(result.getMonthlyEquivalentKg())
                    + "  |  Annual Projection: " + NumberFormatter.formatKg(result.getAnnualProjectionKg()));

            if (recommendations == null || recommendations.isEmpty()) {
                tipsArea.setText("All entered activities currently sum to 0.00 kg CO2e direct emissions.");
            } else {
                StringBuilder sb = new StringBuilder();
                int limit = Math.min(3, recommendations.size());
                for (int i = 0; i < limit; i++) {
                    Recommendation r = recommendations.get(i);
                    sb.append("• [").append(r.getPriority().getLabel()).append("] ")
                            .append(r.getTitle()).append(" — ").append(r.getActionSummary()).append("\n");
                }
                tipsArea.setText(sb.toString().trim());
            }
        }

        pieChart.setCarbonResult(result);
        revalidate();
        repaint();
    }

    private void updateCategoryCard(
            StatCard card, CarbonResult result, EmissionCategory category, String detailText) {
        double val = result.getEmission(category);
        double pct = result.getPercentage(category);
        card.updateMetric(
                NumberFormatter.formatKg(val),
                NumberFormatter.formatPercent(pct) + " share · " + detailText,
                pct);
    }
}

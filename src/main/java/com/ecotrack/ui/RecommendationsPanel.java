package com.ecotrack.ui;

import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.Recommendation;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Module 7: Rule-Based Smart Recommendations Panel.
 */
public class RecommendationsPanel extends JPanel {

    private final JLabel subtitleLabel;
    private final JPanel listContainer;

    public RecommendationsPanel() {
        setBackground(ThemeManager.BG_MAIN);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        StyledCardPanel headerCard = new StyledCardPanel();
        headerCard.setLayout(new BorderLayout(0, 6));
        headerCard.add(ThemeManager.createSectionTitle("Rule-Based Sustainability Recommendations"), BorderLayout.NORTH);
        subtitleLabel = ThemeManager.createMutedLabel(
                "Deterministic recommendations ranked by emission impact from your entered activity data (no AI claims).");
        headerCard.add(subtitleLabel, BorderLayout.SOUTH);

        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));

        add(headerCard, BorderLayout.NORTH);
        add(listContainer, BorderLayout.CENTER);
    }

    public void updateRecommendations(CarbonResult result, List<Recommendation> recommendations) {
        listContainer.removeAll();

        if (result == null || !result.isHasAnyData() || recommendations == null || recommendations.isEmpty()) {
            StyledCardPanel emptyCard = new StyledCardPanel();
            emptyCard.setLayout(new BorderLayout(0, 8));
            emptyCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel title = ThemeManager.createSectionTitle("No Recommendations Generated Yet");
            JLabel desc = ThemeManager.createMutedLabel(
                    "Enter values in Transportation, Electricity, Food, or Waste to generate tailored reduction steps.");
            emptyCard.add(title, BorderLayout.NORTH);
            emptyCard.add(desc, BorderLayout.CENTER);
            listContainer.add(emptyCard);
        } else {
            subtitleLabel.setText("Showing " + recommendations.size()
                    + " rule-based recommendations based on your " + result.getPeriod().getDisplayName().toLowerCase()
                    + " footprint.");

            for (Recommendation rec : recommendations) {
                StyledCardPanel card = new StyledCardPanel();
                card.setLayout(new BorderLayout(14, 8));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Left category color indicator
                JPanel strip = new JPanel();
                strip.setPreferredSize(new Dimension(6, 60));
                strip.setBackground(ThemeManager.getCategoryColor(rec.getCategory()));
                card.add(strip, BorderLayout.WEST);

                JPanel content = new JPanel();
                content.setOpaque(false);
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

                JPanel topMeta = new JPanel(new BorderLayout(12, 0));
                topMeta.setOpaque(false);
                topMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel titleLbl = new JLabel(rec.getTitle());
                titleLbl.setFont(ThemeManager.FONT_SUBTITLE);
                titleLbl.setForeground(ThemeManager.TEXT_PRIMARY);

                JLabel badge = new JLabel(rec.getCategory().getDisplayName() + " · " + rec.getPriority().getLabel());
                badge.setFont(ThemeManager.FONT_SMALL_BOLD);
                Color badgeFg = switch (rec.getPriority()) {
                    case HIGH -> ThemeManager.STATUS_WARNING;
                    case MEDIUM -> ThemeManager.EMERALD;
                    case LOW -> ThemeManager.TEXT_SECONDARY;
                };
                badge.setForeground(badgeFg);

                topMeta.add(titleLbl, BorderLayout.CENTER);
                topMeta.add(badge, BorderLayout.EAST);

                JTextArea actionArea = new JTextArea(rec.getActionSummary());
                actionArea.setEditable(false);
                actionArea.setLineWrap(true);
                actionArea.setWrapStyleWord(true);
                actionArea.setOpaque(false);
                actionArea.setFont(ThemeManager.FONT_BODY);
                actionArea.setForeground(ThemeManager.TEXT_PRIMARY);
                actionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel rationaleLbl = new JLabel("Trigger: " + rec.getRationale() + "  |  Potential: " + rec.getEstimatedSavingsNote());
                rationaleLbl.setFont(ThemeManager.FONT_SMALL);
                rationaleLbl.setForeground(ThemeManager.TEXT_SECONDARY);
                rationaleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

                content.add(topMeta);
                content.add(Box.createVerticalStrut(6));
                content.add(actionArea);
                content.add(Box.createVerticalStrut(6));
                content.add(rationaleLbl);

                card.add(content, BorderLayout.CENTER);
                listContainer.add(card);
                listContainer.add(Box.createVerticalStrut(12));
            }
        }

        revalidate();
        repaint();
    }
}

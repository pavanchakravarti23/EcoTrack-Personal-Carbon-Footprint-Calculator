package com.ecotrack.components;

import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard KPI card displaying a metric title, primary value, subtitle, and optional share bar.
 */
public class StatCard extends StyledCardPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final CustomProgressBar progressBar;
    private Color accentColor;

    public StatCard(String title, Icon icon, Color accentColor) {
        super(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        this.accentColor = accentColor != null ? accentColor : ThemeManager.EMERALD;
        setLayout(new BorderLayout(0, 10));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        titleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        topRow.add(titleLabel, BorderLayout.WEST);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            topRow.add(iconLabel, BorderLayout.EAST);
        }

        valueLabel = new JLabel("--");
        valueLabel.setFont(new Font(ThemeManager.FONT_TITLE.getFamily(), Font.BOLD, 18));
        valueLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 6));
        bottomPanel.setOpaque(false);

        subtitleLabel = new JLabel("Awaiting input");
        subtitleLabel.setFont(ThemeManager.FONT_SMALL);
        subtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        progressBar = new CustomProgressBar();
        progressBar.setProgress(0.0, this.accentColor);

        bottomPanel.add(subtitleLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        add(topRow, BorderLayout.NORTH);
        add(valueLabel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updateMetric(String valueText, String subtitleText, double progressPct) {
        valueLabel.setText(valueText);
        subtitleLabel.setText(subtitleText);
        progressBar.setProgress(progressPct, accentColor);
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
        repaint();
    }
}

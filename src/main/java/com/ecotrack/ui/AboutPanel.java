package com.ecotrack.ui;

import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Module 10 (About): Application overview, calculation methodology, limitations,
 * and verified academic project details.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setBackground(ThemeManager.BG_MAIN);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        StyledCardPanel overviewCard = new StyledCardPanel();
        overviewCard.setLayout(new BorderLayout(0, 10));
        overviewCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        overviewCard.add(ThemeManager.createSectionTitle("EcoTrack – Personal Carbon Footprint Calculator (v1.0.0)"),
                BorderLayout.NORTH);
        JTextArea descArea = createReadOnlyArea(
                "EcoTrack is a standalone, offline Java Swing desktop application built to help individuals estimate "
                        + "their personal greenhouse gas emissions (in kg CO2e) across four daily activity domains: "
                        + "Transportation, Household Electricity, Food Habits, and Waste & Recycling.\n\n"
                        + "All calculations, custom Graphics2D visualizations, deterministic rule-based recommendations, "
                        + "What-If lifestyle simulations, and PDF/TXT report exports run 100% locally inside the JVM without "
                        + "requiring any database, backend server, or internet connection.");
        overviewCard.add(descArea, BorderLayout.CENTER);

        StyledCardPanel methodCard = new StyledCardPanel();
        methodCard.setLayout(new BorderLayout(0, 10));
        methodCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodCard.add(ThemeManager.createSectionTitle("Calculation Methodology & Scientific Honesty"), BorderLayout.NORTH);
        JTextArea methodArea = createReadOnlyArea(
                "Core Equation: Emissions (kg CO2e) = Activity Data x Emission Factor\n\n"
                        + "1. Transportation: Distinguishes tailpipe fuel-combustion modes (Petrol Car, Diesel Car, Motorcycle) "
                        + "from passenger-km transit benchmarks (Bus, Train), grid-coupled Electric Vehicles, and zero-tailpipe active modes (Bicycle, Walking).\n"
                        + "2. Electricity: Uses Monthly kWh x User-Selected or User-Entered Grid Factor (kg CO2e/kWh). "
                        + "Built-in grid presets are clearly labelled as illustrative configurable scenarios, never presented as official national factors.\n"
                        + "3. Food: Uses representative dietary LCA approximations or weekly meal counts, explicitly documented as estimates.\n"
                        + "4. Waste: Cleanly partitions each waste stream into Diverted (recycled/composted) and Disposed mass before applying separate factors so emissions are never double-counted.");
        methodCard.add(methodArea, BorderLayout.CENTER);

        StyledCardPanel teamCard = new StyledCardPanel();
        teamCard.setLayout(new BorderLayout(0, 10));
        teamCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        teamCard.add(ThemeManager.createSectionTitle("Academic Project Details (2026–2027)"), BorderLayout.NORTH);
        JTextArea teamArea = createReadOnlyArea(
                "Institution: RNS Institute of Technology — Department of Computer Science and Engineering, Bengaluru, Karnataka\n"
                        + "Project Guide: Mrs Vinutha H.\n\n"
                        + "Team Members:\n"
                        + "  1. Pavan Chakravarti – 1IB25CS142\n"
                        + "  2. Parikshith R – 1IB25CS140\n"
                        + "  3. Avijeet Singh – 1RN25CS047\n"
                        + "  4. Ashutosh Fobba – 1RN25CS045");
        teamCard.add(teamArea, BorderLayout.CENTER);

        add(overviewCard);
        add(Box.createVerticalStrut(16));
        add(methodCard);
        add(Box.createVerticalStrut(16));
        add(teamCard);
    }

    private JTextArea createReadOnlyArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(ThemeManager.FONT_BODY);
        area.setForeground(ThemeManager.TEXT_SECONDARY);
        return area;
    }
}

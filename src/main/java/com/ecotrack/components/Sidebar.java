package com.ecotrack.components;

import com.ecotrack.utils.IconFactory;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Persistent left navigation sidebar with EcoTrack branding and interactive states.
 */
public class Sidebar extends JPanel {

    public static final String NAV_DASHBOARD = "DASHBOARD";
    public static final String NAV_TRANSPORT = "TRANSPORT";
    public static final String NAV_ELECTRICITY = "ELECTRICITY";
    public static final String NAV_FOOD = "FOOD";
    public static final String NAV_WASTE = "WASTE";
    public static final String NAV_ANALYTICS = "ANALYTICS";
    public static final String NAV_RECOMMENDATIONS = "RECOMMENDATIONS";
    public static final String NAV_SIMULATOR = "SIMULATOR";
    public static final String NAV_SETTINGS = "SETTINGS";
    public static final String NAV_ABOUT = "ABOUT";

    private final Map<String, NavButton> buttons = new LinkedHashMap<>();
    private final Consumer<String> onNavigate;
    private String activeKey = NAV_DASHBOARD;

    public Sidebar(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setBackground(ThemeManager.BG_SIDEBAR);
        setPreferredSize(new Dimension(240, 720));
        setMinimumSize(new Dimension(220, 500));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER_SUBTLE));
        setLayout(new BorderLayout());

        // Brand block
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 18, 20));

        JLabel logoIcon = new JLabel(IconFactory.create(IconFactory.Kind.LEAF, 28, ThemeManager.EMERALD));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel appName = new JLabel("EcoTrack");
        appName.setFont(new Font(ThemeManager.FONT_TITLE.getFamily(), Font.BOLD, 20));
        appName.setForeground(ThemeManager.TEXT_PRIMARY);
        JLabel appTag = new JLabel("Carbon Calculator");
        appTag.setFont(ThemeManager.FONT_SMALL);
        appTag.setForeground(ThemeManager.EMERALD);
        brandText.add(appName);
        brandText.add(appTag);

        brandPanel.add(logoIcon, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);

        // Navigation items
        JPanel navList = new JPanel();
        navList.setOpaque(false);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));

        addSectionHeader(navList, "OVERVIEW");
        addNavItem(navList, NAV_DASHBOARD, "Dashboard", IconFactory.Kind.DASHBOARD);

        addSectionHeader(navList, "CALCULATORS");
        addNavItem(navList, NAV_TRANSPORT, "Transportation", IconFactory.Kind.TRANSPORT);
        addNavItem(navList, NAV_ELECTRICITY, "Electricity", IconFactory.Kind.ELECTRICITY);
        addNavItem(navList, NAV_FOOD, "Food Habits", IconFactory.Kind.FOOD);
        addNavItem(navList, NAV_WASTE, "Waste & Recycling", IconFactory.Kind.WASTE);

        addSectionHeader(navList, "INSIGHTS & TOOLS");
        addNavItem(navList, NAV_ANALYTICS, "Analytics Charts", IconFactory.Kind.ANALYTICS);
        addNavItem(navList, NAV_RECOMMENDATIONS, "Recommendations", IconFactory.Kind.RECOMMENDATIONS);
        addNavItem(navList, NAV_SIMULATOR, "What-If Simulator", IconFactory.Kind.SIMULATOR);

        navList.add(Box.createVerticalGlue());
        addSectionHeader(navList, "SYSTEM");
        addNavItem(navList, NAV_SETTINGS, "Settings & Factors", IconFactory.Kind.SETTINGS);
        addNavItem(navList, NAV_ABOUT, "About & Methodology", IconFactory.Kind.ABOUT);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 18, 14, 18)));
        JLabel versionLabel = new JLabel("v1.0.0  |  Session In-Memory");
        versionLabel.setFont(ThemeManager.FONT_SMALL);
        versionLabel.setForeground(ThemeManager.TEXT_MUTED);
        footerPanel.add(versionLabel, BorderLayout.CENTER);

        add(brandPanel, BorderLayout.NORTH);
        add(navList, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setActiveItem(NAV_DASHBOARD);
    }

    private void addSectionHeader(JPanel container, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(ThemeManager.FONT_SMALL.getFamily(), Font.BOLD, 10));
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(lbl);
    }

    private void addNavItem(JPanel container, String key, String label, IconFactory.Kind iconKind) {
        NavButton btn = new NavButton(key, label, iconKind);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttons.put(key, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(3));
    }

    public void setActiveItem(String key) {
        this.activeKey = key;
        for (Map.Entry<String, NavButton> entry : buttons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(key));
        }
    }

    public String getActiveKey() {
        return activeKey;
    }

    private class NavButton extends JPanel {
        private final String key;
        private final JLabel textLabel;
        private final JLabel iconLabel;
        private final IconFactory.Kind iconKind;
        private boolean active = false;
        private boolean hovered = false;

        NavButton(String key, String text, IconFactory.Kind iconKind) {
            this.key = key;
            this.iconKind = iconKind;
            setOpaque(false);
            setLayout(new BorderLayout(10, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setPreferredSize(new Dimension(210, 38));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(IconFactory.create(iconKind, 17, ThemeManager.TEXT_SECONDARY));
            textLabel = new JLabel(text);
            textLabel.setFont(ThemeManager.FONT_BODY);
            textLabel.setForeground(ThemeManager.TEXT_SECONDARY);

            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    setActiveItem(NavButton.this.key);
                    if (onNavigate != null) {
                        onNavigate.accept(NavButton.this.key);
                    }
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            Color fg = active ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_SECONDARY;
            Color ic = active ? ThemeManager.EMERALD : ThemeManager.TEXT_SECONDARY;
            textLabel.setForeground(fg);
            textLabel.setFont(active ? ThemeManager.FONT_BODY_BOLD : ThemeManager.FONT_BODY);
            iconLabel.setIcon(IconFactory.create(iconKind, 17, ic));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (active) {
                g2.setColor(ThemeManager.EMERALD_TINT);
                g2.fillRoundRect(0, 0, w, h, 9, 9);
                g2.setColor(ThemeManager.EMERALD);
                g2.fillRoundRect(0, 6, 4, h - 12, 4, 4);
            } else if (hovered) {
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRoundRect(0, 0, w, h, 9, 9);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

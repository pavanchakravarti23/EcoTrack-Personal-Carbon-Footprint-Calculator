package com.ecotrack.components;

import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Custom Graphics2D horizontal progress/share indicator bar with rounded ends.
 */
public class CustomProgressBar extends JComponent {

    private double value = 0.0; // 0 to 100
    private Color fillColor = ThemeManager.EMERALD;

    public CustomProgressBar() {
        setPreferredSize(new Dimension(80, 10));
        setMinimumSize(new Dimension(40, 10));
        setOpaque(false);
    }

    public void setProgress(double percentage, Color color) {
        this.value = Math.max(0.0, Math.min(100.0, percentage));
        if (color != null) {
            this.fillColor = color;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int arc = h;

        g2.setColor(ThemeManager.BG_INPUT);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        if (value > 0.0) {
            int fillW = Math.max(h, (int) Math.round((w * value) / 100.0));
            fillW = Math.min(w, fillW);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, fillW, h, arc, arc);
        }
        g2.dispose();
    }
}

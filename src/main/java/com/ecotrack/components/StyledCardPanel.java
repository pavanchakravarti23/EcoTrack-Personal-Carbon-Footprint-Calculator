package com.ecotrack.components;

import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

/**
 * Reusable rounded card container with subtle border and dark-card surface.
 */
public class StyledCardPanel extends JPanel {

    private final int arcRadius;
    private Color cardColor;
    private Color borderColor;

    public StyledCardPanel() {
        this(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
    }

    public StyledCardPanel(Color cardColor, Color borderColor, int arcRadius) {
        this.cardColor = cardColor;
        this.borderColor = borderColor;
        this.arcRadius = arcRadius;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
    }

    public void setCardColor(Color cardColor) {
        this.cardColor = cardColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(cardColor);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arcRadius, arcRadius);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arcRadius, arcRadius);
        g2.dispose();
        super.paintComponent(g);
    }
}

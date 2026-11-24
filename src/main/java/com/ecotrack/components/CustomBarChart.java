package com.ecotrack.components;

import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom Java Graphics2D vertical bar chart with dynamic Y-axis scaling,
 * gridlines, units, interactive hover tooltips, and empty states.
 */
public class CustomBarChart extends StyledCardPanel {

    public static class BarEntry {
        final String label;
        final double value;
        final Color color;
        Rectangle2D.Double hitBox;

        public BarEntry(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final String title;
    private String subtitle = "Unit: kg CO2e";
    private final List<BarEntry> entries = new ArrayList<>();
    private boolean hasAnyData = false;
    private int hoveredIndex = -1;
    private Point hoverPoint = null;

    public CustomBarChart(String title) {
        super(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        this.title = title;
        setPreferredSize(new Dimension(420, 290));
        setMinimumSize(new Dimension(280, 240));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPoint = e.getPoint();
                hoveredIndex = -1;
                for (int i = 0; i < entries.size(); i++) {
                    Rectangle2D.Double box = entries.get(i).hitBox;
                    if (box != null && box.contains(e.getPoint())) {
                        hoveredIndex = i;
                        break;
                    }
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                hoverPoint = null;
                repaint();
            }
        };
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }

    public void setCategoryData(CarbonResult result) {
        entries.clear();
        hoveredIndex = -1;
        if (result == null || !result.isHasAnyData()) {
            hasAnyData = false;
            subtitle = "Unit: kg CO2e";
            repaint();
            return;
        }
        hasAnyData = true;
        subtitle = "Unit: kg CO2e (" + result.getPeriod().getDisplayName() + ")";
        for (EmissionCategory cat : EmissionCategory.values()) {
            String shortName = switch (cat) {
                case TRANSPORT -> "Transport";
                case ELECTRICITY -> "Electricity";
                case FOOD -> "Food";
                case WASTE -> "Waste";
            };
            entries.add(new BarEntry(shortName, result.getEmission(cat), ThemeManager.getCategoryColor(cat)));
        }
        repaint();
    }

    public void setSeriesData(String subtitleText, Map<String, Double> series, Color[] palette) {
        entries.clear();
        hoveredIndex = -1;
        this.subtitle = subtitleText;
        if (series == null || series.isEmpty()) {
            hasAnyData = false;
            repaint();
            return;
        }
        hasAnyData = true;
        int idx = 0;
        for (Map.Entry<String, Double> e : series.entrySet()) {
            Color c = (palette != null && palette.length > 0)
                    ? palette[idx % palette.length]
                    : ThemeManager.EMERALD;
            entries.add(new BarEntry(e.getKey(), e.getValue(), c));
            idx++;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setFont(ThemeManager.FONT_SUBTITLE);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        g2.drawString(title, 20, 32);

        g2.setFont(ThemeManager.FONT_SMALL);
        g2.setColor(ThemeManager.TEXT_SECONDARY);
        g2.drawString(subtitle, 20, 49);

        if (!hasAnyData || entries.isEmpty()) {
            drawEmptyState(g2, w, h);
            g2.dispose();
            return;
        }

        double maxVal = 0.0;
        for (BarEntry entry : entries) {
            maxVal = Math.max(maxVal, entry.value);
        }
        double niceMax = computeNiceMax(maxVal <= 0.0 ? 10.0 : maxVal);

        int leftPad = 62;
        int rightPad = 24;
        int topPad = 70;
        int bottomPad = 48;
        int plotW = Math.max(80, w - leftPad - rightPad);
        int plotH = Math.max(60, h - topPad - bottomPad);

        // Gridlines and Y-axis ticks
        int ticks = 4;
        g2.setFont(ThemeManager.FONT_SMALL);
        for (int i = 0; i <= ticks; i++) {
            double frac = i / (double) ticks;
            int gy = topPad + plotH - (int) Math.round(plotH * frac);
            g2.setColor(ThemeManager.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(leftPad, gy, leftPad + plotW, gy);

            double tickVal = niceMax * frac;
            String tickLbl = NumberFormatter.formatNumber(tickVal);
            if (tickVal >= 1000) {
                tickLbl = String.format("%.0f", tickVal);
            }
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.drawString(tickLbl, leftPad - fm.stringWidth(tickLbl) - 8, gy + 4);
        }

        int n = entries.size();
        int slotW = plotW / n;
        int barW = Math.max(22, Math.min(58, (int) (slotW * 0.52)));

        for (int i = 0; i < n; i++) {
            BarEntry entry = entries.get(i);
            int cx = leftPad + i * slotW + slotW / 2;
            int bx = cx - barW / 2;
            int bh = (int) Math.round((entry.value / niceMax) * plotH);
            bh = Math.max(entry.value > 0 ? 4 : 0, bh);
            int by = topPad + plotH - bh;

            entry.hitBox = new Rectangle2D.Double(bx - 6, topPad, barW + 12, plotH + 20);

            Color drawColor = (i == hoveredIndex) ? entry.color.brighter() : entry.color;
            g2.setColor(drawColor);
            if (bh > 0) {
                g2.fillRoundRect(bx, by, barW, bh, 8, 8);
                if (bh > 8) {
                    g2.fillRect(bx, by + bh - 6, barW, 6);
                }
            }

            // Value above bar
            String valStr = NumberFormatter.formatNumber(entry.value);
            g2.setFont(ThemeManager.FONT_SMALL_BOLD);
            g2.setColor(ThemeManager.TEXT_PRIMARY);
            FontMetrics fmVal = g2.getFontMetrics();
            g2.drawString(valStr, cx - fmVal.stringWidth(valStr) / 2, Math.max(topPad - 4, by - 6));

            // X-axis category label (ellipsized if needed)
            String lbl = ellipsize(entry.label, g2.getFontMetrics(ThemeManager.FONT_SMALL), slotW - 8);
            g2.setFont(ThemeManager.FONT_SMALL);
            g2.setColor(i == hoveredIndex ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_SECONDARY);
            FontMetrics fmLbl = g2.getFontMetrics();
            g2.drawString(lbl, cx - fmLbl.stringWidth(lbl) / 2, topPad + plotH + 20);
        }

        if (hoveredIndex >= 0 && hoveredIndex < entries.size() && hoverPoint != null) {
            BarEntry hovered = entries.get(hoveredIndex);
            String tip = hovered.label + ": " + NumberFormatter.formatKg(hovered.value);
            drawTooltip(g2, tip, hoverPoint.x, hoverPoint.y, w);
        }

        g2.dispose();
    }

    private double computeNiceMax(double rawMax) {
        double exp = Math.pow(10, Math.floor(Math.log10(rawMax)));
        double frac = rawMax / exp;
        double niceFrac;
        if (frac <= 1.2) niceFrac = 1.2;
        else if (frac <= 1.5) niceFrac = 1.5;
        else if (frac <= 2.0) niceFrac = 2.0;
        else if (frac <= 3.0) niceFrac = 3.0;
        else if (frac <= 5.0) niceFrac = 5.0;
        else if (frac <= 8.0) niceFrac = 8.0;
        else niceFrac = 10.0;
        return niceFrac * exp;
    }

    private String ellipsize(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ell = "...";
        int limit = text.length();
        while (limit > 2 && fm.stringWidth(text.substring(0, limit) + ell) > maxWidth) {
            limit--;
        }
        return text.substring(0, limit) + ell;
    }

    private void drawEmptyState(Graphics2D g2, int w, int h) {
        int cy = h / 2 + 12;
        g2.setFont(ThemeManager.FONT_BODY_BOLD);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        String msg = "No chart data available";
        g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, cy);
        g2.setFont(ThemeManager.FONT_SMALL);
        g2.setColor(ThemeManager.TEXT_SECONDARY);
        String sub = "Calculate any module to populate bar comparisons.";
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, cy + 20);
    }

    private void drawTooltip(Graphics2D g2, String text, int mx, int my, int maxW) {
        g2.setFont(ThemeManager.FONT_SMALL_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(text) + 18;
        int bh = 26;
        int bx = Math.min(maxW - bw - 10, mx + 12);
        int by = Math.max(10, my - bh - 8);
        if (bx < 10) bx = 10;
        g2.setColor(ThemeManager.BG_MAIN);
        g2.fillRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(ThemeManager.EMERALD_DARK);
        g2.drawRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        g2.drawString(text, bx + 9, by + 17);
    }
}

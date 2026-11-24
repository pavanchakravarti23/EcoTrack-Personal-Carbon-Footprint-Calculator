package com.ecotrack.components;

import com.ecotrack.model.CarbonResult;
import com.ecotrack.model.EmissionCategory;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Java Graphics2D Donut/Pie Chart displaying category-wise carbon footprint
 * shares with hover tooltips, legend, percentage labels, and empty-state guidance.
 */
public class CustomPieChart extends StyledCardPanel {

    private static class Slice {
        final EmissionCategory category;
        final double valueKg;
        final double percentage;
        final Color color;
        double startAngle;
        double extentAngle;

        Slice(EmissionCategory category, double valueKg, double percentage, Color color) {
            this.category = category;
            this.valueKg = valueKg;
            this.percentage = percentage;
            this.color = color;
        }
    }

    private final String title;
    private final List<Slice> slices = new ArrayList<>();
    private double totalKg = 0.0;
    private String periodLabel = "Monthly";
    private boolean hasAnyData = false;
    private int hoveredIndex = -1;
    private Point hoverPoint = null;
    private int lastCenterX;
    private int lastCenterY;
    private int lastOuterRadius;
    private int lastInnerRadius;

    public CustomPieChart(String title) {
        super(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        this.title = title;
        setPreferredSize(new Dimension(380, 290));
        setMinimumSize(new Dimension(280, 240));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPoint = e.getPoint();
                hoveredIndex = hitTest(e.getX(), e.getY());
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

    public void setCarbonResult(CarbonResult result) {
        slices.clear();
        hoveredIndex = -1;
        if (result == null || !result.isHasAnyData()) {
            this.hasAnyData = false;
            this.totalKg = 0.0;
            repaint();
            return;
        }

        this.hasAnyData = true;
        this.totalKg = result.getTotalPeriodEmissionsKg();
        this.periodLabel = result.getPeriod().getDisplayName();

        double angleCursor = 90.0;
        for (EmissionCategory cat : EmissionCategory.values()) {
            double val = result.getEmission(cat);
            if (val > 0.0 && totalKg > 0.0) {
                double pct = (val / totalKg) * 100.0;
                double extent = -(val / totalKg) * 360.0;
                Slice slice = new Slice(cat, val, pct, ThemeManager.getCategoryColor(cat));
                slice.startAngle = angleCursor;
                slice.extentAngle = extent;
                slices.add(slice);
                angleCursor += extent;
            }
        }
        repaint();
    }

    private int hitTest(int mx, int my) {
        if (slices.isEmpty() || lastOuterRadius <= 0) {
            return -1;
        }
        double dx = mx - lastCenterX;
        double dy = my - lastCenterY;
        double dist = Math.hypot(dx, dy);
        if (dist < lastInnerRadius || dist > lastOuterRadius + 8) {
            return -1;
        }
        // Convert screen vector to Java Arc2D angle (0 = East, 90 = North, counter-clockwise positive)
        double deg = Math.toDegrees(Math.atan2(-dy, dx));
        if (deg < 0) {
            deg += 360.0;
        }
        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            // extentAngle is negative (clockwise sweep from startAngle)
            double start = normalizeDeg(s.startAngle);
            double sweep = -s.extentAngle; // positive clockwise angle
            double delta = normalizeDeg(start - deg);
            if (delta >= 0.0 && delta <= sweep) {
                return i;
            }
        }
        return -1;
    }

    private double normalizeDeg(double a) {
        double r = a % 360.0;
        return r < 0 ? r + 360.0 : r;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Title
        g2.setFont(ThemeManager.FONT_SUBTITLE);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        g2.drawString(title, 20, 32);

        g2.setFont(ThemeManager.FONT_SMALL);
        g2.setColor(ThemeManager.TEXT_SECONDARY);
        g2.drawString("Unit: kg CO2e (" + periodLabel + ")", 20, 49);

        if (!hasAnyData) {
            drawEmptyState(g2, w, h, "No emissions calculated yet",
                    "Enter values in any calculator module to see category share.");
            g2.dispose();
            return;
        }

        if (totalKg <= 0.0 || slices.isEmpty()) {
            drawEmptyState(g2, w, h, "0.00 kg CO2e Recorded",
                    "Current entered activities produce zero direct emissions.");
            g2.dispose();
            return;
        }

        int chartAreaW = (int) (w * 0.56);
        int availH = h - 70;
        int diameter = Math.max(100, Math.min(chartAreaW - 36, availH - 20));
        lastOuterRadius = diameter / 2;
        lastInnerRadius = (int) (lastOuterRadius * 0.56);
        lastCenterX = 20 + (chartAreaW - 20) / 2;
        lastCenterY = 62 + availH / 2;

        // Draw slices
        for (int i = 0; i < slices.size(); i++) {
            Slice slice = slices.get(i);
            boolean isHovered = (i == hoveredIndex);
            int rad = isHovered ? lastOuterRadius + 6 : lastOuterRadius;
            Arc2D.Double arc = new Arc2D.Double(
                    lastCenterX - rad,
                    lastCenterY - rad,
                    rad * 2.0,
                    rad * 2.0,
                    slice.startAngle,
                    slice.extentAngle,
                    Arc2D.PIE);
            g2.setColor(slice.color);
            g2.fill(arc);
            g2.setColor(ThemeManager.BG_CARD);
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(arc);
        }

        // Donut hole
        Ellipse2D.Double hole = new Ellipse2D.Double(
                lastCenterX - lastInnerRadius,
                lastCenterY - lastInnerRadius,
                lastInnerRadius * 2.0,
                lastInnerRadius * 2.0);
        g2.setColor(ThemeManager.BG_CARD);
        g2.fill(hole);

        // Center total readout
        String totalText = NumberFormatter.formatKgCompact(totalKg);
        g2.setFont(new Font(ThemeManager.FONT_TITLE.getFamily(), Font.BOLD, 17));
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        FontMetrics fmVal = g2.getFontMetrics();
        g2.drawString(totalText, lastCenterX - fmVal.stringWidth(totalText) / 2, lastCenterY + 2);

        g2.setFont(ThemeManager.FONT_SMALL);
        g2.setColor(ThemeManager.TEXT_SECONDARY);
        String unitSub = totalText.endsWith("t") ? "tonnes CO2e" : "kg CO2e";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(unitSub, lastCenterX - fmSub.stringWidth(unitSub) / 2, lastCenterY + 19);

        // Legend on the right
        int legendX = Math.max(chartAreaW + 8, lastCenterX + lastOuterRadius + 22);
        int legendY = 78;
        int rowH = 40;
        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            int ry = legendY + i * rowH;
            if (ry + 30 > h - 10) {
                break;
            }
            if (i == hoveredIndex) {
                g2.setColor(ThemeManager.BG_CARD_ALT);
                g2.fillRoundRect(legendX - 8, ry - 6, Math.max(80, w - legendX - 12), 34, 8, 8);
            }
            g2.setColor(s.color);
            g2.fillRoundRect(legendX, ry + 2, 11, 11, 4, 4);

            g2.setFont(ThemeManager.FONT_SMALL_BOLD);
            g2.setColor(ThemeManager.TEXT_PRIMARY);
            g2.drawString(s.category.getDisplayName(), legendX + 18, ry + 12);

            g2.setFont(ThemeManager.FONT_SMALL);
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            g2.drawString(NumberFormatter.formatKg(s.valueKg) + " (" + NumberFormatter.formatPercent(s.percentage) + ")",
                    legendX + 18, ry + 26);
        }

        // Hover tooltip
        if (hoveredIndex >= 0 && hoveredIndex < slices.size() && hoverPoint != null) {
            Slice s = slices.get(hoveredIndex);
            String tip = s.category.getDisplayName() + ": "
                    + NumberFormatter.formatKg(s.valueKg) + " (" + NumberFormatter.formatPercent(s.percentage) + ")";
            drawTooltip(g2, tip, hoverPoint.x, hoverPoint.y, w, h);
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int w, int h, String heading, String sub) {
        int cy = h / 2 + 10;
        g2.setColor(ThemeManager.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5f, 5f}, 0));
        g2.drawOval(w / 2 - 42, cy - 58, 84, 84);

        g2.setFont(ThemeManager.FONT_BODY_BOLD);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(heading, (w - fm1.stringWidth(heading)) / 2, cy + 48);

        g2.setFont(ThemeManager.FONT_SMALL);
        g2.setColor(ThemeManager.TEXT_SECONDARY);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, cy + 66);
    }

    private void drawTooltip(Graphics2D g2, String text, int mx, int my, int maxW, int maxH) {
        g2.setFont(ThemeManager.FONT_SMALL_BOLD);
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(text) + 18;
        int bh = 26;
        int bx = Math.min(maxW - bw - 10, mx + 12);
        int by = Math.max(10, my - bh - 8);
        if (bx < 10) {
            bx = 10;
        }
        g2.setColor(ThemeManager.BG_MAIN);
        g2.fillRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(ThemeManager.EMERALD_DARK);
        g2.drawRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(ThemeManager.TEXT_PRIMARY);
        g2.drawString(text, bx + 9, by + 17);
    }
}

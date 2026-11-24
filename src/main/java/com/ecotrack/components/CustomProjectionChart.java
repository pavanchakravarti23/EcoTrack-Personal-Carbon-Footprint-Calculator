package com.ecotrack.components;

import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Java Graphics2D 12-month cumulative annual projection chart comparing
 * the user's baseline trajectory against a 20% reduction pathway.
 */
public class CustomProjectionChart extends StyledCardPanel {

    private final String title;
    private List<Double> baselinePoints = new ArrayList<>();
    private List<Double> reducedPoints = new ArrayList<>();
    private boolean hasAnyData = false;
    private int hoveredMonthIndex = -1;
    private int lastLeftPad;
    private int lastPlotW;

    public CustomProjectionChart(String title) {
        super(ThemeManager.BG_CARD, ThemeManager.BORDER_SUBTLE, 14);
        this.title = title;
        setPreferredSize(new Dimension(560, 290));
        setMinimumSize(new Dimension(320, 240));

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!hasAnyData || baselinePoints.isEmpty() || lastPlotW <= 0) {
                    hoveredMonthIndex = -1;
                    return;
                }
                int relX = e.getX() - lastLeftPad;
                if (relX < -10 || relX > lastPlotW + 10) {
                    hoveredMonthIndex = -1;
                } else {
                    double frac = Math.max(0.0, Math.min(1.0, relX / (double) lastPlotW));
                    hoveredMonthIndex = (int) Math.round(frac * (baselinePoints.size() - 1));
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredMonthIndex = -1;
                repaint();
            }
        };
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }

    public void setProjectionData(boolean hasAnyData, List<Double> baseline, List<Double> reduced) {
        this.hasAnyData = hasAnyData;
        this.baselinePoints = baseline != null ? new ArrayList<>(baseline) : new ArrayList<>();
        this.reducedPoints = reduced != null ? new ArrayList<>(reduced) : new ArrayList<>();
        this.hoveredMonthIndex = -1;
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
        g2.drawString("12-Month Cumulative Trajectory (kg CO2e)", 20, 49);

        // Legend top-right
        int legX = w - 290;
        if (legX > 250) {
            g2.setColor(ThemeManager.COLOR_FOOD);
            g2.fillRect(legX, 24, 14, 3);
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            g2.drawString("Current Pace", legX + 20, 29);

            g2.setColor(ThemeManager.EMERALD);
            g2.fillRect(legX + 120, 24, 14, 3);
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            g2.drawString("20% Reduction", legX + 140, 29);
        }

        if (!hasAnyData || baselinePoints.isEmpty()) {
            int cy = h / 2 + 12;
            g2.setFont(ThemeManager.FONT_BODY_BOLD);
            g2.setColor(ThemeManager.TEXT_PRIMARY);
            String msg = "No annual projection available";
            g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, cy);
            g2.setFont(ThemeManager.FONT_SMALL);
            g2.setColor(ThemeManager.TEXT_SECONDARY);
            String sub = "Complete at least one calculator module to project 12-month emissions.";
            g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, cy + 20);
            g2.dispose();
            return;
        }

        double maxVal = 10.0;
        for (Double d : baselinePoints) {
            maxVal = Math.max(maxVal, d);
        }
        maxVal *= 1.12;

        lastLeftPad = 66;
        int rightPad = 24;
        int topPad = 72;
        int bottomPad = 44;
        lastPlotW = Math.max(100, w - lastLeftPad - rightPad);
        int plotH = Math.max(70, h - topPad - bottomPad);

        // Gridlines
        int ticks = 4;
        g2.setFont(ThemeManager.FONT_SMALL);
        for (int i = 0; i <= ticks; i++) {
            double frac = i / (double) ticks;
            int gy = topPad + plotH - (int) Math.round(plotH * frac);
            g2.setColor(ThemeManager.BORDER_SUBTLE);
            g2.drawLine(lastLeftPad, gy, lastLeftPad + lastPlotW, gy);
            String lbl = String.format("%.0f", maxVal * frac);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.drawString(lbl, lastLeftPad - fm.stringWidth(lbl) - 8, gy + 4);
        }

        int count = baselinePoints.size();
        int[] xs = new int[count];
        int[] ysBase = new int[count];
        int[] ysRed = new int[count];

        for (int i = 0; i < count; i++) {
            xs[i] = lastLeftPad + (int) Math.round((i / (double) (count - 1)) * lastPlotW);
            ysBase[i] = topPad + plotH - (int) Math.round((baselinePoints.get(i) / maxVal) * plotH);
            double redVal = (i < reducedPoints.size()) ? reducedPoints.get(i) : 0.0;
            ysRed[i] = topPad + plotH - (int) Math.round((redVal / maxVal) * plotH);

            String mLbl = "M" + (i + 1);
            g2.setColor(i == hoveredMonthIndex ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(mLbl, xs[i] - fm.stringWidth(mLbl) / 2, topPad + plotH + 20);
        }

        // Translucent fill under 20% reduction curve
        Path2D.Double area = new Path2D.Double();
        area.moveTo(xs[0], topPad + plotH);
        for (int i = 0; i < count; i++) {
            area.lineTo(xs[i], ysRed[i]);
        }
        area.lineTo(xs[count - 1], topPad + plotH);
        area.closePath();
        g2.setColor(new Color(0x23, 0xC4, 0x7E, 36));
        g2.fill(area);

        // Baseline curve
        g2.setColor(ThemeManager.COLOR_FOOD);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < count - 1; i++) {
            g2.drawLine(xs[i], ysBase[i], xs[i + 1], ysBase[i + 1]);
        }

        // Reduced curve (dashed)
        g2.setColor(ThemeManager.EMERALD);
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[]{7f, 5f}, 0));
        for (int i = 0; i < count - 1; i++) {
            g2.drawLine(xs[i], ysRed[i], xs[i + 1], ysRed[i + 1]);
        }

        // Data points
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < count; i++) {
            int r = (i == hoveredMonthIndex) ? 5 : 3;
            g2.setColor(ThemeManager.COLOR_FOOD);
            g2.fillOval(xs[i] - r, ysBase[i] - r, r * 2, r * 2);
            g2.setColor(ThemeManager.EMERALD);
            g2.fillOval(xs[i] - r, ysRed[i] - r, r * 2, r * 2);
        }

        // Hover vertical guide & readout
        if (hoveredMonthIndex >= 0 && hoveredMonthIndex < count) {
            int hx = xs[hoveredMonthIndex];
            g2.setColor(new Color(0x94, 0xA3, 0x9A, 90));
            g2.drawLine(hx, topPad, hx, topPad + plotH);

            double baseV = baselinePoints.get(hoveredMonthIndex);
            double redV = reducedPoints.get(hoveredMonthIndex);
            String tip = "Month " + (hoveredMonthIndex + 1)
                    + " | Pace: " + NumberFormatter.formatKg(baseV)
                    + " | -20%: " + NumberFormatter.formatKg(redV);
            g2.setFont(ThemeManager.FONT_SMALL_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(tip) + 18;
            int bx = Math.min(w - bw - 14, Math.max(14, hx - bw / 2));
            int by = topPad - 2;
            g2.setColor(ThemeManager.BG_MAIN);
            g2.fillRoundRect(bx, by, bw, 24, 8, 8);
            g2.setColor(ThemeManager.EMERALD_DARK);
            g2.drawRoundRect(bx, by, bw, 24, 8, 8);
            g2.setColor(ThemeManager.TEXT_PRIMARY);
            g2.drawString(tip, bx + 9, by + 16);
        }

        g2.dispose();
    }
}

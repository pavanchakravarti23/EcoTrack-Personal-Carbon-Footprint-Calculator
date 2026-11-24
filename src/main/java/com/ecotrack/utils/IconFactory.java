package com.ecotrack.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Vector Graphics2D icon factory so navigation and action controls render crisply
 * on all DPI settings without relying on external image assets or OS-specific emoji fonts.
 */
public final class IconFactory {

    public enum Kind {
        LEAF,
        DASHBOARD,
        TRANSPORT,
        ELECTRICITY,
        FOOD,
        WASTE,
        ANALYTICS,
        RECOMMENDATIONS,
        SIMULATOR,
        SETTINGS,
        ABOUT,
        EXPORT
    }

    private IconFactory() {
    }

    public static Icon create(Kind kind, int size, Color color) {
        return new VectorIcon(kind, size, color);
    }

    private static class VectorIcon implements Icon {
        private final Kind kind;
        private final int size;
        private final Color color;

        VectorIcon(Kind kind, int size, Color color) {
            this.kind = kind;
            this.size = size;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            float s = size / 24.0f;
            g2.scale(s, s);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (kind) {
                case LEAF -> {
                    Path2D leaf = new Path2D.Float();
                    leaf.moveTo(5, 19);
                    leaf.curveTo(5, 9, 11, 4, 20, 4);
                    leaf.curveTo(20, 13, 15, 19, 5, 19);
                    leaf.closePath();
                    g2.draw(leaf);
                    g2.drawLine(5, 19, 14, 10);
                }
                case DASHBOARD -> {
                    g2.drawRoundRect(3, 3, 7, 8, 2, 2);
                    g2.drawRoundRect(14, 3, 7, 5, 2, 2);
                    g2.drawRoundRect(14, 12, 7, 9, 2, 2);
                    g2.drawRoundRect(3, 15, 7, 6, 2, 2);
                }
                case TRANSPORT -> {
                    g2.drawRoundRect(3, 8, 18, 8, 3, 3);
                    g2.drawLine(6, 8, 8, 4);
                    g2.drawLine(18, 8, 16, 4);
                    g2.drawLine(8, 4, 16, 4);
                    g2.fillOval(5, 15, 4, 4);
                    g2.fillOval(15, 15, 4, 4);
                }
                case ELECTRICITY -> {
                    Path2D bolt = new Path2D.Float();
                    bolt.moveTo(13, 2);
                    bolt.lineTo(5, 13);
                    bolt.lineTo(12, 13);
                    bolt.lineTo(11, 22);
                    bolt.lineTo(19, 11);
                    bolt.lineTo(12, 11);
                    bolt.closePath();
                    g2.draw(bolt);
                }
                case FOOD -> {
                    g2.drawLine(7, 3, 7, 21);
                    g2.drawLine(4, 3, 4, 9);
                    g2.drawLine(10, 3, 10, 9);
                    g2.drawLine(4, 9, 10, 9);
                    g2.drawOval(14, 4, 6, 8);
                    g2.drawLine(17, 12, 17, 21);
                }
                case WASTE -> {
                    g2.drawLine(4, 7, 20, 7);
                    g2.drawLine(9, 4, 15, 4);
                    g2.drawRoundRect(6, 7, 12, 14, 2, 2);
                    g2.drawLine(10, 11, 10, 17);
                    g2.drawLine(14, 11, 14, 17);
                }
                case ANALYTICS -> {
                    g2.drawLine(3, 20, 21, 20);
                    g2.drawLine(3, 4, 3, 20);
                    g2.fillRoundRect(6, 12, 3, 8, 1, 1);
                    g2.fillRoundRect(11, 7, 3, 13, 1, 1);
                    g2.fillRoundRect(16, 10, 3, 10, 1, 1);
                }
                case RECOMMENDATIONS -> {
                    g2.drawOval(7, 3, 10, 10);
                    g2.drawLine(9, 15, 15, 15);
                    g2.drawLine(10, 19, 14, 19);
                }
                case SIMULATOR -> {
                    g2.drawLine(4, 7, 20, 7);
                    g2.drawLine(4, 17, 20, 17);
                    g2.fillOval(12, 4, 6, 6);
                    g2.fillOval(6, 14, 6, 6);
                }
                case SETTINGS -> {
                    g2.drawOval(8, 8, 8, 8);
                    g2.drawOval(3, 3, 18, 18);
                }
                case ABOUT -> {
                    g2.drawOval(3, 3, 18, 18);
                    g2.drawLine(12, 11, 12, 17);
                    g2.fillOval(11, 7, 2, 2);
                }
                case EXPORT -> {
                    g2.drawRoundRect(4, 3, 16, 18, 2, 2);
                    g2.drawLine(8, 9, 16, 9);
                    g2.drawLine(8, 13, 16, 13);
                    g2.drawLine(8, 17, 13, 17);
                }
            }
            g2.dispose();
        }
    }
}

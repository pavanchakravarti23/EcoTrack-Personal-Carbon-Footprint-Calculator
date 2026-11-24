package com.ecotrack.components;

import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom-painted Swing button supporting PRIMARY (emerald), SECONDARY (card/outline),
 * and GHOST variants with smooth hover, pressed, and disabled states.
 */
public class CustomButton extends JButton {

    public enum Variant {
        PRIMARY,
        SECONDARY,
        DANGER
    }

    private final Variant variant;
    private boolean hovered = false;
    private boolean pressed = false;

    public CustomButton(String text) {
        this(text, Variant.PRIMARY);
    }

    public CustomButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(ThemeManager.FONT_BODY_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(9, 18, 9, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        Color bg;
        Color fg;
        Color border = null;

        if (!isEnabled()) {
            bg = ThemeManager.BG_CARD_ALT;
            fg = ThemeManager.TEXT_MUTED;
            border = ThemeManager.BORDER_SUBTLE;
        } else {
            switch (variant) {
                case PRIMARY -> {
                    bg = pressed ? ThemeManager.EMERALD_DARK
                            : (hovered ? ThemeManager.EMERALD_HOVER : ThemeManager.EMERALD);
                    fg = new Color(0x09, 0x18, 0x11);
                }
                case DANGER -> {
                    bg = pressed ? new Color(0x9A, 0x3D, 0x45)
                            : (hovered ? new Color(0x45, 0x22, 0x26) : new Color(0x36, 0x1D, 0x20));
                    fg = ThemeManager.STATUS_ERROR;
                    border = new Color(0x70, 0x34, 0x3A);
                }
                default -> {
                    bg = pressed ? ThemeManager.BG_MAIN
                            : (hovered ? new Color(0x2B, 0x37, 0x32) : ThemeManager.BG_CARD_ALT);
                    fg = ThemeManager.TEXT_PRIMARY;
                    border = hovered ? ThemeManager.EMERALD_DARK : ThemeManager.BORDER_SUBTLE;
                }
            }
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int iconWidth = getIcon() != null ? getIcon().getIconWidth() + getIconTextGap() : 0;
        int textWidth = fm.stringWidth(getText());
        int totalWidth = iconWidth + textWidth;
        int startX = (w - totalWidth) / 2;

        if (getIcon() != null) {
            int iconY = (h - getIcon().getIconHeight()) / 2;
            getIcon().paintIcon(this, g2, startX, iconY);
            startX += iconWidth;
        }

        g2.setColor(fg);
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), startX, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 12, 110), Math.max(d.height + 6, 38));
    }
}

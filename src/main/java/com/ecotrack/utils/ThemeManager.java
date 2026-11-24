package com.ecotrack.utils;

import com.ecotrack.model.EmissionCategory;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Centralized dark-charcoal & emerald-green design system for EcoTrack.
 * Ensures consistent typography, spacing, borders, and category colors across all Swing panels.
 */
public final class ThemeManager {

    // Core Palette (from Design Specification)
    public static final Color BG_MAIN = new Color(0x11, 0x15, 0x14);        // #111514
    public static final Color BG_SIDEBAR = new Color(0x15, 0x1B, 0x19);     // #151B19
    public static final Color BG_CARD = new Color(0x1D, 0x25, 0x22);        // #1D2522
    public static final Color BG_CARD_ALT = new Color(0x23, 0x2C, 0x28);    // #232C28
    public static final Color BG_INPUT = new Color(0x16, 0x1D, 0x1A);

    public static final Color EMERALD = new Color(0x23, 0xC4, 0x7E);        // #23C47E
    public static final Color EMERALD_HOVER = new Color(0x2B, 0xD8, 0x8D);
    public static final Color EMERALD_DARK = new Color(0x18, 0x84, 0x58);   // #188458
    public static final Color EMERALD_TINT = new Color(0x19, 0x35, 0x2A);

    public static final Color TEXT_PRIMARY = new Color(0xEF, 0xF5, 0xF1);   // #EFF5F1
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0x9A); // #94A39A
    public static final Color TEXT_MUTED = new Color(0x6E, 0x7D, 0x75);

    public static final Color BORDER_SUBTLE = new Color(0x2A, 0x35, 0x30);
    public static final Color BORDER_FOCUS = EMERALD;

    // Semantic & Chart Palette (harmonious with dark charcoal + emerald)
    public static final Color COLOR_TRANSPORT = new Color(0x23, 0xC4, 0x7E);   // Emerald
    public static final Color COLOR_ELECTRICITY = new Color(0x4E, 0xB8, 0xC9); // Crisp Teal-Cyan
    public static final Color COLOR_FOOD = new Color(0xE0, 0xA8, 0x4B);        // Warm Amber
    public static final Color COLOR_WASTE = new Color(0x8E, 0x9B, 0xE6);       // Soft Periwinkle

    public static final Color STATUS_SUCCESS = new Color(0x23, 0xC4, 0x7E);
    public static final Color STATUS_WARNING = new Color(0xE0, 0xA8, 0x4B);
    public static final Color STATUS_ERROR = new Color(0xE0, 0x6C, 0x75);

    // Typography (cross-platform clean sans-serif stack)
    private static final String FONT_FAMILY = resolveFontFamily();

    public static final Font FONT_HERO = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 21);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private ThemeManager() {
    }

    private static String resolveFontFamily() {
        String[] preferred = {"Segoe UI", "Inter", "SF Pro Text", "Liberation Sans", Font.SANS_SERIF};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String candidate : preferred) {
            for (String f : available) {
                if (f.equalsIgnoreCase(candidate)) {
                    return f;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    public static Color getCategoryColor(EmissionCategory category) {
        if (category == null) {
            return EMERALD;
        }
        return switch (category) {
            case TRANSPORT -> COLOR_TRANSPORT;
            case ELECTRICITY -> COLOR_ELECTRICITY;
            case FOOD -> COLOR_FOOD;
            case WASTE -> COLOR_WASTE;
        };
    }

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background", BG_MAIN);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY_BOLD);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", EMERALD_DARK);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", EMERALD);
        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("CheckBox.background", BG_CARD);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("ScrollPane.background", BG_MAIN);
        UIManager.put("ToolTip.background", BG_CARD_ALT);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER_SUBTLE));
        UIManager.put("ToolTip.font", FONT_SMALL);
    }

    public static void styleTextField(JTextField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(EMERALD);
        field.setFont(FONT_BODY);
        field.setSelectionColor(EMERALD_DARK);
        field.setSelectedTextColor(TEXT_PRIMARY);
        field.setBorder(createInputBorder(BORDER_SUBTLE));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 36));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setFont(FONT_BODY);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                if (isSelected) {
                    lbl.setBackground(EMERALD_DARK);
                    lbl.setForeground(TEXT_PRIMARY);
                } else {
                    lbl.setBackground(BG_INPUT);
                    lbl.setForeground(TEXT_PRIMARY);
                }
                return lbl;
            }
        });
    }

    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_BODY);
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
        spinner.setPreferredSize(new Dimension(spinner.getPreferredSize().width, 36));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defEditor) {
            JTextField tf = defEditor.getTextField();
            tf.setBackground(BG_INPUT);
            tf.setForeground(TEXT_PRIMARY);
            tf.setCaretColor(EMERALD);
            tf.setFont(FONT_BODY);
            tf.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        }
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_MAIN);
        scrollPane.setBackground(BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
    }

    public static Border createInputBorder(Color borderColor) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    public static JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private static class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(0x2F, 0x3D, 0x37);
            this.thumbDarkShadowColor = BG_MAIN;
            this.thumbHighlightColor = EMERALD_DARK;
            this.trackColor = BG_MAIN;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging || isThumbRollover() ? EMERALD_DARK : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                    Math.max(4, thumbBounds.width - 4), Math.max(4, thumbBounds.height - 4), 8, 8);
            g2.dispose();
        }
    }
}

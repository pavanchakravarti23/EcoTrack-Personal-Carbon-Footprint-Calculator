package com.ecotrack.components;

import com.ecotrack.model.ActivityData;
import com.ecotrack.utils.IconFactory;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Top application bar displaying active view title, calculation period selector,
 * and quick PDF/TXT export triggers.
 */
public class HeaderPanel extends JPanel {

    private final JLabel pageTitleLabel;
    private final JLabel pageSubtitleLabel;
    private final JComboBox<ActivityData.CalculationPeriod> periodCombo;
    private final JLabel statusBadgeLabel;

    public HeaderPanel(
            Consumer<ActivityData.CalculationPeriod> onPeriodChanged,
            Runnable onExportPdf,
            Runnable onExportTxt) {
        setBackground(ThemeManager.BG_SIDEBAR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)));
        setLayout(new BorderLayout(16, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        pageTitleLabel = new JLabel("Dashboard Overview");
        pageTitleLabel.setFont(ThemeManager.FONT_TITLE);
        pageTitleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        pageTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        pageSubtitleLabel = new JLabel("Offline personal carbon footprint estimation & analytics");
        pageSubtitleLabel.setFont(ThemeManager.FONT_SMALL);
        pageSubtitleLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        pageSubtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        left.add(pageTitleLabel);
        left.add(Box.createVerticalStrut(3));
        left.add(pageSubtitleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        statusBadgeLabel = new JLabel("Offline Mode");
        statusBadgeLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        statusBadgeLabel.setForeground(ThemeManager.EMERALD);
        statusBadgeLabel.setOpaque(true);
        statusBadgeLabel.setBackground(ThemeManager.EMERALD_TINT);
        statusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.EMERALD_DARK, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        periodLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        periodCombo = new JComboBox<>(ActivityData.CalculationPeriod.values());
        periodCombo.setSelectedItem(ActivityData.CalculationPeriod.MONTHLY);
        ThemeManager.styleComboBox(periodCombo);
        periodCombo.setPreferredSize(new Dimension(115, 34));
        periodCombo.addActionListener(e -> {
            ActivityData.CalculationPeriod selected = (ActivityData.CalculationPeriod) periodCombo.getSelectedItem();
            if (selected != null && onPeriodChanged != null) {
                onPeriodChanged.accept(selected);
            }
        });

        CustomButton txtBtn = new CustomButton("Export TXT", CustomButton.Variant.SECONDARY);
        txtBtn.setIcon(IconFactory.create(IconFactory.Kind.EXPORT, 15, ThemeManager.TEXT_PRIMARY));
        txtBtn.setPreferredSize(new Dimension(125, 34));
        txtBtn.addActionListener(e -> {
            if (onExportTxt != null) {
                onExportTxt.run();
            }
        });

        CustomButton pdfBtn = new CustomButton("Export PDF", CustomButton.Variant.PRIMARY);
        pdfBtn.setIcon(IconFactory.create(IconFactory.Kind.EXPORT, 15, new Color(0x09, 0x18, 0x11)));
        pdfBtn.setPreferredSize(new Dimension(125, 34));
        pdfBtn.addActionListener(e -> {
            if (onExportPdf != null) {
                onExportPdf.run();
            }
        });

        right.add(statusBadgeLabel);
        right.add(periodLabel);
        right.add(periodCombo);
        right.add(txtBtn);
        right.add(pdfBtn);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    public void setHeaderContext(String title, String subtitle) {
        pageTitleLabel.setText(title);
        pageSubtitleLabel.setText(subtitle);
        revalidate();
        repaint();
    }

    public void setSelectedPeriod(ActivityData.CalculationPeriod period) {
        periodCombo.setSelectedItem(period);
    }
}

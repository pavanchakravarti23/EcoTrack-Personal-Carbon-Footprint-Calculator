package com.ecotrack.ui;

import com.ecotrack.calculation.CarbonCalculator;
import com.ecotrack.components.CustomButton;
import com.ecotrack.components.StyledCardPanel;
import com.ecotrack.model.EmissionFactorInfo;
import com.ecotrack.model.UserProfile;
import com.ecotrack.utils.InputValidator;
import com.ecotrack.utils.NumberFormatter;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Module 10 (Settings): Session profile settings, configurable emission factor registry,
 * theme reference, and session reset controls (no server/database/online accounts).
 */
public class SettingsPanel extends JPanel {

    private final UserProfile userProfile;
    private final CarbonCalculator calculator;
    private final Runnable onSessionChanged;
    private final Runnable onResetSession;

    private final JTextField nameField;
    private final JTextField locationField;
    private final DefaultTableModel factorTableModel;
    private final JTable factorTable;
    private final JTextField editFactorField;
    private final JLabel selectedFactorLabel;

    public SettingsPanel(
            UserProfile userProfile,
            CarbonCalculator calculator,
            Runnable onSessionChanged,
            Runnable onResetSession) {
        this.userProfile = userProfile;
        this.calculator = calculator;
        this.onSessionChanged = onSessionChanged;
        this.onResetSession = onResetSession;

        setBackground(ThemeManager.BG_MAIN);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Top Grid: Profile & Session + Theme / Offline Notice
        JPanel topGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        topGrid.setOpaque(false);

        StyledCardPanel profileCard = new StyledCardPanel();
        profileCard.setLayout(new BorderLayout(0, 12));
        profileCard.add(ThemeManager.createSectionTitle("Local Session Profile"), BorderLayout.NORTH);

        JPanel profFields = new JPanel(new GridBagLayout());
        profFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 10);

        nameField = new JTextField(userProfile.getDisplayName());
        locationField = new JTextField(userProfile.getLocationLabel());
        ThemeManager.styleTextField(nameField);
        ThemeManager.styleTextField(locationField);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        profFields.add(new JLabel("Display Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        profFields.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        profFields.add(new JLabel("Session Label:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        profFields.add(locationField, gbc);

        JPanel profBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        profBtns.setOpaque(false);
        CustomButton saveProfileBtn = new CustomButton("Save Profile", CustomButton.Variant.PRIMARY);
        CustomButton resetSessionBtn = new CustomButton("Reset Current Session", CustomButton.Variant.DANGER);
        saveProfileBtn.addActionListener(e -> {
            userProfile.setDisplayName(nameField.getText());
            userProfile.setLocationLabel(locationField.getText());
            if (onSessionChanged != null) onSessionChanged.run();
            JOptionPane.showMessageDialog(this, "Session profile updated in memory.",
                    "Profile Saved", JOptionPane.INFORMATION_MESSAGE);
        });
        resetSessionBtn.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(
                    this,
                    "Clear all entered Transportation, Electricity, Food, and Waste inputs for this session?",
                    "Reset Current Session",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (ans == JOptionPane.YES_OPTION && onResetSession != null) {
                onResetSession.run();
            }
        });
        profBtns.add(saveProfileBtn);
        profBtns.add(resetSessionBtn);

        profileCard.add(profFields, BorderLayout.CENTER);
        profileCard.add(profBtns, BorderLayout.SOUTH);

        // Theme & Architecture Card
        StyledCardPanel themeCard = new StyledCardPanel();
        themeCard.setLayout(new BorderLayout(0, 10));
        themeCard.add(ThemeManager.createSectionTitle("Theme & Offline Data Policy"), BorderLayout.NORTH);
        JTextArea themeInfo = new JTextArea(
                "• Visual Theme: Dark Charcoal (#111514) & Emerald Green (#23C47E).\n"
                        + "• Data Storage: Strictly local in-memory Java objects (ArrayList / EnumMap / LinkedHashMap).\n"
                        + "• Persistence: Export PDF or TXT reports locally to retain calculations across sessions.\n"
                        + "• Network / Database: Zero databases, zero servers, and zero online account settings.");
        themeInfo.setEditable(false);
        themeInfo.setLineWrap(true);
        themeInfo.setWrapStyleWord(true);
        themeInfo.setOpaque(false);
        themeInfo.setFont(ThemeManager.FONT_BODY);
        themeInfo.setForeground(ThemeManager.TEXT_SECONDARY);
        themeCard.add(themeInfo, BorderLayout.CENTER);

        topGrid.add(profileCard);
        topGrid.add(themeCard);

        // Bottom: Configurable Emission Factors Registry
        StyledCardPanel factorsCard = new StyledCardPanel();
        factorsCard.setLayout(new BorderLayout(0, 12));
        factorsCard.add(ThemeManager.createSectionTitle("Configurable Emission Factor Registry"), BorderLayout.NORTH);

        String[] cols = {"Key", "Factor Name", "Value", "Unit", "Methodology", "Source / Notes"};
        factorTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        factorTable = new JTable(factorTableModel);
        factorTable.setBackground(ThemeManager.BG_CARD_ALT);
        factorTable.setForeground(ThemeManager.TEXT_PRIMARY);
        factorTable.setGridColor(ThemeManager.BORDER_SUBTLE);
        factorTable.setRowHeight(26);
        factorTable.setFont(ThemeManager.FONT_SMALL);
        factorTable.getTableHeader().setBackground(ThemeManager.BG_SIDEBAR);
        factorTable.getTableHeader().setForeground(ThemeManager.TEXT_SECONDARY);
        factorTable.getTableHeader().setFont(ThemeManager.FONT_SMALL_BOLD);
        factorTable.setSelectionBackground(ThemeManager.EMERALD_DARK);
        factorTable.setSelectionForeground(ThemeManager.TEXT_PRIMARY);

        JScrollPane scroll = new JScrollPane(factorTable);
        ThemeManager.styleScrollPane(scroll);
        scroll.setPreferredSize(new Dimension(600, 260));

        JPanel editBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        editBar.setOpaque(false);
        selectedFactorLabel = new JLabel("Select a factor row above to customize:");
        selectedFactorLabel.setFont(ThemeManager.FONT_SMALL_BOLD);
        selectedFactorLabel.setForeground(ThemeManager.TEXT_SECONDARY);

        editFactorField = new JTextField(8);
        ThemeManager.styleTextField(editFactorField);

        CustomButton updateFactorBtn = new CustomButton("Update Factor", CustomButton.Variant.PRIMARY);
        CustomButton resetFactorsBtn = new CustomButton("Restore Defaults", CustomButton.Variant.SECONDARY);

        factorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = factorTable.getSelectedRow();
                if (row >= 0) {
                    selectedFactorLabel.setText("Editing " + factorTableModel.getValueAt(row, 1) + ":");
                    editFactorField.setText(factorTableModel.getValueAt(row, 2).toString());
                }
            }
        });

        updateFactorBtn.addActionListener(e -> handleUpdateFactor());
        resetFactorsBtn.addActionListener(e -> {
            calculator.getEmissionFactors().resetToDefaults();
            populateFactorTable();
            if (onSessionChanged != null) onSessionChanged.run();
        });

        editBar.add(selectedFactorLabel);
        editBar.add(editFactorField);
        editBar.add(updateFactorBtn);
        editBar.add(resetFactorsBtn);

        factorsCard.add(scroll, BorderLayout.CENTER);
        factorsCard.add(editBar, BorderLayout.SOUTH);

        add(topGrid, BorderLayout.NORTH);
        add(factorsCard, BorderLayout.CENTER);

        populateFactorTable();
    }

    private void handleUpdateFactor() {
        int row = factorTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an emission factor row first.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String key = factorTableModel.getValueAt(row, 0).toString();
            double val = InputValidator.parseNonNegativeDouble(editFactorField.getText(), "Emission factor");
            calculator.getEmissionFactors().setValue(key, val);
            populateFactorTable();
            if (onSessionChanged != null) onSessionChanged.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Factor", JOptionPane.WARNING_MESSAGE);
        }
    }

    public final void populateFactorTable() {
        factorTableModel.setRowCount(0);
        for (EmissionFactorInfo info : calculator.getEmissionFactors().getAllFactors().values()) {
            factorTableModel.addRow(new Object[]{
                    info.getKey(),
                    info.getLabel(),
                    NumberFormatter.formatFactor(info.getValue()),
                    info.getUnit(),
                    info.getMethodologyType(),
                    info.getSource()
            });
        }
    }
}

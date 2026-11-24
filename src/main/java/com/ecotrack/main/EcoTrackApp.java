package com.ecotrack.main;

import com.ecotrack.ui.MainFrame;
import com.ecotrack.utils.ThemeManager;

import javax.swing.*;

/**
 * Desktop entry point for EcoTrack – Personal Carbon Footprint Calculator.
 * Initializes look-and-feel theme defaults and launches MainFrame on the Swing EDT.
 */
public final class EcoTrackApp {

    private EcoTrackApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default Swing look and feel
            }
            ThemeManager.applyGlobalDefaults();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

package com.pgtr.main;

import com.pgtr.util.Theme;
import com.pgtr.view.SplashScreen;

import javax.swing.*;

/**
 * Main application entry point for VYNTRIX — Fitness & Personal Gain Tracking App.
 * B.Tech Computer Science and Engineering OOP Course Project.
 */
public class Main {

    public static void main(String[] args) {
        // Configure high DPI scaling & font anti-aliasing
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize modern FlatLaf dark theme & Vyntrix styling tokens
        Theme.setupFlatLaf();

        // Launch professional Vyntrix splash screen on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                SplashScreen splash = new SplashScreen();
                splash.startSplashSequence();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Vyntrix UI: " + e.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

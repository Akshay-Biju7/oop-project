package com.pgtr.main;

import com.pgtr.database.DatabaseManager;
import com.pgtr.util.Theme;
import com.pgtr.view.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for Personal Gain Tracking System (PGTR).
 * B.Tech Computer Science and Engineering OOP Course Project.
 */
public class Main {

    public static void main(String[] args) {
        // Configure high DPI scaling & rendering
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize modern FlatLaf dark theme & styling tokens
        Theme.setupFlatLaf();

        // Initialize Database layer asynchronously to ensure fast startup
        new Thread(() -> {
            try {
                DatabaseManager.getInstance();
            } catch (Exception e) {
                System.err.println("[Main] Database init background notice: " + e.getMessage());
            }
        }).start();

        // Launch Authentication GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch application UI: " + e.getMessage(),
                        "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

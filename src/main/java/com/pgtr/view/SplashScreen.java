package com.pgtr.view;

import com.pgtr.database.DatabaseManager;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;

import javax.swing.*;
import java.awt.*;

/**
 * Professional futuristic splash screen for Vyntrix.
 * Features official logo artwork, cyan & gold branding accents, and animated initialization progress.
 */
public class SplashScreen extends JWindow {

    private ModernProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        setSize(540, 400);
        setLocationRelativeTo(null);
        Theme.setWindowIcon(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.BG_DARK);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY, 1, true),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));

        // Center: Logo and App Title
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // Logo
        ImageIcon logoIcon = Theme.getAppLogo(130, 130);
        JLabel logoLabel = new JLabel(logoIcon != null ? logoIcon : new ImageIcon(), SwingConstants.CENTER);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(logoLabel);
        center.add(Box.createVerticalStrut(14));

        // Title
        JLabel titleLabel = new JLabel("VYNTRIX", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(4));

        // Subtitle / Tagline
        JLabel subLabel = new JLabel("FITNESS & PERSONAL GAIN TRACKING APP", SwingConstants.CENTER);
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        subLabel.setForeground(Theme.PRIMARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(subLabel);
        center.add(Box.createVerticalStrut(2));

        JLabel tagLabel = new JLabel("REDEFINE YOUR LIMITS.", SwingConstants.CENTER);
        tagLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagLabel.setForeground(Theme.GOLD);
        tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(tagLabel);

        content.add(center, BorderLayout.CENTER);

        // Bottom: Status and Progress Bar
        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        statusLabel = new JLabel("Initializing Vyntrix engines...", SwingConstants.CENTER);
        statusLabel.setFont(Theme.SMALL);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        bottom.add(statusLabel, BorderLayout.NORTH);

        progressBar = new ModernProgressBar(0, 100, Theme.PRIMARY);
        progressBar.setPreferredSize(new Dimension(0, 8));
        bottom.add(progressBar, BorderLayout.CENTER);

        JLabel versionLabel = new JLabel("B.Tech CSE OOP Project • v1.0.0", SwingConstants.RIGHT);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(Theme.TEXT_DIM);
        bottom.add(versionLabel, BorderLayout.SOUTH);

        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);
    }

    /**
     * Runs initialization sequence and transitions to LoginFrame.
     */
    public void startSplashSequence() {
        setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                updateProgress(15, "Loading Vyntrix design tokens & theme...");
                Thread.sleep(300);

                updateProgress(35, "Checking database connectivity (Supabase / Local SQLite)...");
                DatabaseManager.getInstance();
                Thread.sleep(350);

                updateProgress(65, "Preparing Gym Workout & Nutrition Cut/Bulk plans...");
                Thread.sleep(300);

                updateProgress(88, "Synchronizing health metrics & achievement engines...");
                Thread.sleep(250);

                updateProgress(100, "Ready. Welcome to Vyntrix!");
                Thread.sleep(250);
                return null;
            }

            private void updateProgress(int progress, String message) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setProgress(progress, 100);
                    statusLabel.setText(message);
                });
            }

            @Override
            protected void done() {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true);
                });
            }
        };

        worker.execute();
    }
}

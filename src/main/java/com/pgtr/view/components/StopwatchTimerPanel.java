package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Dedicated workout session stopwatch & set rest interval timer.
 */
public class StopwatchTimerPanel extends ModernCard {

    // Workout Stopwatch State
    private int elapsedSeconds = 0;
    private boolean isRunning = false;
    private Timer workoutTimer;
    private final JLabel timeDisplay;
    private final NeonButton startBtn;
    private final NeonButton pauseBtn;
    private final NeonButton resetBtn;

    // Rest Countdown State
    private int restSecondsRemaining = 0;
    private Timer restTimer;
    private final JLabel restDisplay;
    private final JLabel restStatusLabel;

    public StopwatchTimerPanel() {
        super(16);
        setLayout(new BorderLayout(14, 12));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("WORKOUT & REST TIMERS");
        titleLabel.setFont(Theme.SUBTITLE);
        titleLabel.setForeground(Theme.PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center split: Stopwatch on Left, Rest Timer on Right
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        contentGrid.setOpaque(false);

        // 1. Workout Session Stopwatch Panel
        JPanel stopwatchCard = new JPanel(new BorderLayout(8, 8));
        stopwatchCard.setOpaque(false);

        JLabel swTitle = new JLabel("Workout Session Duration", SwingConstants.CENTER);
        swTitle.setFont(Theme.SMALL);
        swTitle.setForeground(Theme.TEXT_MUTED);
        stopwatchCard.add(swTitle, BorderLayout.NORTH);

        timeDisplay = new JLabel("00:00:00", SwingConstants.CENTER);
        timeDisplay.setFont(new Font("Consolas", Font.BOLD, 28));
        timeDisplay.setForeground(Theme.TEXT_PRIMARY);
        stopwatchCard.add(timeDisplay, BorderLayout.CENTER);

        JPanel swControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        swControls.setOpaque(false);

        startBtn = new NeonButton("Start", NeonButton.ButtonType.PRIMARY);
        pauseBtn = new NeonButton("Pause", NeonButton.ButtonType.SECONDARY);
        resetBtn = new NeonButton("Reset", NeonButton.ButtonType.SECONDARY);

        pauseBtn.setEnabled(false);
        resetBtn.setEnabled(false);

        swControls.add(startBtn);
        swControls.add(pauseBtn);
        swControls.add(resetBtn);
        stopwatchCard.add(swControls, BorderLayout.SOUTH);

        contentGrid.add(stopwatchCard);

        // 2. Set Rest Timer Panel
        JPanel restCard = new JPanel(new BorderLayout(8, 8));
        restCard.setOpaque(false);

        JLabel restTitle = new JLabel("Between-Set Rest Countdown", SwingConstants.CENTER);
        restTitle.setFont(Theme.SMALL);
        restTitle.setForeground(Theme.TEXT_MUTED);
        restCard.add(restTitle, BorderLayout.NORTH);

        restDisplay = new JLabel("00:00", SwingConstants.CENTER);
        restDisplay.setFont(new Font("Consolas", Font.BOLD, 28));
        restDisplay.setForeground(Theme.WATER_CYAN);
        restCard.add(restDisplay, BorderLayout.CENTER);

        JPanel restControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        restControls.setOpaque(false);

        NeonButton btn30 = new NeonButton("+30s", NeonButton.ButtonType.OUTLINE);
        NeonButton btn60 = new NeonButton("+60s", NeonButton.ButtonType.OUTLINE);
        NeonButton btn90 = new NeonButton("+90s", NeonButton.ButtonType.OUTLINE);
        NeonButton btnStop = new NeonButton("Stop", NeonButton.ButtonType.SECONDARY);

        btn30.setFont(Theme.SMALL);
        btn60.setFont(Theme.SMALL);
        btn90.setFont(Theme.SMALL);
        btnStop.setFont(Theme.SMALL);

        restControls.add(btn30);
        restControls.add(btn60);
        restControls.add(btn90);
        restControls.add(btnStop);
        restCard.add(restControls, BorderLayout.SOUTH);

        contentGrid.add(restCard);
        add(contentGrid, BorderLayout.CENTER);

        restStatusLabel = new JLabel("Ready for next set", SwingConstants.CENTER);
        restStatusLabel.setFont(Theme.SMALL);
        restStatusLabel.setForeground(Theme.TEXT_DIM);
        add(restStatusLabel, BorderLayout.SOUTH);

        // --- Timer Action Listeners ---

        workoutTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            updateStopwatchDisplay();
        });

        restTimer = new Timer(1000, e -> {
            if (restSecondsRemaining > 0) {
                restSecondsRemaining--;
                updateRestDisplay();
                if (restSecondsRemaining == 0) {
                    Toolkit.getDefaultToolkit().beep();
                    restStatusLabel.setText("Rest time is over! Start your next set!");
                    restStatusLabel.setForeground(Theme.PRIMARY);
                    restDisplay.setForeground(Theme.PRIMARY);
                }
            } else {
                restTimer.stop();
            }
        });

        startBtn.addActionListener(e -> {
            isRunning = true;
            workoutTimer.start();
            startBtn.setEnabled(false);
            pauseBtn.setEnabled(true);
            resetBtn.setEnabled(true);
        });

        pauseBtn.addActionListener(e -> {
            isRunning = false;
            workoutTimer.stop();
            startBtn.setEnabled(true);
            startBtn.setText("Resume");
            pauseBtn.setEnabled(false);
        });

        resetBtn.addActionListener(e -> {
            isRunning = false;
            workoutTimer.stop();
            elapsedSeconds = 0;
            updateStopwatchDisplay();
            startBtn.setEnabled(true);
            startBtn.setText("Start");
            pauseBtn.setEnabled(false);
            resetBtn.setEnabled(false);
        });

        btn30.addActionListener(e -> startRestCountdown(30));
        btn60.addActionListener(e -> startRestCountdown(60));
        btn90.addActionListener(e -> startRestCountdown(90));
        btnStop.addActionListener(e -> {
            if (restTimer != null) restTimer.stop();
            restSecondsRemaining = 0;
            updateRestDisplay();
            restStatusLabel.setText("Rest timer stopped");
            restStatusLabel.setForeground(Theme.TEXT_DIM);
        });
    }

    private void updateStopwatchDisplay() {
        int hours = elapsedSeconds / 3600;
        int minutes = (elapsedSeconds % 3600) / 60;
        int seconds = elapsedSeconds % 60;
        timeDisplay.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void updateRestDisplay() {
        int minutes = restSecondsRemaining / 60;
        int seconds = restSecondsRemaining % 60;
        restDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void startRestCountdown(int seconds) {
        this.restSecondsRemaining = seconds;
        updateRestDisplay();
        restDisplay.setForeground(Theme.WATER_CYAN);
        restStatusLabel.setText("Resting between sets (" + seconds + "s)...");
        restStatusLabel.setForeground(Theme.TEXT_MUTED);
        restTimer.restart();
    }

    public int getElapsedMinutes() {
        return Math.max(1, (int) Math.round(elapsedSeconds / 60.0));
    }
}

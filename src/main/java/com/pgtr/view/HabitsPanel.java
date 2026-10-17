package com.pgtr.view;

import com.pgtr.model.SleepLog;
import com.pgtr.model.User;
import com.pgtr.model.WaterLog;
import com.pgtr.service.HabitService;
import com.pgtr.service.UserSession;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;
import com.pgtr.view.components.NeonButton;
import com.pgtr.view.components.StatCard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Water Hydration and Sleep Recovery Habit tracking module.
 */
public class HabitsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final HabitService habitService;

    // Water UI
    private StatCard waterTotalCard;
    private ModernProgressBar waterProgressBar;
    private JLabel waterProgressText;
    private DefaultTableModel waterTableModel;

    // Sleep UI
    private StatCard lastSleepCard;
    private StatCard avgSleepCard;
    private DefaultTableModel sleepTableModel;

    public HabitsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.habitService = new HabitService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel container = new JPanel(new GridLayout(1, 2, 20, 0));
        container.setOpaque(false);

        // LEFT COLUMN: WATER HYDRATION TRACKING
        JPanel waterCol = createWaterColumn();
        container.add(waterCol);

        // RIGHT COLUMN: SLEEP RECOVERY TRACKING
        JPanel sleepCol = createSleepColumn();
        container.add(sleepCol);

        add(container, BorderLayout.CENTER);
    }

    private JPanel createWaterColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // Title
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("HYDRATION TRACKER");
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.WATER_CYAN);
        header.add(title, BorderLayout.WEST);
        col.add(header);
        col.add(Box.createVerticalStrut(12));

        // Stat Card
        waterTotalCard = new StatCard("💧", "TODAY'S WATER CONSUMED", "0 ml", "Target: 3000 ml", Theme.WATER_CYAN);
        col.add(waterTotalCard);
        col.add(Box.createVerticalStrut(12));

        // Progress Card
        ModernCard progCard = new ModernCard(16);
        progCard.setLayout(new BorderLayout(0, 8));
        JLabel pTitle = new JLabel("Daily Hydration Progress");
        pTitle.setFont(Theme.BODY_BOLD);
        pTitle.setForeground(Theme.TEXT_PRIMARY);
        waterProgressText = new JLabel("0 / 3000 ml (0%)");
        waterProgressText.setFont(Theme.SMALL);
        waterProgressText.setForeground(Theme.TEXT_MUTED);
        progCard.add(pTitle, BorderLayout.WEST);
        progCard.add(waterProgressText, BorderLayout.EAST);
        waterProgressBar = new ModernProgressBar(0, 3000, Theme.WATER_CYAN);
        progCard.add(waterProgressBar, BorderLayout.SOUTH);
        col.add(progCard);
        col.add(Box.createVerticalStrut(12));

        // Quick Add Buttons Row
        ModernCard quickCard = new ModernCard(14);
        quickCard.setLayout(new GridLayout(1, 4, 8, 0));

        NeonButton btn250 = new NeonButton("+250ml", NeonButton.ButtonType.PRIMARY);
        NeonButton btn500 = new NeonButton("+500ml", NeonButton.ButtonType.PRIMARY);
        NeonButton btn750 = new NeonButton("+750ml", NeonButton.ButtonType.PRIMARY);
        NeonButton btnCustom = new NeonButton("+ Custom", NeonButton.ButtonType.SECONDARY);

        btn250.addActionListener(e -> addWaterAmount(250));
        btn500.addActionListener(e -> addWaterAmount(500));
        btn750.addActionListener(e -> addWaterAmount(750));
        btnCustom.addActionListener(e -> showCustomWaterDialog());

        quickCard.add(btn250);
        quickCard.add(btn500);
        quickCard.add(btn750);
        quickCard.add(btnCustom);
        col.add(quickCard);
        col.add(Box.createVerticalStrut(12));

        // Today's Logs Table
        ModernCard tableCard = new ModernCard(14);
        tableCard.setLayout(new BorderLayout(0, 8));
        JLabel logsTitle = new JLabel("TODAY'S WATER LOGS");
        logsTitle.setFont(Theme.SUBTITLE);
        logsTitle.setForeground(Theme.TEXT_PRIMARY);
        tableCard.add(logsTitle, BorderLayout.NORTH);

        waterTableModel = new DefaultTableModel(new String[]{"Time", "Amount", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCard.add(DarkTable.createScrollPane(new DarkTable(waterTableModel)), BorderLayout.CENTER);
        col.add(tableCard);

        return col;
    }

    private JPanel createSleepColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // Title + Log Button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("SLEEP & RECOVERY");
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.SLEEP_PURPLE);
        header.add(title, BorderLayout.WEST);

        NeonButton logSleepBtn = new NeonButton("+ Log Sleep", NeonButton.ButtonType.PRIMARY);
        logSleepBtn.addActionListener(e -> showLogSleepDialog());
        header.add(logSleepBtn, BorderLayout.EAST);
        col.add(header);
        col.add(Box.createVerticalStrut(12));

        // Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 2, 12, 0));
        statsGrid.setOpaque(false);

        lastSleepCard = new StatCard("🌙", "LAST SLEEP DURATION", "-- hrs", "Target: 8.0 hrs", Theme.SLEEP_PURPLE);
        avgSleepCard = new StatCard("💤", "7-DAY AVERAGE", "-- hrs", "Recovery baseline", Theme.WATER_CYAN);
        statsGrid.add(lastSleepCard);
        statsGrid.add(avgSleepCard);
        col.add(statsGrid);
        col.add(Box.createVerticalStrut(12));

        // Recent Sleep Logs Table
        ModernCard tableCard = new ModernCard(14);
        tableCard.setLayout(new BorderLayout(0, 8));
        JLabel logsTitle = new JLabel("RECENT SLEEP HISTORY");
        logsTitle.setFont(Theme.SUBTITLE);
        logsTitle.setForeground(Theme.TEXT_PRIMARY);
        tableCard.add(logsTitle, BorderLayout.NORTH);

        sleepTableModel = new DefaultTableModel(new String[]{"Date", "Bedtime", "Wake Time", "Duration", "Quality", "Notes"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCard.add(DarkTable.createScrollPane(new DarkTable(sleepTableModel)), BorderLayout.CENTER);
        col.add(tableCard);

        return col;
    }

    private void addWaterAmount(int amountMl) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        try {
            habitService.addWater(user.getId(), amountMl);
            refreshHabits();
            mainFrame.getDashboardPanel().refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error logging water: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomWaterDialog() {
        String input = JOptionPane.showInputDialog(this, "Enter water amount in millilitres (ml):", "Custom Water Intake", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int ml = Integer.parseInt(input.trim());
                if (ml > 0) {
                    addWaterAmount(ml);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void showLogSleepDialog() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, "Record Night Sleep", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard card = new ModernCard(20);
        card.setLayout(new GridLayout(0, 2, 12, 12));

        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextField sleepTimeField = new JTextField("23:00");
        JTextField wakeTimeField = new JTextField("07:00");
        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(8.0, 0.5, 24.0, 0.5));
        JComboBox<String> qualityCombo = new JComboBox<>(new String[]{"Excellent", "Good", "Fair", "Poor"});
        JTextField notesField = new JTextField("Woke up feeling rested");

        card.add(wrapField("DATE (YYYY-MM-DD)", dateField));
        card.add(wrapField("SLEEP QUALITY", qualityCombo));
        card.add(wrapField("BEDTIME (HH:MM)", sleepTimeField));
        card.add(wrapField("WAKE TIME (HH:MM)", wakeTimeField));
        card.add(wrapField("DURATION (HOURS)", durationSpinner));
        card.add(wrapField("NOTES", notesField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton("Save Sleep Record", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                LocalTime sTime = LocalTime.parse(sleepTimeField.getText().trim());
                LocalTime wTime = LocalTime.parse(wakeTimeField.getText().trim());
                double dur = ((Number) durationSpinner.getValue()).doubleValue();
                String qual = (String) qualityCombo.getSelectedItem();

                SleepLog log = new SleepLog(null, user.getId(), date, sTime, wTime, dur, qual, notesField.getText().trim());
                habitService.logSleep(log);

                dialog.dispose();
                refreshHabits();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Sleep logged successfully! (" + dur + " hrs)", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(card, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void refreshHabits() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<HabitData, Void> worker = new SwingWorker<>() {
            @Override
            protected HabitData doInBackground() throws Exception {
                String uid = user.getId();
                int waterToday = habitService.getTodayWaterTotal(uid);
                List<WaterLog> waterLogs = habitService.getTodayWaterLogs(uid);

                var latestSleep = habitService.getLatestSleep(uid);
                List<SleepLog> recentSleep = habitService.getRecentSleepLogs(uid, 15);
                double avgSleep = habitService.getAverageSleep(uid, LocalDate.now().minusDays(7), LocalDate.now());

                return new HabitData(waterToday, waterLogs, latestSleep.orElse(null), recentSleep, avgSleep);
            }

            @Override
            protected void done() {
                try {
                    HabitData data = get();

                    // Update Water UI
                    waterTotalCard.setValue(data.waterToday + " ml");
                    int waterTarget = Math.max(1, user.getDailyWaterTarget());
                    waterProgressBar.setProgress(data.waterToday, waterTarget);
                    int waterPct = (int) Math.round(((double) data.waterToday / waterTarget) * 100.0);
                    waterProgressText.setText(String.format("%d / %d ml (%d%%)", data.waterToday, waterTarget, waterPct));

                    waterTableModel.setRowCount(0);
                    for (WaterLog w : data.waterLogs) {
                        waterTableModel.addRow(new Object[]{
                                DateUtil.formatTime(w.getLoggedAt()),
                                "+" + w.getAmountMl() + " ml",
                                "Logged"
                        });
                    }

                    // Update Sleep UI
                    if (data.latestSleep != null) {
                        lastSleepCard.setValue(String.format("%.1f hrs", data.latestSleep.getDurationHours()));
                        lastSleepCard.setSubtext("Quality: " + data.latestSleep.getSleepQuality());
                    } else {
                        lastSleepCard.setValue("-- hrs");
                    }
                    avgSleepCard.setValue(data.avgSleep > 0 ? String.format("%.1f hrs", data.avgSleep) : "-- hrs");

                    sleepTableModel.setRowCount(0);
                    for (SleepLog s : data.recentSleep) {
                        sleepTableModel.addRow(new Object[]{
                                DateUtil.formatDate(s.getSleepDate()),
                                DateUtil.formatTime(s.getSleepTime()),
                                DateUtil.formatTime(s.getWakeTime()),
                                String.format("%.1f hrs", s.getDurationHours()),
                                s.getSleepQuality(),
                                s.getNotes() != null ? s.getNotes() : ""
                        });
                    }

                } catch (Exception ex) {
                    System.err.println("[HabitsPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel wrapField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private static class HabitData {
        final int waterToday;
        final List<WaterLog> waterLogs;
        final SleepLog latestSleep;
        final List<SleepLog> recentSleep;
        final double avgSleep;

        public HabitData(int waterToday, List<WaterLog> waterLogs, SleepLog latestSleep,
                         List<SleepLog> recentSleep, double avgSleep) {
            this.waterToday = waterToday;
            this.waterLogs = waterLogs;
            this.latestSleep = latestSleep;
            this.recentSleep = recentSleep;
            this.avgSleep = avgSleep;
        }
    }
}

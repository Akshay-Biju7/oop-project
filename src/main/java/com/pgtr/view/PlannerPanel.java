package com.pgtr.view;

import java.time.DayOfWeek;
import com.pgtr.model.User;
import com.pgtr.model.WorkoutPlan;
import com.pgtr.service.PlannerService;
import com.pgtr.service.UserSession;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Weekly Workout Routine Planner supporting Cut Mode and Bulk Mode schedules.
 */
public class PlannerPanel extends JPanel {

    private final MainFrame mainFrame;
    private final PlannerService plannerService;

    private String currentMode = "BULK";
    private final Map<java.time.DayOfWeek, JPanel> dayCardsMap = new HashMap<>();

    // Mode Switcher Buttons
    private NeonButton cutModeBtn;
    private NeonButton bulkModeBtn;
    private JLabel modeTitleLabel;

    public PlannerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.plannerService = new PlannerService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("WEEKLY WORKOUT PLANNER");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        modeTitleLabel = new JLabel("Weekly split for " + currentMode + " Mode (Monday – Sunday)");
        modeTitleLabel.setFont(Theme.BODY);
        modeTitleLabel.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(modeTitleLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Actions: Cut/Bulk Switcher + Reset
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        cutModeBtn = new NeonButton("Cut Mode Plan", NeonButton.ButtonType.SECONDARY);
        bulkModeBtn = new NeonButton("Bulk Mode Plan", NeonButton.ButtonType.GOLD);

        cutModeBtn.addActionListener(e -> switchMode("CUT"));
        bulkModeBtn.addActionListener(e -> switchMode("BULK"));

        NeonButton resetBtn = new NeonButton("Reset to Default", NeonButton.ButtonType.SECONDARY);
        resetBtn.addActionListener(e -> handleReset());

        actions.add(new JLabel("Routine Phase:"));
        actions.add(cutModeBtn);
        actions.add(bulkModeBtn);
        actions.add(resetBtn);

        headerPanel.add(actions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Monday - Sunday Cards Grid (2 rows x 4 columns)
        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setOpaque(false);

        java.time.DayOfWeek[] days = {
                java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        };

        for (java.time.DayOfWeek d : days) {
            JPanel dayPanel = createDayCard(d);
            dayCardsMap.put(d, dayPanel);
            grid.add(dayPanel);
        }

        // 8th slot in grid: Training Wisdom
        ModernCard tipCard = new ModernCard(16);
        tipCard.setBackgroundColor(new Color(0x13, 0x1B, 0x2A));
        tipCard.setBorderColor(Theme.BORDER);
        tipCard.setLayout(new BorderLayout(0, 8));
        JLabel tipTitle = new JLabel("💡 VYNTRIX TRAINING WISDOM");
        tipTitle.setFont(Theme.SUBTITLE);
        tipTitle.setForeground(Theme.PRIMARY);
        tipCard.add(tipTitle, BorderLayout.NORTH);

        JLabel tipBody = new JLabel("<html><body style='color:#94A3B8; font-size:11px;'>" +
                "• <b>Cut Mode:</b> 45-60s rest periods. Compound volume preserves muscle during a deficit.<br><br>" +
                "• <b>Bulk Mode:</b> 90-150s rest. Focus on mechanical tension and adding load or reps each week.<br><br>" +
                "• Allow 48 hours before training the same muscle group again." +
                "</body></html>");
        tipCard.add(tipBody, BorderLayout.CENTER);
        grid.add(tipCard);

        JScrollPane scrollPane = new JScrollPane(grid);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void switchMode(String mode) {
        this.currentMode = mode.toUpperCase();
        modeTitleLabel.setText("Weekly split for " + currentMode + " Mode (Monday – Sunday)");
        if ("CUT".equalsIgnoreCase(currentMode)) {
            cutModeBtn.setBackground(Theme.PRIMARY);
        }
        refreshPlanner();
    }

    private void handleReset() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset " + currentMode + " Mode weekly schedule to default recommended split?",
                "Reset Routine", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                plannerService.resetToDefault(user.getId(), currentMode);
                refreshPlanner();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createDayCard(java.time.DayOfWeek day) {
        ModernCard card = new ModernCard(14);
        card.setLayout(new BorderLayout(0, 8));

        boolean isToday = java.time.LocalDate.now().getDayOfWeek() == day;
        if (isToday) {
            card.setBorderColor(Theme.PRIMARY);
            card.setBackgroundColor(new Color(0x13, 0x22, 0x2E));
        }

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel dayName = new JLabel(day.name() + (isToday ? " (TODAY)" : ""));
        dayName.setFont(Theme.BODY_BOLD);
        dayName.setForeground(isToday ? Theme.PRIMARY : Theme.TEXT_PRIMARY);
        top.add(dayName, BorderLayout.WEST);

        NeonButton editBtn = new NeonButton("Edit", NeonButton.ButtonType.SECONDARY);
        editBtn.setFont(Theme.SMALL);
        editBtn.addActionListener(e -> showEditDayDialog(day));
        top.add(editBtn, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Content placeholder panel
        JPanel body = new JPanel();
        body.setName("DAY_BODY");
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    public void refreshPlanner() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<WorkoutPlan>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<WorkoutPlan> doInBackground() throws Exception {
                return plannerService.getWeeklySchedule(user.getId(), currentMode);
            }

            @Override
            protected void done() {
                try {
                    List<WorkoutPlan> plans = get();
                    Map<java.time.DayOfWeek, WorkoutPlan> planMap = new HashMap<>();
                    for (WorkoutPlan p : plans) {
                        planMap.put(p.getDayOfWeek(), p);
                    }

                    for (Map.Entry<java.time.DayOfWeek, JPanel> entry : dayCardsMap.entrySet()) {
                        java.time.DayOfWeek day = entry.getKey();
                        JPanel card = entry.getValue();

                        JPanel body = null;
                        for (Component c : card.getComponents()) {
                            if ("DAY_BODY".equals(c.getName())) {
                                body = (JPanel) c;
                                break;
                            }
                        }

                        if (body != null) {
                            body.removeAll();
                            WorkoutPlan p = planMap.get(day);
                            if (p != null) {
                                JLabel wName = new JLabel("🏋️ " + p.getWorkoutName());
                                wName.setFont(Theme.BODY_BOLD);
                                wName.setForeground(Theme.TEXT_PRIMARY);
                                body.add(wName);

                                JLabel mGroup = new JLabel("Target: " + p.getMuscleGroup());
                                mGroup.setFont(Theme.SMALL);
                                mGroup.setForeground(Theme.PRIMARY);
                                body.add(mGroup);

                                JLabel restLabel = new JLabel("Rest: " + p.getRestSeconds() + "s | Cardio: " + p.getCardio());
                                restLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                                restLabel.setForeground(Theme.GOLD);
                                body.add(restLabel);

                                if (p.getPlannedExercises() != null && !p.getPlannedExercises().isEmpty()) {
                                    JLabel ex = new JLabel("<html><body style='width:125px; color:#94A3B8; font-size:10px;'>" +
                                            p.getPlannedExercises() + "</body></html>");
                                    body.add(ex);
                                }

                                JCheckBox doneCheck = new JCheckBox("Completed", p.isCompleted());
                                doneCheck.setFont(Theme.SMALL);
                                doneCheck.setForeground(p.isCompleted() ? Theme.SUCCESS : Theme.TEXT_MUTED);
                                doneCheck.setOpaque(false);
                                doneCheck.addActionListener(e -> {
                                    try {
                                        plannerService.setPlanCompleted(p.getId(), user.getId(), doneCheck.isSelected());
                                        refreshPlanner();
                                    } catch (Exception ignored) {}
                                });
                                body.add(doneCheck);

                            } else {
                                JLabel rest = new JLabel("🏖️ Active Rest / Recovery");
                                rest.setFont(Theme.BODY);
                                rest.setForeground(Theme.TEXT_DIM);
                                body.add(rest);
                            }
                            body.revalidate();
                            body.repaint();
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[PlannerPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showEditDayDialog(java.time.DayOfWeek day) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, "Schedule " + currentMode + " Mode Routine for " + day.name(), true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new GridLayout(0, 1, 0, 10));

        JTextField nameField = new JTextField("Push Day");
        JComboBox<String> muscleCombo = new JComboBox<>(new String[]{
                "Chest & Triceps", "Back & Biceps", "Legs & Core", "Shoulders & Traps",
                "Upper Body", "Full Body", "Active Rest", "Cardio & Mobility"
        });
        JTextField exercisesField = new JTextField("Bench Press 4x8, Shoulder Press 3x10, Incline DB 3x10");
        JSpinner restSpinner = new JSpinner(new SpinnerNumberModel("CUT".equalsIgnoreCase(currentMode) ? 60 : 120, 15, 300, 15));
        JTextField cardioField = new JTextField("CUT".equalsIgnoreCase(currentMode) ? "15 min Incline Treadmill" : "None");
        JTextField timeField = new JTextField("18:00");
        JTextField notesField = new JTextField("Warmup rotator cuffs first");

        container.add(wrapField("WORKOUT ROUTINE NAME", nameField));
        container.add(wrapField("TARGET MUSCLE GROUP", muscleCombo));
        container.add(wrapField("PLANNED EXERCISES", exercisesField));

        JPanel splitRow = new JPanel(new GridLayout(1, 2, 10, 0));
        splitRow.setOpaque(false);
        splitRow.add(wrapField("REST DURATION (SECONDS)", restSpinner));
        splitRow.add(wrapField("SCHEDULED TIME (HH:MM)", timeField));
        container.add(splitRow);

        container.add(wrapField("OPTIONAL CARDIO", cardioField));
        container.add(wrapField("NOTES", notesField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton clear = new NeonButton("Set As Rest Day", NeonButton.ButtonType.DANGER);
        clear.addActionListener(e -> {
            try {
                plannerService.deletePlanForDay(user.getId(), currentMode, day);
                dialog.dispose();
                refreshPlanner();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        NeonButton save = new NeonButton("Save Schedule", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                WorkoutPlan plan = new WorkoutPlan();
                plan.setUserId(user.getId());
                plan.setPlanMode(currentMode);
                plan.setDayOfWeek(day);
                plan.setWorkoutName(nameField.getText().trim());
                plan.setMuscleGroup((String) muscleCombo.getSelectedItem());
                plan.setPlannedExercises(exercisesField.getText().trim());
                plan.setRestSeconds(((Number) restSpinner.getValue()).intValue());
                plan.setCardio(cardioField.getText().trim());
                try { plan.setScheduledTime(LocalTime.parse(timeField.getText().trim())); } catch (Exception ignored) {}
                plan.setNotes(notesField.getText().trim());
                plan.setActive(true);
                plan.setCompleted(false);

                plannerService.savePlan(plan);
                dialog.dispose();
                refreshPlanner();
                JOptionPane.showMessageDialog(mainFrame, "Schedule updated for " + day.name() + " (" + currentMode + " Mode)!",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving schedule: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(clear);
        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(container, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
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
}

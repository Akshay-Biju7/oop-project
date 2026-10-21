package com.pgtr.view;

import java.time.DayOfWeek;
import com.pgtr.model.Exercise;
import com.pgtr.model.FitnessGoal;
import com.pgtr.model.GoalCategory;
import com.pgtr.model.GoalStatus;
import com.pgtr.model.User;
import com.pgtr.model.Workout;
import com.pgtr.model.WorkoutPlan;
import com.pgtr.service.AchievementService;
import com.pgtr.service.GoalService;
import com.pgtr.service.PlannerService;
import com.pgtr.service.UserSession;
import com.pgtr.service.WorkoutService;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;
import com.pgtr.view.components.NeonButton;
import com.pgtr.view.components.StopwatchTimerPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated Gym Workouts module featuring:
 * 1. Cut Mode vs Bulk Mode program selector
 * 2. Editable Monday to Sunday Weekly Workout Planner
 * 3. Rest timer and session stopwatch
 * 4. Actual workout logger, search, and volume history
 * 5. Integrated Fitness Goals & Athletic Badges Showcase with dedicated headings
 */
public class WorkoutPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WorkoutService workoutService;
    private final PlannerService plannerService;
    private final GoalService goalService;
    private final AchievementService achievementService;

    // Active Training Mode: "CUT" or "BULK"
    private String currentMode = "BULK";

    // Mode Selector Cards
    private ModernCard cutModeCard;
    private ModernCard bulkModeCard;
    private JLabel cutStatusLabel;
    private JLabel bulkStatusLabel;

    // Weekly Planner Day Cards Container
    private JPanel weeklyGridPanel;
    private final Map<java.time.DayOfWeek, JPanel> dayCardsMap = new HashMap<>();

    // Timers
    private StopwatchTimerPanel timerPanel;

    // Search and Table
    private JTextField searchField;
    private JComboBox<String> muscleFilterCombo;
    private DefaultTableModel historyTableModel;
    private DarkTable historyTable;
    private List<Workout> currentWorkoutsList = new ArrayList<>();

    // Gym Goals & Trophy Case
    private JPanel activeGoalsContainer;
    private JPanel badgesGrid;

    public WorkoutPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.workoutService = new WorkoutService();
        this.plannerService = new PlannerService();
        this.goalService = new GoalService();
        this.achievementService = new AchievementService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel scrollableContainer = new JPanel();
        scrollableContainer.setLayout(new BoxLayout(scrollableContainer, BoxLayout.Y_AXIS));
        scrollableContainer.setOpaque(false);

        // 1. Top Header with Reset and Record Buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("GYM WORKOUTS & TRAINING SPLITS");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Switch between Cut & Bulk modes • Plan your weekly Monday–Sunday routine • Time rests and log actual lifts");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        NeonButton resetPlanBtn = new NeonButton("Reset Mode to Default Routine", NeonButton.ButtonType.SECONDARY);
        resetPlanBtn.addActionListener(e -> handleResetToDefault());

        NeonButton addWorkoutBtn = new NeonButton("+ Record Workout Session", NeonButton.ButtonType.PRIMARY);
        addWorkoutBtn.addActionListener(e -> showWorkoutDialog(null, null));

        headerActions.add(resetPlanBtn);
        headerActions.add(addWorkoutBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        scrollableContainer.add(headerPanel);
        scrollableContainer.add(Box.createVerticalStrut(16));

        // 2. Cut Mode vs Bulk Mode Selector Cards
        JPanel modeSelectorPanel = createModeSelectorCards();
        scrollableContainer.add(modeSelectorPanel);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 3. Weekly Planner Section (Monday - Sunday)
        JPanel weeklyHeader = new JPanel(new BorderLayout());
        weeklyHeader.setOpaque(false);

        JLabel weeklyTitle = new JLabel("WEEKLY WORKOUT PLANNER (MONDAY – SUNDAY)");
        weeklyTitle.setFont(Theme.SUBTITLE);
        weeklyTitle.setForeground(Theme.PRIMARY);
        weeklyHeader.add(weeklyTitle, BorderLayout.WEST);

        JLabel modeIndicator = new JLabel("Active: " + currentMode + " Mode Routine");
        modeIndicator.setFont(Theme.SMALL);
        modeIndicator.setForeground(Theme.GOLD);
        weeklyHeader.add(modeIndicator, BorderLayout.EAST);

        scrollableContainer.add(weeklyHeader);
        scrollableContainer.add(Box.createVerticalStrut(10));

        weeklyGridPanel = new JPanel(new GridLayout(2, 4, 14, 14));
        weeklyGridPanel.setOpaque(false);

        java.time.DayOfWeek[] days = {
                java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY,
                java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY,
                java.time.DayOfWeek.SUNDAY
        };

        for (java.time.DayOfWeek d : days) {
            JPanel dayPanel = createDayCard(d);
            dayCardsMap.put(d, dayPanel);
            weeklyGridPanel.add(dayPanel);
        }

        // 8th slot in 2x4 grid: Training phase strategy card
        ModernCard infoCard = new ModernCard(14);
        infoCard.setBackgroundColor(new Color(0x13, 0x1B, 0x2A));
        infoCard.setBorderColor(Theme.BORDER);
        infoCard.setLayout(new BorderLayout(0, 6));

        JLabel infoTitle = new JLabel("⚡ TRAINING PRINCIPLES");
        infoTitle.setFont(Theme.BODY_BOLD);
        infoTitle.setForeground(Theme.PRIMARY);
        infoCard.add(infoTitle, BorderLayout.NORTH);

        JLabel infoBody = new JLabel("<html><body style='color:#94A3B8; font-size:11px;'>" +
                "• <b>Cut Mode:</b> Keep rest intervals to 45-60s to maintain elevated heart rate. Preserve heavy loads to retain lean muscle.<br><br>" +
                "• <b>Bulk Mode:</b> Rest 90-150s between heavy compound sets. Focus on progressive overload and adding reps or weight weekly.<br><br>" +
                "• Click <b>'Mark Completed'</b> on any day to log volume and advance your streak!" +
                "</body></html>");
        infoCard.add(infoBody, BorderLayout.CENTER);
        weeklyGridPanel.add(infoCard);

        scrollableContainer.add(weeklyGridPanel);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 4. Interactive Workout Timer & Rest Timer
        timerPanel = new StopwatchTimerPanel();
        scrollableContainer.add(timerPanel);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 5. Recorded Workouts History & Search
        ModernCard historyCard = createHistoryCard();
        scrollableContainer.add(historyCard);

        // 6. Section Divider & Distinct Heading for Gym Goals & Badges
        scrollableContainer.add(Box.createVerticalStrut(32));

        JPanel goalsSectionHeader = new JPanel(new BorderLayout());
        goalsSectionHeader.setOpaque(false);

        JPanel goalsTitleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        goalsTitleBlock.setOpaque(false);

        JLabel goalsMainTitle = new JLabel("🎯 GYM MILESTONE GOALS & ATHLETIC TROPHY CASE");
        goalsMainTitle.setFont(Theme.TITLE_LARGE);
        goalsMainTitle.setForeground(Theme.PRIMARY);

        JLabel goalsMainSub = new JLabel("Set targeted progressive overload milestones (e.g. Bench Press 100 kg), bodyweight goals, and unlock prestigious athletic badges.");
        goalsMainSub.setFont(Theme.BODY);
        goalsMainSub.setForeground(Theme.TEXT_MUTED);

        goalsTitleBlock.add(goalsMainTitle);
        goalsTitleBlock.add(goalsMainSub);
        goalsSectionHeader.add(goalsTitleBlock, BorderLayout.WEST);

        NeonButton addGoalBtn = new NeonButton("+ Set New Goal", NeonButton.ButtonType.GOLD);
        addGoalBtn.addActionListener(e -> showGoalDialog(null));
        goalsSectionHeader.add(addGoalBtn, BorderLayout.EAST);

        scrollableContainer.add(goalsSectionHeader);
        scrollableContainer.add(Box.createVerticalStrut(14));

        // Sub-heading: Active Goals
        JLabel activeGoalsTitle = new JLabel("ACTIVE GYM MILESTONES & TARGETS");
        activeGoalsTitle.setFont(Theme.SUBTITLE);
        activeGoalsTitle.setForeground(Theme.PRIMARY);
        scrollableContainer.add(activeGoalsTitle);
        scrollableContainer.add(Box.createVerticalStrut(8));

        activeGoalsContainer = new JPanel(new GridLayout(0, 2, 16, 16));
        activeGoalsContainer.setOpaque(false);
        scrollableContainer.add(activeGoalsContainer);
        scrollableContainer.add(Box.createVerticalStrut(28));

        // Sub-heading: Badges
        JLabel badgesTitle = new JLabel("🏆 TROPHY CASE & EARNED BADGES");
        badgesTitle.setFont(Theme.SUBTITLE);
        badgesTitle.setForeground(Theme.GOLD);
        scrollableContainer.add(badgesTitle);
        scrollableContainer.add(Box.createVerticalStrut(8));

        badgesGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        badgesGrid.setOpaque(false);
        scrollableContainer.add(badgesGrid);
        scrollableContainer.add(Box.createVerticalStrut(24));

        JScrollPane scrollPane = new JScrollPane(scrollableContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createModeSelectorCards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);

        // CUT MODE CARD
        cutModeCard = new ModernCard(16);
        cutModeCard.setLayout(new BorderLayout(12, 6));
        cutModeCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel cutLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        cutLeft.setOpaque(false);
        JLabel cutTitle = new JLabel("🔥  CUT MODE (FAT LOSS & RETENTION)");
        cutTitle.setFont(Theme.TITLE);
        cutTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel cutDesc = new JLabel("<html><body style='color:#94A3B8; font-size:12px;'>" +
                "Designed for caloric deficit. High-density hypertrophy with controlled 45-60s rest periods, " +
                "accessory supersets, and structured metabolic cardio conditioning." +
                "</body></html>");
        cutLeft.add(cutTitle);
        cutLeft.add(cutDesc);
        cutModeCard.add(cutLeft, BorderLayout.CENTER);

        cutStatusLabel = new JLabel("INACTIVE", SwingConstants.CENTER);
        cutStatusLabel.setFont(Theme.BODY_BOLD);
        cutStatusLabel.setPreferredSize(new Dimension(100, 32));
        cutModeCard.add(cutStatusLabel, BorderLayout.EAST);

        // BULK MODE CARD
        bulkModeCard = new ModernCard(16);
        bulkModeCard.setLayout(new BorderLayout(12, 6));
        bulkModeCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bulkLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        bulkLeft.setOpaque(false);
        JLabel bulkTitle = new JLabel("🦾  BULK MODE (HYPERTROPHY & POWER)");
        bulkTitle.setFont(Theme.TITLE);
        bulkTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel bulkDesc = new JLabel("<html><body style='color:#94A3B8; font-size:12px;'>" +
                "Designed for progressive overload and caloric surplus. Heavy compound multi-joint movements " +
                "(Squat, Bench, Deadlift, OHP) with 90-150s rest for maximal neuromuscular adaptation." +
                "</body></html>");
        bulkLeft.add(bulkTitle);
        bulkLeft.add(bulkDesc);
        bulkModeCard.add(bulkLeft, BorderLayout.CENTER);

        bulkStatusLabel = new JLabel("ACTIVE", SwingConstants.CENTER);
        bulkStatusLabel.setFont(Theme.BODY_BOLD);
        bulkStatusLabel.setPreferredSize(new Dimension(100, 32));
        bulkModeCard.add(bulkStatusLabel, BorderLayout.EAST);

        // Card Click Handlers
        cutModeCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchMode("CUT");
            }
        });

        bulkModeCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchMode("BULK");
            }
        });

        updateModeCardVisuals();

        panel.add(cutModeCard);
        panel.add(bulkModeCard);
        return panel;
    }

    private void switchMode(String newMode) {
        if (newMode.equalsIgnoreCase(currentMode)) return;
        this.currentMode = newMode.toUpperCase();
        updateModeCardVisuals();
        refreshWeeklyPlanner();
    }

    private void updateModeCardVisuals() {
        if ("CUT".equalsIgnoreCase(currentMode)) {
            cutModeCard.setBackgroundColor(new Color(0x13, 0x22, 0x2E));
            cutModeCard.setBorderColor(Theme.PRIMARY);
            cutStatusLabel.setText("● ACTIVE");
            cutStatusLabel.setForeground(Theme.PRIMARY);

            bulkModeCard.setBackgroundColor(Theme.BG_PANEL);
            bulkModeCard.setBorderColor(Theme.BORDER);
            bulkStatusLabel.setText("SWITCH");
            bulkStatusLabel.setForeground(Theme.TEXT_MUTED);
        } else {
            bulkModeCard.setBackgroundColor(new Color(0x28, 0x20, 0x12));
            bulkModeCard.setBorderColor(Theme.GOLD);
            bulkStatusLabel.setText("● ACTIVE");
            bulkStatusLabel.setForeground(Theme.GOLD);

            cutModeCard.setBackgroundColor(Theme.BG_PANEL);
            cutModeCard.setBorderColor(Theme.BORDER);
            cutStatusLabel.setText("SWITCH");
            cutStatusLabel.setForeground(Theme.TEXT_MUTED);
        }
        cutModeCard.repaint();
        bulkModeCard.repaint();
    }

    private JPanel createDayCard(java.time.DayOfWeek day) {
        ModernCard card = new ModernCard(14);
        card.setLayout(new BorderLayout(0, 8));

        boolean isToday = java.time.LocalDate.now().getDayOfWeek() == day;
        if (isToday) {
            card.setBorderColor(Theme.PRIMARY);
            card.setBackgroundColor(new Color(0x13, 0x22, 0x2E));
        }

        // Header: Day Name + Edit Button
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

        // Body container
        JPanel body = new JPanel();
        body.setName("DAY_BODY");
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    public void refreshWorkouts() {
        refreshWeeklyPlanner();
        refreshHistory();
        refreshGoalsAndBadges();
    }

    public void refreshWeeklyPlanner() {
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
                                body.add(Box.createVerticalStrut(3));

                                JLabel mGroup = new JLabel("Target: " + p.getMuscleGroup());
                                mGroup.setFont(Theme.SMALL);
                                mGroup.setForeground(Theme.PRIMARY);
                                body.add(mGroup);
                                body.add(Box.createVerticalStrut(3));

                                JLabel restLabel = new JLabel("⏱️ Rest: " + p.getRestSeconds() + "s | Cardio: " + p.getCardio());
                                restLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                                restLabel.setForeground(Theme.GOLD);
                                body.add(restLabel);
                                body.add(Box.createVerticalStrut(4));

                                if (p.getPlannedExercises() != null && !p.getPlannedExercises().isEmpty()) {
                                    JLabel ex = new JLabel("<html><body style='width:140px; color:#94A3B8; font-size:10px;'>" +
                                            p.getPlannedExercises() + "</body></html>");
                                    body.add(ex);
                                    body.add(Box.createVerticalStrut(6));
                                }

                                // Completion & Action Row
                                JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                                actionRow.setOpaque(false);

                                JCheckBox doneCheck = new JCheckBox("Completed", p.isCompleted());
                                doneCheck.setFont(Theme.SMALL);
                                doneCheck.setForeground(p.isCompleted() ? Theme.SUCCESS : Theme.TEXT_MUTED);
                                doneCheck.setOpaque(false);

                                doneCheck.addActionListener(e -> {
                                    boolean isDone = doneCheck.isSelected();
                                    handleToggleDayCompletion(p, isDone);
                                });

                                actionRow.add(doneCheck);
                                body.add(actionRow);

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
                    System.err.println("[WorkoutPanel] Error loading weekly planner: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleToggleDayCompletion(WorkoutPlan plan, boolean completed) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            plannerService.setPlanCompleted(plan.getId(), user.getId(), completed);

            if (completed) {
                // Prompt user to log this completed session into actual workout history
                int choice = JOptionPane.showConfirmDialog(this,
                        "Great job crushing today's " + plan.getWorkoutName() + "!\n" +
                                "Would you like to record this session into your workout history and update dashboard stats?",
                        "Workout Complete!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    Workout w = new Workout(
                            null,
                            user.getId(),
                            LocalDate.now(),
                            plan.getWorkoutName(),
                            plan.getMuscleGroup(),
                            45,
                            "CUT".equalsIgnoreCase(plan.getPlanMode()) ? 320 : 380,
                            "Completed " + plan.getPlanMode() + " mode planned workout"
                    );

                    // Add planned exercises if present
                    if (plan.getPlannedExercises() != null) {
                        String[] items = plan.getPlannedExercises().split(",");
                        for (String item : items) {
                            String trimmed = item.trim();
                            if (!trimmed.isEmpty()) {
                                w.addExercise(new Exercise(null, null, user.getId(), trimmed, "Strength", 3, 10, 50.0, "Planned Routine"));
                            }
                        }
                    }

                    workoutService.logWorkout(w);
                    refreshHistory();
                    mainFrame.getDashboardPanel().refreshDashboard();
                }
            }

            refreshWeeklyPlanner();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating completion: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetToDefault() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset " + currentMode + " Mode schedule to the recommended default Vyntrix routine?\n" +
                        "This will replace customized routines for " + currentMode + " mode.",
                "Reset " + currentMode + " Routine", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                plannerService.resetToDefault(user.getId(), currentMode);
                refreshWeeklyPlanner();
                JOptionPane.showMessageDialog(this, currentMode + " Mode routine reset to default successfully!",
                        "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDayDialog(java.time.DayOfWeek day) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, "Edit " + currentMode + " Mode Schedule — " + day.name(), true);
        dialog.setSize(540, 520);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new GridLayout(0, 1, 0, 10));

        JTextField nameField = new JTextField("Push Day — Chest & Triceps");
        JComboBox<String> muscleCombo = new JComboBox<>(new String[]{
                "Chest & Triceps", "Back & Biceps", "Legs & Core", "Shoulders & Traps",
                "Upper Body", "Full Body", "Active Rest", "Cardio & Conditioning"
        });
        JTextField exercisesField = new JTextField("Incline DB Press 4x10, Bench Press 3x12, Cable Flyes 3x15, Pushdowns 3x15");
        JSpinner restSpinner = new JSpinner(new SpinnerNumberModel("CUT".equalsIgnoreCase(currentMode) ? 60 : 120, 15, 300, 15));
        JTextField cardioField = new JTextField("CUT".equalsIgnoreCase(currentMode) ? "15 min Incline Treadmill" : "None");
        JTextField timeField = new JTextField("18:00");
        JTextField notesField = new JTextField("Maintain clean form and explosive concentric");

        // Try prefilling with existing day plan
        try {
            var existingOpt = plannerService.getPlanForToday(user.getId(), currentMode);
            List<WorkoutPlan> plans = plannerService.getWeeklySchedule(user.getId(), currentMode);
            for (WorkoutPlan p : plans) {
                if (p.getDayOfWeek() == day) {
                    nameField.setText(p.getWorkoutName());
                    muscleCombo.setSelectedItem(p.getMuscleGroup());
                    exercisesField.setText(p.getPlannedExercises());
                    restSpinner.setValue(p.getRestSeconds());
                    cardioField.setText(p.getCardio());
                    if (p.getScheduledTime() != null) timeField.setText(p.getScheduledTime().toString());
                    notesField.setText(p.getNotes());
                    break;
                }
            }
        } catch (Exception ignored) {}

        container.add(wrapField("WORKOUT NAME", nameField));
        container.add(wrapField("TARGET MUSCLE GROUP", muscleCombo));
        container.add(wrapField("PLANNED EXERCISES (COMMA SEPARATED WITH SETS & REPS)", exercisesField));

        JPanel splitRow = new JPanel(new GridLayout(1, 2, 10, 0));
        splitRow.setOpaque(false);
        splitRow.add(wrapField("REST DURATION (SECONDS)", restSpinner));
        splitRow.add(wrapField("SCHEDULED TIME (HH:MM)", timeField));
        container.add(splitRow);

        container.add(wrapField("OPTIONAL CARDIO / CONDITIONING", cardioField));
        container.add(wrapField("NOTES & FOCUS", notesField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton clear = new NeonButton("Set As Rest Day", NeonButton.ButtonType.DANGER);
        clear.addActionListener(e -> {
            try {
                plannerService.deletePlanForDay(user.getId(), currentMode, day);
                dialog.dispose();
                refreshWeeklyPlanner();
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
                refreshWeeklyPlanner();
                JOptionPane.showMessageDialog(mainFrame, "Workout schedule updated for " + day.name() + " (" + currentMode + " Mode)!",
                        "Schedule Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(clear);
        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(container, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private ModernCard createHistoryCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 12));

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(12, 0));
        filterBar.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout(6, 0));
        searchWrap.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setForeground(Theme.TEXT_MUTED);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 34));
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        filterBar.add(searchWrap, BorderLayout.WEST);

        JPanel comboWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        comboWrap.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter Muscle:");
        filterLabel.setFont(Theme.SMALL);
        filterLabel.setForeground(Theme.TEXT_MUTED);
        comboWrap.add(filterLabel);

        muscleFilterCombo = new JComboBox<>(new String[]{"All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Cardio"});
        muscleFilterCombo.setPreferredSize(new Dimension(130, 34));
        comboWrap.add(muscleFilterCombo);

        NeonButton searchBtn = new NeonButton("Search", NeonButton.ButtonType.SECONDARY);
        searchBtn.addActionListener(e -> performSearch());
        comboWrap.add(searchBtn);

        NeonButton resetBtn = new NeonButton("Reset", NeonButton.ButtonType.SECONDARY);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            muscleFilterCombo.setSelectedIndex(0);
            refreshHistory();
        });
        comboWrap.add(resetBtn);

        filterBar.add(comboWrap, BorderLayout.EAST);
        card.add(filterBar, BorderLayout.NORTH);

        // History Table
        historyTableModel = new DefaultTableModel(new String[]{
                "Date", "Workout Name", "Muscle Group", "Duration", "Calories", "Total Volume", "Exercises Breakdown"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new DarkTable(historyTableModel);
        card.add(DarkTable.createScrollPane(historyTable), BorderLayout.CENTER);

        // Actions Bar
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActions.setOpaque(false);

        NeonButton viewDetailsBtn = new NeonButton("View Details & Exercises", NeonButton.ButtonType.OUTLINE);
        NeonButton editBtn = new NeonButton("Edit Session", NeonButton.ButtonType.SECONDARY);
        NeonButton deleteBtn = new NeonButton("Delete Session", NeonButton.ButtonType.DANGER);

        viewDetailsBtn.addActionListener(e -> handleViewDetails());
        editBtn.addActionListener(e -> handleEditWorkout());
        deleteBtn.addActionListener(e -> handleDeleteWorkout());

        tableActions.add(viewDetailsBtn);
        tableActions.add(editBtn);
        tableActions.add(deleteBtn);
        card.add(tableActions, BorderLayout.SOUTH);

        searchField.addActionListener(e -> performSearch());
        muscleFilterCombo.addActionListener(e -> performSearch());

        return card;
    }

    public void refreshHistory() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<Workout>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Workout> doInBackground() throws Exception {
                return workoutService.getUserWorkouts(user.getId());
            }

            @Override
            protected void done() {
                try {
                    currentWorkoutsList = get();
                    populateHistoryTable(currentWorkoutsList);
                } catch (Exception ex) {
                    System.err.println("[WorkoutPanel] Error loading history: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        String query = searchField.getText();
        String muscle = (String) muscleFilterCombo.getSelectedItem();

        SwingWorker<List<Workout>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Workout> doInBackground() throws Exception {
                return workoutService.searchWorkouts(user.getId(), query, muscle);
            }

            @Override
            protected void done() {
                try {
                    currentWorkoutsList = get();
                    populateHistoryTable(currentWorkoutsList);
                } catch (Exception ex) {
                    System.err.println("[WorkoutPanel] Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateHistoryTable(List<Workout> list) {
        historyTableModel.setRowCount(0);
        for (Workout w : list) {
            StringBuilder exSummary = new StringBuilder();
            if (w.getExercises() != null && !w.getExercises().isEmpty()) {
                for (int i = 0; i < Math.min(3, w.getExercises().size()); i++) {
                    Exercise ex = w.getExercises().get(i);
                    if (i > 0) exSummary.append(", ");
                    exSummary.append(ex.getExerciseName()).append(" (").append(ex.getSets()).append("x").append(ex.getReps()).append(")");
                }
                if (w.getExercises().size() > 3) exSummary.append(" +").append(w.getExercises().size() - 3).append(" more");
            } else {
                exSummary.append("No individual exercises logged");
            }

            historyTableModel.addRow(new Object[]{
                    DateUtil.formatDate(w.getWorkoutDate()),
                    w.getWorkoutName(),
                    w.getMuscleGroup(),
                    w.getDurationMinutes() + " min",
                    w.getCaloriesBurned() + " kcal",
                    String.format("%.0f kg", w.getTotalVolume()),
                    exSummary.toString()
            });
        }
    }

    private Workout getSelectedWorkout() {
        int row = historyTable.getSelectedRow();
        if (row >= 0 && row < currentWorkoutsList.size()) {
            return currentWorkoutsList.get(row);
        }
        return null;
    }

    private void handleViewDetails() {
        Workout w = getSelectedWorkout();
        if (w == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout from the table to view details.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(w.getWorkoutName()).append(" ===\n");
        sb.append("Date: ").append(DateUtil.formatDate(w.getWorkoutDate())).append("\n");
        sb.append("Muscle Group: ").append(w.getMuscleGroup()).append("\n");
        sb.append("Duration: ").append(w.getDurationMinutes()).append(" minutes\n");
        sb.append("Calories Burned: ").append(w.getCaloriesBurned()).append(" kcal\n");
        sb.append("Total Training Volume: ").append(String.format("%.0f kg\n\n", w.getTotalVolume()));
        sb.append("Exercises Performed:\n");

        if (w.getExercises() != null && !w.getExercises().isEmpty()) {
            int idx = 1;
            for (Exercise ex : w.getExercises()) {
                sb.append(idx++).append(". ").append(ex.getExerciseName())
                        .append(" [").append(ex.getExerciseType()).append("] -> ")
                        .append(ex.getSets()).append(" sets × ").append(ex.getReps()).append(" reps @ ")
                        .append(ex.getWeightKg()).append(" kg  (Volume: ")
                        .append(String.format("%.0f kg)", ex.getVolumeKg()));
                if (ex.getNotes() != null && !ex.getNotes().isEmpty()) {
                    sb.append(" - Note: ").append(ex.getNotes());
                }
                sb.append("\n");
            }
        } else {
            sb.append("(No individual exercises listed)\n");
        }

        if (w.getNotes() != null && !w.getNotes().isEmpty()) {
            sb.append("\nSession Notes:\n").append(w.getNotes()).append("\n");
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(Theme.BG_DARK);
        area.setForeground(Theme.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 350));
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JOptionPane.showMessageDialog(this, sp, "Workout Details — " + w.getWorkoutName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void handleEditWorkout() {
        Workout w = getSelectedWorkout();
        if (w == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showWorkoutDialog(w, null);
    }

    private void handleDeleteWorkout() {
        Workout w = getSelectedWorkout();
        if (w == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + w.getWorkoutName() + "' on " + DateUtil.formatDate(w.getWorkoutDate()) + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                workoutService.deleteWorkout(w.getId(), w.getUserId());
                refreshHistory();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(this, "Workout deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting workout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showWorkoutDialog(Workout existing, WorkoutPlan sourcePlan) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Record Workout Session" : "Edit Workout Session", true);
        dialog.setSize(680, 620);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BorderLayout(0, 12));

        JPanel headerInputs = new JPanel(new GridLayout(2, 3, 10, 8));
        headerInputs.setOpaque(false);

        String initialName = existing != null ? existing.getWorkoutName() :
                (sourcePlan != null ? sourcePlan.getWorkoutName() : (currentMode + " Mode Session"));
        JTextField nameField = new JTextField(initialName);

        JComboBox<String> muscleCombo = new JComboBox<>(new String[]{
                "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Cardio"
        });
        if (existing != null) muscleCombo.setSelectedItem(existing.getMuscleGroup());
        else if (sourcePlan != null) muscleCombo.setSelectedItem(sourcePlan.getMuscleGroup());

        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getDurationMinutes() : Math.max(30, timerPanel.getElapsedMinutes()), 1, 300, 5));
        JSpinner caloriesSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCaloriesBurned() : ("CUT".equalsIgnoreCase(currentMode) ? 350 : 400), 0, 3000, 25));
        JTextField dateField = new JTextField(existing != null ? existing.getWorkoutDate().toString() : LocalDate.now().toString());

        headerInputs.add(wrapField("WORKOUT NAME", nameField));
        headerInputs.add(wrapField("PRIMARY MUSCLE", muscleCombo));
        headerInputs.add(wrapField("DATE (YYYY-MM-DD)", dateField));
        headerInputs.add(wrapField("DURATION (MINUTES)", durationSpinner));
        headerInputs.add(wrapField("CALORIES BURNED (EST)", caloriesSpinner));

        container.add(headerInputs, BorderLayout.NORTH);

        // Exercises Builder Section
        JPanel exercisesSection = new JPanel(new BorderLayout(0, 8));
        exercisesSection.setOpaque(false);

        JPanel exHeader = new JPanel(new BorderLayout());
        exHeader.setOpaque(false);
        JLabel exTitle = new JLabel("EXERCISES, SETS, REPS & VOLUME");
        exTitle.setFont(Theme.BODY_BOLD);
        exTitle.setForeground(Theme.PRIMARY);
        exHeader.add(exTitle, BorderLayout.WEST);

        DefaultTableModel exTableModel = new DefaultTableModel(new String[]{"Exercise Name", "Type", "Sets", "Reps", "Weight (kg)", "Volume (kg)"}, 0);
        DarkTable exTable = new DarkTable(exTableModel);

        List<Exercise> exercisesList = new ArrayList<>();
        if (existing != null && existing.getExercises() != null) {
            for (Exercise ex : existing.getExercises()) {
                exercisesList.add(ex);
                exTableModel.addRow(new Object[]{ex.getExerciseName(), ex.getExerciseType(), ex.getSets(), ex.getReps(), ex.getWeightKg(), String.format("%.0f", ex.getVolumeKg())});
            }
        }

        // Add Exercise quick row
        JPanel addExRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        addExRow.setOpaque(false);

        JComboBox<String> presetExerciseCombo = new JComboBox<>(new String[]{
                "Barbell Bench Press", "Incline Dumbbell Press", "Barbell Squats", "Romanian Deadlift",
                "Overhead Shoulder Press", "Lat Pulldown", "Barbell Biceps Curl", "Triceps Cable Pushdown",
                "Leg Press", "Dumbbell Lateral Raise", "Pull-ups", "Treadmill Run", "Custom Exercise..."
        });
        presetExerciseCombo.setEditable(true);
        presetExerciseCombo.setPreferredSize(new Dimension(170, 30));

        JComboBox<String> exTypeCombo = new JComboBox<>(new String[]{"Strength", "Hypertrophy", "Endurance", "Cardio"});
        exTypeCombo.setPreferredSize(new Dimension(95, 30));

        JSpinner setsSp = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        setsSp.setPreferredSize(new Dimension(42, 30));

        JSpinner repsSp = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        repsSp.setPreferredSize(new Dimension(45, 30));

        JSpinner weightSp = new JSpinner(new SpinnerNumberModel(60.0, 0.0, 500.0, 2.5));
        weightSp.setPreferredSize(new Dimension(65, 30));

        NeonButton addExBtn = new NeonButton("+ Add", NeonButton.ButtonType.PRIMARY);
        addExBtn.setFont(Theme.SMALL);

        NeonButton removeExBtn = new NeonButton("Remove", NeonButton.ButtonType.DANGER);
        removeExBtn.setFont(Theme.SMALL);

        addExRow.add(presetExerciseCombo);
        addExRow.add(exTypeCombo);
        addExRow.add(new JLabel("Sets:"));
        addExRow.add(setsSp);
        addExRow.add(new JLabel("Reps:"));
        addExRow.add(repsSp);
        addExRow.add(new JLabel("Kg:"));
        addExRow.add(weightSp);
        addExRow.add(addExBtn);
        addExRow.add(removeExBtn);

        exHeader.add(addExRow, BorderLayout.SOUTH);
        exercisesSection.add(exHeader, BorderLayout.NORTH);
        exercisesSection.add(DarkTable.createScrollPane(exTable), BorderLayout.CENTER);

        container.add(exercisesSection, BorderLayout.CENTER);

        // Notes and Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);

        JTextField notesField = new JTextField(existing != null ? existing.getNotes() : "");
        bottomPanel.add(wrapField("SESSION NOTES & RECOVERY", notesField), BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancelBtn = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        NeonButton saveBtn = new NeonButton(existing == null ? "Save & Log Session" : "Save Changes", NeonButton.ButtonType.PRIMARY);
        saveBtn.addActionListener(e -> {
            try {
                String wName = nameField.getText().trim();
                if (wName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a workout name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                LocalDate wDate;
                try {
                    wDate = LocalDate.parse(dateField.getText().trim());
                } catch (Exception parseEx) {
                    JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Workout workout = existing != null ? existing : new Workout();
                workout.setUserId(user.getId());
                workout.setWorkoutName(wName);
                workout.setMuscleGroup((String) muscleCombo.getSelectedItem());
                workout.setWorkoutDate(wDate);
                workout.setDurationMinutes((int) durationSpinner.getValue());
                workout.setCaloriesBurned((int) caloriesSpinner.getValue());
                workout.setNotes(notesField.getText().trim());
                workout.setExercises(exercisesList);

                if (existing == null) {
                    workoutService.logWorkout(workout);
                } else {
                    workoutService.updateWorkout(workout);
                }

                dialog.dispose();
                refreshHistory();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Workout saved successfully! Training Volume: " +
                        String.format("%.0f kg", workout.getTotalVolume()), "Workout Saved", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving workout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);
        bottomPanel.add(btnBar, BorderLayout.SOUTH);

        container.add(bottomPanel, BorderLayout.SOUTH);
        dialog.add(container, BorderLayout.CENTER);

        // Exercise builder actions
        addExBtn.addActionListener(e -> {
            String exName = presetExerciseCombo.getSelectedItem() != null ? presetExerciseCombo.getSelectedItem().toString().trim() : "Exercise";
            String exType = (String) exTypeCombo.getSelectedItem();
            int sets = (int) setsSp.getValue();
            int reps = (int) repsSp.getValue();
            double weight = ((Number) weightSp.getValue()).doubleValue();

            Exercise ex = new Exercise(null, null, user.getId(), exName, exType, sets, reps, weight, "");
            exercisesList.add(ex);
            exTableModel.addRow(new Object[]{exName, exType, sets, reps, weight, String.format("%.0f", ex.getVolumeKg())});
        });

        removeExBtn.addActionListener(e -> {
            int selected = exTable.getSelectedRow();
            if (selected >= 0 && selected < exercisesList.size()) {
                exercisesList.remove(selected);
                exTableModel.removeRow(selected);
            }
        });

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

    public void refreshGoalsAndBadges() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<GoalData, Void> worker = new SwingWorker<>() {
            @Override
            protected GoalData doInBackground() throws Exception {
                List<FitnessGoal> goals = goalService.getGoalsByUser(user.getId());
                var badgeStatuses = achievementService.getCatalogWithUserStatus(user.getId());
                return new GoalData(goals, badgeStatuses);
            }

            @Override
            protected void done() {
                try {
                    GoalData data = get();

                    // Render Active Goals Cards
                    if (activeGoalsContainer != null) {
                        activeGoalsContainer.removeAll();
                        if (data.goals.isEmpty()) {
                            ModernCard empty = new ModernCard(20);
                            empty.setLayout(new BorderLayout());
                            JLabel l = new JLabel("<html><center style='color:#94A3B8; padding:12px;'>No active fitness goals created yet.<br>Click '+ Set New Goal' above to track your lifting milestone!</center></html>", SwingConstants.CENTER);
                            empty.add(l, BorderLayout.CENTER);
                            activeGoalsContainer.add(empty);
                        } else {
                            for (FitnessGoal g : data.goals) {
                                activeGoalsContainer.add(createGoalCard(g));
                            }
                        }
                        activeGoalsContainer.revalidate();
                        activeGoalsContainer.repaint();
                    }

                    // Render Badges Grid
                    if (badgesGrid != null) {
                        badgesGrid.removeAll();
                        for (AchievementService.AchievementBadgeStatus bs : data.badges) {
                            badgesGrid.add(createBadgeCard(bs));
                        }
                        badgesGrid.revalidate();
                        badgesGrid.repaint();
                    }

                } catch (Exception ex) {
                    System.err.println("[WorkoutPanel] Error refreshing goals & badges: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private ModernCard createGoalCard(FitnessGoal g) {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel catLabel = new JLabel(g.getCategory().getDisplayName().toUpperCase());
        catLabel.setFont(Theme.SMALL);
        catLabel.setForeground(Theme.TEXT_MUTED);
        top.add(catLabel, BorderLayout.WEST);

        JLabel statusLabel = new JLabel(g.getStatus().getDisplayName());
        statusLabel.setFont(Theme.SMALL);
        statusLabel.setForeground(g.getStatus() == GoalStatus.ACHIEVED ? Theme.SUCCESS :
                (g.getStatus() == GoalStatus.IN_PROGRESS ? Theme.PRIMARY : Theme.DANGER));
        top.add(statusLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 4));
        center.setOpaque(false);

        JLabel titleLabel = new JLabel(g.getTitle());
        titleLabel.setFont(Theme.TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        center.add(titleLabel);

        int pct = g.getProgressPercentage();
        JLabel valLabel = new JLabel(String.format("Current: %.1f %s  →  Target: %.1f %s (%d%%)",
                g.getCurrentValue(), g.getUnit(), g.getTargetValue(), g.getUnit(), pct));
        valLabel.setFont(Theme.BODY);
        valLabel.setForeground(Theme.TEXT_PRIMARY);
        center.add(valLabel);

        ModernProgressBar pb = new ModernProgressBar(pct, 100, Theme.PRIMARY);
        center.add(pb);
        card.add(center, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel deadline = new JLabel("Deadline: " + DateUtil.formatDate(g.getDeadline()));
        deadline.setFont(Theme.SMALL);
        deadline.setForeground(Theme.TEXT_DIM);
        bottom.add(deadline, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);

        if (g.getStatus() == GoalStatus.IN_PROGRESS) {
            NeonButton completeBtn = new NeonButton("Done ✓", NeonButton.ButtonType.PRIMARY);
            completeBtn.setFont(Theme.SMALL);
            completeBtn.addActionListener(e -> {
                try {
                    goalService.markAchieved(g.getId(), g.getUserId());
                    refreshGoalsAndBadges();
                    mainFrame.getDashboardPanel().refreshDashboard();
                    JOptionPane.showMessageDialog(this, "Congratulations on crushing '" + g.getTitle() + "'! Achievement unlocked!", "Goal Achieved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnRow.add(completeBtn);
        }

        NeonButton editBtn = new NeonButton("Edit", NeonButton.ButtonType.SECONDARY);
        editBtn.setFont(Theme.SMALL);
        editBtn.addActionListener(e -> showGoalDialog(g));
        btnRow.add(editBtn);

        NeonButton delBtn = new NeonButton("Delete", NeonButton.ButtonType.DANGER);
        delBtn.setFont(Theme.SMALL);
        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete goal '" + g.getTitle() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    goalService.deleteGoal(g.getId(), g.getUserId());
                    refreshGoalsAndBadges();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting goal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnRow.add(delBtn);

        bottom.add(btnRow, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private ModernCard createBadgeCard(AchievementService.AchievementBadgeStatus bs) {
        ModernCard card = new ModernCard(14);
        card.setLayout(new BorderLayout(0, 6));

        if (bs.unlocked) {
            card.setBackgroundColor(new Color(0x1B, 0x24, 0x1E));
            card.setBorderColor(Theme.BORDER_ACCENT);
        } else {
            card.setBackgroundColor(Theme.BG_PANEL);
            card.setBorderColor(Theme.BORDER);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel icon = new JLabel(getBadgeEmoji(bs.achievement.getCode()), SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        top.add(icon, BorderLayout.WEST);

        JLabel tier = new JLabel(bs.unlocked ? ("UNLOCKED • " + bs.achievement.getBadgeTier()) : "LOCKED");
        tier.setFont(Theme.SMALL);
        tier.setForeground(bs.unlocked ? Theme.GOLD : Theme.TEXT_DIM);
        top.add(tier, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);

        JLabel title = new JLabel(bs.achievement.getTitle());
        title.setFont(Theme.BODY_BOLD);
        title.setForeground(bs.unlocked ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
        center.add(title);

        JLabel desc = new JLabel("<html><body style='width:160px; color:#94A3B8; font-size:11px;'>" +
                bs.achievement.getDescription() + "</body></html>");
        center.add(desc);
        card.add(center, BorderLayout.CENTER);

        if (bs.unlocked && bs.earnedAt != null) {
            JLabel earned = new JLabel("Earned: " + bs.earnedAt.toLocalDate().format(DateUtil.DATE_FORMATTER));
            earned.setFont(Theme.SMALL);
            earned.setForeground(Theme.TEXT_DIM);
            card.add(earned, BorderLayout.SOUTH);
        }

        return card;
    }

    private String getBadgeEmoji(String code) {
        if (code == null) return "🏆";
        switch (code) {
            case "FIRST_WORKOUT": return "🏋️";
            case "STREAK_7_DAYS": return "🔥";
            case "WORKOUTS_10": return "🥉";
            case "WORKOUTS_50": return "👑";
            case "PROTEIN_TARGET": return "🥩";
            case "NEW_PR": return "⚡";
            case "GOAL_COMPLETED": return "🎯";
            case "HYDRATION_HERO": return "💧";
            case "SLEEP_CHAMPION": return "🌙";
            default: return "🏆";
        }
    }

    private void showGoalDialog(FitnessGoal existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Create Personal Gym Goal" : "Edit Goal", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new GridLayout(0, 2, 12, 12));

        JTextField titleField = new JTextField(existing != null ? existing.getTitle() : "Bench Press 100 kg");
        JComboBox<GoalCategory> categoryCombo = new JComboBox<>(GoalCategory.values());
        if (existing != null) categoryCombo.setSelectedItem(existing.getCategory());

        JSpinner startVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getStartingValue() : 70.0, 0.0, 10000.0, 1.0));
        JSpinner targetVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getTargetValue() : 100.0, 0.0, 10000.0, 1.0));
        JSpinner currentVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCurrentValue() : 75.0, 0.0, 10000.0, 1.0));
        JTextField unitField = new JTextField(existing != null ? existing.getUnit() : "kg");

        JTextField startDateField = new JTextField(existing != null ? existing.getStartDate().toString() : LocalDate.now().toString());
        JTextField deadlineField = new JTextField(existing != null ? existing.getDeadline().toString() : LocalDate.now().plusMonths(3).toString());
        JComboBox<GoalStatus> statusCombo = new JComboBox<>(GoalStatus.values());
        if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

        JTextField descField = new JTextField(existing != null ? existing.getDescription() : "Progressive overload on flat bench");

        container.add(wrapField("GOAL TITLE", titleField));
        container.add(wrapField("CATEGORY", categoryCombo));
        container.add(wrapField("STARTING VALUE", startVal));
        container.add(wrapField("TARGET VALUE", targetVal));
        container.add(wrapField("CURRENT VALUE", currentVal));
        container.add(wrapField("UNIT (KG, CM, DAYS)", unitField));
        container.add(wrapField("START DATE (YYYY-MM-DD)", startDateField));
        container.add(wrapField("DEADLINE (YYYY-MM-DD)", deadlineField));
        container.add(wrapField("STATUS", statusCombo));
        container.add(wrapField("DESCRIPTION / NOTES", descField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton(existing == null ? "Create Goal" : "Save Changes", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                String t = titleField.getText().trim();
                if (t.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a goal title.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                FitnessGoal goal = existing != null ? existing : new FitnessGoal();
                goal.setUserId(user.getId());
                goal.setTitle(t);
                goal.setDescription(descField.getText().trim());
                goal.setCategory((GoalCategory) categoryCombo.getSelectedItem());
                goal.setStartingValue(((Number) startVal.getValue()).doubleValue());
                goal.setTargetValue(((Number) targetVal.getValue()).doubleValue());
                goal.setCurrentValue(((Number) currentVal.getValue()).doubleValue());
                goal.setUnit(unitField.getText().trim());
                goal.setStartDate(LocalDate.parse(startDateField.getText().trim()));
                goal.setDeadline(LocalDate.parse(deadlineField.getText().trim()));
                goal.setStatus((GoalStatus) statusCombo.getSelectedItem());

                if (existing == null) {
                    goalService.createGoal(goal);
                } else {
                    goalService.updateGoal(goal);
                }

                dialog.dispose();
                refreshGoalsAndBadges();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Goal saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(container, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static class GoalData {
        final List<FitnessGoal> goals;
        final List<AchievementService.AchievementBadgeStatus> badges;

        public GoalData(List<FitnessGoal> goals, List<AchievementService.AchievementBadgeStatus> badges) {
            this.goals = goals;
            this.badges = badges;
        }
    }
}

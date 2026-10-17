package com.pgtr.view;

import com.pgtr.model.Exercise;
import com.pgtr.model.User;
import com.pgtr.model.Workout;
import com.pgtr.service.UserSession;
import com.pgtr.service.WorkoutService;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;
import com.pgtr.view.components.StopwatchTimerPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete Gym Workout Management module featuring workout logger,
 * exercise builder, live stopwatch & rest timers, volume analytics, and search/filter.
 */
public class WorkoutPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WorkoutService workoutService;

    // Filters & Search
    private JTextField searchField;
    private JComboBox<String> muscleFilterCombo;

    // Workouts Table
    private DefaultTableModel tableModel;
    private DarkTable workoutTable;
    private List<Workout> currentWorkoutsList = new ArrayList<>();

    // Timer Component
    private StopwatchTimerPanel timerPanel;

    public WorkoutPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.workoutService = new WorkoutService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        // Top Section: Header & Live Timer Panel
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header + Add Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("GYM WORKOUT MANAGEMENT");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Log sessions, track progressive overload volume, and time your rests");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        NeonButton addWorkoutBtn = new NeonButton("+ Record Workout", NeonButton.ButtonType.PRIMARY);
        addWorkoutBtn.addActionListener(e -> showWorkoutDialog(null));
        headerActions.add(addWorkoutBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Integrated Stopwatch and Rest Timer
        timerPanel = new StopwatchTimerPanel();
        topContainer.add(timerPanel, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);

        // Center Section: Search/Filter Bar + Workouts Table
        ModernCard tableCard = new ModernCard(16);
        tableCard.setLayout(new BorderLayout(0, 12));

        // Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout(12, 0));
        filterBar.setOpaque(false);

        JPanel searchWrap = new JPanel(new BorderLayout(6, 0));
        searchWrap.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍 ");
        searchIcon.setForeground(Theme.TEXT_MUTED);
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 34));
        searchWrap.add(searchIcon, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        filterBar.add(searchWrap, BorderLayout.WEST);

        JPanel comboWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        comboWrap.setOpaque(false);

        JLabel filterLabel = new JLabel("Muscle Group:");
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
            refreshWorkouts();
        });
        comboWrap.add(resetBtn);

        filterBar.add(comboWrap, BorderLayout.EAST);
        tableCard.add(filterBar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{
                "Date", "Workout Name", "Muscle Group", "Duration", "Calories", "Total Volume", "Exercises Summary"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        workoutTable = new DarkTable(tableModel);
        tableCard.add(DarkTable.createScrollPane(workoutTable), BorderLayout.CENTER);

        // Action Buttons Row below table
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableActions.setOpaque(false);

        NeonButton viewDetailsBtn = new NeonButton("View Details & Exercises", NeonButton.ButtonType.OUTLINE);
        NeonButton editBtn = new NeonButton("Edit Workout", NeonButton.ButtonType.SECONDARY);
        NeonButton deleteBtn = new NeonButton("Delete Workout", NeonButton.ButtonType.DANGER);

        viewDetailsBtn.addActionListener(e -> handleViewDetails());
        editBtn.addActionListener(e -> handleEditWorkout());
        deleteBtn.addActionListener(e -> handleDeleteWorkout());

        tableActions.add(viewDetailsBtn);
        tableActions.add(editBtn);
        tableActions.add(deleteBtn);
        tableCard.add(tableActions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);

        searchField.addActionListener(e -> performSearch());
        muscleFilterCombo.addActionListener(e -> performSearch());
    }

    public void refreshWorkouts() {
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
                    populateTable(currentWorkoutsList);
                } catch (Exception ex) {
                    System.err.println("[WorkoutPanel] Error loading workouts: " + ex.getMessage());
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
                    populateTable(currentWorkoutsList);
                } catch (Exception ex) {
                    System.err.println("[WorkoutPanel] Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void populateTable(List<Workout> list) {
        tableModel.setRowCount(0);
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
                exSummary.append("No exercises logged");
            }

            tableModel.addRow(new Object[]{
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
        int row = workoutTable.getSelectedRow();
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
            sb.append("\nWorkout Notes:\n").append(w.getNotes()).append("\n");
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
        showWorkoutDialog(w);
    }

    private void handleDeleteWorkout() {
        Workout w = getSelectedWorkout();
        if (w == null) {
            JOptionPane.showMessageDialog(this, "Please select a workout to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + w.getWorkoutName() + "' on " + DateUtil.formatDate(w.getWorkoutDate()) + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                workoutService.deleteWorkout(w.getId(), w.getUserId());
                refreshWorkouts();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(this, "Workout deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting workout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Modal dialog for creating or updating a workout session along with individual exercises.
     */
    private void showWorkoutDialog(Workout existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Record New Gym Workout" : "Edit Gym Workout", true);
        dialog.setSize(680, 620);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BorderLayout(0, 12));

        // Header inputs
        JPanel headerInputs = new JPanel(new GridLayout(2, 3, 10, 8));
        headerInputs.setOpaque(false);

        JTextField nameField = new JTextField(existing != null ? existing.getWorkoutName() : "Push Day — Chest & Shoulders");
        JComboBox<String> muscleCombo = new JComboBox<>(new String[]{"Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Cardio"});
        if (existing != null) muscleCombo.setSelectedItem(existing.getMuscleGroup());

        JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getDurationMinutes() : timerPanel.getElapsedMinutes(), 1, 300, 5));
        JSpinner caloriesSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCaloriesBurned() : 250, 0, 3000, 25));
        JTextField dateField = new JTextField(existing != null ? existing.getWorkoutDate().toString() : LocalDate.now().toString());

        headerInputs.add(wrapField("WORKOUT NAME", nameField));
        headerInputs.add(wrapField("MUSCLE GROUP", muscleCombo));
        headerInputs.add(wrapField("DATE (YYYY-MM-DD)", dateField));
        headerInputs.add(wrapField("DURATION (MINUTES)", durationSpinner));
        headerInputs.add(wrapField("CALORIES BURNED (EST)", caloriesSpinner));

        container.add(headerInputs, BorderLayout.NORTH);

        // Exercises Builder Section
        JPanel exercisesSection = new JPanel(new BorderLayout(0, 8));
        exercisesSection.setOpaque(false);

        JPanel exHeader = new JPanel(new BorderLayout());
        exHeader.setOpaque(false);
        JLabel exTitle = new JLabel("EXERCISES, SETS & REPS");
        exTitle.setFont(Theme.BODY_BOLD);
        exTitle.setForeground(Theme.PRIMARY);
        exHeader.add(exTitle, BorderLayout.WEST);

        DefaultTableModel exTableModel = new DefaultTableModel(new String[]{"Exercise Name", "Type", "Sets", "Reps", "Weight (kg)", "Volume (kg)"}, 0);
        DarkTable exTable = new DarkTable(exTableModel);

        // Load existing exercises if any
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
        presetExerciseCombo.setPreferredSize(new Dimension(180, 30));

        JComboBox<String> exTypeCombo = new JComboBox<>(new String[]{"Strength", "Hypertrophy", "Endurance", "Cardio"});
        exTypeCombo.setPreferredSize(new Dimension(100, 30));

        JSpinner setsSp = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
        setsSp.setPreferredSize(new Dimension(45, 30));

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

        // Notes and Save / Cancel
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
        bottomPanel.setOpaque(false);

        JTextField notesField = new JTextField(existing != null ? existing.getNotes() : "");
        bottomPanel.add(wrapField("SESSION NOTES", notesField), BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancelBtn = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancelBtn.addActionListener(e -> dialog.dispose());

        NeonButton saveBtn = new NeonButton(existing == null ? "Save & Log Workout" : "Save Changes", NeonButton.ButtonType.PRIMARY);
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
                refreshWorkouts();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Workout saved successfully! Volume: " +
                        String.format("%.0f kg", workout.getTotalVolume()), "Workout Logged", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving workout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);
        bottomPanel.add(btnBar, BorderLayout.SOUTH);

        container.add(bottomPanel, BorderLayout.SOUTH);
        dialog.add(container, BorderLayout.CENTER);

        // Actions for Exercises Builder
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
}

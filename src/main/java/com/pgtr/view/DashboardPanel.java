package com.pgtr.view;

import com.pgtr.model.BodyMeasurement;
import com.pgtr.model.Meal;
import com.pgtr.model.NutritionPlanItem;
import com.pgtr.model.Reminder;
import com.pgtr.model.ReminderType;
import com.pgtr.model.User;
import com.pgtr.model.Workout;
import com.pgtr.service.*;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;
import com.pgtr.view.components.NeonButton;
import com.pgtr.view.components.StatCard;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Main dashboard displaying personalized fitness status, progress bars, real-time charts, and quick logs.
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final WorkoutService workoutService;
    private final NutritionService nutritionService;
    private final HealthService healthService;
    private final HabitService habitService;
    private final GoalService goalService;
    private final ReportService reportService;
    private final ReminderService reminderService;

    // Metric Stat Cards
    private StatCard weightCard;
    private StatCard caloriesInCard;
    private StatCard caloriesOutCard;
    private StatCard proteinCard;
    private StatCard waterCard;
    private StatCard carbsCard;
    private StatCard workoutsThisWeekCard;
    private StatCard streakCard;

    // Reminders Widget
    private ModernCard remindersCard;
    private JPanel remindersContainer;

    // Progress Bars
    private ModernProgressBar calorieProgressBar;
    private JLabel calorieProgressLabel;
    private ModernProgressBar proteinProgressBar;
    private JLabel proteinProgressLabel;
    private ModernProgressBar waterProgressBar;
    private JLabel waterProgressLabel;

    // Chart Containers
    private JPanel weeklyActivityChartPanel;
    private JPanel weightChartPanel;

    // Tables
    private DefaultTableModel recentWorkoutsModel;
    private DefaultTableModel recentMealsModel;
    private JLabel mealCompletionBadgeLabel;
    private List<NutritionPlanItem> currentDashboardPlannedMeals = new java.util.ArrayList<>();

    // Empty State Panel
    private JPanel emptyStatePanel;
    private JPanel dashboardContentPanel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.workoutService = new WorkoutService();
        this.nutritionService = new NutritionService();
        this.healthService = new HealthService();
        this.habitService = new HabitService();
        this.goalService = new GoalService();
        this.reportService = new ReportService();
        this.reminderService = new ReminderService();

        setOpaque(false);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        dashboardContentPanel = new JPanel();
        dashboardContentPanel.setLayout(new BoxLayout(dashboardContentPanel, BoxLayout.Y_AXIS));
        dashboardContentPanel.setOpaque(false);
        dashboardContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Welcome Banner
        JPanel banner = createWelcomeBanner();
        dashboardContentPanel.add(banner);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 2. Metrics Grid (4 columns x 2 rows)
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 16, 16));
        statsGrid.setOpaque(false);

        weightCard = new StatCard("⚖️", "CURRENT WEIGHT", "-- kg", "Target: -- kg", Theme.PRIMARY);
        caloriesInCard = new StatCard("🔥", "CALORIES TODAY", "-- kcal", "Target: -- kcal", Theme.CALORIE_ORANGE);
        caloriesOutCard = new StatCard("⚡", "CALORIES BURNED", "-- kcal", "Through recorded workouts", Theme.PRIMARY);
        proteinCard = new StatCard("🥩", "PROTEIN INTAKE", "-- g", "Target: -- g", Theme.PROTEIN_BLUE);

        waterCard = new StatCard("💧", "WATER INTAKE", "-- ml", "Daily target: 3000 ml", Theme.WATER_CYAN);
        carbsCard = new StatCard("🌾", "CARBS INTAKE", "-- g", "Daily energy fuel", Theme.GOLD);
        workoutsThisWeekCard = new StatCard("🏋️", "WORKOUTS THIS WEEK", "--", "Active sessions", Theme.PRIMARY);
        streakCard = new StatCard("🔥", "WORKOUT STREAK", "-- Days", "Iron consistency", Theme.WARNING);

        statsGrid.add(weightCard);
        statsGrid.add(caloriesInCard);
        statsGrid.add(caloriesOutCard);
        statsGrid.add(proteinCard);
        statsGrid.add(waterCard);
        statsGrid.add(carbsCard);
        statsGrid.add(workoutsThisWeekCard);
        statsGrid.add(streakCard);

        dashboardContentPanel.add(statsGrid);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 3. Today's Reminders & Alerts Card
        remindersCard = createRemindersCard();
        dashboardContentPanel.add(remindersCard);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 4. Daily Target Progress Bars Card
        ModernCard progressCard = createProgressCard();
        dashboardContentPanel.add(progressCard);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 4. Charts Split Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);

        weeklyActivityChartPanel = new ModernCard(8);
        weeklyActivityChartPanel.setPreferredSize(new Dimension(0, 280));

        weightChartPanel = new ModernCard(8);
        weightChartPanel.setPreferredSize(new Dimension(0, 280));

        chartsRow.add(weeklyActivityChartPanel);
        chartsRow.add(weightChartPanel);
        dashboardContentPanel.add(chartsRow);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 5. Recent Activity Split Row (Workouts & Meals)
        JPanel tablesRow = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesRow.setOpaque(false);

        tablesRow.add(createRecentWorkoutsCard());
        tablesRow.add(createRecentMealsCard());
        dashboardContentPanel.add(tablesRow);

        // Empty state container
        emptyStatePanel = createEmptyStatePanel();

        JScrollPane scrollPane = new JScrollPane(dashboardContentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomeBanner() {
        ModernCard banner = new ModernCard(18);
        banner.setLayout(new BorderLayout());
        banner.setBackgroundColor(new Color(0x13, 0x1B, 0x2A));
        banner.setBorderColor(Theme.BORDER_ACCENT);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel greeting = new JLabel("Welcome to Vyntrix Command Center");
        greeting.setFont(Theme.TITLE_LARGE);
        greeting.setForeground(Theme.TEXT_PRIMARY);

        JLabel quote = new JLabel("“Redefine your limits. Consistency is the foundation of athletic excellence.”");
        quote.setFont(Theme.BODY);
        quote.setForeground(Theme.GOLD);

        left.add(greeting);
        left.add(quote);
        banner.add(left, BorderLayout.WEST);

        // Status indicator pill
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusBox.setOpaque(false);
        JLabel statusPill = new JLabel("● COMMAND CENTER ACTIVE");
        statusPill.setFont(Theme.SMALL);
        statusPill.setForeground(Theme.PRIMARY);
        statusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        statusBox.add(statusPill);
        banner.add(statusBox, BorderLayout.EAST);

        return banner;
    }

    private ModernCard createProgressCard() {
        ModernCard card = new ModernCard(18);
        card.setLayout(new GridLayout(3, 1, 0, 12));

        // Calorie Progress
        JPanel calPanel = new JPanel(new BorderLayout(10, 4));
        calPanel.setOpaque(false);
        JLabel calTitle = new JLabel("Daily Calories Progress");
        calTitle.setFont(Theme.BODY_BOLD);
        calTitle.setForeground(Theme.TEXT_PRIMARY);
        calorieProgressLabel = new JLabel("0 / 2400 kcal (0%)");
        calorieProgressLabel.setFont(Theme.SMALL);
        calorieProgressLabel.setForeground(Theme.TEXT_MUTED);
        calPanel.add(calTitle, BorderLayout.WEST);
        calPanel.add(calorieProgressLabel, BorderLayout.EAST);
        calorieProgressBar = new ModernProgressBar(0, 2400, Theme.CALORIE_ORANGE);
        calPanel.add(calorieProgressBar, BorderLayout.SOUTH);
        card.add(calPanel);

        // Protein Progress
        JPanel protPanel = new JPanel(new BorderLayout(10, 4));
        protPanel.setOpaque(false);
        JLabel protTitle = new JLabel("Daily Protein Target");
        protTitle.setFont(Theme.BODY_BOLD);
        protTitle.setForeground(Theme.TEXT_PRIMARY);
        proteinProgressLabel = new JLabel("0 / 150 g (0%)");
        proteinProgressLabel.setFont(Theme.SMALL);
        proteinProgressLabel.setForeground(Theme.TEXT_MUTED);
        protPanel.add(protTitle, BorderLayout.WEST);
        protPanel.add(proteinProgressLabel, BorderLayout.EAST);
        proteinProgressBar = new ModernProgressBar(0, 150, Theme.PRIMARY);
        protPanel.add(proteinProgressBar, BorderLayout.SOUTH);
        card.add(protPanel);

        // Water Progress
        JPanel waterPanel = new JPanel(new BorderLayout(10, 4));
        waterPanel.setOpaque(false);
        JLabel waterTitle = new JLabel("Daily Water Intake");
        waterTitle.setFont(Theme.BODY_BOLD);
        waterTitle.setForeground(Theme.TEXT_PRIMARY);
        waterProgressLabel = new JLabel("0 / 3000 ml (0%)");
        waterProgressLabel.setFont(Theme.SMALL);
        waterProgressLabel.setForeground(Theme.TEXT_MUTED);
        waterPanel.add(waterTitle, BorderLayout.WEST);
        waterPanel.add(waterProgressLabel, BorderLayout.EAST);
        waterProgressBar = new ModernProgressBar(0, 3000, Theme.WATER_CYAN);
        waterPanel.add(waterProgressBar, BorderLayout.SOUTH);
        card.add(waterPanel);

        return card;
    }

    private ModernCard createRecentWorkoutsCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("RECENT WORKOUTS");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        NeonButton viewAll = new NeonButton("View All", NeonButton.ButtonType.SECONDARY);
        viewAll.setFont(Theme.SMALL);
        viewAll.addActionListener(e -> mainFrame.showView("WORKOUTS"));
        top.add(viewAll, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        recentWorkoutsModel = new DefaultTableModel(new String[]{"Date", "Workout", "Muscle Group", "Duration", "Volume"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        DarkTable table = new DarkTable(recentWorkoutsModel);
        card.add(DarkTable.createScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private ModernCard createRecentMealsCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel leftBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftBox.setOpaque(false);
        JLabel title = new JLabel("TODAY'S MEAL SCHEDULE & COMPLETION");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        leftBox.add(title);

        mealCompletionBadgeLabel = new JLabel("● Completed: 0 / 0 Planned Meals (0%)");
        mealCompletionBadgeLabel.setFont(Theme.BODY_BOLD);
        mealCompletionBadgeLabel.setForeground(Theme.GOLD);
        leftBox.add(mealCompletionBadgeLabel);

        top.add(leftBox, BorderLayout.WEST);

        NeonButton viewAll = new NeonButton("Manage Plan & Meals", NeonButton.ButtonType.SECONDARY);
        viewAll.setFont(Theme.SMALL);
        viewAll.addActionListener(e -> mainFrame.showView("NUTRITION"));
        top.add(viewAll, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        recentMealsModel = new DefaultTableModel(new String[]{
                "Done", "Status", "Time", "Meal Type", "Food / Meal", "Calories", "Protein"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Boolean.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0; // Checkbox is directly interactive
            }

            @Override
            public void setValueAt(Object aValue, int row, int col) {
                super.setValueAt(aValue, row, col);
                if (col == 0 && currentDashboardPlannedMeals != null && row >= 0 && row < currentDashboardPlannedMeals.size()) {
                    boolean done = Boolean.TRUE.equals(aValue);
                    NutritionPlanItem item = currentDashboardPlannedMeals.get(row);
                    item.setConsumed(done);
                    handleDashboardMealCompletionToggled(item, done);
                }
            }
        };

        DarkTable table = new DarkTable(recentMealsModel);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(0).setMaxWidth(60);
            table.getColumnModel().getColumn(1).setPreferredWidth(115);
            table.getColumnModel().getColumn(1).setMaxWidth(130);
            table.getColumnModel().getColumn(2).setPreferredWidth(85);
            table.getColumnModel().getColumn(2).setMaxWidth(100);
        }

        // Custom Renderer for Status column to highlight completion in green vs pending in amber
        table.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                String str = value != null ? value.toString() : "";
                if (str.contains("Completed") || str.contains("Logged") || str.contains("Custom")) {
                    comp.setForeground(Theme.PRIMARY);
                    setFont(Theme.BODY_BOLD);
                } else {
                    comp.setForeground(Theme.GOLD);
                    setFont(Theme.BODY);
                }
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Theme.BG_PANEL : Theme.BG_PANEL_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return comp;
            }
        });

        card.add(DarkTable.createScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void handleDashboardMealCompletionToggled(NutritionPlanItem item, boolean done) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (done) {
                    nutritionService.logPlannedMealAsConsumed(item, LocalDate.now());
                } else {
                    nutritionService.unlogPlannedMeal(item, LocalDate.now());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshDashboard();
                    if (mainFrame != null && mainFrame.getNutritionPanel() != null) {
                        mainFrame.getNutritionPanel().refreshNutrition();
                    }
                } catch (Exception ex) {
                    System.err.println("[DashboardPanel] Error toggling meal completion: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private ModernCard createRemindersCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBox.setOpaque(false);
        JLabel icon = new JLabel("🔔");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        JLabel title = new JLabel("TODAY'S ACTIVE REMINDERS & ALERTS");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.PRIMARY);
        titleBox.add(icon);
        titleBox.add(title);
        top.add(titleBox, BorderLayout.WEST);

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);

        NeonButton addReminderBtn = new NeonButton("+ Add Reminder", NeonButton.ButtonType.GOLD);
        addReminderBtn.setFont(Theme.SMALL);
        addReminderBtn.addActionListener(e -> showAddReminderDialog());
        actionBox.add(addReminderBtn);

        top.add(actionBox, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        remindersContainer = new JPanel();
        remindersContainer.setLayout(new BoxLayout(remindersContainer, BoxLayout.Y_AXIS));
        remindersContainer.setOpaque(false);

        card.add(remindersContainer, BorderLayout.CENTER);
        return card;
    }

    private void renderReminders(List<Reminder> reminders) {
        if (remindersContainer == null) return;
        remindersContainer.removeAll();

        if (reminders == null || reminders.isEmpty()) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
            empty.setOpaque(false);
            JLabel emptyLabel = new JLabel("✨ No active reminders right now. All caught up with your fitness schedule!");
            emptyLabel.setFont(Theme.BODY);
            emptyLabel.setForeground(Theme.TEXT_MUTED);
            empty.add(emptyLabel);

            NeonButton quickBtn = new NeonButton("+ Quick Workout Reminder", NeonButton.ButtonType.SECONDARY);
            quickBtn.setFont(Theme.SMALL);
            quickBtn.addActionListener(e -> {
                User u = UserSession.getInstance().getCurrentUser();
                if (u != null) {
                    try {
                        Reminder r = new Reminder(null, u.getId(), ReminderType.WORKOUT,
                                "Daily Workout Session", "Stay consistent with your weekly routine!",
                                LocalTime.of(18, 0), true, false);
                        reminderService.createReminder(r);
                        refreshDashboard();
                        mainFrame.updateRemindersCount();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error adding reminder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            empty.add(quickBtn);
            remindersContainer.add(empty);
        } else {
            for (Reminder r : reminders) {
                JPanel item = new JPanel(new BorderLayout(12, 0));
                item.setBackground(new Color(0x13, 0x1B, 0x2A));
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                left.setOpaque(false);

                JLabel typeIcon = new JLabel(getReminderEmoji(r.getReminderType()));
                typeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                left.add(typeIcon);

                JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
                text.setOpaque(false);

                JLabel titleLabel = new JLabel(r.getTitle() + " • " + DateUtil.formatTime(r.getReminderTime()));
                titleLabel.setFont(Theme.BODY_BOLD);
                titleLabel.setForeground(Theme.TEXT_PRIMARY);

                JLabel msgLabel = new JLabel(r.getMessage());
                msgLabel.setFont(Theme.SMALL);
                msgLabel.setForeground(Theme.TEXT_MUTED);

                text.add(titleLabel);
                text.add(msgLabel);
                left.add(text);
                item.add(left, BorderLayout.CENTER);

                NeonButton dismissBtn = new NeonButton("Dismiss ✓", NeonButton.ButtonType.SECONDARY);
                dismissBtn.setFont(Theme.SMALL);
                dismissBtn.addActionListener(e -> {
                    User u = UserSession.getInstance().getCurrentUser();
                    if (u != null) {
                        try {
                            reminderService.dismissReminder(r.getId(), u.getId());
                            refreshDashboard();
                            mainFrame.updateRemindersCount();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                item.add(dismissBtn, BorderLayout.EAST);

                remindersContainer.add(item);
                remindersContainer.add(Box.createVerticalStrut(8));
            }
        }

        remindersContainer.revalidate();
        remindersContainer.repaint();
    }

    private String getReminderEmoji(ReminderType type) {
        if (type == null) return "🔔";
        switch (type) {
            case WORKOUT: return "🏋️";
            case MEAL: return "🥗";
            case HYDRATION: return "💧";
            case CUSTOM: return "⚡";
            case SLEEP: return "🌙";
            default: return "🔔";
        }
    }

    private void showAddReminderDialog() {
        User u = UserSession.getInstance().getCurrentUser();
        if (u == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Daily Reminder", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(18);
        container.setLayout(new GridLayout(0, 1, 10, 10));

        JTextField titleField = new JTextField("Workout Session");
        JComboBox<ReminderType> typeCombo = new JComboBox<>(ReminderType.values());
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(18, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        JTextField msgField = new JTextField("Stay consistent with your goals today!");

        JPanel timeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        timeRow.setOpaque(false);
        timeRow.add(wrapField("HOUR (0-23)", hourSpinner));
        timeRow.add(wrapField("MINUTE (0-59)", minuteSpinner));

        container.add(wrapField("REMINDER TITLE", titleField));
        container.add(wrapField("REMINDER CATEGORY", typeCombo));
        container.add(timeRow);
        container.add(wrapField("MESSAGE / MOTIVATION", msgField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton("Create Reminder", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a reminder title.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int hr = (int) hourSpinner.getValue();
            int min = (int) minuteSpinner.getValue();
            Reminder r = new Reminder(null, u.getId(), (ReminderType) typeCombo.getSelectedItem(),
                    title, msgField.getText().trim(), LocalTime.of(hr, min), true, false);
            try {
                reminderService.createReminder(r);
                dialog.dispose();
                refreshDashboard();
                mainFrame.updateRemindersCount();
                JOptionPane.showMessageDialog(this, "Reminder scheduled successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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

    private JPanel createEmptyStatePanel() {
        ModernCard card = new ModernCard(32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🏋️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Welcome to Vyntrix!", SwingConstants.CENTER);
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel desc = new JLabel("<html><center style='color:#94A3B8; font-size:13px;'>" +
                "Your journey starts today. Record your first workout, log your morning meal,<br>" +
                "or log your current body measurements to begin seeing live metrics and charts." +
                "</center></html>", SwingConstants.CENTER);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(desc);
        card.add(Box.createVerticalStrut(24));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);

        NeonButton b1 = new NeonButton("Log First Workout", NeonButton.ButtonType.PRIMARY);
        b1.addActionListener(e -> mainFrame.showView("WORKOUTS"));

        NeonButton b2 = new NeonButton("Log First Meal", NeonButton.ButtonType.SECONDARY);
        b2.addActionListener(e -> mainFrame.showView("NUTRITION"));

        NeonButton b3 = new NeonButton("Record Weight", NeonButton.ButtonType.SECONDARY);
        b3.addActionListener(e -> mainFrame.showView("HEALTH"));

        btnRow.add(b1);
        btnRow.add(b2);
        btnRow.add(b3);
        card.add(btnRow);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);
        return wrapper;
    }

    /**
     * Refreshes dashboard statistics and charts asynchronously from the database.
     */
    public void refreshDashboard() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                String uid = user.getId();
                LocalDate today = LocalDate.now();

                // 1. Weight & Latest measurement
                Optional<BodyMeasurement> latestMeasure = healthService.getLatestMeasurement(uid);
                double currentWeight = latestMeasure.map(BodyMeasurement::getWeightKg).orElse(user.getCurrentWeightKg());

                // 2. Nutrition & Planned Meals
                NutritionService.DailyNutritionSummary nutSummary = nutritionService.getDailySummary(uid, today);
                List<Meal> todayMeals = nutritionService.getMealsForDate(uid, today);
                String activeMode = UserSession.getInstance().getActiveNutritionMode();
                List<NutritionPlanItem> plannedMeals = nutritionService.getNutritionPlan(uid, activeMode);

                // 3. Workouts
                List<Workout> recentWorkouts = workoutService.getRecentWorkouts(uid, 5);
                int workoutsThisWeek = workoutService.getWorkoutsThisWeek(uid);
                int streak = workoutService.calculateCurrentStreak(uid);

                // Calculate calories burned today from workouts
                List<Workout> todayWorkouts = workoutService.getUserWorkouts(uid);
                int caloriesBurnedToday = 0;
                for (Workout w : todayWorkouts) {
                    if (w.getWorkoutDate().equals(today)) {
                        caloriesBurnedToday += w.getCaloriesBurned();
                    }
                }

                // 4. Water
                int waterToday = habitService.getTodayWaterTotal(uid);

                // 5. Active Reminders
                List<Reminder> activeReminders = reminderService.getActiveReminders(uid);

                // 6. Charts
                JFreeChart activityChart = reportService.createWeeklyActivityChart(uid);
                JFreeChart weightChart = reportService.createWeightProgressChart(uid, user.getTargetWeightKg());

                return new DashboardData(currentWeight, nutSummary, todayMeals, plannedMeals, recentWorkouts,
                        workoutsThisWeek, streak, caloriesBurnedToday, waterToday,
                        activeReminders, activityChart, weightChart);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();

                    // Update Metric Cards
                    weightCard.setValue(String.format("%.1f kg", data.currentWeight));
                    weightCard.setSubtext(String.format("Target: %.1f kg (%s)", user.getTargetWeightKg(),
                            data.currentWeight > user.getTargetWeightKg() ?
                                    String.format("-%.1f kg", data.currentWeight - user.getTargetWeightKg()) :
                                    String.format("+%.1f kg", user.getTargetWeightKg() - data.currentWeight)));

                    caloriesInCard.setValue(String.format("%.0f kcal", data.nutSummary.totalCalories));
                    caloriesInCard.setSubtext(String.format("Goal: %d kcal", user.getDailyCalorieTarget()));

                    caloriesOutCard.setValue(String.format("%d kcal", data.caloriesBurnedToday));
                    proteinCard.setValue(String.format("%.0f g", data.nutSummary.totalProtein));
                    proteinCard.setSubtext(String.format("Target: %d g", user.getDailyProteinTarget()));

                    waterCard.setValue(String.format("%d ml", data.waterToday));
                    waterCard.setSubtext(String.format("Target: %d ml", user.getDailyWaterTarget()));

                    carbsCard.setValue(String.format("%.0f g", data.nutSummary.totalCarbs));
                    carbsCard.setSubtext("Tracked today");
                    workoutsThisWeekCard.setValue(String.valueOf(data.workoutsThisWeek));
                    streakCard.setValue(data.streak + " Days");

                    // Update Top Bar Streak badge
                    mainFrame.updateStreak(data.streak);

                    // Update Reminders widget
                    renderReminders(data.activeReminders);

                    // Update Progress Bars
                    int calTarget = Math.max(1, user.getDailyCalorieTarget());
                    calorieProgressBar.setProgress((int) data.nutSummary.totalCalories, calTarget);
                    int calPct = (int) Math.round((data.nutSummary.totalCalories / calTarget) * 100.0);
                    calorieProgressLabel.setText(String.format("%.0f / %d kcal (%d%%)", data.nutSummary.totalCalories, calTarget, calPct));

                    int protTarget = Math.max(1, user.getDailyProteinTarget());
                    proteinProgressBar.setProgress((int) data.nutSummary.totalProtein, protTarget);
                    int protPct = (int) Math.round((data.nutSummary.totalProtein / protTarget) * 100.0);
                    proteinProgressLabel.setText(String.format("%.0f / %d g (%d%%)", data.nutSummary.totalProtein, protTarget, protPct));

                    int waterTarget = Math.max(1, user.getDailyWaterTarget());
                    waterProgressBar.setProgress(data.waterToday, waterTarget);
                    int waterPct = (int) Math.round(((double) data.waterToday / waterTarget) * 100.0);
                    waterProgressLabel.setText(String.format("%d / %d ml (%d%%)", data.waterToday, waterTarget, waterPct));

                    // Update Charts
                    weeklyActivityChartPanel.removeAll();
                    weeklyActivityChartPanel.add(new ChartPanel(data.activityChart), BorderLayout.CENTER);
                    weeklyActivityChartPanel.revalidate();
                    weeklyActivityChartPanel.repaint();

                    weightChartPanel.removeAll();
                    weightChartPanel.add(new ChartPanel(data.weightChart), BorderLayout.CENTER);
                    weightChartPanel.revalidate();
                    weightChartPanel.repaint();

                    // Update Recent Workouts Table
                    recentWorkoutsModel.setRowCount(0);
                    for (Workout w : data.recentWorkouts) {
                        recentWorkoutsModel.addRow(new Object[]{
                                DateUtil.formatDate(w.getWorkoutDate()),
                                w.getWorkoutName(),
                                w.getMuscleGroup(),
                                w.getDurationMinutes() + " min",
                                String.format("%.0f kg", w.getTotalVolume())
                        });
                    }

                    // Update Recent Meals Table & Planned Completion Status
                    currentDashboardPlannedMeals = data.plannedMeals != null ? data.plannedMeals : new java.util.ArrayList<>();
                    recentMealsModel.setRowCount(0);

                    int completedPlannedCount = 0;
                    int totalPlanned = currentDashboardPlannedMeals.size();

                    for (NutritionPlanItem p : currentDashboardPlannedMeals) {
                        boolean isDone = p.isConsumed();
                        if (isDone) completedPlannedCount++;

                        recentMealsModel.addRow(new Object[]{
                                Boolean.valueOf(isDone),
                                isDone ? "✓ Completed" : "⏳ Pending",
                                p.getMealTime(),
                                p.getMealType().getDisplayName(),
                                p.getFoodName() + " (" + p.getServingSize() + ")",
                                String.format("%.0f kcal", p.getCalories()),
                                String.format("%.1f g", p.getProteinG())
                        });
                    }

                    // Any additional custom meals logged today that aren't in the plan
                    if (data.todayMeals != null) {
                        for (Meal m : data.todayMeals) {
                            boolean isFromPlan = false;
                            for (NutritionPlanItem p : currentDashboardPlannedMeals) {
                                String foodDesc = p.getFoodName() + " (" + p.getServingSize() + ")";
                                if (foodDesc.equalsIgnoreCase(m.getFoodName()) && p.getMealType() == m.getMealType()) {
                                    isFromPlan = true;
                                    break;
                                }
                            }
                            if (!isFromPlan) {
                                recentMealsModel.addRow(new Object[]{
                                        Boolean.TRUE,
                                        "✓ Custom Log",
                                        DateUtil.formatTime(m.getMealTime()),
                                        m.getMealType().getDisplayName(),
                                        m.getFoodName(),
                                        String.format("%.0f kcal", m.getCalories()),
                                        String.format("%.1f g", m.getProteinG())
                                });
                            }
                        }
                    }

                    // Update Completion Badge
                    if (totalPlanned > 0) {
                        int pct = (completedPlannedCount * 100) / totalPlanned;
                        if (completedPlannedCount == totalPlanned) {
                            mealCompletionBadgeLabel.setText(String.format("● Completed: %d / %d Planned Meals (100%%) 🔥 All Done!", completedPlannedCount, totalPlanned));
                            mealCompletionBadgeLabel.setForeground(Theme.PRIMARY);
                        } else {
                            mealCompletionBadgeLabel.setText(String.format("● Completed: %d / %d Planned Meals (%d%%)", completedPlannedCount, totalPlanned, pct));
                            mealCompletionBadgeLabel.setForeground(Theme.GOLD);
                        }
                    } else {
                        mealCompletionBadgeLabel.setText("● " + (data.todayMeals != null ? data.todayMeals.size() : 0) + " Logged Today");
                        mealCompletionBadgeLabel.setForeground(Theme.PRIMARY);
                    }

                } catch (Exception ex) {
                    System.err.println("[DashboardPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class DashboardData {
        final double currentWeight;
        final NutritionService.DailyNutritionSummary nutSummary;
        final List<Meal> todayMeals;
        final List<NutritionPlanItem> plannedMeals;
        final List<Workout> recentWorkouts;
        final int workoutsThisWeek;
        final int streak;
        final int caloriesBurnedToday;
        final int waterToday;
        final List<Reminder> activeReminders;
        final JFreeChart activityChart;
        final JFreeChart weightChart;

        public DashboardData(double currentWeight, NutritionService.DailyNutritionSummary nutSummary,
                             List<Meal> todayMeals, List<NutritionPlanItem> plannedMeals,
                             List<Workout> recentWorkouts,
                             int workoutsThisWeek, int streak, int caloriesBurnedToday,
                             int waterToday, List<Reminder> activeReminders,
                             JFreeChart activityChart, JFreeChart weightChart) {
            this.currentWeight = currentWeight;
            this.nutSummary = nutSummary;
            this.todayMeals = todayMeals;
            this.plannedMeals = plannedMeals;
            this.recentWorkouts = recentWorkouts;
            this.workoutsThisWeek = workoutsThisWeek;
            this.streak = streak;
            this.caloriesBurnedToday = caloriesBurnedToday;
            this.waterToday = waterToday;
            this.activeReminders = activeReminders;
            this.activityChart = activityChart;
            this.weightChart = weightChart;
        }
    }
}

package com.pgtr.view;

import com.pgtr.model.BodyMeasurement;
import com.pgtr.model.Meal;
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

    // Metric Stat Cards
    private StatCard weightCard;
    private StatCard caloriesInCard;
    private StatCard caloriesOutCard;
    private StatCard proteinCard;
    private StatCard waterCard;
    private StatCard sleepCard;
    private StatCard workoutsThisWeekCard;
    private StatCard streakCard;

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
        sleepCard = new StatCard("🌙", "LAST SLEEP", "-- hrs", "Recovery target: 8.0 hrs", Theme.SLEEP_PURPLE);
        workoutsThisWeekCard = new StatCard("🏋️", "WORKOUTS THIS WEEK", "--", "Active sessions", Theme.PRIMARY);
        streakCard = new StatCard("🔥", "WORKOUT STREAK", "-- Days", "Iron consistency", Theme.WARNING);

        statsGrid.add(weightCard);
        statsGrid.add(caloriesInCard);
        statsGrid.add(caloriesOutCard);
        statsGrid.add(proteinCard);
        statsGrid.add(waterCard);
        statsGrid.add(sleepCard);
        statsGrid.add(workoutsThisWeekCard);
        statsGrid.add(streakCard);

        dashboardContentPanel.add(statsGrid);
        dashboardContentPanel.add(Box.createVerticalStrut(20));

        // 3. Daily Target Progress Bars Card
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
        banner.setBackgroundColor(new Color(0x18, 0x1E, 0x1A));
        banner.setBorderColor(Theme.BORDER_ACCENT);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);

        JLabel greeting = new JLabel("Welcome back to your command center");
        greeting.setFont(Theme.TITLE_LARGE);
        greeting.setForeground(Theme.TEXT_PRIMARY);

        JLabel quote = new JLabel("“Consistency is the foundation of progress. Every set and every calorie counts.”");
        quote.setFont(Theme.BODY);
        quote.setForeground(Theme.PRIMARY);

        left.add(greeting);
        left.add(quote);
        banner.add(left, BorderLayout.WEST);

        // Quick action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        NeonButton logWorkoutBtn = new NeonButton("+ Log Workout", NeonButton.ButtonType.PRIMARY);
        logWorkoutBtn.addActionListener(e -> mainFrame.showView("WORKOUTS"));

        NeonButton logMealBtn = new NeonButton("+ Log Meal", NeonButton.ButtonType.SECONDARY);
        logMealBtn.addActionListener(e -> mainFrame.showView("NUTRITION"));

        NeonButton addWaterBtn = new NeonButton("+ Water", NeonButton.ButtonType.SECONDARY);
        addWaterBtn.addActionListener(e -> {
            try {
                User u = UserSession.getInstance().getCurrentUser();
                if (u != null) {
                    habitService.addWater(u.getId(), 250);
                    refreshDashboard();
                    JOptionPane.showMessageDialog(this, "Logged 250 ml of water! Stay hydrated.", "Hydration Logged", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error logging water: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actions.add(logWorkoutBtn);
        actions.add(logMealBtn);
        actions.add(addWaterBtn);
        banner.add(actions, BorderLayout.EAST);

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
        JLabel title = new JLabel("TODAY'S RECORDED MEALS");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        NeonButton viewAll = new NeonButton("View All", NeonButton.ButtonType.SECONDARY);
        viewAll.setFont(Theme.SMALL);
        viewAll.addActionListener(e -> mainFrame.showView("NUTRITION"));
        top.add(viewAll, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        recentMealsModel = new DefaultTableModel(new String[]{"Time", "Meal Type", "Food Name", "Calories", "Protein"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        DarkTable table = new DarkTable(recentMealsModel);
        card.add(DarkTable.createScrollPane(table), BorderLayout.CENTER);

        return card;
    }

    private JPanel createEmptyStatePanel() {
        ModernCard card = new ModernCard(32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🏋️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Welcome to Your Fitness Dashboard!", SwingConstants.CENTER);
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

                // 2. Nutrition
                NutritionService.DailyNutritionSummary nutSummary = nutritionService.getDailySummary(uid, today);
                List<Meal> todayMeals = nutritionService.getMealsForDate(uid, today);

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

                // 4. Water & Sleep
                int waterToday = habitService.getTodayWaterTotal(uid);
                Optional<com.pgtr.model.SleepLog> latestSleep = habitService.getLatestSleep(uid);
                double sleepHrs = latestSleep.map(com.pgtr.model.SleepLog::getDurationHours).orElse(0.0);

                // 5. Charts
                JFreeChart activityChart = reportService.createWeeklyActivityChart(uid);
                JFreeChart weightChart = reportService.createWeightProgressChart(uid, user.getTargetWeightKg());

                return new DashboardData(currentWeight, nutSummary, todayMeals, recentWorkouts,
                        workoutsThisWeek, streak, caloriesBurnedToday, waterToday, sleepHrs,
                        activityChart, weightChart);
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

                    sleepCard.setValue(String.format("%.1f hrs", data.sleepHrs));
                    workoutsThisWeekCard.setValue(String.valueOf(data.workoutsThisWeek));
                    streakCard.setValue(data.streak + " Days");

                    // Update Top Bar Streak badge
                    mainFrame.updateStreak(data.streak);

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

                    // Update Recent Meals Table
                    recentMealsModel.setRowCount(0);
                    for (Meal m : data.todayMeals) {
                        recentMealsModel.addRow(new Object[]{
                                DateUtil.formatTime(m.getMealTime()),
                                m.getMealType().getDisplayName(),
                                m.getFoodName(),
                                String.format("%.0f kcal", m.getCalories()),
                                String.format("%.1f g", m.getProteinG())
                        });
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
        final List<Workout> recentWorkouts;
        final int workoutsThisWeek;
        final int streak;
        final int caloriesBurnedToday;
        final int waterToday;
        final double sleepHrs;
        final JFreeChart activityChart;
        final JFreeChart weightChart;

        public DashboardData(double currentWeight, NutritionService.DailyNutritionSummary nutSummary,
                             List<Meal> todayMeals, List<Workout> recentWorkouts,
                             int workoutsThisWeek, int streak, int caloriesBurnedToday,
                             int waterToday, double sleepHrs, JFreeChart activityChart,
                             JFreeChart weightChart) {
            this.currentWeight = currentWeight;
            this.nutSummary = nutSummary;
            this.todayMeals = todayMeals;
            this.recentWorkouts = recentWorkouts;
            this.workoutsThisWeek = workoutsThisWeek;
            this.streak = streak;
            this.caloriesBurnedToday = caloriesBurnedToday;
            this.waterToday = waterToday;
            this.sleepHrs = sleepHrs;
            this.activityChart = activityChart;
            this.weightChart = weightChart;
        }
    }
}

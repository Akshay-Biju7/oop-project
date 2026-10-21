package com.pgtr.view;

import com.pgtr.model.ActivityLevel;
import com.pgtr.model.FitnessGoalType;
import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import com.pgtr.model.MealType;
import com.pgtr.model.NutritionPlanItem;
import com.pgtr.model.User;
import com.pgtr.model.WaterLog;
import com.pgtr.service.HabitService;
import com.pgtr.service.NutritionService;
import com.pgtr.service.ReportService;
import com.pgtr.service.UserSession;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;
import com.pgtr.view.components.NeonButton;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete Vyntrix Nutrition and Calorie Engine featuring:
 * 1. Independent Cut Nutrition vs Bulk Nutrition selectable modes
 * 2. Editable daily meal planner (Breakfast, Mid-morning, Lunch, Evening Snack, Dinner, Post-workout)
 * 3. Planned vs Actually Consumed tracking with "Log as Consumed" actions
 * 4. Personalized BMR/TDEE energy target calculator with medical disclaimers
 * 5. Actual consumed food logs and JFreeChart macronutrient split
 */
public class NutritionPanel extends JPanel {

    private final MainFrame mainFrame;
    private final NutritionService nutritionService;
    private final ReportService reportService;
    private final HabitService habitService;

    // Active Nutrition Mode: "CUT" or "BULK"
    private String currentPlanMode = "BULK";

    // Mode Selector Cards
    private ModernCard cutModeCard;
    private ModernCard bulkModeCard;
    private JLabel cutStatusLabel;
    private JLabel bulkStatusLabel;

    // Planned Meals Table
    private DefaultTableModel planTableModel;
    private DarkTable planTable;
    private List<NutritionPlanItem> currentPlanItems = new ArrayList<>();
    private JLabel planTotalsLabel;

    // Actual Consumed Section
    private LocalDate selectedDate = LocalDate.now();
    private JLabel dateLabel;
    private JLabel calStatLabel;
    private ModernProgressBar calProgressBar;
    private JLabel protStatLabel;
    private ModernProgressBar protProgressBar;
    private JLabel carbsStatLabel;
    private JLabel fatStatLabel;
    private JPanel chartPanel;

    // Actual Consumed Meals Table
    private DefaultTableModel consumedTableModel;
    private DarkTable consumedTable;
    private List<Meal> currentConsumedMeals = new ArrayList<>();

    // Daily Water Hydration Section
    private JLabel waterTotalStatLabel;
    private JLabel waterTargetTextLabel;
    private ModernProgressBar waterProgressBar;
    private DefaultTableModel waterTableModel;
    private DarkTable waterTable;

    public NutritionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.nutritionService = new NutritionService();
        this.reportService = new ReportService();
        this.habitService = new HabitService();
        this.currentPlanMode = UserSession.getInstance().getActiveNutritionMode();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel scrollableContainer = new JPanel();
        scrollableContainer.setLayout(new BoxLayout(scrollableContainer, BoxLayout.Y_AXIS));
        scrollableContainer.setOpaque(false);

        // 1. Top Section: Header & Action Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("NUTRITION & MACRONUTRIENT ENGINE");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Cut & Bulk meal planners • Daily macronutrient targets • Actual consumption tracking");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);

        NeonButton calcTargetsBtn = new NeonButton("Personalize Targets ⚙️", NeonButton.ButtonType.GOLD);
        calcTargetsBtn.addActionListener(e -> showPersonalizedTargetsDialog());

        NeonButton copyPrevBtn = new NeonButton("Copy Yesterday 📋", NeonButton.ButtonType.SECONDARY);
        copyPrevBtn.addActionListener(e -> handleCopyPreviousDay());

        NeonButton logCustomFoodBtn = new NeonButton("+ Log Custom Food", NeonButton.ButtonType.PRIMARY);
        logCustomFoodBtn.addActionListener(e -> showLogMealDialog(null));

        headerActions.add(calcTargetsBtn);
        headerActions.add(copyPrevBtn);
        headerActions.add(logCustomFoodBtn);
        headerPanel.add(headerActions, BorderLayout.EAST);

        scrollableContainer.add(headerPanel);
        scrollableContainer.add(Box.createVerticalStrut(16));

        // 2. Cut Mode vs Bulk Mode Selector Cards
        JPanel modeSelectorPanel = createModeSelectorCards();
        scrollableContainer.add(modeSelectorPanel);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 3. Meal Plan Interface (Cut or Bulk Editable Planner)
        ModernCard planCard = createMealPlanCard();
        scrollableContainer.add(planCard);
        scrollableContainer.add(Box.createVerticalStrut(24));

        // 4. Actual Consumed Progress vs Targets & Chart Row
        JPanel consumptionSectionHeader = new JPanel(new BorderLayout());
        consumptionSectionHeader.setOpaque(false);

        JLabel consumedTitle = new JLabel("ACTUAL CONSUMPTION & DAILY TARGETS");
        consumedTitle.setFont(Theme.SUBTITLE);
        consumedTitle.setForeground(Theme.PRIMARY);
        consumptionSectionHeader.add(consumedTitle, BorderLayout.WEST);

        // Date navigator
        JPanel dateNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        dateNav.setOpaque(false);

        NeonButton prevDay = new NeonButton("◀", NeonButton.ButtonType.SECONDARY);
        prevDay.setFont(Theme.SMALL);
        prevDay.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            refreshActualNutrition();
        });

        dateLabel = new JLabel(DateUtil.formatDate(selectedDate));
        dateLabel.setFont(Theme.SUBTITLE);
        dateLabel.setForeground(Theme.PRIMARY);

        NeonButton nextDay = new NeonButton("▶", NeonButton.ButtonType.SECONDARY);
        nextDay.setFont(Theme.SMALL);
        nextDay.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            refreshActualNutrition();
        });

        NeonButton todayBtn = new NeonButton("Today", NeonButton.ButtonType.SECONDARY);
        todayBtn.setFont(Theme.SMALL);
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            refreshActualNutrition();
        });

        dateNav.add(prevDay);
        dateNav.add(dateLabel);
        dateNav.add(nextDay);
        dateNav.add(todayBtn);
        consumptionSectionHeader.add(dateNav, BorderLayout.EAST);

        scrollableContainer.add(consumptionSectionHeader);
        scrollableContainer.add(Box.createVerticalStrut(10));

        // Split row: Progress card on Left, JFreeChart Macro Split on Right
        JPanel actualSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        actualSplit.setOpaque(false);

        ModernCard macroCard = createMacroSummaryCard();
        actualSplit.add(macroCard);

        chartPanel = new ModernCard(8);
        chartPanel.setPreferredSize(new Dimension(0, 240));
        actualSplit.add(chartPanel);

        scrollableContainer.add(actualSplit);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 5. Actually Recorded Meals Table (with Edit & Delete)
        ModernCard consumedTableCard = createConsumedTableCard();
        scrollableContainer.add(consumedTableCard);
        scrollableContainer.add(Box.createVerticalStrut(20));

        // 6. Daily Water Hydration Tracking (No Sleep)
        ModernCard waterCard = createWaterHydrationCard();
        scrollableContainer.add(waterCard);

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

        // CUT NUTRITION CARD
        cutModeCard = new ModernCard(16);
        cutModeCard.setLayout(new BorderLayout(12, 6));
        cutModeCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel cutLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        cutLeft.setOpaque(false);
        JLabel cutTitle = new JLabel("🥗  CUT NUTRITION PLAN (CALORIE DEFICIT)");
        cutTitle.setFont(Theme.TITLE);
        cutTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel cutDesc = new JLabel("<html><body style='color:#94A3B8; font-size:12px;'>" +
                "Estimated deficit targets (~1,800 - 1,900 kcal). High protein density (~2.0-2.2g/kg bodyweight) " +
                "using oats, egg whites, lean chicken, white fish, steamed vegetables, and fruits to spare muscle." +
                "</body></html>");
        cutLeft.add(cutTitle);
        cutLeft.add(cutDesc);
        cutModeCard.add(cutLeft, BorderLayout.CENTER);

        cutStatusLabel = new JLabel("INACTIVE", SwingConstants.CENTER);
        cutStatusLabel.setFont(Theme.BODY_BOLD);
        cutStatusLabel.setPreferredSize(new Dimension(100, 32));
        cutModeCard.add(cutStatusLabel, BorderLayout.EAST);

        // BULK NUTRITION CARD
        bulkModeCard = new ModernCard(16);
        bulkModeCard.setLayout(new BorderLayout(12, 6));
        bulkModeCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel bulkLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        bulkLeft.setOpaque(false);
        JLabel bulkTitle = new JLabel("🥩  BULK NUTRITION PLAN (CALORIE SURPLUS)");
        bulkTitle.setFont(Theme.TITLE);
        bulkTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel bulkDesc = new JLabel("<html><body style='color:#94A3B8; font-size:12px;'>" +
                "Estimated surplus targets (~3,200 - 3,400 kcal). Energy surplus fueling using whole eggs, whole milk, " +
                "paneer, basmati rice, lentils/dal, peanut butter, and bananas for rapid recovery." +
                "</body></html>");
        bulkLeft.add(bulkTitle);
        bulkLeft.add(bulkDesc);
        bulkModeCard.add(bulkLeft, BorderLayout.CENTER);

        bulkStatusLabel = new JLabel("ACTIVE", SwingConstants.CENTER);
        bulkStatusLabel.setFont(Theme.BODY_BOLD);
        bulkStatusLabel.setPreferredSize(new Dimension(100, 32));
        bulkModeCard.add(bulkStatusLabel, BorderLayout.EAST);

        // Click Handlers
        cutModeCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchPlanMode("CUT");
            }
        });

        bulkModeCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switchPlanMode("BULK");
            }
        });

        updatePlanModeVisuals();

        panel.add(cutModeCard);
        panel.add(bulkModeCard);
        return panel;
    }

    private void switchPlanMode(String mode) {
        if (mode.equalsIgnoreCase(currentPlanMode)) return;
        this.currentPlanMode = mode.toUpperCase();
        UserSession.getInstance().setActiveNutritionMode(this.currentPlanMode);
        updatePlanModeVisuals();
        refreshPlanItems();
        if (mainFrame != null && mainFrame.getDashboardPanel() != null) {
            mainFrame.getDashboardPanel().refreshDashboard();
        }
    }

    private void updatePlanModeVisuals() {
        if ("CUT".equalsIgnoreCase(currentPlanMode)) {
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

    private ModernCard createMealPlanCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("DAILY MEAL PLANNER (" + currentPlanMode + " MODE)");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        top.add(title, BorderLayout.WEST);

        JPanel planBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        planBtns.setOpaque(false);

        NeonButton addPlanItemBtn = new NeonButton("+ Add Food to Plan", NeonButton.ButtonType.PRIMARY);
        addPlanItemBtn.setFont(Theme.SMALL);
        addPlanItemBtn.addActionListener(e -> showEditPlanItemDialog(null));

        NeonButton resetDefaultPlanBtn = new NeonButton("Reset Plan to Default", NeonButton.ButtonType.SECONDARY);
        resetDefaultPlanBtn.setFont(Theme.SMALL);
        resetDefaultPlanBtn.addActionListener(e -> handleResetPlanToDefault());

        planBtns.add(addPlanItemBtn);
        planBtns.add(resetDefaultPlanBtn);
        top.add(planBtns, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Plan Table with Completion Checkbox Column
        planTableModel = new DefaultTableModel(new String[]{
                "Done", "Time", "Meal Category", "Food & Serving Size", "Calories", "Protein", "Carbs", "Fat", "Notes / Prep"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Boolean.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 0; // Checkbox column is directly interactive
            }

            @Override
            public void setValueAt(Object aValue, int row, int col) {
                super.setValueAt(aValue, row, col);
                if (col == 0 && row >= 0 && row < currentPlanItems.size()) {
                    boolean done = Boolean.TRUE.equals(aValue);
                    NutritionPlanItem item = currentPlanItems.get(row);
                    item.setConsumed(done);
                    handlePlanItemCompletionToggled(item, done);
                }
            }
        };

        planTable = new DarkTable(planTableModel);
        if (planTable.getColumnModel().getColumnCount() > 0) {
            planTable.getColumnModel().getColumn(0).setPreferredWidth(55);
            planTable.getColumnModel().getColumn(0).setMaxWidth(65);
            planTable.getColumnModel().getColumn(1).setPreferredWidth(85);
            planTable.getColumnModel().getColumn(1).setMaxWidth(100);
        }
        card.add(DarkTable.createScrollPane(planTable), BorderLayout.CENTER);

        // Bottom: Plan Totals Bar + Action Buttons (Log Consumed, Edit, Delete)
        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);

        planTotalsLabel = new JLabel("Planned Totals: -- kcal | -- g Protein | -- g Carbs | -- g Fat");
        planTotalsLabel.setFont(Theme.BODY_BOLD);
        planTotalsLabel.setForeground(Theme.PRIMARY);
        bottom.add(planTotalsLabel, BorderLayout.WEST);

        JPanel itemActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        itemActions.setOpaque(false);

        NeonButton logConsumedBtn = new NeonButton("Log as Consumed Today ✓", NeonButton.ButtonType.PRIMARY);
        logConsumedBtn.setFont(Theme.SMALL);
        logConsumedBtn.addActionListener(e -> handleLogSelectedPlanItemAsConsumed());

        NeonButton editItemBtn = new NeonButton("Edit Item", NeonButton.ButtonType.SECONDARY);
        editItemBtn.setFont(Theme.SMALL);
        editItemBtn.addActionListener(e -> handleEditPlanItem());

        NeonButton deleteItemBtn = new NeonButton("Delete Item", NeonButton.ButtonType.DANGER);
        deleteItemBtn.setFont(Theme.SMALL);
        deleteItemBtn.addActionListener(e -> handleDeletePlanItem());

        itemActions.add(logConsumedBtn);
        itemActions.add(editItemBtn);
        itemActions.add(deleteItemBtn);
        bottom.add(itemActions, BorderLayout.EAST);

        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private ModernCard createMacroSummaryCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new GridLayout(4, 1, 0, 8));

        // Calories Progress
        JPanel calP = new JPanel(new BorderLayout(6, 2));
        calP.setOpaque(false);
        JLabel calTitle = new JLabel("Actual Calories Consumed");
        calTitle.setFont(Theme.BODY_BOLD);
        calTitle.setForeground(Theme.TEXT_PRIMARY);
        calStatLabel = new JLabel("0 / 2400 kcal (0%)");
        calStatLabel.setFont(Theme.SMALL);
        calStatLabel.setForeground(Theme.TEXT_MUTED);
        calP.add(calTitle, BorderLayout.WEST);
        calP.add(calStatLabel, BorderLayout.EAST);
        calProgressBar = new ModernProgressBar(0, 2400, Theme.CALORIE_ORANGE);
        calP.add(calProgressBar, BorderLayout.SOUTH);
        card.add(calP);

        // Protein Progress
        JPanel protP = new JPanel(new BorderLayout(6, 2));
        protP.setOpaque(false);
        JLabel protTitle = new JLabel("Actual Protein Intake");
        protTitle.setFont(Theme.BODY_BOLD);
        protTitle.setForeground(Theme.TEXT_PRIMARY);
        protStatLabel = new JLabel("0 / 150 g (0%)");
        protStatLabel.setFont(Theme.SMALL);
        protStatLabel.setForeground(Theme.TEXT_MUTED);
        protP.add(protTitle, BorderLayout.WEST);
        protP.add(protStatLabel, BorderLayout.EAST);
        protProgressBar = new ModernProgressBar(0, 150, Theme.PRIMARY);
        protP.add(protProgressBar, BorderLayout.SOUTH);
        card.add(protP);

        // Carbs
        JPanel carbsP = new JPanel(new BorderLayout(6, 2));
        carbsP.setOpaque(false);
        JLabel carbsTitle = new JLabel("Total Carbohydrates");
        carbsTitle.setFont(Theme.BODY_BOLD);
        carbsTitle.setForeground(Theme.TEXT_PRIMARY);
        carbsStatLabel = new JLabel("0 g");
        carbsStatLabel.setFont(Theme.BODY_BOLD);
        carbsStatLabel.setForeground(Theme.WATER_CYAN);
        carbsP.add(carbsTitle, BorderLayout.WEST);
        carbsP.add(carbsStatLabel, BorderLayout.EAST);
        card.add(carbsP);

        // Fat
        JPanel fatP = new JPanel(new BorderLayout(6, 2));
        fatP.setOpaque(false);
        JLabel fatTitle = new JLabel("Total Fats");
        fatTitle.setFont(Theme.BODY_BOLD);
        fatTitle.setForeground(Theme.TEXT_PRIMARY);
        fatStatLabel = new JLabel("0 g");
        fatStatLabel.setFont(Theme.BODY_BOLD);
        fatStatLabel.setForeground(Theme.WARNING);
        fatP.add(fatTitle, BorderLayout.WEST);
        fatP.add(fatStatLabel, BorderLayout.EAST);
        card.add(fatP);

        return card;
    }

    private ModernCard createConsumedTableCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("TODAY'S RECORDED CONSUMPTION LOG");
        tableTitle.setFont(Theme.SUBTITLE);
        tableTitle.setForeground(Theme.TEXT_PRIMARY);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JLabel estNotice = new JLabel("⚡ Based on verified consumption entries");
        estNotice.setFont(Theme.SMALL);
        estNotice.setForeground(Theme.TEXT_MUTED);
        tableHeader.add(estNotice, BorderLayout.EAST);
        card.add(tableHeader, BorderLayout.NORTH);

        consumedTableModel = new DefaultTableModel(new String[]{
                "Time", "Category", "Food Item", "Quantity", "Calories", "Protein", "Carbs", "Fat"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        consumedTable = new DarkTable(consumedTableModel);
        card.add(DarkTable.createScrollPane(consumedTable), BorderLayout.CENTER);

        // Table Actions: Edit / Delete
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        NeonButton editMealBtn = new NeonButton("Edit Entry", NeonButton.ButtonType.SECONDARY);
        editMealBtn.addActionListener(e -> handleEditConsumedMeal());

        NeonButton deleteMealBtn = new NeonButton("Delete Entry", NeonButton.ButtonType.DANGER);
        deleteMealBtn.addActionListener(e -> handleDeleteConsumedMeal());

        tableActions.add(editMealBtn);
        tableActions.add(deleteMealBtn);
        card.add(tableActions, BorderLayout.SOUTH);

        return card;
    }

    public void refreshNutrition() {
        refreshPlanItems();
        refreshActualNutrition();
    }

    public void refreshPlanItems() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<NutritionPlanItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<NutritionPlanItem> doInBackground() throws Exception {
                return nutritionService.getNutritionPlan(user.getId(), currentPlanMode);
            }

            @Override
            protected void done() {
                try {
                    currentPlanItems = get();
                    if (currentPlanItems != null) {
                        currentPlanItems.sort(java.util.Comparator.comparing(item -> DateUtil.parseTime(item.getMealTime())));
                    }
                    planTableModel.setRowCount(0);

                    double totCal = 0;
                    double totProt = 0;
                    double totCarbs = 0;
                    double totFat = 0;

                    for (NutritionPlanItem item : currentPlanItems) {
                        totCal += item.getCalories();
                        totProt += item.getProteinG();
                        totCarbs += item.getCarbsG();
                        totFat += item.getFatG();

                        planTableModel.addRow(new Object[]{
                                Boolean.valueOf(item.isConsumed()),
                                item.getMealTime(),
                                item.getMealType().getDisplayName(),
                                item.getFoodName() + " (" + item.getServingSize() + ")",
                                String.format("%.0f kcal", item.getCalories()),
                                String.format("%.1f g", item.getProteinG()),
                                String.format("%.1f g", item.getCarbsG()),
                                String.format("%.1f g", item.getFatG()),
                                item.getNotes() != null ? item.getNotes() : ""
                        });
                    }

                    planTotalsLabel.setText(String.format("Planned %s Totals: %.0f kcal | %.1f g Protein | %.1f g Carbs | %.1f g Fat",
                            currentPlanMode, totCal, totProt, totCarbs, totFat));

                } catch (Exception ex) {
                    System.err.println("[NutritionPanel] Error loading plan: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public void refreshActualNutrition() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        dateLabel.setText(DateUtil.formatDate(selectedDate));

        SwingWorker<NutritionService.DailyNutritionSummary, Void> worker = new SwingWorker<>() {
            List<Meal> meals;
            @Override
            protected NutritionService.DailyNutritionSummary doInBackground() throws Exception {
                meals = nutritionService.getMealsForDate(user.getId(), selectedDate);
                return nutritionService.getDailySummary(user.getId(), selectedDate);
            }

            @Override
            protected void done() {
                try {
                    var summary = get();
                    currentConsumedMeals = meals;
                    if (currentConsumedMeals != null) {
                        currentConsumedMeals.sort(java.util.Comparator.comparing(Meal::getMealTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
                    }

                    // Progress Bars & Labels
                    int calTarget = Math.max(1, user.getDailyCalorieTarget());
                    calProgressBar.setProgress((int) summary.totalCalories, calTarget);
                    int calPct = (int) Math.round((summary.totalCalories / calTarget) * 100.0);
                    calStatLabel.setText(String.format("%.0f / %d kcal (%d%%)", summary.totalCalories, calTarget, calPct));

                    int protTarget = Math.max(1, user.getDailyProteinTarget());
                    protProgressBar.setProgress((int) summary.totalProtein, protTarget);
                    int protPct = (int) Math.round((summary.totalProtein / protTarget) * 100.0);
                    protStatLabel.setText(String.format("%.0f / %d g (%d%%)", summary.totalProtein, protTarget, protPct));

                    carbsStatLabel.setText(String.format("%.1f g", summary.totalCarbs));
                    fatStatLabel.setText(String.format("%.1f g", summary.totalFat));

                    // Macro Chart
                    JFreeChart macroChart = reportService.createMacroSplitChart(summary.totalProtein, summary.totalCarbs, summary.totalFat);
                    chartPanel.removeAll();
                    chartPanel.add(new ChartPanel(macroChart), BorderLayout.CENTER);
                    chartPanel.revalidate();
                    chartPanel.repaint();

                    // Consumed Table
                    consumedTableModel.setRowCount(0);
                    for (Meal m : currentConsumedMeals) {
                        consumedTableModel.addRow(new Object[]{
                                DateUtil.formatTime(m.getMealTime()),
                                m.getMealType().getDisplayName(),
                                m.getFoodName(),
                                String.format("%.1f", m.getServingQuantity()),
                                String.format("%.0f kcal", m.getCalories()),
                                String.format("%.1f g", m.getProteinG()),
                                String.format("%.1f g", m.getCarbsG()),
                                String.format("%.1f g", m.getFatG())
                        });
                    }

                } catch (Exception ex) {
                    System.err.println("[NutritionPanel] Error loading consumption: " + ex.getMessage());
                }
            }
        };
        worker.execute();
        refreshWaterHydration();
    }

    private NutritionPlanItem getSelectedPlanItem() {
        int row = planTable.getSelectedRow();
        if (row >= 0 && row < currentPlanItems.size()) {
            return currentPlanItems.get(row);
        }
        return null;
    }

    private void handlePlanItemCompletionToggled(NutritionPlanItem item, boolean done) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (done) {
                    nutritionService.logPlannedMealAsConsumed(item, selectedDate);
                } else {
                    nutritionService.unlogPlannedMeal(item, selectedDate);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    refreshActualNutrition();
                    if (mainFrame != null && mainFrame.getDashboardPanel() != null) {
                        mainFrame.getDashboardPanel().refreshDashboard();
                    }
                } catch (Exception ex) {
                    System.err.println("[NutritionPanel] Error toggling plan item completion: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleLogSelectedPlanItemAsConsumed() {
        NutritionPlanItem item = getSelectedPlanItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select a planned meal from the table to log as consumed.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean logged = nutritionService.logPlannedMealAsConsumed(item, selectedDate);
            if (logged) {
                item.setConsumed(true);
                refreshPlanItems();
                refreshActualNutrition();
                if (mainFrame != null && mainFrame.getDashboardPanel() != null) {
                    mainFrame.getDashboardPanel().refreshDashboard();
                }
                JOptionPane.showMessageDialog(this,
                        "Successfully logged '" + item.getFoodName() + "' to actual consumption!\n" +
                                String.format("Added: %.0f kcal | %.1f g Protein", item.getCalories(), item.getProteinG()),
                        "Meal Consumed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error logging consumed meal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditPlanItem() {
        NutritionPlanItem item = getSelectedPlanItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select a planned food item to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showEditPlanItemDialog(item);
    }

    private void handleDeletePlanItem() {
        NutritionPlanItem item = getSelectedPlanItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + item.getFoodName() + "' from " + currentPlanMode + " Mode meal plan?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nutritionService.deletePlanItem(item.getId(), item.getUserId());
                refreshPlanItems();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleResetPlanToDefault() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset " + currentPlanMode + " Nutrition Plan to the recommended default example menu?",
                "Reset " + currentPlanMode + " Nutrition", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nutritionService.resetNutritionPlanToDefault(user.getId(), currentPlanMode);
                refreshPlanItems();
                JOptionPane.showMessageDialog(this, currentPlanMode + " Nutrition plan restored to default example!",
                        "Restored", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCopyPreviousDay() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        LocalDate yesterday = selectedDate.minusDays(1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Copy all recorded consumed meals from yesterday (" + DateUtil.formatDate(yesterday) + ") to today (" + DateUtil.formatDate(selectedDate) + ")?",
                "Copy Meals", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int copied = nutritionService.copyPreviousDayMeals(user.getId(), yesterday, selectedDate);
                refreshActualNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(this, "Copied " + copied + " meal records from yesterday!", "Meals Copied", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditPlanItemDialog(NutritionPlanItem existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Add Food to " + currentPlanMode + " Plan" : "Edit Planned Food Item", true);
        dialog.setSize(540, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new GridLayout(0, 2, 12, 10));

        JComboBox<MealType> mealTypeCombo = new JComboBox<>(MealType.values());
        if (existing != null) mealTypeCombo.setSelectedItem(existing.getMealType());

        JTextField timeField = new JTextField(existing != null ? existing.getMealTime() : ((MealType) mealTypeCombo.getSelectedItem()).getDefaultTime());
        mealTypeCombo.addActionListener(e -> {
            if (existing == null) {
                MealType mt = (MealType) mealTypeCombo.getSelectedItem();
                if (mt != null) timeField.setText(mt.getDefaultTime());
            }
        });

        JTextField foodNameField = new JTextField(existing != null ? existing.getFoodName() : "Rolled Oats with Whey");
        JTextField servingField = new JTextField(existing != null ? existing.getServingSize() : "1 bowl (100g oats)");

        JSpinner calSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCalories() : 380.0, 0.0, 5000.0, 10.0));
        JSpinner protSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getProteinG() : 30.0, 0.0, 500.0, 1.0));
        JSpinner carbsSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCarbsG() : 50.0, 0.0, 500.0, 1.0));
        JSpinner fatSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getFatG() : 6.0, 0.0, 500.0, 1.0));
        JTextField notesField = new JTextField(existing != null ? existing.getNotes() : "Cook with water or milk");

        container.add(wrapField("MEAL CATEGORY", mealTypeCombo));
        container.add(wrapField("MEAL TIMING", timeField));
        container.add(wrapField("FOOD NAME", foodNameField));
        container.add(wrapField("SERVING SIZE", servingField));
        container.add(wrapField("CALORIES (KCAL)", calSpinner));
        container.add(wrapField("PROTEIN (G)", protSpinner));
        container.add(wrapField("CARBOHYDRATES (G)", carbsSpinner));
        container.add(wrapField("FATS (G)", fatSpinner));

        JPanel fullNotes = new JPanel(new BorderLayout());
        fullNotes.setOpaque(false);
        fullNotes.add(wrapField("PREPARATION NOTES", notesField));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(container, BorderLayout.CENTER);
        centerPanel.add(fullNotes, BorderLayout.SOUTH);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton(existing == null ? "Save to Plan" : "Save Changes", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                String food = foodNameField.getText().trim();
                if (food.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a food name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                NutritionPlanItem item = existing != null ? existing : new NutritionPlanItem();
                item.setUserId(user.getId());
                item.setPlanMode(currentPlanMode);
                item.setMealType((MealType) mealTypeCombo.getSelectedItem());
                item.setMealTime(timeField.getText().trim());
                item.setFoodName(food);
                item.setServingSize(servingField.getText().trim());
                item.setCalories(((Number) calSpinner.getValue()).doubleValue());
                item.setProteinG(((Number) protSpinner.getValue()).doubleValue());
                item.setCarbsG(((Number) carbsSpinner.getValue()).doubleValue());
                item.setFatG(((Number) fatSpinner.getValue()).doubleValue());
                item.setNotes(notesField.getText().trim());

                if (existing == null) {
                    nutritionService.savePlanItem(item);
                } else {
                    nutritionService.updatePlanItem(item);
                }

                dialog.dispose();
                refreshPlanItems();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving plan item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showPersonalizedTargetsDialog() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, "Personalized Nutrition Targets Calculator", true);
        dialog.setSize(620, 560);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Heading
        JLabel title = new JLabel("Configurable Nutrition & Energy Targets");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        container.add(title);
        container.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Input your biometric metrics to calculate estimated BMR, TDEE, and Cut/Bulk caloric goals.");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        container.add(sub);
        container.add(Box.createVerticalStrut(14));

        // Form fields grid
        JPanel form = new JPanel(new GridLayout(0, 3, 10, 8));
        form.setOpaque(false);

        JSpinner ageSp = new JSpinner(new SpinnerNumberModel(user.getAge() > 0 ? user.getAge() : 22, 12, 100, 1));
        JSpinner heightSp = new JSpinner(new SpinnerNumberModel(user.getHeightCm() > 0 ? user.getHeightCm() : 175.0, 80.0, 240.0, 0.5));
        JSpinner weightSp = new JSpinner(new SpinnerNumberModel(user.getCurrentWeightKg() > 0 ? user.getCurrentWeightKg() : 75.0, 30.0, 250.0, 0.5));
        JSpinner targetWeightSp = new JSpinner(new SpinnerNumberModel(user.getTargetWeightKg() > 0 ? user.getTargetWeightKg() : 80.0, 30.0, 250.0, 0.5));

        JComboBox<ActivityLevel> actCombo = new JComboBox<>(ActivityLevel.values());
        actCombo.setSelectedItem(user.getActivityLevel());

        JComboBox<String> trainFreqCombo = new JComboBox<>(new String[]{"3-4 days/week", "5-6 days/week", "Daily High Volume"});

        form.add(wrapField("AGE", ageSp));
        form.add(wrapField("HEIGHT (CM)", heightSp));
        form.add(wrapField("WEIGHT (KG)", weightSp));
        form.add(wrapField("TARGET WEIGHT (KG)", targetWeightSp));
        form.add(wrapField("ACTIVITY LEVEL", actCombo));
        form.add(wrapField("TRAINING FREQ", trainFreqCombo));

        container.add(form);
        container.add(Box.createVerticalStrut(14));

        // Calculated Targets Box
        JPanel calcBox = new JPanel(new GridLayout(1, 3, 10, 0));
        calcBox.setBackground(Theme.BG_PANEL_ALT);
        calcBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JSpinner calTargetSp = new JSpinner(new SpinnerNumberModel(user.getDailyCalorieTarget(), 1000, 8000, 50));
        JSpinner protTargetSp = new JSpinner(new SpinnerNumberModel(user.getDailyProteinTarget(), 40, 500, 5));
        JSpinner waterTargetSp = new JSpinner(new SpinnerNumberModel(user.getDailyWaterTarget(), 1000, 8000, 250));

        calcBox.add(wrapField("CONFIGURED CALORIES (KCAL)", calTargetSp));
        calcBox.add(wrapField("CONFIGURED PROTEIN (G)", protTargetSp));
        calcBox.add(wrapField("CONFIGURED WATER (ML)", waterTargetSp));
        container.add(calcBox);
        container.add(Box.createVerticalStrut(10));

        // Quick Preset Buttons: Cut Deficit (-450 kcal) vs Bulk Surplus (+400 kcal)
        JPanel presets = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        presets.setOpaque(false);
        presets.add(new JLabel("Quick Estimates:"));

        NeonButton applyCutBtn = new NeonButton("Estimate Cut Deficit (-450 kcal)", NeonButton.ButtonType.SECONDARY);
        NeonButton applyBulkBtn = new NeonButton("Estimate Bulk Surplus (+400 kcal)", NeonButton.ButtonType.GOLD);

        applyCutBtn.addActionListener(e -> {
            double w = ((Number) weightSp.getValue()).doubleValue();
            double h = ((Number) heightSp.getValue()).doubleValue();
            int a = (int) ageSp.getValue();
            ActivityLevel act = (ActivityLevel) actCombo.getSelectedItem();
            double bmr = (10 * w) + (6.25 * h) - (5 * a) + 5;
            double tdee = bmr * (act != null ? act.getMultiplier() : 1.55);
            calTargetSp.setValue((int) Math.round(tdee - 450));
            protTargetSp.setValue((int) Math.round(w * 2.2)); // 2.2g/kg in deficit
        });

        applyBulkBtn.addActionListener(e -> {
            double w = ((Number) weightSp.getValue()).doubleValue();
            double h = ((Number) heightSp.getValue()).doubleValue();
            int a = (int) ageSp.getValue();
            ActivityLevel act = (ActivityLevel) actCombo.getSelectedItem();
            double bmr = (10 * w) + (6.25 * h) - (5 * a) + 5;
            double tdee = bmr * (act != null ? act.getMultiplier() : 1.55);
            calTargetSp.setValue((int) Math.round(tdee + 400));
            protTargetSp.setValue((int) Math.round(w * 2.0)); // 2.0g/kg in surplus
        });

        presets.add(applyCutBtn);
        presets.add(applyBulkBtn);
        container.add(presets);
        container.add(Box.createVerticalStrut(12));

        // Medical Disclaimer
        JLabel disclaimer = new JLabel("<html><body style='color:#64748B; font-size:11px;'>" +
                "⚠️ <b>Nutrition Estimate Disclaimer:</b> Calculated energy targets and macronutrient distributions are " +
                "general screening estimates based on the Mifflin-St Jeor equation. They do not constitute medical or clinical dietary advice. " +
                "Consult a licensed sports dietitian or healthcare practitioner for personalized dietary prescriptions." +
                "</body></html>");
        container.add(disclaimer);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton("Save & Update Profile Targets", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                user.setAge((int) ageSp.getValue());
                user.setHeightCm(((Number) heightSp.getValue()).doubleValue());
                user.setCurrentWeightKg(((Number) weightSp.getValue()).doubleValue());
                user.setTargetWeightKg(((Number) targetWeightSp.getValue()).doubleValue());
                user.setActivityLevel((ActivityLevel) actCombo.getSelectedItem());
                user.setDailyCalorieTarget(((Number) calTargetSp.getValue()).intValue());
                user.setDailyProteinTarget(((Number) protTargetSp.getValue()).intValue());
                user.setDailyWaterTarget(((Number) waterTargetSp.getValue()).intValue());

                // Persist updated user targets
                com.pgtr.dao.UserDAO userDAO = new com.pgtr.dao.impl.UserDAOImpl();
                userDAO.updateProfile(user);

                dialog.dispose();
                refreshActualNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Nutritional targets updated successfully! Daily Goal: " +
                        user.getDailyCalorieTarget() + " kcal | " + user.getDailyProteinTarget() + " g Protein",
                        "Targets Saved", JOptionPane.INFORMATION_MESSAGE);
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

    private Meal getSelectedConsumedMeal() {
        int row = consumedTable.getSelectedRow();
        if (row >= 0 && row < currentConsumedMeals.size()) {
            return currentConsumedMeals.get(row);
        }
        return null;
    }

    private void handleEditConsumedMeal() {
        Meal m = getSelectedConsumedMeal();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a meal entry to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showLogMealDialog(m);
    }

    private void handleDeleteConsumedMeal() {
        Meal m = getSelectedConsumedMeal();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a meal entry to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete meal entry for '" + m.getFoodName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nutritionService.deleteMeal(m.getId(), m.getUserId(), m.getMealDate());
                refreshActualNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting meal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showLogMealDialog(Meal existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Log Food & Nutrition" : "Edit Meal Record", true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BorderLayout(0, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JComboBox<MealType> categoryCombo = new JComboBox<>(MealType.values());
        if (existing != null) categoryCombo.setSelectedItem(existing.getMealType());

        JTextField timeField = new JTextField(existing != null ? DateUtil.formatTime(existing.getMealTime()) : ((MealType) categoryCombo.getSelectedItem()).getDefaultTime());
        categoryCombo.addActionListener(e -> {
            if (existing == null) {
                MealType mt = (MealType) categoryCombo.getSelectedItem();
                if (mt != null) timeField.setText(mt.getDefaultTime());
            }
        });

        JTextField foodSearchField = new JTextField(existing != null ? existing.getFoodName() : "");
        DefaultComboBoxModel<FoodItem> foodLibraryModel = new DefaultComboBoxModel<>();
        JComboBox<FoodItem> foodCombo = new JComboBox<>(foodLibraryModel);

        try {
            List<FoodItem> presets = nutritionService.searchFoodItems("", user.getId());
            for (FoodItem f : presets) foodLibraryModel.addElement(f);
        } catch (Exception ignored) {}

        JSpinner servingSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getServingQuantity() : 1.0, 0.1, 50.0, 0.5));
        JSpinner caloriesSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCalories() : 150.0, 0.0, 5000.0, 10.0));
        JSpinner proteinSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getProteinG() : 20.0, 0.0, 500.0, 1.0));
        JSpinner carbsSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCarbsG() : 15.0, 0.0, 500.0, 1.0));
        JSpinner fatSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getFatG() : 5.0, 0.0, 500.0, 1.0));
        JTextField notesField = new JTextField(existing != null ? existing.getNotes() : "");

        form.add(wrapField("MEAL CATEGORY", categoryCombo));
        form.add(wrapField("MEAL TIMING (E.G. 01:30 PM)", timeField));
        form.add(wrapField("CHOOSE FROM DATABASE", foodCombo));
        form.add(wrapField("FOOD NAME (OR CUSTOM)", foodSearchField));
        form.add(wrapField("EST. CALORIES (KCAL)", caloriesSpinner));
        form.add(wrapField("EST. PROTEIN (G)", proteinSpinner));
        form.add(wrapField("EST. CARBOHYDRATES (G)", carbsSpinner));
        form.add(wrapField("EST. FATS (G)", fatSpinner));

        container.add(form, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton(existing == null ? "Log Meal" : "Update Meal", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                String foodName = foodSearchField.getText().trim();
                if (foodName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter or select a food name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Meal meal = existing != null ? existing : new Meal();
                meal.setUserId(user.getId());
                meal.setMealDate(selectedDate);
                meal.setMealTime(DateUtil.parseTime(timeField.getText().trim()));
                meal.setMealType((MealType) categoryCombo.getSelectedItem());
                meal.setFoodName(foodName);
                meal.setServingQuantity(((Number) servingSpinner.getValue()).doubleValue());
                meal.setCalories(((Number) caloriesSpinner.getValue()).doubleValue());
                meal.setProteinG(((Number) proteinSpinner.getValue()).doubleValue());
                meal.setCarbsG(((Number) carbsSpinner.getValue()).doubleValue());
                meal.setFatG(((Number) fatSpinner.getValue()).doubleValue());
                meal.setNotes(notesField.getText().trim());

                if (existing == null) {
                    nutritionService.logMeal(meal);
                } else {
                    nutritionService.updateMeal(meal);
                }

                dialog.dispose();
                refreshActualNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);
        container.add(btnBar, BorderLayout.SOUTH);

        foodCombo.addActionListener(e -> {
            FoodItem selected = (FoodItem) foodCombo.getSelectedItem();
            if (selected != null) {
                foodSearchField.setText(selected.getFoodName());
                double qty = ((Number) servingSpinner.getValue()).doubleValue();
                caloriesSpinner.setValue(selected.getCalories() * qty);
                proteinSpinner.setValue(selected.getProteinG() * qty);
                carbsSpinner.setValue(selected.getCarbsG() * qty);
                fatSpinner.setValue(selected.getFatG() * qty);
            }
        });

        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private ModernCard createWaterHydrationCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 14));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleBlock.setOpaque(false);
        JLabel icon = new JLabel("💧");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        JLabel title = new JLabel("DAILY HYDRATION & WATER TRACKING");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.WATER_CYAN);
        titleBlock.add(icon);
        titleBlock.add(title);
        header.add(titleBlock, BorderLayout.WEST);

        waterTargetTextLabel = new JLabel("Daily Target: 3000 ml");
        waterTargetTextLabel.setFont(Theme.BODY_BOLD);
        waterTargetTextLabel.setForeground(Theme.TEXT_MUTED);
        header.add(waterTargetTextLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Body: 2-column split (Left: Progress & Quick add, Right: Water logs table)
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);

        // Left Panel: Big Stat + Progress Bar + Quick Log Buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        JPanel statRow = new JPanel(new BorderLayout(10, 4));
        statRow.setOpaque(false);
        JLabel statTitle = new JLabel("Water Consumed");
        statTitle.setFont(Theme.BODY_BOLD);
        statTitle.setForeground(Theme.TEXT_PRIMARY);
        waterTotalStatLabel = new JLabel("0 / 3000 ml (0%)");
        waterTotalStatLabel.setFont(Theme.BODY_BOLD);
        waterTotalStatLabel.setForeground(Theme.WATER_CYAN);
        statRow.add(statTitle, BorderLayout.WEST);
        statRow.add(waterTotalStatLabel, BorderLayout.EAST);

        waterProgressBar = new ModernProgressBar(0, 3000, Theme.WATER_CYAN);

        leftPanel.add(statRow);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(waterProgressBar);
        leftPanel.add(Box.createVerticalStrut(14));

        // Quick log buttons row
        JLabel quickLabel = new JLabel("Quick Log Water:");
        quickLabel.setFont(Theme.SMALL);
        quickLabel.setForeground(Theme.TEXT_MUTED);
        leftPanel.add(quickLabel);
        leftPanel.add(Box.createVerticalStrut(6));

        JPanel quickButtons = new JPanel(new GridLayout(1, 4, 8, 0));
        quickButtons.setOpaque(false);

        NeonButton b250 = new NeonButton("+250 ml", NeonButton.ButtonType.PRIMARY);
        NeonButton b500 = new NeonButton("+500 ml", NeonButton.ButtonType.PRIMARY);
        NeonButton b750 = new NeonButton("+750 ml", NeonButton.ButtonType.PRIMARY);
        NeonButton bCustom = new NeonButton("+ Custom", NeonButton.ButtonType.SECONDARY);

        b250.addActionListener(e -> handleAddWater(250));
        b500.addActionListener(e -> handleAddWater(500));
        b750.addActionListener(e -> handleAddWater(750));
        bCustom.addActionListener(e -> showCustomWaterDialog());

        quickButtons.add(b250);
        quickButtons.add(b500);
        quickButtons.add(b750);
        quickButtons.add(bCustom);
        leftPanel.add(quickButtons);

        body.add(leftPanel);

        // Right Panel: Water Logs Table
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setOpaque(false);

        JPanel logsHeader = new JPanel(new BorderLayout());
        logsHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("HYDRATION INTAKE LOGS");
        tableTitle.setFont(Theme.SMALL);
        tableTitle.setForeground(Theme.TEXT_MUTED);
        logsHeader.add(tableTitle, BorderLayout.WEST);

        NeonButton deleteWaterBtn = new NeonButton("Delete Selected", NeonButton.ButtonType.DANGER);
        deleteWaterBtn.setFont(Theme.SMALL);
        deleteWaterBtn.addActionListener(e -> handleDeleteWaterLog());
        logsHeader.add(deleteWaterBtn, BorderLayout.EAST);
        rightPanel.add(logsHeader, BorderLayout.NORTH);

        waterTableModel = new DefaultTableModel(new String[]{"ID", "Time", "Amount (ml)", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        waterTable = new DarkTable(waterTableModel);
        // Hide ID column
        waterTable.getColumnModel().getColumn(0).setMinWidth(0);
        waterTable.getColumnModel().getColumn(0).setMaxWidth(0);
        waterTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        JScrollPane tableScroll = DarkTable.createScrollPane(waterTable);
        tableScroll.setPreferredSize(new Dimension(0, 140));
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        body.add(rightPanel);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private void handleAddWater(int amount) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        try {
            habitService.addWaterForDate(user.getId(), selectedDate, amount);
            refreshWaterHydration();
            mainFrame.getDashboardPanel().refreshDashboard();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error logging water: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomWaterDialog() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Enter water or fluid amount in milliliters (ml):",
                "Log Custom Hydration", JOptionPane.PLAIN_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int amount = Integer.parseInt(input.trim());
                if (amount <= 0 || amount > 5000) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid amount between 1 and 5000 ml.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                handleAddWater(amount);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric value in ml.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void handleDeleteWaterLog() {
        int row = waterTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a water log entry to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String logId = (String) waterTableModel.getValueAt(row, 0);
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null || logId == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this water intake entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                habitService.deleteWaterLog(logId, user.getId());
                refreshWaterHydration();
                mainFrame.getDashboardPanel().refreshDashboard();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting water log: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshWaterHydration() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int waterTotal = 0;
            List<WaterLog> logs;

            @Override
            protected Void doInBackground() throws Exception {
                waterTotal = habitService.getWaterTotalForDate(user.getId(), selectedDate);
                logs = habitService.getWaterLogsForDate(user.getId(), selectedDate);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    int target = Math.max(1, user.getDailyWaterTarget());
                    if (waterTargetTextLabel != null) {
                        waterTargetTextLabel.setText(String.format("Daily Target: %d ml", target));
                    }
                    if (waterProgressBar != null) {
                        waterProgressBar.setProgress(waterTotal, target);
                    }
                    int pct = (int) Math.round(((double) waterTotal / target) * 100.0);
                    if (waterTotalStatLabel != null) {
                        waterTotalStatLabel.setText(String.format("%d / %d ml (%d%%)", waterTotal, target, pct));
                    }

                    if (waterTableModel != null) {
                        waterTableModel.setRowCount(0);
                        if (logs != null) {
                            for (WaterLog w : logs) {
                                waterTableModel.addRow(new Object[]{
                                        w.getId(),
                                        DateUtil.formatTime(w.getLoggedAt()),
                                        w.getAmountMl() + " ml",
                                        waterTotal >= target ? "Target Achieved ✓" : "Logged"
                                });
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[NutritionPanel] Error loading water logs: " + ex.getMessage());
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
}

package com.pgtr.view;

import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import com.pgtr.model.MealType;
import com.pgtr.model.User;
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
 * Nutrition and Calorie Tracking module.
 * Features meal logging across categories (Breakfast, Lunch, Dinner, Snack),
 * searchable food database with estimates, daily macronutrient targets, and JFreeChart macro split visualization.
 */
public class NutritionPanel extends JPanel {

    private final MainFrame mainFrame;
    private final NutritionService nutritionService;
    private final ReportService reportService;

    // Daily Selected Date
    private LocalDate selectedDate = LocalDate.now();
    private JLabel dateLabel;

    // Macro Progress Cards
    private JLabel calStatLabel;
    private ModernProgressBar calProgressBar;
    private JLabel protStatLabel;
    private ModernProgressBar protProgressBar;
    private JLabel carbsStatLabel;
    private JLabel fatStatLabel;

    // Macro Split Chart
    private JPanel chartPanel;

    // Meals Table
    private DefaultTableModel mealTableModel;
    private DarkTable mealTable;
    private List<Meal> currentMealsList = new ArrayList<>();

    public NutritionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.nutritionService = new NutritionService();
        this.reportService = new ReportService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        // Top Section: Title & Date Selector
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("NUTRITION & CALORIE TRACKER");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Fuel your training • Track macros, calories, and meals across the day");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        topContainer.add(titleBlock, BorderLayout.WEST);

        // Date navigation & Log button
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navPanel.setOpaque(false);

        NeonButton prevDay = new NeonButton("◀", NeonButton.ButtonType.SECONDARY);
        prevDay.setFont(Theme.SMALL);
        prevDay.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            refreshNutrition();
        });

        dateLabel = new JLabel(DateUtil.formatDate(selectedDate));
        dateLabel.setFont(Theme.SUBTITLE);
        dateLabel.setForeground(Theme.PRIMARY);

        NeonButton nextDay = new NeonButton("▶", NeonButton.ButtonType.SECONDARY);
        nextDay.setFont(Theme.SMALL);
        nextDay.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            refreshNutrition();
        });

        NeonButton todayBtn = new NeonButton("Today", NeonButton.ButtonType.SECONDARY);
        todayBtn.setFont(Theme.SMALL);
        todayBtn.addActionListener(e -> {
            selectedDate = LocalDate.now();
            refreshNutrition();
        });

        NeonButton logMealBtn = new NeonButton("+ Log Meal", NeonButton.ButtonType.PRIMARY);
        logMealBtn.addActionListener(e -> showLogMealDialog(null));

        navPanel.add(prevDay);
        navPanel.add(dateLabel);
        navPanel.add(nextDay);
        navPanel.add(todayBtn);
        navPanel.add(Box.createHorizontalStrut(10));
        navPanel.add(logMealBtn);

        topContainer.add(navPanel, BorderLayout.EAST);
        add(topContainer, BorderLayout.NORTH);

        // Center Content: Left = Macro Targets & Chart, Right = Meals Table
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        centerSplit.setOpaque(false);

        // Left Container: Macro Summary & Chart
        JPanel leftContainer = new JPanel(new BorderLayout(0, 14));
        leftContainer.setOpaque(false);

        ModernCard macroCard = createMacroSummaryCard();
        leftContainer.add(macroCard, BorderLayout.NORTH);

        chartPanel = new ModernCard(8);
        chartPanel.setPreferredSize(new Dimension(0, 260));
        leftContainer.add(chartPanel, BorderLayout.CENTER);

        centerSplit.add(leftContainer);

        // Right Container: Meals Table with Edit/Delete
        ModernCard tableCard = new ModernCard(16);
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("MEALS LOGGED FOR THIS DAY");
        tableTitle.setFont(Theme.SUBTITLE);
        tableTitle.setForeground(Theme.TEXT_PRIMARY);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JLabel estNotice = new JLabel("⚡ Standard Food Estimates");
        estNotice.setFont(Theme.SMALL);
        estNotice.setForeground(Theme.TEXT_MUTED);
        tableHeader.add(estNotice, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        mealTableModel = new DefaultTableModel(new String[]{
                "Time", "Category", "Food Item", "Qty", "Calories", "Protein", "Carbs", "Fat"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        mealTable = new DarkTable(mealTableModel);
        tableCard.add(DarkTable.createScrollPane(mealTable), BorderLayout.CENTER);

        // Actions
        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        NeonButton editMealBtn = new NeonButton("Edit Meal", NeonButton.ButtonType.SECONDARY);
        editMealBtn.addActionListener(e -> handleEditMeal());

        NeonButton deleteMealBtn = new NeonButton("Delete Meal", NeonButton.ButtonType.DANGER);
        deleteMealBtn.addActionListener(e -> handleDeleteMeal());

        tableActions.add(editMealBtn);
        tableActions.add(deleteMealBtn);
        tableCard.add(tableActions, BorderLayout.SOUTH);

        centerSplit.add(tableCard);
        add(centerSplit, BorderLayout.CENTER);
    }

    private ModernCard createMacroSummaryCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new GridLayout(4, 1, 0, 10));

        // Calories
        JPanel calP = new JPanel(new BorderLayout(6, 2));
        calP.setOpaque(false);
        JLabel calTitle = new JLabel("Calories Consumed");
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

        // Protein
        JPanel protP = new JPanel(new BorderLayout(6, 2));
        protP.setOpaque(false);
        JLabel protTitle = new JLabel("Protein Intake");
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
        JLabel carbsTitle = new JLabel("Carbohydrates");
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
        JLabel fatTitle = new JLabel("Fats");
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

    public void refreshNutrition() {
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
                    currentMealsList = meals;

                    // Update Progress Bars & Labels
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

                    // Update Macro Split Chart
                    JFreeChart macroChart = reportService.createMacroSplitChart(summary.totalProtein, summary.totalCarbs, summary.totalFat);
                    chartPanel.removeAll();
                    chartPanel.add(new ChartPanel(macroChart), BorderLayout.CENTER);
                    chartPanel.revalidate();
                    chartPanel.repaint();

                    // Update Table
                    mealTableModel.setRowCount(0);
                    for (Meal m : currentMealsList) {
                        mealTableModel.addRow(new Object[]{
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
                    System.err.println("[NutritionPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private Meal getSelectedMeal() {
        int row = mealTable.getSelectedRow();
        if (row >= 0 && row < currentMealsList.size()) {
            return currentMealsList.get(row);
        }
        return null;
    }

    private void handleEditMeal() {
        Meal m = getSelectedMeal();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a meal from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showLogMealDialog(m);
    }

    private void handleDeleteMeal() {
        Meal m = getSelectedMeal();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a meal from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete meal entry for '" + m.getFoodName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                nutritionService.deleteMeal(m.getId(), m.getUserId(), m.getMealDate());
                refreshNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(this, "Meal entry deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting meal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Modal Dialog for Logging or Editing a Meal Entry with Searchable Food Library.
     */
    private void showLogMealDialog(Meal existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Log Food & Nutrition" : "Edit Meal Record", true);
        dialog.setSize(580, 560);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BorderLayout(0, 12));

        // Form fields
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JComboBox<MealType> categoryCombo = new JComboBox<>(MealType.values());
        if (existing != null) categoryCombo.setSelectedItem(existing.getMealType());

        JTextField foodSearchField = new JTextField(existing != null ? existing.getFoodName() : "");
        DefaultComboBoxModel<FoodItem> foodLibraryModel = new DefaultComboBoxModel<>();
        JComboBox<FoodItem> foodCombo = new JComboBox<>(foodLibraryModel);

        // Preload food library items
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
        form.add(wrapField("SERVINGS / MULTIPLIER", servingSpinner));
        form.add(wrapField("CHOOSE FROM DATABASE", foodCombo));
        form.add(wrapField("FOOD NAME (OR CUSTOM)", foodSearchField));
        form.add(wrapField("EST. CALORIES (KCAL)", caloriesSpinner));
        form.add(wrapField("EST. PROTEIN (G)", proteinSpinner));
        form.add(wrapField("EST. CARBOHYDRATES (G)", carbsSpinner));
        form.add(wrapField("EST. FATS (G)", fatSpinner));

        container.add(form, BorderLayout.CENTER);

        // Notice & Save Button
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        JLabel disclaimer = new JLabel("<html><center style='color:#64748B; font-size:11px;'>" +
                "Nutrition values from the food library are standardized estimates. " +
                "You can freely adjust the numbers to match your exact food product label." +
                "</center></html>", SwingConstants.CENTER);
        bottom.add(disclaimer, BorderLayout.NORTH);

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
                meal.setMealTime(LocalTime.now());
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
                refreshNutrition();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Meal logged successfully! " +
                        String.format("%.0f kcal | %.1f g protein", meal.getCalories(), meal.getProteinG()),
                        "Meal Logged", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving meal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);
        bottom.add(btnBar, BorderLayout.SOUTH);

        container.add(bottom, BorderLayout.SOUTH);
        dialog.add(container, BorderLayout.CENTER);

        // Auto-fill when picking from Food Database
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

        // Recalculate if multiplier changes
        servingSpinner.addChangeListener(e -> {
            FoodItem selected = (FoodItem) foodCombo.getSelectedItem();
            if (selected != null) {
                double qty = ((Number) servingSpinner.getValue()).doubleValue();
                caloriesSpinner.setValue(selected.getCalories() * qty);
                proteinSpinner.setValue(selected.getProteinG() * qty);
                carbsSpinner.setValue(selected.getCarbsG() * qty);
                fatSpinner.setValue(selected.getFatG() * qty);
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

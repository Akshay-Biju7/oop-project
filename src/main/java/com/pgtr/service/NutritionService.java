package com.pgtr.service;

import com.pgtr.dao.MealDAO;
import com.pgtr.dao.NutritionPlanDAO;
import com.pgtr.dao.impl.MealDAOImpl;
import com.pgtr.dao.impl.NutritionPlanDAOImpl;
import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import com.pgtr.model.NutritionPlanItem;
import com.pgtr.model.User;
import com.pgtr.util.DateUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class NutritionService {

    private final MealDAO mealDAO;
    private final NutritionPlanDAO nutritionPlanDAO;
    private final AchievementService achievementService;

    public NutritionService() {
        this.mealDAO = new MealDAOImpl();
        this.nutritionPlanDAO = new NutritionPlanDAOImpl();
        this.achievementService = new AchievementService();
    }

    public NutritionService(MealDAO mealDAO, NutritionPlanDAO nutritionPlanDAO, AchievementService achievementService) {
        this.mealDAO = mealDAO;
        this.nutritionPlanDAO = nutritionPlanDAO;
        this.achievementService = achievementService;
    }

    // --- Planned Meals (Cut & Bulk Modes) ---

    public List<NutritionPlanItem> getNutritionPlan(String userId, String planMode) throws SQLException {
        return nutritionPlanDAO.getItemsByMode(userId, planMode);
    }

    public boolean savePlanItem(NutritionPlanItem item) throws SQLException {
        return nutritionPlanDAO.saveItem(item);
    }

    public boolean updatePlanItem(NutritionPlanItem item) throws SQLException {
        return nutritionPlanDAO.updateItem(item);
    }

    public boolean deletePlanItem(String id, String userId) throws SQLException {
        return nutritionPlanDAO.deleteItem(id, userId);
    }

    public void resetNutritionPlanToDefault(String userId, String planMode) throws SQLException {
        nutritionPlanDAO.resetToDefaultPlan(userId, planMode);
    }

    public boolean setPlanItemConsumed(String id, String userId, boolean consumed) throws SQLException {
        return nutritionPlanDAO.setItemConsumed(id, userId, consumed);
    }

    /**
     * Converts a planned meal item into an actually consumed and recorded meal for today.
     * Updates dashboard calories and macronutrient statistics immediately.
     */
    public boolean logPlannedMealAsConsumed(NutritionPlanItem planItem, LocalDate date) throws SQLException {
        LocalTime time = DateUtil.parseTime(planItem.getMealTime());
        String foodDesc = planItem.getFoodName() + " (" + planItem.getServingSize() + ")";
        LocalDate targetDate = date != null ? date : LocalDate.now();

        // Check if an identical meal was already recorded today to avoid duplicate entries
        List<Meal> existing = mealDAO.getMealsByDate(planItem.getUserId(), targetDate);
        for (Meal m : existing) {
            if (foodDesc.equalsIgnoreCase(m.getFoodName()) && planItem.getMealType() == m.getMealType()) {
                nutritionPlanDAO.setItemConsumed(planItem.getId(), planItem.getUserId(), true);
                return true;
            }
        }

        Meal meal = new Meal(
                null,
                planItem.getUserId(),
                targetDate,
                time,
                planItem.getMealType(),
                foodDesc,
                1.0,
                planItem.getCalories(),
                planItem.getProteinG(),
                planItem.getCarbsG(),
                planItem.getFatG(),
                planItem.getNotes() != null && !planItem.getNotes().isEmpty() ? planItem.getNotes() : "Logged from " + planItem.getPlanMode() + " Nutrition Plan"
        );

        boolean logged = logMeal(meal);
        if (logged) {
            nutritionPlanDAO.setItemConsumed(planItem.getId(), planItem.getUserId(), true);
        }
        return logged;
    }

    /**
     * Unmarks a planned meal as consumed and removes today's matching logged meal.
     */
    public boolean unlogPlannedMeal(NutritionPlanItem planItem, LocalDate date) throws SQLException {
        nutritionPlanDAO.setItemConsumed(planItem.getId(), planItem.getUserId(), false);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        String foodDesc = planItem.getFoodName() + " (" + planItem.getServingSize() + ")";
        List<Meal> existing = mealDAO.getMealsByDate(planItem.getUserId(), targetDate);
        for (Meal m : existing) {
            if (foodDesc.equalsIgnoreCase(m.getFoodName()) && planItem.getMealType() == m.getMealType()) {
                mealDAO.deleteMeal(m.getId(), planItem.getUserId());
                break;
            }
        }
        return true;
    }

    /**
     * Copies all actual recorded meals from a previous date to a new target date.
     */
    public int copyPreviousDayMeals(String userId, LocalDate fromDate, LocalDate toDate) throws SQLException {
        List<Meal> pastMeals = mealDAO.getMealsByDate(userId, fromDate);
        int copied = 0;
        for (Meal oldMeal : pastMeals) {
            Meal newMeal = new Meal(
                    null,
                    userId,
                    toDate,
                    oldMeal.getMealTime(),
                    oldMeal.getMealType(),
                    oldMeal.getFoodName(),
                    oldMeal.getServingQuantity(),
                    oldMeal.getCalories(),
                    oldMeal.getProteinG(),
                    oldMeal.getCarbsG(),
                    oldMeal.getFatG(),
                    oldMeal.getNotes()
            );
            if (logMeal(newMeal)) {
                copied++;
            }
        }
        return copied;
    }

    // --- Actual Consumed Meals ---

    public boolean logMeal(Meal meal) throws SQLException {
        boolean saved = mealDAO.createMeal(meal);
        if (saved) {
            checkProteinTarget(meal.getUserId(), meal.getMealDate());
        }
        return saved;
    }

    public boolean updateMeal(Meal meal) throws SQLException {
        boolean updated = mealDAO.updateMeal(meal);
        if (updated) {
            checkProteinTarget(meal.getUserId(), meal.getMealDate());
        }
        return updated;
    }

    public boolean deleteMeal(String mealId, String userId, LocalDate mealDate) throws SQLException {
        return mealDAO.deleteMeal(mealId, userId);
    }

    public List<Meal> getMealsForDate(String userId, LocalDate date) throws SQLException {
        return mealDAO.getMealsByDate(userId, date);
    }

    public List<Meal> getRecentMeals(String userId, int limit) throws SQLException {
        return mealDAO.getRecentMeals(userId, limit);
    }

    public List<FoodItem> searchFoodItems(String query, String userId) throws SQLException {
        return mealDAO.searchFoodItems(query, userId);
    }

    public boolean createCustomFoodItem(FoodItem item) throws SQLException {
        return mealDAO.createCustomFoodItem(item);
    }

    public DailyNutritionSummary getDailySummary(String userId, LocalDate date) throws SQLException {
        List<Meal> meals = mealDAO.getMealsByDate(userId, date);
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        for (Meal m : meals) {
            totalCalories += m.getCalories();
            totalProtein += m.getProteinG();
            totalCarbs += m.getCarbsG();
            totalFat += m.getFatG();
        }

        return new DailyNutritionSummary(date, totalCalories, totalProtein, totalCarbs, totalFat, meals.size());
    }

    private void checkProteinTarget(String userId, LocalDate date) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null && current.getId().equals(userId)) {
                DailyNutritionSummary summary = getDailySummary(userId, date);
                if (summary.totalProtein >= current.getDailyProteinTarget()) {
                    achievementService.awardIfEligible(userId, "PROTEIN_TARGET");
                }
            }
        } catch (Exception ignored) {}
    }

    public static class DailyNutritionSummary {
        public final LocalDate date;
        public final double totalCalories;
        public final double totalProtein;
        public final double totalCarbs;
        public final double totalFat;
        public final int mealsCount;

        public DailyNutritionSummary(LocalDate date, double totalCalories, double totalProtein,
                                     double totalCarbs, double totalFat, int mealsCount) {
            this.date = date;
            this.totalCalories = totalCalories;
            this.totalProtein = totalProtein;
            this.totalCarbs = totalCarbs;
            this.totalFat = totalFat;
            this.mealsCount = mealsCount;
        }
    }
}

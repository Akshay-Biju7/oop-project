package com.pgtr.service;

import com.pgtr.dao.MealDAO;
import com.pgtr.dao.impl.MealDAOImpl;
import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import com.pgtr.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NutritionService {

    private final MealDAO mealDAO;
    private final AchievementService achievementService;

    public NutritionService() {
        this.mealDAO = new MealDAOImpl();
        this.achievementService = new AchievementService();
    }

    public NutritionService(MealDAO mealDAO, AchievementService achievementService) {
        this.mealDAO = mealDAO;
        this.achievementService = achievementService;
    }

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

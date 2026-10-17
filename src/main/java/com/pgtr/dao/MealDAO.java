package com.pgtr.dao;

import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for food database and daily meals tracking.
 */
public interface MealDAO {
    boolean createMeal(Meal meal) throws SQLException;
    boolean updateMeal(Meal meal) throws SQLException;
    boolean deleteMeal(String mealId, String userId) throws SQLException;
    List<Meal> getMealsByDate(String userId, LocalDate date) throws SQLException;
    List<Meal> getMealsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException;
    List<Meal> getRecentMeals(String userId, int limit) throws SQLException;
    List<FoodItem> searchFoodItems(String query, String userId) throws SQLException;
    boolean createCustomFoodItem(FoodItem item) throws SQLException;
}

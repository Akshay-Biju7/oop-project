package com.pgtr.dao.impl;

import com.pgtr.dao.MealDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.FoodItem;
import com.pgtr.model.Meal;
import com.pgtr.model.MealType;
import com.pgtr.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MealDAOImpl implements MealDAO {

    @Override
    public boolean createMeal(Meal meal) throws SQLException {
        if (meal.getId() == null || meal.getId().trim().isEmpty()) {
            meal.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO meals (id, user_id, meal_date, meal_time, meal_type, " +
                "food_name, serving_quantity, calories, protein_g, carbs_g, fat_g, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meal.getId());
            ps.setString(2, meal.getUserId());
            ps.setString(3, meal.getMealDate().toString());
            ps.setString(4, meal.getMealTime() != null ? meal.getMealTime().toString() : LocalTime.now().toString());
            ps.setString(5, meal.getMealType() != null ? meal.getMealType().name() : "LUNCH");
            ps.setString(6, meal.getFoodName());
            ps.setDouble(7, meal.getServingQuantity());
            ps.setDouble(8, meal.getCalories());
            ps.setDouble(9, meal.getProteinG());
            ps.setDouble(10, meal.getCarbsG());
            ps.setDouble(11, meal.getFatG());
            ps.setString(12, meal.getNotes());
            ps.setString(13, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateMeal(Meal meal) throws SQLException {
        String sql = "UPDATE meals SET meal_date = ?, meal_time = ?, meal_type = ?, " +
                "food_name = ?, serving_quantity = ?, calories = ?, protein_g = ?, " +
                "carbs_g = ?, fat_g = ?, notes = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, meal.getMealDate().toString());
            ps.setString(2, meal.getMealTime() != null ? meal.getMealTime().toString() : LocalTime.now().toString());
            ps.setString(3, meal.getMealType() != null ? meal.getMealType().name() : "LUNCH");
            ps.setString(4, meal.getFoodName());
            ps.setDouble(5, meal.getServingQuantity());
            ps.setDouble(6, meal.getCalories());
            ps.setDouble(7, meal.getProteinG());
            ps.setDouble(8, meal.getCarbsG());
            ps.setDouble(9, meal.getFatG());
            ps.setString(10, meal.getNotes());
            ps.setString(11, meal.getId());
            ps.setString(12, meal.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteMeal(String mealId, String userId) throws SQLException {
        String sql = "DELETE FROM meals WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mealId);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Meal> getMealsByDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM meals WHERE user_id = ? AND meal_date = ? ORDER BY meal_time ASC";
        return fetchMeals(sql, userId, date.toString(), null);
    }

    @Override
    public List<Meal> getMealsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM meals WHERE user_id = ? AND meal_date >= ? AND meal_date <= ? ORDER BY meal_date DESC, meal_time ASC";
        return fetchMeals(sql, userId, start.toString(), end.toString());
    }

    @Override
    public List<Meal> getRecentMeals(String userId, int limit) throws SQLException {
        String sql = "SELECT * FROM meals WHERE user_id = ? ORDER BY meal_date DESC, meal_time DESC LIMIT " + limit;
        return fetchMeals(sql, userId, null, null);
    }

    @Override
    public List<FoodItem> searchFoodItems(String query, String userId) throws SQLException {
        String sql;
        List<FoodItem> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            sql = "SELECT * FROM food_items WHERE is_custom = 0 OR user_id = ? ORDER BY food_name ASC LIMIT 50";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToFood(rs));
                }
            }
        } else {
            sql = "SELECT * FROM food_items WHERE (is_custom = 0 OR user_id = ?) AND LOWER(food_name) LIKE ? ORDER BY food_name ASC LIMIT 50";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, userId);
                ps.setString(2, "%" + query.trim().toLowerCase() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapResultSetToFood(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean createCustomFoodItem(FoodItem item) throws SQLException {
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO food_items (id, food_name, category, serving_unit, calories, " +
                "protein_g, carbs_g, fat_g, is_custom, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getId());
            ps.setString(2, item.getFoodName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getServingUnit());
            ps.setDouble(5, item.getCalories());
            ps.setDouble(6, item.getProteinG());
            ps.setDouble(7, item.getCarbsG());
            ps.setDouble(8, item.getFatG());
            ps.setInt(9, item.isCustom() ? 1 : 0);
            ps.setString(10, item.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    private List<Meal> fetchMeals(String sql, String userId, String p2, String p3) throws SQLException {
        List<Meal> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            if (p2 != null) ps.setString(2, p2);
            if (p3 != null) ps.setString(3, p3);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMeal(rs));
                }
            }
        }
        return list;
    }

    private Meal mapResultSetToMeal(ResultSet rs) throws SQLException {
        Meal m = new Meal();
        m.setId(rs.getString("id"));
        m.setUserId(rs.getString("user_id"));
        String dateStr = rs.getString("meal_date");
        if (dateStr != null) {
            try { m.setMealDate(LocalDate.parse(dateStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        String timeStr = rs.getString("meal_time");
        if (timeStr != null) {
            try { m.setMealTime(DateUtil.parseTime(timeStr)); } catch (Exception ignored) {}
        }
        m.setMealType(MealType.fromString(rs.getString("meal_type")));
        m.setFoodName(rs.getString("food_name"));
        m.setServingQuantity(rs.getDouble("serving_quantity"));
        m.setCalories(rs.getDouble("calories"));
        m.setProteinG(rs.getDouble("protein_g"));
        m.setCarbsG(rs.getDouble("carbs_g"));
        m.setFatG(rs.getDouble("fat_g"));
        m.setNotes(rs.getString("notes"));
        return m;
    }

    private FoodItem mapResultSetToFood(ResultSet rs) throws SQLException {
        FoodItem f = new FoodItem();
        f.setId(rs.getString("id"));
        f.setFoodName(rs.getString("food_name"));
        f.setCategory(rs.getString("category"));
        f.setServingUnit(rs.getString("serving_unit"));
        f.setCalories(rs.getDouble("calories"));
        f.setProteinG(rs.getDouble("protein_g"));
        f.setCarbsG(rs.getDouble("carbs_g"));
        f.setFatG(rs.getDouble("fat_g"));
        f.setCustom(rs.getInt("is_custom") == 1);
        f.setUserId(rs.getString("user_id"));
        return f;
    }
}

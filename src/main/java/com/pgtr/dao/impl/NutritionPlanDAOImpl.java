package com.pgtr.dao.impl;

import com.pgtr.dao.NutritionPlanDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.MealType;
import com.pgtr.model.NutritionPlanItem;
import com.pgtr.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class NutritionPlanDAOImpl implements NutritionPlanDAO {

    @Override
    public boolean saveItem(NutritionPlanItem item) throws SQLException {
        if (item.getId() == null || item.getId().trim().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO nutrition_plans (id, user_id, plan_mode, meal_type, food_name, " +
                "serving_size, calories, protein_g, carbs_g, fat_g, meal_time, notes, is_consumed, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getId());
            ps.setString(2, item.getUserId());
            ps.setString(3, item.getPlanMode().toUpperCase());
            ps.setString(4, item.getMealType().name());
            ps.setString(5, item.getFoodName());
            ps.setString(6, item.getServingSize());
            ps.setDouble(7, item.getCalories());
            ps.setDouble(8, item.getProteinG());
            ps.setDouble(9, item.getCarbsG());
            ps.setDouble(10, item.getFatG());
            ps.setString(11, item.getMealTime());
            ps.setString(12, item.getNotes() != null ? item.getNotes() : "");
            ps.setInt(13, item.isConsumed() ? 1 : 0);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateItem(NutritionPlanItem item) throws SQLException {
        String sql = "UPDATE nutrition_plans SET meal_type = ?, food_name = ?, serving_size = ?, " +
                "calories = ?, protein_g = ?, carbs_g = ?, fat_g = ?, meal_time = ?, notes = ?, is_consumed = ?, plan_mode = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getMealType().name());
            ps.setString(2, item.getFoodName());
            ps.setString(3, item.getServingSize());
            ps.setDouble(4, item.getCalories());
            ps.setDouble(5, item.getProteinG());
            ps.setDouble(6, item.getCarbsG());
            ps.setDouble(7, item.getFatG());
            ps.setString(8, item.getMealTime());
            ps.setString(9, item.getNotes());
            ps.setInt(10, item.isConsumed() ? 1 : 0);
            ps.setString(11, item.getPlanMode().toUpperCase());
            ps.setString(12, item.getId());
            ps.setString(13, item.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteItem(String id, String userId) throws SQLException {
        String sql = "DELETE FROM nutrition_plans WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<NutritionPlanItem> getItemsByMode(String userId, String planMode) throws SQLException {
        String targetMode = planMode != null ? planMode.toUpperCase() : "BULK";
        List<NutritionPlanItem> list = new ArrayList<>();
        String sql = "SELECT * FROM nutrition_plans WHERE user_id = ? AND plan_mode = ? ORDER BY meal_time ASC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, targetMode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }

        if (list.isEmpty()) {
            resetToDefaultPlan(userId, targetMode);
            return getItemsByMode(userId, targetMode);
        }

        // Sort strictly in chronological order by meal time
        list.sort(Comparator.comparing(item -> DateUtil.parseTime(item.getMealTime())));
        return list;
    }

    @Override
    public boolean setItemConsumed(String id, String userId, boolean consumed) throws SQLException {
        String sql = "UPDATE nutrition_plans SET is_consumed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consumed ? 1 : 0);
            ps.setString(2, id);
            ps.setString(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void resetToDefaultPlan(String userId, String planMode) throws SQLException {
        String targetMode = planMode != null ? planMode.toUpperCase() : "BULK";

        // Delete existing items for this mode
        String delSql = "DELETE FROM nutrition_plans WHERE user_id = ? AND plan_mode = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(delSql)) {
            ps.setString(1, userId);
            ps.setString(2, targetMode);
            ps.executeUpdate();
        }

        List<NutritionPlanItem> defaults = new ArrayList<>();

        if ("CUT".equalsIgnoreCase(targetMode)) {
            // Cut Mode: Deficit-support (~1,800 - 1,900 kcal), High Protein (~175g), Low Fat
            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.BREAKFAST,
                    "Rolled Oats with Boiled Egg Whites", "60g oats + 4 egg whites + 1 whole egg",
                    380.0, 32.0, 44.0, 8.0, "08:00 AM", "Cook oats in water with cinnamon; sprinkle pinch of salt", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.MID_MORNING,
                    "Whey Protein Shake & Fresh Apple", "1 scoop whey (30g) + 1 medium crisp apple",
                    220.0, 25.0, 26.0, 1.5, "11:00 AM", "Mix with cold water to keep calories clean", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.LUNCH,
                    "Grilled Chicken Breast with Steamed Rice & Broccoli", "160g cooked chicken breast + 120g white rice + 100g broccoli",
                    480.0, 52.0, 40.0, 7.5, "01:30 PM", "Season with black pepper, garlic powder, and paprika", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.EVENING_SNACK,
                    "Roasted Bengal Gram (Chana) & Green Tea", "45g roasted chana + hot unsweetened green tea",
                    165.0, 9.5, 25.0, 2.5, "05:00 PM", "High fiber snack to maintain satiety during deficit", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.POST_WORKOUT,
                    "Post-Workout Whey Protein & Rice Cake", "1 scoop whey + 2 plain rice cakes",
                    210.0, 26.0, 18.0, 1.5, "07:30 PM", "Rapid muscle recovery post-workout window", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "CUT", MealType.DINNER,
                    "Baked Salmon or Low-Fat Paneer with Veggie Stir-Fry", "150g baked fish/paneer + 1 whole wheat roti + mixed greens",
                    420.0, 38.0, 28.0, 14.0, "08:45 PM", "Low glycemic index dinner for smooth overnight digestion", false));
        } else {
            // Bulk Mode: Muscle surplus (~3,200 - 3,400 kcal), High Protein (~210g), Sustained Carbs
            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.BREAKFAST,
                    "Power Oats Bowl with Whole Milk, Eggs & Peanut Butter", "100g rolled oats + 300ml whole milk + 2 whole boiled eggs + 1 tbsp peanut butter",
                    760.0, 42.0, 92.0, 25.0, "08:00 AM", "Dense morning fuel to jumpstart progressive hypertrophy", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.MID_MORNING,
                    "Paneer / Chicken Whole Wheat Sandwich with Almonds", "100g cottage cheese/chicken + 2 whole wheat slices + 20g almonds",
                    490.0, 28.0, 38.0, 22.0, "11:00 AM", "Healthy fats and complete amino acids", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.LUNCH,
                    "Chicken Breast / Paneer with Basmati Rice & Moong Dal", "200g chicken or 150g paneer + 250g cooked basmati rice + 1 bowl dal",
                    780.0, 62.0, 95.0, 15.0, "01:30 PM", "Core surplus training meal; add cucumber and carrots", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.EVENING_SNACK,
                    "Pre-Workout Bananas with Peanut Butter Toast", "2 large bananas + 2 slices whole wheat toast with 2 tbsp peanut butter",
                    460.0, 14.0, 75.0, 16.0, "05:00 PM", "Fast and complex glycogen loading before training", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.POST_WORKOUT,
                    "Post-Workout Whey Protein & Dextrose Shake", "1.5 scoops whey (45g) + 300ml whole milk",
                    350.0, 44.0, 22.0, 8.0, "07:30 PM", "Rapid protein synthesis post-workout window", false));

            defaults.add(new NutritionPlanItem(UUID.randomUUID().toString(), userId, "BULK", MealType.DINNER,
                    "Hearty Chicken or Paneer Curry with 3 Rotis & Curd", "160g chicken/paneer + 3 rotis + 1 bowl plain curd + steamed vegetables",
                    680.0, 48.0, 72.0, 19.0, "09:00 PM", "Rich calorie surplus dinner to replenish glycogen stores", false));
        }

        for (NutritionPlanItem item : defaults) {
            saveItem(item);
        }
    }

    private NutritionPlanItem mapResultSet(ResultSet rs) throws SQLException {
        NutritionPlanItem item = new NutritionPlanItem();
        item.setId(rs.getString("id"));
        item.setUserId(rs.getString("user_id"));
        item.setPlanMode(rs.getString("plan_mode"));
        try {
            item.setMealType(MealType.valueOf(rs.getString("meal_type").toUpperCase()));
        } catch (Exception e) {
            item.setMealType(MealType.fromString(rs.getString("meal_type")));
        }
        item.setFoodName(rs.getString("food_name"));
        item.setServingSize(rs.getString("serving_size"));
        item.setCalories(rs.getDouble("calories"));
        item.setProteinG(rs.getDouble("protein_g"));
        item.setCarbsG(rs.getDouble("carbs_g"));
        item.setFatG(rs.getDouble("fat_g"));
        item.setMealTime(rs.getString("meal_time"));
        item.setNotes(rs.getString("notes"));
        item.setConsumed(rs.getInt("is_consumed") == 1);
        return item;
    }
}

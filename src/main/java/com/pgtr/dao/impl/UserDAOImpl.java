package com.pgtr.dao.impl;

import com.pgtr.dao.UserDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.ActivityLevel;
import com.pgtr.model.FitnessGoalType;
import com.pgtr.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean createUser(User user) throws SQLException {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO users (id, email, password_hash, full_name, age, height_cm, " +
                "current_weight_kg, target_weight_kg, fitness_goal, activity_level, " +
                "daily_calorie_target, daily_protein_target, daily_water_target, daily_sleep_target, " +
                "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName().trim());
            ps.setInt(5, user.getAge());
            ps.setDouble(6, user.getHeightCm());
            ps.setDouble(7, user.getCurrentWeightKg());
            ps.setDouble(8, user.getTargetWeightKg());
            ps.setString(9, user.getFitnessGoal() != null ? user.getFitnessGoal().name() : "MUSCLE_GAIN");
            ps.setString(10, user.getActivityLevel() != null ? user.getActivityLevel().name() : "MODERATELY_ACTIVE");
            ps.setInt(11, user.getDailyCalorieTarget());
            ps.setInt(12, user.getDailyProteinTarget());
            ps.setInt(13, user.getDailyWaterTarget());
            ps.setDouble(14, user.getDailySleepTarget());
            ps.setString(15, LocalDateTime.now().toString());
            ps.setString(16, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<User> getUserById(String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, age = ?, height_cm = ?, " +
                "current_weight_kg = ?, target_weight_kg = ?, fitness_goal = ?, " +
                "activity_level = ?, daily_calorie_target = ?, daily_protein_target = ?, " +
                "daily_water_target = ?, daily_sleep_target = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName().trim());
            ps.setInt(2, user.getAge());
            ps.setDouble(3, user.getHeightCm());
            ps.setDouble(4, user.getCurrentWeightKg());
            ps.setDouble(5, user.getTargetWeightKg());
            ps.setString(6, user.getFitnessGoal() != null ? user.getFitnessGoal().name() : "MUSCLE_GAIN");
            ps.setString(7, user.getActivityLevel() != null ? user.getActivityLevel().name() : "MODERATELY_ACTIVE");
            ps.setInt(8, user.getDailyCalorieTarget());
            ps.setInt(9, user.getDailyProteinTarget());
            ps.setInt(10, user.getDailyWaterTarget());
            ps.setDouble(11, user.getDailySleepTarget());
            ps.setString(12, LocalDateTime.now().toString());
            ps.setString(13, user.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(String userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setString(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setAge(rs.getInt("age"));
        user.setHeightCm(rs.getDouble("height_cm"));
        user.setCurrentWeightKg(rs.getDouble("current_weight_kg"));
        user.setTargetWeightKg(rs.getDouble("target_weight_kg"));
        user.setFitnessGoal(FitnessGoalType.fromString(rs.getString("fitness_goal")));
        user.setActivityLevel(ActivityLevel.fromString(rs.getString("activity_level")));
        user.setDailyCalorieTarget(rs.getInt("daily_calorie_target"));
        user.setDailyProteinTarget(rs.getInt("daily_protein_target"));
        user.setDailyWaterTarget(rs.getInt("daily_water_target"));
        user.setDailySleepTarget(rs.getDouble("daily_sleep_target"));
        return user;
    }
}

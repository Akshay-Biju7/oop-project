package com.pgtr.dao.impl;

import com.pgtr.dao.PlannerDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.WorkoutPlan;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlannerDAOImpl implements PlannerDAO {

    @Override
    public boolean savePlan(WorkoutPlan plan) throws SQLException {
        if (plan.getId() == null || plan.getId().trim().isEmpty()) {
            plan.setId(UUID.randomUUID().toString());
        }

        // Delete existing plan for this day to keep 1 plan per day
        String deleteSql = "DELETE FROM workout_plans WHERE user_id = ? AND day_of_week = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
            psDel.setString(1, plan.getUserId());
            psDel.setString(2, plan.getDayOfWeek().name());
            psDel.executeUpdate();
        }

        String sql = "INSERT INTO workout_plans (id, user_id, day_of_week, workout_name, " +
                "muscle_group, planned_exercises, scheduled_time, notes, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getId());
            ps.setString(2, plan.getUserId());
            ps.setString(3, plan.getDayOfWeek().name());
            ps.setString(4, plan.getWorkoutName());
            ps.setString(5, plan.getMuscleGroup());
            ps.setString(6, plan.getPlannedExercises());
            ps.setString(7, plan.getScheduledTime() != null ? plan.getScheduledTime().toString() : "18:00");
            ps.setString(8, plan.getNotes());
            ps.setInt(9, plan.isActive() ? 1 : 0);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePlan(WorkoutPlan plan) throws SQLException {
        String sql = "UPDATE workout_plans SET workout_name = ?, muscle_group = ?, " +
                "planned_exercises = ?, scheduled_time = ?, notes = ?, is_active = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getWorkoutName());
            ps.setString(2, plan.getMuscleGroup());
            ps.setString(3, plan.getPlannedExercises());
            ps.setString(4, plan.getScheduledTime() != null ? plan.getScheduledTime().toString() : "18:00");
            ps.setString(5, plan.getNotes());
            ps.setInt(6, plan.isActive() ? 1 : 0);
            ps.setString(7, plan.getId());
            ps.setString(8, plan.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deletePlan(String id, String userId) throws SQLException {
        String sql = "DELETE FROM workout_plans WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<WorkoutPlan> getWeeklyPlans(String userId) throws SQLException {
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_plans WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<WorkoutPlan> getPlanForDay(String userId, DayOfWeek dayOfWeek) throws SQLException {
        String sql = "SELECT * FROM workout_plans WHERE user_id = ? AND day_of_week = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, dayOfWeek.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    private WorkoutPlan mapResultSet(ResultSet rs) throws SQLException {
        WorkoutPlan p = new WorkoutPlan();
        p.setId(rs.getString("id"));
        p.setUserId(rs.getString("user_id"));
        try { p.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week").toUpperCase())); } catch (Exception ignored) {}
        p.setWorkoutName(rs.getString("workout_name"));
        p.setMuscleGroup(rs.getString("muscle_group"));
        p.setPlannedExercises(rs.getString("planned_exercises"));
        String timeStr = rs.getString("scheduled_time");
        if (timeStr != null) {
            try { p.setScheduledTime(LocalTime.parse(timeStr.substring(0, 5))); } catch (Exception ignored) {}
        }
        p.setNotes(rs.getString("notes"));
        p.setActive(rs.getInt("is_active") == 1);
        return p;
    }
}

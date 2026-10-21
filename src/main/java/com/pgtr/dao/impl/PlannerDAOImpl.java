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

        String mode = plan.getPlanMode() != null ? plan.getPlanMode().toUpperCase() : "BULK";

        // Delete existing plan for this mode and day to avoid duplicate schedules
        String deleteSql = "DELETE FROM workout_plans WHERE user_id = ? AND day_of_week = ? AND (plan_mode = ? OR plan_mode IS NULL)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement psDel = conn.prepareStatement(deleteSql)) {
            psDel.setString(1, plan.getUserId());
            psDel.setString(2, plan.getDayOfWeek().name());
            psDel.setString(3, mode);
            psDel.executeUpdate();
        }

        String sql = "INSERT INTO workout_plans (id, user_id, plan_mode, day_of_week, workout_name, " +
                "muscle_group, planned_exercises, rest_seconds, cardio, scheduled_time, notes, is_active, is_completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getId());
            ps.setString(2, plan.getUserId());
            ps.setString(3, mode);
            ps.setString(4, plan.getDayOfWeek().name());
            ps.setString(5, plan.getWorkoutName());
            ps.setString(6, plan.getMuscleGroup());
            ps.setString(7, plan.getPlannedExercises());
            ps.setInt(8, plan.getRestSeconds());
            ps.setString(9, plan.getCardio() != null ? plan.getCardio() : "None");
            ps.setString(10, plan.getScheduledTime() != null ? plan.getScheduledTime().toString() : "18:00");
            ps.setString(11, plan.getNotes());
            ps.setInt(12, plan.isActive() ? 1 : 0);
            ps.setInt(13, plan.isCompleted() ? 1 : 0);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePlan(WorkoutPlan plan) throws SQLException {
        String mode = plan.getPlanMode() != null ? plan.getPlanMode().toUpperCase() : "BULK";
        String sql = "UPDATE workout_plans SET workout_name = ?, muscle_group = ?, " +
                "planned_exercises = ?, rest_seconds = ?, cardio = ?, scheduled_time = ?, notes = ?, is_active = ?, is_completed = ?, plan_mode = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getWorkoutName());
            ps.setString(2, plan.getMuscleGroup());
            ps.setString(3, plan.getPlannedExercises());
            ps.setInt(4, plan.getRestSeconds());
            ps.setString(5, plan.getCardio() != null ? plan.getCardio() : "None");
            ps.setString(6, plan.getScheduledTime() != null ? plan.getScheduledTime().toString() : "18:00");
            ps.setString(7, plan.getNotes());
            ps.setInt(8, plan.isActive() ? 1 : 0);
            ps.setInt(9, plan.isCompleted() ? 1 : 0);
            ps.setString(10, mode);
            ps.setString(11, plan.getId());
            ps.setString(12, plan.getUserId());

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
    public boolean deletePlanForDay(String userId, String mode, DayOfWeek day) throws SQLException {
        String sql = "DELETE FROM workout_plans WHERE user_id = ? AND day_of_week = ? AND (plan_mode = ? OR plan_mode IS NULL)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, day.name());
            ps.setString(3, mode.toUpperCase());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<WorkoutPlan> getWeeklyPlans(String userId) throws SQLException {
        return getWeeklyPlans(userId, "BULK");
    }

    @Override
    public List<WorkoutPlan> getWeeklyPlans(String userId, String mode) throws SQLException {
        String targetMode = mode != null ? mode.toUpperCase() : "BULK";
        List<WorkoutPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM workout_plans WHERE user_id = ? AND (plan_mode = ? OR (plan_mode IS NULL AND ? = 'BULK'))";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, targetMode);
            ps.setString(3, targetMode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }

        // If no plans found for this mode, auto-populate default example plans
        if (list.isEmpty()) {
            resetPlansToDefault(userId, targetMode);
            return getWeeklyPlans(userId, targetMode);
        }

        return list;
    }

    @Override
    public Optional<WorkoutPlan> getPlanForDay(String userId, DayOfWeek dayOfWeek) throws SQLException {
        return getPlanForDay(userId, "BULK", dayOfWeek);
    }

    @Override
    public Optional<WorkoutPlan> getPlanForDay(String userId, String mode, DayOfWeek dayOfWeek) throws SQLException {
        String targetMode = mode != null ? mode.toUpperCase() : "BULK";
        String sql = "SELECT * FROM workout_plans WHERE user_id = ? AND day_of_week = ? AND (plan_mode = ? OR (plan_mode IS NULL AND ? = 'BULK')) LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, dayOfWeek.name());
            ps.setString(3, targetMode);
            ps.setString(4, targetMode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean setPlanCompleted(String planId, String userId, boolean completed) throws SQLException {
        String sql = "UPDATE workout_plans SET is_completed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, completed ? 1 : 0);
            ps.setString(2, planId);
            ps.setString(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void resetPlansToDefault(String userId, String mode) throws SQLException {
        String targetMode = mode != null ? mode.toUpperCase() : "BULK";

        // Remove existing plans for this mode
        String delSql = "DELETE FROM workout_plans WHERE user_id = ? AND (plan_mode = ? OR (plan_mode IS NULL AND ? = 'BULK'))";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(delSql)) {
            ps.setString(1, userId);
            ps.setString(2, targetMode);
            ps.setString(3, targetMode);
            ps.executeUpdate();
        }

        List<WorkoutPlan> defaults = new ArrayList<>();

        if ("CUT".equalsIgnoreCase(targetMode)) {
            // Cut Mode: Fat loss support, higher density, short rest, optional cardio
            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.MONDAY,
                    "Chest & Triceps (Deficit Support)", "Chest & Triceps",
                    "Incline DB Press 4x10, Flat Barbell Bench 3x12, Cable Chest Flyes 3x15, Cable Triceps Pushdown 3x15",
                    60, "15 min Incline Treadmill Walk", LocalTime.of(18, 0), "Focus on mind-muscle connection and controlled eccentrics", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.TUESDAY,
                    "Back & Biceps (High Density)", "Back & Biceps",
                    "Lat Pulldown 4x12, Seated Cable Row 3x12, DB Hammer Curls 3x15, Face Pulls 3x15",
                    60, "15 min Rowing Machine (Steady)", LocalTime.of(18, 0), "Keep rest strictly to 60 seconds", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.WEDNESDAY,
                    "Legs & Abs (Metabolic Conditioning)", "Legs & Core",
                    "Goblet Squats 4x12, Walking Lunges 3x15 per leg, Leg Curls 3x15, Hanging Knee Raises 3x20, Planks 3x60s",
                    60, "10 min HIIT Sprints (30s on / 30s off)", LocalTime.of(18, 0), "High caloric burn session", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.THURSDAY,
                    "Shoulders & Traps (Definition)", "Shoulders & Traps",
                    "DB Overhead Press 4x10, DB Lateral Raises 4x15, DB Shrugs 3x15, Rear Delt Flyes 3x15",
                    60, "15 min Stairmaster", LocalTime.of(18, 0), "Strict lateral raises, pause at top", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.FRIDAY,
                    "Upper Body Circuit / Weak Points", "Upper Body",
                    "Push-ups 3xMax, Lat Machine Pulldowns 3x10, Cable Lateral Raise 3x15, Overhead Tricep Extension 3x12, EZ Bar Curl 3x12",
                    45, "20 min Steady State Elliptical", LocalTime.of(18, 0), "Compound volume to retain muscle mass in deficit", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.SATURDAY,
                    "Optional Cardio & Core Conditioning", "Cardio & Core",
                    "Kettlebell Swings 4x20, Battle Ropes 4x30s, Ab Wheel Rollouts 3x12, Russian Twists 3x20",
                    45, "25 min Zone 2 Incline Walking", LocalTime.of(10, 0), "Optional active fat oxidation session", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "CUT", DayOfWeek.SUNDAY,
                    "Active Rest & Recovery", "Rest & Recovery",
                    "Full body mobility, foam rolling, dynamic hamstring and hip flexor stretches",
                    0, "None (Active Walk)", LocalTime.of(9, 0), "Prioritize sleep, hydration, and nutritional deficit", true, false));
        } else {
            // Bulk Mode: Progressive overload, heavy compound lifts, long rest intervals
            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.MONDAY,
                    "Heavy Chest & Triceps Blast", "Chest & Triceps",
                    "Flat Barbell Bench Press 4x8, Incline DB Press 4x8-10, Weighted Dips 3x8-10, Close-Grip Bench 3x8, Skull Crushers 3x10",
                    120, "None (Preserve caloric surplus)", LocalTime.of(18, 0), "Progressive overload on flat bench press", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.TUESDAY,
                    "Back & Biceps (Mass Builder)", "Back & Biceps",
                    "Conventional Barbell Deadlift 4x5, Barbell Bent-Over Row 4x8, Weighted Pull-ups 3x8, Single-Arm DB Row 3x10, Barbell Curl 4x8",
                    150, "None", LocalTime.of(18, 0), "Drive through the floor on deadlifts", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.WEDNESDAY,
                    "Heavy Legs & Calves (Max Strength)", "Legs & Core",
                    "Barbell Back Squats 4x8, Romanian Deadlifts 4x8, Heavy Leg Press 4x10, Standing Calf Raises 4x12-15",
                    150, "None", LocalTime.of(18, 0), "Hit parallel or below on squats", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.THURSDAY,
                    "Heavy Shoulders & Traps", "Shoulders & Traps",
                    "Standing Barbell Overhead Press 4x6-8, Seated DB Shoulder Press 3x8-10, Heavy DB Lateral Raise 4x12, Heavy Barbell Shrugs 4x10",
                    120, "None", LocalTime.of(18, 0), "Lock out elbows overhead with control", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.FRIDAY,
                    "Upper Body Hypertrophy & Arms", "Upper Body & Arms",
                    "Incline Barbell Press 3x8-10, Neutral Lat Pulldown 3x10, Incline DB Curl 3x10, Cable Overhead Extension 3x10, Cable Lateral Raise 3x12",
                    90, "None", LocalTime.of(18, 0), "Arm focus & pump volume", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.SATURDAY,
                    "Lower Body Volume & Hamstrings", "Legs",
                    "Hack Squat / Front Squat 3x8-10, Lying Hamstring Curl 4x10, Bulgarian Split Squat 3x10 per leg, Seated Calf Raise 4x12",
                    120, "None", LocalTime.of(11, 0), "Full range of motion quad and hamstring hypertrophy", true, false));

            defaults.add(new WorkoutPlan(UUID.randomUUID().toString(), userId, "BULK", DayOfWeek.SUNDAY,
                    "Rest & Surplus Growth Day", "Rest & Recovery",
                    "Rest day - Eat high quality surplus calories, 8+ hours sleep, hydrate with electrolytes",
                    0, "None", LocalTime.of(9, 0), "Rest is when muscle fibers rebuild larger and stronger", true, false));
        }

        for (WorkoutPlan p : defaults) {
            savePlan(p);
        }
    }

    private WorkoutPlan mapResultSet(ResultSet rs) throws SQLException {
        WorkoutPlan p = new WorkoutPlan();
        p.setId(rs.getString("id"));
        p.setUserId(rs.getString("user_id"));
        try {
            String modeStr = rs.getString("plan_mode");
            p.setPlanMode(modeStr != null ? modeStr.toUpperCase() : "BULK");
        } catch (Exception ignored) {
            p.setPlanMode("BULK");
        }
        try { p.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week").toUpperCase())); } catch (Exception ignored) {}
        p.setWorkoutName(rs.getString("workout_name"));
        p.setMuscleGroup(rs.getString("muscle_group"));
        p.setPlannedExercises(rs.getString("planned_exercises"));
        try { p.setRestSeconds(rs.getInt("rest_seconds")); } catch (Exception ignored) { p.setRestSeconds(90); }
        try { p.setCardio(rs.getString("cardio")); } catch (Exception ignored) { p.setCardio("None"); }
        String timeStr = rs.getString("scheduled_time");
        if (timeStr != null) {
            try { p.setScheduledTime(LocalTime.parse(timeStr.substring(0, 5))); } catch (Exception ignored) {}
        }
        p.setNotes(rs.getString("notes"));
        p.setActive(rs.getInt("is_active") == 1);
        try { p.setCompleted(rs.getInt("is_completed") == 1); } catch (Exception ignored) { p.setCompleted(false); }
        return p;
    }
}

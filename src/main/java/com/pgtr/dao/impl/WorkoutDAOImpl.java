package com.pgtr.dao.impl;

import com.pgtr.dao.WorkoutDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.Exercise;
import com.pgtr.model.Workout;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WorkoutDAOImpl implements WorkoutDAO {

    @Override
    public boolean createWorkout(Workout workout) throws SQLException {
        if (workout.getId() == null || workout.getId().trim().isEmpty()) {
            workout.setId(UUID.randomUUID().toString());
        }

        String insertWorkoutSql = "INSERT INTO workouts (id, user_id, workout_date, workout_name, " +
                "muscle_group, duration_minutes, calories_burned, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String insertExerciseSql = "INSERT INTO workout_exercises (id, workout_id, user_id, exercise_name, " +
                "exercise_type, sets, reps, weight_kg, volume_kg, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement ps = conn.prepareStatement(insertWorkoutSql)) {
                ps.setString(1, workout.getId());
                ps.setString(2, workout.getUserId());
                ps.setString(3, workout.getWorkoutDate().toString());
                ps.setString(4, workout.getWorkoutName());
                ps.setString(5, workout.getMuscleGroup());
                ps.setInt(6, workout.getDurationMinutes());
                ps.setInt(7, workout.getCaloriesBurned());
                ps.setString(8, workout.getNotes());
                ps.setString(9, LocalDateTime.now().toString());
                ps.executeUpdate();
            }

            if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                try (PreparedStatement psEx = conn.prepareStatement(insertExerciseSql)) {
                    for (Exercise ex : workout.getExercises()) {
                        if (ex.getId() == null || ex.getId().trim().isEmpty()) {
                            ex.setId(UUID.randomUUID().toString());
                        }
                        psEx.setString(1, ex.getId());
                        psEx.setString(2, workout.getId());
                        psEx.setString(3, workout.getUserId());
                        psEx.setString(4, ex.getExerciseName());
                        psEx.setString(5, ex.getExerciseType());
                        psEx.setInt(6, ex.getSets());
                        psEx.setInt(7, ex.getReps());
                        psEx.setDouble(8, ex.getWeightKg());
                        psEx.setDouble(9, ex.getVolumeKg());
                        psEx.setString(10, ex.getNotes());
                        psEx.addBatch();
                    }
                    psEx.executeBatch();
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean updateWorkout(Workout workout) throws SQLException {
        String updateWorkoutSql = "UPDATE workouts SET workout_date = ?, workout_name = ?, " +
                "muscle_group = ?, duration_minutes = ?, calories_burned = ?, notes = ? WHERE id = ? AND user_id = ?";

        String deleteOldExercises = "DELETE FROM workout_exercises WHERE workout_id = ?";

        String insertExerciseSql = "INSERT INTO workout_exercises (id, workout_id, user_id, exercise_name, " +
                "exercise_type, sets, reps, weight_kg, volume_kg, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateWorkoutSql)) {
                ps.setString(1, workout.getWorkoutDate().toString());
                ps.setString(2, workout.getWorkoutName());
                ps.setString(3, workout.getMuscleGroup());
                ps.setInt(4, workout.getDurationMinutes());
                ps.setInt(5, workout.getCaloriesBurned());
                ps.setString(6, workout.getNotes());
                ps.setString(7, workout.getId());
                ps.setString(8, workout.getUserId());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(deleteOldExercises)) {
                psDel.setString(1, workout.getId());
                psDel.executeUpdate();
            }

            if (workout.getExercises() != null && !workout.getExercises().isEmpty()) {
                try (PreparedStatement psEx = conn.prepareStatement(insertExerciseSql)) {
                    for (Exercise ex : workout.getExercises()) {
                        if (ex.getId() == null || ex.getId().trim().isEmpty()) {
                            ex.setId(UUID.randomUUID().toString());
                        }
                        psEx.setString(1, ex.getId());
                        psEx.setString(2, workout.getId());
                        psEx.setString(3, workout.getUserId());
                        psEx.setString(4, ex.getExerciseName());
                        psEx.setString(5, ex.getExerciseType());
                        psEx.setInt(6, ex.getSets());
                        psEx.setInt(7, ex.getReps());
                        psEx.setDouble(8, ex.getWeightKg());
                        psEx.setDouble(9, ex.getVolumeKg());
                        psEx.setString(10, ex.getNotes());
                        psEx.addBatch();
                    }
                    psEx.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean deleteWorkout(String workoutId, String userId) throws SQLException {
        String sql = "DELETE FROM workouts WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workoutId);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Workout> getWorkoutById(String workoutId, String userId) throws SQLException {
        String sql = "SELECT * FROM workouts WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workoutId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Workout w = mapResultSetToWorkout(rs);
                    w.setExercises(getExercisesForWorkout(conn, w.getId()));
                    return Optional.of(w);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Workout> getWorkoutsByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM workouts WHERE user_id = ? ORDER BY workout_date DESC, created_at DESC";
        return fetchWorkoutsWithSql(sql, userId, null, null, null);
    }

    @Override
    public List<Workout> getWorkoutsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM workouts WHERE user_id = ? AND workout_date >= ? AND workout_date <= ? " +
                "ORDER BY workout_date DESC, created_at DESC";
        return fetchWorkoutsWithSql(sql, userId, start.toString(), end.toString(), null);
    }

    @Override
    public List<Workout> getRecentWorkouts(String userId, int limit) throws SQLException {
        String sql = "SELECT * FROM workouts WHERE user_id = ? ORDER BY workout_date DESC, created_at DESC LIMIT " + limit;
        return fetchWorkoutsWithSql(sql, userId, null, null, null);
    }

    @Override
    public int getWorkoutCountThisWeek(String userId) throws SQLException {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ? AND workout_date >= ? AND workout_date <= ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, monday.toString());
            ps.setString(3, sunday.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getTotalWorkoutsCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM workouts WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public double getMaxWeightLiftedForExercise(String userId, String exerciseName) throws SQLException {
        String sql = "SELECT MAX(weight_kg) FROM workout_exercises WHERE user_id = ? AND LOWER(exercise_name) = LOWER(?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, exerciseName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public List<Workout> searchWorkouts(String userId, String query, String muscleGroup) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM workouts WHERE user_id = ?");
        List<String> params = new ArrayList<>();
        params.add(userId);

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(workout_name) LIKE ? OR LOWER(notes) LIKE ?)");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
        }

        if (muscleGroup != null && !muscleGroup.equalsIgnoreCase("All")) {
            sql.append(" AND LOWER(muscle_group) = LOWER(?)");
            params.add(muscleGroup.trim());
        }

        sql.append(" ORDER BY workout_date DESC, created_at DESC");

        List<Workout> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workout w = mapResultSetToWorkout(rs);
                    w.setExercises(getExercisesForWorkout(conn, w.getId()));
                    list.add(w);
                }
            }
        }
        return list;
    }

    private List<Workout> fetchWorkoutsWithSql(String sql, String p1, String p2, String p3, String p4) throws SQLException {
        List<Workout> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (p1 != null) ps.setString(idx++, p1);
            if (p2 != null) ps.setString(idx++, p2);
            if (p3 != null) ps.setString(idx++, p3);
            if (p4 != null) ps.setString(idx++, p4);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workout w = mapResultSetToWorkout(rs);
                    w.setExercises(getExercisesForWorkout(conn, w.getId()));
                    list.add(w);
                }
            }
        }
        return list;
    }

    private List<Exercise> getExercisesForWorkout(Connection conn, String workoutId) throws SQLException {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM workout_exercises WHERE workout_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workoutId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exercise ex = new Exercise();
                    ex.setId(rs.getString("id"));
                    ex.setWorkoutId(rs.getString("workout_id"));
                    ex.setUserId(rs.getString("user_id"));
                    ex.setExerciseName(rs.getString("exercise_name"));
                    ex.setExerciseType(rs.getString("exercise_type"));
                    ex.setSets(rs.getInt("sets"));
                    ex.setReps(rs.getInt("reps"));
                    ex.setWeightKg(rs.getDouble("weight_kg"));
                    ex.setVolumeKg(rs.getDouble("volume_kg"));
                    ex.setNotes(rs.getString("notes"));
                    exercises.add(ex);
                }
            }
        }
        return exercises;
    }

    private Workout mapResultSetToWorkout(ResultSet rs) throws SQLException {
        Workout w = new Workout();
        w.setId(rs.getString("id"));
        w.setUserId(rs.getString("user_id"));
        String dateStr = rs.getString("workout_date");
        if (dateStr != null) {
            try { w.setWorkoutDate(LocalDate.parse(dateStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        w.setWorkoutName(rs.getString("workout_name"));
        w.setMuscleGroup(rs.getString("muscle_group"));
        w.setDurationMinutes(rs.getInt("duration_minutes"));
        w.setCaloriesBurned(rs.getInt("calories_burned"));
        w.setNotes(rs.getString("notes"));
        return w;
    }
}

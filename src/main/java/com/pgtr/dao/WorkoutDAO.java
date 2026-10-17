package com.pgtr.dao;

import com.pgtr.model.Workout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for gym workout sessions and exercises.
 */
public interface WorkoutDAO {
    boolean createWorkout(Workout workout) throws SQLException;
    boolean updateWorkout(Workout workout) throws SQLException;
    boolean deleteWorkout(String workoutId, String userId) throws SQLException;
    Optional<Workout> getWorkoutById(String workoutId, String userId) throws SQLException;
    List<Workout> getWorkoutsByUser(String userId) throws SQLException;
    List<Workout> getWorkoutsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException;
    List<Workout> getRecentWorkouts(String userId, int limit) throws SQLException;
    int getWorkoutCountThisWeek(String userId) throws SQLException;
    int getTotalWorkoutsCount(String userId) throws SQLException;
    double getMaxWeightLiftedForExercise(String userId, String exerciseName) throws SQLException;
    List<Workout> searchWorkouts(String userId, String query, String muscleGroup) throws SQLException;
}

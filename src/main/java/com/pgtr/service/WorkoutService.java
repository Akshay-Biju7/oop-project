package com.pgtr.service;

import com.pgtr.dao.WorkoutDAO;
import com.pgtr.dao.impl.WorkoutDAOImpl;
import com.pgtr.model.Exercise;
import com.pgtr.model.Workout;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class WorkoutService {

    private final WorkoutDAO workoutDAO;
    private final AchievementService achievementService;

    public WorkoutService() {
        this.workoutDAO = new WorkoutDAOImpl();
        this.achievementService = new AchievementService();
    }

    public WorkoutService(WorkoutDAO workoutDAO, AchievementService achievementService) {
        this.workoutDAO = workoutDAO;
        this.achievementService = achievementService;
    }

    public boolean logWorkout(Workout workout) throws SQLException {
        if (workout == null || workout.getUserId() == null) {
            throw new IllegalArgumentException("Workout data is invalid.");
        }

        // Check if any exercise breaks an existing personal record (PR)
        boolean hasNewPr = false;
        if (workout.getExercises() != null) {
            for (Exercise ex : workout.getExercises()) {
                if (ex.getWeightKg() > 0) {
                    double prevMax = workoutDAO.getMaxWeightLiftedForExercise(workout.getUserId(), ex.getExerciseName());
                    if (ex.getWeightKg() > prevMax && prevMax > 0) {
                        hasNewPr = true;
                    }
                }
            }
        }

        boolean saved = workoutDAO.createWorkout(workout);
        if (saved) {
            // Milestone check 1: First workout ever
            int total = workoutDAO.getTotalWorkoutsCount(workout.getUserId());
            if (total == 1) {
                achievementService.awardIfEligible(workout.getUserId(), "FIRST_WORKOUT");
            } else if (total >= 10) {
                achievementService.awardIfEligible(workout.getUserId(), "WORKOUTS_10");
            }
            if (total >= 50) {
                achievementService.awardIfEligible(workout.getUserId(), "WORKOUTS_50");
            }

            // Milestone check 2: PR Smasher
            if (hasNewPr) {
                achievementService.awardIfEligible(workout.getUserId(), "NEW_PR");
            }

            // Milestone check 3: 7-day streak
            if (calculateCurrentStreak(workout.getUserId()) >= 7) {
                achievementService.awardIfEligible(workout.getUserId(), "STREAK_7_DAYS");
            }
        }
        return saved;
    }

    public boolean updateWorkout(Workout workout) throws SQLException {
        return workoutDAO.updateWorkout(workout);
    }

    public boolean deleteWorkout(String workoutId, String userId) throws SQLException {
        return workoutDAO.deleteWorkout(workoutId, userId);
    }

    public List<Workout> getUserWorkouts(String userId) throws SQLException {
        return workoutDAO.getWorkoutsByUser(userId);
    }

    public List<Workout> getRecentWorkouts(String userId, int limit) throws SQLException {
        return workoutDAO.getRecentWorkouts(userId, limit);
    }

    public List<Workout> searchWorkouts(String userId, String query, String muscleGroup) throws SQLException {
        return workoutDAO.searchWorkouts(userId, query, muscleGroup);
    }

    public int getWorkoutsThisWeek(String userId) throws SQLException {
        return workoutDAO.getWorkoutCountThisWeek(userId);
    }

    /**
     * Calculates consecutive day streak of active gym workouts.
     */
    public int calculateCurrentStreak(String userId) {
        try {
            List<Workout> workouts = workoutDAO.getWorkoutsByUser(userId);
            if (workouts.isEmpty()) return 0;

            Set<LocalDate> activeDates = new TreeSet<>(Collections.reverseOrder());
            for (Workout w : workouts) {
                activeDates.add(w.getWorkoutDate());
            }

            LocalDate today = LocalDate.now();
            LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);

            int streak = 0;
            while (activeDates.contains(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            }
            return streak;
        } catch (Exception e) {
            return 0;
        }
    }
}

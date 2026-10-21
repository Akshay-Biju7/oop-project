package com.pgtr.dao;

import com.pgtr.model.WorkoutPlan;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for weekly workout routine planning supporting Cut and Bulk modes.
 */
public interface PlannerDAO {
    boolean savePlan(WorkoutPlan plan) throws SQLException;
    boolean updatePlan(WorkoutPlan plan) throws SQLException;
    boolean deletePlan(String id, String userId) throws SQLException;
    boolean deletePlanForDay(String userId, String mode, DayOfWeek day) throws SQLException;
    List<WorkoutPlan> getWeeklyPlans(String userId) throws SQLException;
    List<WorkoutPlan> getWeeklyPlans(String userId, String mode) throws SQLException;
    Optional<WorkoutPlan> getPlanForDay(String userId, DayOfWeek dayOfWeek) throws SQLException;
    Optional<WorkoutPlan> getPlanForDay(String userId, String mode, DayOfWeek dayOfWeek) throws SQLException;
    boolean setPlanCompleted(String planId, String userId, boolean completed) throws SQLException;
    void resetPlansToDefault(String userId, String mode) throws SQLException;
}

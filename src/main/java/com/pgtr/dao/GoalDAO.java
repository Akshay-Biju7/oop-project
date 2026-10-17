package com.pgtr.dao;

import com.pgtr.model.FitnessGoal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for personal fitness goals and milestones.
 */
public interface GoalDAO {
    boolean createGoal(FitnessGoal goal) throws SQLException;
    boolean updateGoal(FitnessGoal goal) throws SQLException;
    boolean deleteGoal(String id, String userId) throws SQLException;
    Optional<FitnessGoal> getGoalById(String id, String userId) throws SQLException;
    List<FitnessGoal> getGoalsByUser(String userId) throws SQLException;
    List<FitnessGoal> getActiveGoals(String userId) throws SQLException;
    int getCompletedGoalsCount(String userId) throws SQLException;
}

package com.pgtr.service;

import com.pgtr.dao.GoalDAO;
import com.pgtr.dao.impl.GoalDAOImpl;
import com.pgtr.model.FitnessGoal;
import com.pgtr.model.GoalStatus;

import java.sql.SQLException;
import java.util.List;

public class GoalService {

    private final GoalDAO goalDAO;
    private final AchievementService achievementService;

    public GoalService() {
        this.goalDAO = new GoalDAOImpl();
        this.achievementService = new AchievementService();
    }

    public GoalService(GoalDAO goalDAO, AchievementService achievementService) {
        this.goalDAO = goalDAO;
        this.achievementService = achievementService;
    }

    public boolean createGoal(FitnessGoal goal) throws SQLException {
        return goalDAO.createGoal(goal);
    }

    public boolean updateGoal(FitnessGoal goal) throws SQLException {
        boolean updated = goalDAO.updateGoal(goal);
        if (updated && goal.getStatus() == GoalStatus.ACHIEVED) {
            achievementService.awardIfEligible(goal.getUserId(), "GOAL_COMPLETED");
        }
        return updated;
    }

    public boolean deleteGoal(String id, String userId) throws SQLException {
        return goalDAO.deleteGoal(id, userId);
    }

    public boolean markAchieved(String id, String userId) throws SQLException {
        var opt = goalDAO.getGoalById(id, userId);
        if (opt.isPresent()) {
            FitnessGoal g = opt.get();
            g.setStatus(GoalStatus.ACHIEVED);
            g.setCurrentValue(g.getTargetValue());
            boolean ok = goalDAO.updateGoal(g);
            if (ok) {
                achievementService.awardIfEligible(userId, "GOAL_COMPLETED");
            }
            return ok;
        }
        return false;
    }

    public List<FitnessGoal> getGoalsByUser(String userId) throws SQLException {
        return goalDAO.getGoalsByUser(userId);
    }

    public List<FitnessGoal> getActiveGoals(String userId) throws SQLException {
        return goalDAO.getActiveGoals(userId);
    }
}

package com.pgtr.service;

import com.pgtr.dao.PlannerDAO;
import com.pgtr.dao.impl.PlannerDAOImpl;
import com.pgtr.model.WorkoutPlan;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public class PlannerService {

    private final PlannerDAO plannerDAO;

    public PlannerService() {
        this.plannerDAO = new PlannerDAOImpl();
    }

    public PlannerService(PlannerDAO plannerDAO) {
        this.plannerDAO = plannerDAO;
    }

    public boolean savePlan(WorkoutPlan plan) throws SQLException {
        return plannerDAO.savePlan(plan);
    }

    public boolean updatePlan(WorkoutPlan plan) throws SQLException {
        return plannerDAO.updatePlan(plan);
    }

    public boolean deletePlan(String id, String userId) throws SQLException {
        return plannerDAO.deletePlan(id, userId);
    }

    public boolean deletePlanForDay(String userId, String mode, DayOfWeek day) throws SQLException {
        return plannerDAO.deletePlanForDay(userId, mode, day);
    }

    public List<WorkoutPlan> getWeeklySchedule(String userId) throws SQLException {
        return plannerDAO.getWeeklyPlans(userId);
    }

    public List<WorkoutPlan> getWeeklySchedule(String userId, String mode) throws SQLException {
        return plannerDAO.getWeeklyPlans(userId, mode);
    }

    public Optional<WorkoutPlan> getPlanForToday(String userId) throws SQLException {
        return plannerDAO.getPlanForDay(userId, java.time.LocalDate.now().getDayOfWeek());
    }

    public Optional<WorkoutPlan> getPlanForToday(String userId, String mode) throws SQLException {
        return plannerDAO.getPlanForDay(userId, mode, java.time.LocalDate.now().getDayOfWeek());
    }

    public boolean setPlanCompleted(String planId, String userId, boolean completed) throws SQLException {
        return plannerDAO.setPlanCompleted(planId, userId, completed);
    }

    public void resetToDefault(String userId, String mode) throws SQLException {
        plannerDAO.resetPlansToDefault(userId, mode);
    }
}

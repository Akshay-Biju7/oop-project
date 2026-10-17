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

    public List<WorkoutPlan> getWeeklySchedule(String userId) throws SQLException {
        return plannerDAO.getWeeklyPlans(userId);
    }

    public Optional<WorkoutPlan> getPlanForToday(String userId) throws SQLException {
        return plannerDAO.getPlanForDay(userId, java.time.LocalDate.now().getDayOfWeek());
    }
}

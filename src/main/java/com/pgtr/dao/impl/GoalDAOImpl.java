package com.pgtr.dao.impl;

import com.pgtr.dao.GoalDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.FitnessGoal;
import com.pgtr.model.GoalCategory;
import com.pgtr.model.GoalStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GoalDAOImpl implements GoalDAO {

    @Override
    public boolean createGoal(FitnessGoal goal) throws SQLException {
        if (goal.getId() == null || goal.getId().trim().isEmpty()) {
            goal.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO fitness_goals (id, user_id, title, description, category, " +
                "starting_value, target_value, current_value, unit, start_date, deadline, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getId());
            ps.setString(2, goal.getUserId());
            ps.setString(3, goal.getTitle());
            ps.setString(4, goal.getDescription());
            ps.setString(5, goal.getCategory() != null ? goal.getCategory().name() : "WEIGHT");
            ps.setDouble(6, goal.getStartingValue());
            ps.setDouble(7, goal.getTargetValue());
            ps.setDouble(8, goal.getCurrentValue());
            ps.setString(9, goal.getUnit());
            ps.setString(10, goal.getStartDate().toString());
            ps.setString(11, goal.getDeadline().toString());
            ps.setString(12, goal.getStatus() != null ? goal.getStatus().name() : "IN_PROGRESS");
            ps.setString(13, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateGoal(FitnessGoal goal) throws SQLException {
        String sql = "UPDATE fitness_goals SET title = ?, description = ?, category = ?, " +
                "starting_value = ?, target_value = ?, current_value = ?, unit = ?, " +
                "start_date = ?, deadline = ?, status = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getTitle());
            ps.setString(2, goal.getDescription());
            ps.setString(3, goal.getCategory() != null ? goal.getCategory().name() : "WEIGHT");
            ps.setDouble(4, goal.getStartingValue());
            ps.setDouble(5, goal.getTargetValue());
            ps.setDouble(6, goal.getCurrentValue());
            ps.setString(7, goal.getUnit());
            ps.setString(8, goal.getStartDate().toString());
            ps.setString(9, goal.getDeadline().toString());
            ps.setString(10, goal.getStatus() != null ? goal.getStatus().name() : "IN_PROGRESS");
            ps.setString(11, goal.getId());
            ps.setString(12, goal.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteGoal(String id, String userId) throws SQLException {
        String sql = "DELETE FROM fitness_goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<FitnessGoal> getGoalById(String id, String userId) throws SQLException {
        String sql = "SELECT * FROM fitness_goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FitnessGoal> getGoalsByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM fitness_goals WHERE user_id = ? ORDER BY deadline ASC";
        return fetchList(sql, userId);
    }

    @Override
    public List<FitnessGoal> getActiveGoals(String userId) throws SQLException {
        String sql = "SELECT * FROM fitness_goals WHERE user_id = ? AND status = 'IN_PROGRESS' ORDER BY deadline ASC";
        return fetchList(sql, userId);
    }

    @Override
    public int getCompletedGoalsCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM fitness_goals WHERE user_id = ? AND status = 'ACHIEVED'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private List<FitnessGoal> fetchList(String sql, String userId) throws SQLException {
        List<FitnessGoal> list = new ArrayList<>();
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

    private FitnessGoal mapResultSet(ResultSet rs) throws SQLException {
        FitnessGoal g = new FitnessGoal();
        g.setId(rs.getString("id"));
        g.setUserId(rs.getString("user_id"));
        g.setTitle(rs.getString("title"));
        g.setDescription(rs.getString("description"));
        g.setCategory(GoalCategory.fromString(rs.getString("category")));
        g.setStartingValue(rs.getDouble("starting_value"));
        g.setTargetValue(rs.getDouble("target_value"));
        g.setCurrentValue(rs.getDouble("current_value"));
        g.setUnit(rs.getString("unit"));
        String startStr = rs.getString("start_date");
        if (startStr != null) {
            try { g.setStartDate(LocalDate.parse(startStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        String deadlineStr = rs.getString("deadline");
        if (deadlineStr != null) {
            try { g.setDeadline(LocalDate.parse(deadlineStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        g.setStatus(GoalStatus.fromString(rs.getString("status")));
        return g;
    }
}

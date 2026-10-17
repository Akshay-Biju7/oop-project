package com.pgtr.dao.impl;

import com.pgtr.dao.WaterDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.WaterLog;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class WaterDAOImpl implements WaterDAO {

    @Override
    public boolean addWater(WaterLog log) throws SQLException {
        if (log.getId() == null || log.getId().trim().isEmpty()) {
            log.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO water_logs (id, user_id, log_date, amount_ml, logged_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getId());
            ps.setString(2, log.getUserId());
            ps.setString(3, log.getLogDate().toString());
            ps.setInt(4, log.getAmountMl());
            ps.setString(5, log.getLoggedAt() != null ? log.getLoggedAt().toString() : LocalTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteWaterLog(String id, String userId) throws SQLException {
        String sql = "DELETE FROM water_logs WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getTotalWaterForDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT SUM(amount_ml) FROM water_logs WHERE user_id = ? AND log_date = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public List<WaterLog> getWaterLogsForDate(String userId, LocalDate date) throws SQLException {
        List<WaterLog> list = new ArrayList<>();
        String sql = "SELECT * FROM water_logs WHERE user_id = ? AND log_date = ? ORDER BY logged_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WaterLog log = new WaterLog();
                    log.setId(rs.getString("id"));
                    log.setUserId(rs.getString("user_id"));
                    log.setLogDate(LocalDate.parse(rs.getString("log_date").substring(0, 10)));
                    log.setAmountMl(rs.getInt("amount_ml"));
                    String timeStr = rs.getString("logged_at");
                    if (timeStr != null) {
                        try { log.setLoggedAt(LocalTime.parse(timeStr.substring(0, 5))); } catch (Exception ignored) {}
                    }
                    list.add(log);
                }
            }
        }
        return list;
    }

    @Override
    public Map<LocalDate, Integer> getWaterHistory(String userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<LocalDate, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT log_date, SUM(amount_ml) FROM water_logs WHERE user_id = ? " +
                "AND log_date >= ? AND log_date <= ? GROUP BY log_date ORDER BY log_date ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, startDate.toString());
            ps.setString(3, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = LocalDate.parse(rs.getString(1).substring(0, 10));
                    int total = rs.getInt(2);
                    map.put(d, total);
                }
            }
        }
        return map;
    }
}

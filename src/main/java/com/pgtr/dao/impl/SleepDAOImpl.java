package com.pgtr.dao.impl;

import com.pgtr.dao.SleepDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.SleepLog;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SleepDAOImpl implements SleepDAO {

    @Override
    public boolean logSleep(SleepLog log) throws SQLException {
        if (log.getId() == null || log.getId().trim().isEmpty()) {
            log.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO sleep_logs (id, user_id, sleep_date, sleep_time, " +
                "wake_time, duration_hours, sleep_quality, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getId());
            ps.setString(2, log.getUserId());
            ps.setString(3, log.getSleepDate().toString());
            ps.setString(4, log.getSleepTime() != null ? log.getSleepTime().toString() : "23:00");
            ps.setString(5, log.getWakeTime() != null ? log.getWakeTime().toString() : "07:00");
            ps.setDouble(6, log.getDurationHours());
            ps.setString(7, log.getSleepQuality());
            ps.setString(8, log.getNotes());
            ps.setString(9, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSleep(SleepLog log) throws SQLException {
        String sql = "UPDATE sleep_logs SET sleep_date = ?, sleep_time = ?, wake_time = ?, " +
                "duration_hours = ?, sleep_quality = ?, notes = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getSleepDate().toString());
            ps.setString(2, log.getSleepTime() != null ? log.getSleepTime().toString() : "23:00");
            ps.setString(3, log.getWakeTime() != null ? log.getWakeTime().toString() : "07:00");
            ps.setDouble(4, log.getDurationHours());
            ps.setString(5, log.getSleepQuality());
            ps.setString(6, log.getNotes());
            ps.setString(7, log.getId());
            ps.setString(8, log.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteSleepLog(String id, String userId) throws SQLException {
        String sql = "DELETE FROM sleep_logs WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<SleepLog> getSleepLogByDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM sleep_logs WHERE user_id = ? AND sleep_date = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SleepLog> getRecentSleepLogs(String userId, int limit) throws SQLException {
        String sql = "SELECT * FROM sleep_logs WHERE user_id = ? ORDER BY sleep_date DESC LIMIT " + limit;
        return fetchList(sql, userId, null, null);
    }

    @Override
    public List<SleepLog> getSleepLogsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM sleep_logs WHERE user_id = ? AND sleep_date >= ? AND sleep_date <= ? ORDER BY sleep_date ASC";
        return fetchList(sql, userId, start.toString(), end.toString());
    }

    @Override
    public double getAverageSleepDuration(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT AVG(duration_hours) FROM sleep_logs WHERE user_id = ? AND sleep_date >= ? AND sleep_date <= ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private List<SleepLog> fetchList(String sql, String userId, String p2, String p3) throws SQLException {
        List<SleepLog> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            if (p2 != null) ps.setString(2, p2);
            if (p3 != null) ps.setString(3, p3);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private SleepLog mapResultSet(ResultSet rs) throws SQLException {
        SleepLog s = new SleepLog();
        s.setId(rs.getString("id"));
        s.setUserId(rs.getString("user_id"));
        String dateStr = rs.getString("sleep_date");
        if (dateStr != null) {
            try { s.setSleepDate(LocalDate.parse(dateStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        String sleepTimeStr = rs.getString("sleep_time");
        if (sleepTimeStr != null) {
            try { s.setSleepTime(LocalTime.parse(sleepTimeStr.substring(0, 5))); } catch (Exception ignored) {}
        }
        String wakeTimeStr = rs.getString("wake_time");
        if (wakeTimeStr != null) {
            try { s.setWakeTime(LocalTime.parse(wakeTimeStr.substring(0, 5))); } catch (Exception ignored) {}
        }
        s.setDurationHours(rs.getDouble("duration_hours"));
        s.setSleepQuality(rs.getString("sleep_quality"));
        s.setNotes(rs.getString("notes"));
        return s;
    }
}

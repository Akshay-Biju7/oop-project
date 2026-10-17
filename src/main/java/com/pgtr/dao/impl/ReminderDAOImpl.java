package com.pgtr.dao.impl;

import com.pgtr.dao.ReminderDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.Reminder;
import com.pgtr.model.ReminderType;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReminderDAOImpl implements ReminderDAO {

    @Override
    public boolean createReminder(Reminder reminder) throws SQLException {
        if (reminder.getId() == null || reminder.getId().trim().isEmpty()) {
            reminder.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO reminders (id, user_id, reminder_type, title, message, " +
                "reminder_time, is_recurring, is_dismissed, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reminder.getId());
            ps.setString(2, reminder.getUserId());
            ps.setString(3, reminder.getReminderType() != null ? reminder.getReminderType().name() : "WORKOUT");
            ps.setString(4, reminder.getTitle());
            ps.setString(5, reminder.getMessage());
            ps.setString(6, reminder.getReminderTime() != null ? reminder.getReminderTime().toString() : "18:00");
            ps.setInt(7, reminder.isRecurring() ? 1 : 0);
            ps.setInt(8, reminder.isDismissed() ? 1 : 0);
            ps.setString(9, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateReminder(Reminder reminder) throws SQLException {
        String sql = "UPDATE reminders SET reminder_type = ?, title = ?, message = ?, " +
                "reminder_time = ?, is_recurring = ?, is_dismissed = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reminder.getReminderType() != null ? reminder.getReminderType().name() : "WORKOUT");
            ps.setString(2, reminder.getTitle());
            ps.setString(3, reminder.getMessage());
            ps.setString(4, reminder.getReminderTime() != null ? reminder.getReminderTime().toString() : "18:00");
            ps.setInt(5, reminder.isRecurring() ? 1 : 0);
            ps.setInt(6, reminder.isDismissed() ? 1 : 0);
            ps.setString(7, reminder.getId());
            ps.setString(8, reminder.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteReminder(String id, String userId) throws SQLException {
        String sql = "DELETE FROM reminders WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Reminder> getRemindersByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? ORDER BY reminder_time ASC";
        return fetchList(sql, userId, false);
    }

    @Override
    public List<Reminder> getActiveReminders(String userId) throws SQLException {
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND is_dismissed = 0 ORDER BY reminder_time ASC";
        return fetchList(sql, userId, false);
    }

    @Override
    public boolean dismissReminder(String id, String userId) throws SQLException {
        String sql = "UPDATE reminders SET is_dismissed = 1 WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Reminder> fetchList(String sql, String userId, boolean onlyActive) throws SQLException {
        List<Reminder> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reminder r = new Reminder();
                    r.setId(rs.getString("id"));
                    r.setUserId(rs.getString("user_id"));
                    r.setReminderType(ReminderType.fromString(rs.getString("reminder_type")));
                    r.setTitle(rs.getString("title"));
                    r.setMessage(rs.getString("message"));
                    String timeStr = rs.getString("reminder_time");
                    if (timeStr != null) {
                        try { r.setReminderTime(LocalTime.parse(timeStr.substring(0, 5))); } catch (Exception ignored) {}
                    }
                    r.setRecurring(rs.getInt("is_recurring") == 1);
                    r.setDismissed(rs.getInt("is_dismissed") == 1);
                    list.add(r);
                }
            }
        }
        return list;
    }
}

package com.pgtr.dao;

import com.pgtr.model.Reminder;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for in-app reminders and notifications.
 */
public interface ReminderDAO {
    boolean createReminder(Reminder reminder) throws SQLException;
    boolean updateReminder(Reminder reminder) throws SQLException;
    boolean deleteReminder(String id, String userId) throws SQLException;
    List<Reminder> getRemindersByUser(String userId) throws SQLException;
    List<Reminder> getActiveReminders(String userId) throws SQLException;
    boolean dismissReminder(String id, String userId) throws SQLException;
}

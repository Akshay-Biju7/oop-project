package com.pgtr.service;

import com.pgtr.dao.ReminderDAO;
import com.pgtr.dao.impl.ReminderDAOImpl;
import com.pgtr.model.Reminder;

import java.sql.SQLException;
import java.util.List;

public class ReminderService {

    private final ReminderDAO reminderDAO;

    public ReminderService() {
        this.reminderDAO = new ReminderDAOImpl();
    }

    public ReminderService(ReminderDAO reminderDAO) {
        this.reminderDAO = reminderDAO;
    }

    public boolean createReminder(Reminder reminder) throws SQLException {
        return reminderDAO.createReminder(reminder);
    }

    public boolean updateReminder(Reminder reminder) throws SQLException {
        return reminderDAO.updateReminder(reminder);
    }

    public boolean deleteReminder(String id, String userId) throws SQLException {
        return reminderDAO.deleteReminder(id, userId);
    }

    public boolean dismissReminder(String id, String userId) throws SQLException {
        return reminderDAO.dismissReminder(id, userId);
    }

    public List<Reminder> getRemindersByUser(String userId) throws SQLException {
        return reminderDAO.getRemindersByUser(userId);
    }

    public List<Reminder> getActiveReminders(String userId) throws SQLException {
        return reminderDAO.getActiveReminders(userId);
    }
}

package com.pgtr.service;

import com.pgtr.dao.SleepDAO;
import com.pgtr.dao.WaterDAO;
import com.pgtr.dao.impl.SleepDAOImpl;
import com.pgtr.dao.impl.WaterDAOImpl;
import com.pgtr.model.SleepLog;
import com.pgtr.model.User;
import com.pgtr.model.WaterLog;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HabitService {

    private final WaterDAO waterDAO;
    private final SleepDAO sleepDAO;
    private final AchievementService achievementService;

    public HabitService() {
        this.waterDAO = new WaterDAOImpl();
        this.sleepDAO = new SleepDAOImpl();
        this.achievementService = new AchievementService();
    }

    public HabitService(WaterDAO waterDAO, SleepDAO sleepDAO, AchievementService achievementService) {
        this.waterDAO = waterDAO;
        this.sleepDAO = sleepDAO;
        this.achievementService = achievementService;
    }

    // --- Water Tracking ---

    public boolean addWater(String userId, int amountMl) throws SQLException {
        WaterLog log = new WaterLog(null, userId, LocalDate.now(), amountMl, LocalTime.now());
        boolean saved = waterDAO.addWater(log);
        if (saved) {
            User user = UserSession.getInstance().getCurrentUser();
            if (user != null && user.getId().equals(userId)) {
                int total = waterDAO.getTotalWaterForDate(userId, LocalDate.now());
                if (total >= user.getDailyWaterTarget()) {
                    achievementService.awardIfEligible(userId, "HYDRATION_HERO");
                }
            }
        }
        return saved;
    }

    public int getTodayWaterTotal(String userId) throws SQLException {
        return waterDAO.getTotalWaterForDate(userId, LocalDate.now());
    }

    public List<WaterLog> getTodayWaterLogs(String userId) throws SQLException {
        return waterDAO.getWaterLogsForDate(userId, LocalDate.now());
    }

    public int getWaterTotalForDate(String userId, LocalDate date) throws SQLException {
        return waterDAO.getTotalWaterForDate(userId, date);
    }

    public List<WaterLog> getWaterLogsForDate(String userId, LocalDate date) throws SQLException {
        return waterDAO.getWaterLogsForDate(userId, date);
    }

    public boolean addWaterForDate(String userId, LocalDate date, int amountMl) throws SQLException {
        WaterLog log = new WaterLog(null, userId, date, amountMl, LocalTime.now());
        boolean saved = waterDAO.addWater(log);
        if (saved) {
            User user = UserSession.getInstance().getCurrentUser();
            if (user != null && user.getId().equals(userId)) {
                int total = waterDAO.getTotalWaterForDate(userId, date);
                if (total >= user.getDailyWaterTarget()) {
                    achievementService.awardIfEligible(userId, "HYDRATION_HERO");
                }
            }
        }
        return saved;
    }

    public boolean deleteWaterLog(String id, String userId) throws SQLException {
        return waterDAO.deleteWaterLog(id, userId);
    }

    public Map<LocalDate, Integer> getWeeklyWaterHistory(String userId, LocalDate start, LocalDate end) throws SQLException {
        return waterDAO.getWaterHistory(userId, start, end);
    }

    // --- Sleep Tracking ---

    public boolean logSleep(SleepLog log) throws SQLException {
        boolean saved = sleepDAO.logSleep(log);
        if (saved) {
            if (log.getDurationHours() >= 7.0) {
                achievementService.awardIfEligible(log.getUserId(), "SLEEP_CHAMPION");
            }
        }
        return saved;
    }

    public Optional<SleepLog> getLatestSleep(String userId) throws SQLException {
        return sleepDAO.getSleepLogByDate(userId, LocalDate.now());
    }

    public List<SleepLog> getRecentSleepLogs(String userId, int limit) throws SQLException {
        return sleepDAO.getRecentSleepLogs(userId, limit);
    }

    public double getAverageSleep(String userId, LocalDate start, LocalDate end) throws SQLException {
        return sleepDAO.getAverageSleepDuration(userId, start, end);
    }
}

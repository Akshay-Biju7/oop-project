package com.pgtr.dao;

import com.pgtr.model.SleepLog;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for sleep tracking and sleep quality history.
 */
public interface SleepDAO {
    boolean logSleep(SleepLog log) throws SQLException;
    boolean updateSleep(SleepLog log) throws SQLException;
    boolean deleteSleepLog(String id, String userId) throws SQLException;
    Optional<SleepLog> getSleepLogByDate(String userId, LocalDate date) throws SQLException;
    List<SleepLog> getRecentSleepLogs(String userId, int limit) throws SQLException;
    List<SleepLog> getSleepLogsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException;
    double getAverageSleepDuration(String userId, LocalDate start, LocalDate end) throws SQLException;
}

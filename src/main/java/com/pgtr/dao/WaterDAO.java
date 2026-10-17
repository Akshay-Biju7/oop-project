package com.pgtr.dao;

import com.pgtr.model.WaterLog;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for hydration and daily water tracking.
 */
public interface WaterDAO {
    boolean addWater(WaterLog log) throws SQLException;
    boolean deleteWaterLog(String id, String userId) throws SQLException;
    int getTotalWaterForDate(String userId, LocalDate date) throws SQLException;
    List<WaterLog> getWaterLogsForDate(String userId, LocalDate date) throws SQLException;
    Map<LocalDate, Integer> getWaterHistory(String userId, LocalDate startDate, LocalDate endDate) throws SQLException;
}

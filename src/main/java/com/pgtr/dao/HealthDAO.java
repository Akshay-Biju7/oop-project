package com.pgtr.dao;

import com.pgtr.model.BodyMeasurement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for body measurements, weight, and BMI records.
 */
public interface HealthDAO {
    boolean createMeasurement(BodyMeasurement measurement) throws SQLException;
    boolean updateMeasurement(BodyMeasurement measurement) throws SQLException;
    boolean deleteMeasurement(String id, String userId) throws SQLException;
    Optional<BodyMeasurement> getLatestMeasurement(String userId) throws SQLException;
    Optional<BodyMeasurement> getMeasurementByDate(String userId, LocalDate date) throws SQLException;
    List<BodyMeasurement> getMeasurementHistory(String userId) throws SQLException;
    List<BodyMeasurement> getMeasurementsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException;
}

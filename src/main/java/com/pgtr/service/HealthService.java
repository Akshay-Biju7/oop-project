package com.pgtr.service;

import com.pgtr.dao.HealthDAO;
import com.pgtr.dao.UserDAO;
import com.pgtr.dao.impl.HealthDAOImpl;
import com.pgtr.dao.impl.UserDAOImpl;
import com.pgtr.model.BodyMeasurement;
import com.pgtr.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HealthService {

    private final HealthDAO healthDAO;
    private final UserDAO userDAO;

    public HealthService() {
        this.healthDAO = new HealthDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    public HealthService(HealthDAO healthDAO, UserDAO userDAO) {
        this.healthDAO = healthDAO;
        this.userDAO = userDAO;
    }

    public boolean logMeasurement(BodyMeasurement m) throws SQLException {
        boolean saved = healthDAO.createMeasurement(m);
        if (saved) {
            // Synchronize user profile current weight
            User user = UserSession.getInstance().getCurrentUser();
            if (user != null && user.getId().equals(m.getUserId())) {
                user.setCurrentWeightKg(m.getWeightKg());
                userDAO.updateProfile(user);
            }
        }
        return saved;
    }

    public boolean deleteMeasurement(String id, String userId) throws SQLException {
        return healthDAO.deleteMeasurement(id, userId);
    }

    public Optional<BodyMeasurement> getLatestMeasurement(String userId) throws SQLException {
        return healthDAO.getLatestMeasurement(userId);
    }

    public List<BodyMeasurement> getMeasurementHistory(String userId) throws SQLException {
        return healthDAO.getMeasurementHistory(userId);
    }

    public MeasurementComparison compare(BodyMeasurement a, BodyMeasurement b) {
        if (a == null || b == null) return null;
        return new MeasurementComparison(a, b);
    }

    public static class MeasurementComparison {
        public final BodyMeasurement initial;
        public final BodyMeasurement current;
        public final double weightDiff;
        public final double bmiDiff;
        public final double waistDiff;
        public final double chestDiff;
        public final double armsDiff;
        public final double thighsDiff;

        public MeasurementComparison(BodyMeasurement initial, BodyMeasurement current) {
            this.initial = initial;
            this.current = current;
            this.weightDiff = Math.round((current.getWeightKg() - initial.getWeightKg()) * 10.0) / 10.0;
            this.bmiDiff = Math.round((current.getBmi() - initial.getBmi()) * 10.0) / 10.0;
            this.waistDiff = Math.round((current.getWaistCm() - initial.getWaistCm()) * 10.0) / 10.0;
            this.chestDiff = Math.round((current.getChestCm() - initial.getChestCm()) * 10.0) / 10.0;
            this.armsDiff = Math.round((current.getArmsCm() - initial.getArmsCm()) * 10.0) / 10.0;
            this.thighsDiff = Math.round((current.getThighsCm() - initial.getThighsCm()) * 10.0) / 10.0;
        }
    }
}

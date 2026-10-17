package com.pgtr.dao.impl;

import com.pgtr.dao.HealthDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.BodyMeasurement;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HealthDAOImpl implements HealthDAO {

    @Override
    public boolean createMeasurement(BodyMeasurement m) throws SQLException {
        if (m.getId() == null || m.getId().trim().isEmpty()) {
            m.setId(UUID.randomUUID().toString());
        }
        m.recalculateBmi();

        String sql = "INSERT INTO body_measurements (id, user_id, log_date, weight_kg, " +
                "height_cm, bmi, waist_cm, chest_cm, arms_cm, thighs_cm, hips_cm, neck_cm, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getId());
            ps.setString(2, m.getUserId());
            ps.setString(3, m.getLogDate().toString());
            ps.setDouble(4, m.getWeightKg());
            ps.setDouble(5, m.getHeightCm());
            ps.setDouble(6, m.getBmi());
            ps.setDouble(7, m.getWaistCm());
            ps.setDouble(8, m.getChestCm());
            ps.setDouble(9, m.getArmsCm());
            ps.setDouble(10, m.getThighsCm());
            ps.setDouble(11, m.getHipsCm());
            ps.setDouble(12, m.getNeckCm());
            ps.setString(13, m.getNotes());
            ps.setString(14, LocalDateTime.now().toString());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateMeasurement(BodyMeasurement m) throws SQLException {
        m.recalculateBmi();
        String sql = "UPDATE body_measurements SET log_date = ?, weight_kg = ?, height_cm = ?, " +
                "bmi = ?, waist_cm = ?, chest_cm = ?, arms_cm = ?, thighs_cm = ?, hips_cm = ?, neck_cm = ?, notes = ? " +
                "WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getLogDate().toString());
            ps.setDouble(2, m.getWeightKg());
            ps.setDouble(3, m.getHeightCm());
            ps.setDouble(4, m.getBmi());
            ps.setDouble(5, m.getWaistCm());
            ps.setDouble(6, m.getChestCm());
            ps.setDouble(7, m.getArmsCm());
            ps.setDouble(8, m.getThighsCm());
            ps.setDouble(9, m.getHipsCm());
            ps.setDouble(10, m.getNeckCm());
            ps.setString(11, m.getNotes());
            ps.setString(12, m.getId());
            ps.setString(13, m.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteMeasurement(String id, String userId) throws SQLException {
        String sql = "DELETE FROM body_measurements WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<BodyMeasurement> getLatestMeasurement(String userId) throws SQLException {
        String sql = "SELECT * FROM body_measurements WHERE user_id = ? ORDER BY log_date DESC, created_at DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSet(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<BodyMeasurement> getMeasurementByDate(String userId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM body_measurements WHERE user_id = ? AND log_date = ? LIMIT 1";
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
    public List<BodyMeasurement> getMeasurementHistory(String userId) throws SQLException {
        String sql = "SELECT * FROM body_measurements WHERE user_id = ? ORDER BY log_date DESC, created_at DESC";
        return fetchList(sql, userId, null, null);
    }

    @Override
    public List<BodyMeasurement> getMeasurementsByDateRange(String userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT * FROM body_measurements WHERE user_id = ? AND log_date >= ? AND log_date <= ? ORDER BY log_date ASC";
        return fetchList(sql, userId, start.toString(), end.toString());
    }

    private List<BodyMeasurement> fetchList(String sql, String userId, String p2, String p3) throws SQLException {
        List<BodyMeasurement> list = new ArrayList<>();
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

    private BodyMeasurement mapResultSet(ResultSet rs) throws SQLException {
        BodyMeasurement m = new BodyMeasurement();
        m.setId(rs.getString("id"));
        m.setUserId(rs.getString("user_id"));
        String dateStr = rs.getString("log_date");
        if (dateStr != null) {
            try { m.setLogDate(LocalDate.parse(dateStr.substring(0, 10))); } catch (Exception ignored) {}
        }
        m.setWeightKg(rs.getDouble("weight_kg"));
        m.setHeightCm(rs.getDouble("height_cm"));
        m.setBmi(rs.getDouble("bmi"));
        m.setWaistCm(rs.getDouble("waist_cm"));
        m.setChestCm(rs.getDouble("chest_cm"));
        m.setArmsCm(rs.getDouble("arms_cm"));
        m.setThighsCm(rs.getDouble("thighs_cm"));
        m.setHipsCm(rs.getDouble("hips_cm"));
        m.setNeckCm(rs.getDouble("neck_cm"));
        m.setNotes(rs.getString("notes"));
        return m;
    }
}

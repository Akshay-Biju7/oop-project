package com.pgtr.dao.impl;

import com.pgtr.dao.AchievementDAO;
import com.pgtr.database.DatabaseManager;
import com.pgtr.model.Achievement;
import com.pgtr.model.UserAchievement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AchievementDAOImpl implements AchievementDAO {

    @Override
    public List<Achievement> getAllAchievements() throws SQLException {
        List<Achievement> list = new ArrayList<>();
        String sql = "SELECT * FROM achievements ORDER BY code";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Achievement a = new Achievement();
                a.setCode(rs.getString("code"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                a.setIconName(rs.getString("icon_name"));
                a.setBadgeTier(rs.getString("badge_tier"));
                list.add(a);
            }
        }
        return list;
    }

    @Override
    public List<UserAchievement> getUserAchievements(String userId) throws SQLException {
        List<UserAchievement> list = new ArrayList<>();
        String sql = "SELECT ua.id, ua.user_id, ua.achievement_code, ua.earned_at, " +
                "a.title, a.description, a.icon_name, a.badge_tier " +
                "FROM user_achievements ua " +
                "JOIN achievements a ON ua.achievement_code = a.code " +
                "WHERE ua.user_id = ? ORDER BY ua.earned_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserAchievement ua = new UserAchievement();
                    ua.setId(rs.getString("id"));
                    ua.setUserId(rs.getString("user_id"));
                    ua.setAchievementCode(rs.getString("achievement_code"));

                    String earnedStr = rs.getString("earned_at");
                    if (earnedStr != null) {
                        try { ua.setEarnedAt(LocalDateTime.parse(earnedStr.substring(0, 19).replace(' ', 'T'))); } catch (Exception ignored) {}
                    }

                    Achievement a = new Achievement(
                            ua.getAchievementCode(),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("icon_name"),
                            rs.getString("badge_tier")
                    );
                    ua.setAchievement(a);
                    list.add(ua);
                }
            }
        }
        return list;
    }

    @Override
    public boolean awardAchievement(String userId, String achievementCode) throws SQLException {
        if (hasUserEarned(userId, achievementCode)) {
            return false;
        }

        String sql = "INSERT INTO user_achievements (id, user_id, achievement_code, earned_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.setString(3, achievementCode);
            ps.setString(4, LocalDateTime.now().toString());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean hasUserEarned(String userId, String achievementCode) throws SQLException {
        String sql = "SELECT 1 FROM user_achievements WHERE user_id = ? AND achievement_code = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, achievementCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

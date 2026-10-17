package com.pgtr.service;

import com.pgtr.dao.AchievementDAO;
import com.pgtr.dao.impl.AchievementDAOImpl;
import com.pgtr.model.Achievement;
import com.pgtr.model.UserAchievement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AchievementService {

    private final AchievementDAO achievementDAO;

    public AchievementService() {
        this.achievementDAO = new AchievementDAOImpl();
    }

    public AchievementService(AchievementDAO achievementDAO) {
        this.achievementDAO = achievementDAO;
    }

    public boolean awardIfEligible(String userId, String achievementCode) {
        if (userId == null || achievementCode == null) return false;
        try {
            return achievementDAO.awardAchievement(userId, achievementCode);
        } catch (Exception e) {
            System.err.println("[AchievementService] Error awarding " + achievementCode + ": " + e.getMessage());
            return false;
        }
    }

    public List<UserAchievement> getUserAchievements(String userId) {
        try {
            return achievementDAO.getUserAchievements(userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<AchievementBadgeStatus> getCatalogWithUserStatus(String userId) {
        List<AchievementBadgeStatus> list = new ArrayList<>();
        try {
            List<Achievement> all = achievementDAO.getAllAchievements();
            List<UserAchievement> earned = achievementDAO.getUserAchievements(userId);
            Set<String> earnedCodes = earned.stream().map(UserAchievement::getAchievementCode).collect(Collectors.toSet());

            for (Achievement a : all) {
                boolean isEarned = earnedCodes.contains(a.getCode());
                UserAchievement userAch = earned.stream()
                        .filter(u -> u.getAchievementCode().equals(a.getCode()))
                        .findFirst().orElse(null);
                list.add(new AchievementBadgeStatus(a, isEarned, userAch != null ? userAch.getEarnedAt() : null));
            }
        } catch (Exception ignored) {}
        return list;
    }

    public static class AchievementBadgeStatus {
        public final Achievement achievement;
        public final boolean unlocked;
        public final java.time.LocalDateTime earnedAt;

        public AchievementBadgeStatus(Achievement achievement, boolean unlocked, java.time.LocalDateTime earnedAt) {
            this.achievement = achievement;
            this.unlocked = unlocked;
            this.earnedAt = earnedAt;
        }
    }
}

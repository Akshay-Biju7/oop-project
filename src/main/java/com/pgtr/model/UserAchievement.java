package com.pgtr.model;

import java.time.LocalDateTime;

/**
 * Represents an achievement earned by a specific user.
 */
public class UserAchievement extends IdentifiableEntity {
    private String userId;
    private String achievementCode;
    private LocalDateTime earnedAt;
    private Achievement achievement; // Joined details for display

    public UserAchievement() {
        super();
        this.earnedAt = LocalDateTime.now();
    }

    public UserAchievement(String id, String userId, String achievementCode, LocalDateTime earnedAt) {
        super(id);
        this.userId = userId;
        this.achievementCode = achievementCode;
        this.earnedAt = earnedAt != null ? earnedAt : LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAchievementCode() {
        return achievementCode;
    }

    public void setAchievementCode(String achievementCode) {
        this.achievementCode = achievementCode;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }
}

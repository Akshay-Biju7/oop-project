package com.pgtr.dao;

import com.pgtr.model.Achievement;
import com.pgtr.model.UserAchievement;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for gamification badges and achievement tracking.
 */
public interface AchievementDAO {
    List<Achievement> getAllAchievements() throws SQLException;
    List<UserAchievement> getUserAchievements(String userId) throws SQLException;
    boolean awardAchievement(String userId, String achievementCode) throws SQLException;
    boolean hasUserEarned(String userId, String achievementCode) throws SQLException;
}

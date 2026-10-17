package com.pgtr.service;

import com.pgtr.dao.UserDAO;
import com.pgtr.dao.impl.UserDAOImpl;
import com.pgtr.model.ActivityLevel;
import com.pgtr.model.FitnessGoalType;
import com.pgtr.model.User;
import com.pgtr.util.PasswordHasher;
import com.pgtr.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user using email and password. Verifies against BCrypt hash.
     */
    public User login(String email, String password) throws Exception {
        if (!ValidationUtil.isNonEmpty(email) || !ValidationUtil.isNonEmpty(password)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        Optional<User> userOpt = userDAO.getUserByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No account found with this email.");
        }

        User user = userOpt.get();
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }

        UserSession.getInstance().setCurrentUser(user);
        return user;
    }

    /**
     * Registers a new user with input validation, science-backed target recommendations, and BCrypt hashing.
     */
    public User register(String fullName, String email, String password, int age,
                         double heightCm, double weightKg, double targetWeightKg,
                         FitnessGoalType goal, ActivityLevel activityLevel) throws Exception {

        if (!ValidationUtil.isNonEmpty(fullName)) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (age < 12 || age > 100) {
            throw new IllegalArgumentException("Age must be between 12 and 100 years.");
        }
        if (heightCm < 80 || heightCm > 250) {
            throw new IllegalArgumentException("Height must be between 80 cm and 250 cm.");
        }
        if (weightKg < 25 || weightKg > 300) {
            throw new IllegalArgumentException("Current weight must be between 25 kg and 300 kg.");
        }
        if (targetWeightKg < 25 || targetWeightKg > 300) {
            throw new IllegalArgumentException("Target weight must be between 25 kg and 300 kg.");
        }

        if (userDAO.emailExists(email.trim())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setAge(age);
        user.setHeightCm(heightCm);
        user.setCurrentWeightKg(weightKg);
        user.setTargetWeightKg(targetWeightKg);
        user.setFitnessGoal(goal != null ? goal : FitnessGoalType.MUSCLE_GAIN);
        user.setActivityLevel(activityLevel != null ? activityLevel : ActivityLevel.MODERATELY_ACTIVE);

        // Auto-calculate suggested daily targets based on scientific formulas
        user.setDailyCalorieTarget(user.calculateSuggestedCalories());
        user.setDailyProteinTarget(user.calculateSuggestedProtein());
        user.setDailyWaterTarget(3000);
        user.setDailySleepTarget(8.0);

        boolean created = userDAO.createUser(user);
        if (!created) {
            throw new SQLException("Failed to save user profile to database.");
        }

        UserSession.getInstance().setCurrentUser(user);
        return user;
    }

    public void updateProfile(User user) throws Exception {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Invalid user profile.");
        }
        userDAO.updateProfile(user);
        UserSession.getInstance().setCurrentUser(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) throws Exception {
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        Optional<User> userOpt = userDAO.getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOpt.get();
        if (!PasswordHasher.verify(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        String newHash = PasswordHasher.hash(newPassword);
        userDAO.updatePassword(userId, newHash);
        user.setPasswordHash(newHash);
        UserSession.getInstance().setCurrentUser(user);
    }
}

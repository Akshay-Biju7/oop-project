package com.pgtr.model;

import java.time.LocalDateTime;

/**
 * Encapsulates single user profile information, health targets, and scientific BMR/TDEE calculations.
 */
public class User extends IdentifiableEntity {
    private String email;
    private String passwordHash;
    private String fullName;
    private int age;
    private double heightCm;
    private double currentWeightKg;
    private double targetWeightKg;
    private FitnessGoalType fitnessGoal;
    private ActivityLevel activityLevel;
    private int dailyCalorieTarget;
    private int dailyProteinTarget;
    private int dailyWaterTarget; // in ml
    private double dailySleepTarget; // in hours
    private LocalDateTime updatedAt;

    public User() {
        super();
        this.fitnessGoal = FitnessGoalType.MUSCLE_GAIN;
        this.activityLevel = ActivityLevel.MODERATELY_ACTIVE;
        this.dailyCalorieTarget = 2400;
        this.dailyProteinTarget = 150;
        this.dailyWaterTarget = 3000;
        this.dailySleepTarget = 8.0;
        this.updatedAt = LocalDateTime.now();
    }

    public User(String id, String email, String passwordHash, String fullName) {
        super(id);
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.fitnessGoal = FitnessGoalType.MUSCLE_GAIN;
        this.activityLevel = ActivityLevel.MODERATELY_ACTIVE;
        this.dailyCalorieTarget = 2400;
        this.dailyProteinTarget = 150;
        this.dailyWaterTarget = 3000;
        this.dailySleepTarget = 8.0;
        this.updatedAt = LocalDateTime.now();
    }

    // --- Scientific Fitness Calculators ---

    /**
     * Calculates Basal Metabolic Rate using standard Mifflin-St Jeor equation.
     */
    public double calculateBmr() {
        if (currentWeightKg <= 0 || heightCm <= 0 || age <= 0) {
            return 1800.0; // standard baseline estimate
        }
        // Formula: 10 * weight (kg) + 6.25 * height (cm) - 5 * age + 5
        return (10 * currentWeightKg) + (6.25 * heightCm) - (5 * age) + 5;
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE).
     */
    public double calculateTdee() {
        double bmr = calculateBmr();
        double multiplier = activityLevel != null ? activityLevel.getMultiplier() : 1.55;
        return bmr * multiplier;
    }

    /**
     * Suggests daily calories based on fitness goal.
     */
    public int calculateSuggestedCalories() {
        double tdee = calculateTdee();
        if (fitnessGoal == null) return (int) Math.round(tdee);
        switch (fitnessGoal) {
            case WEIGHT_LOSS:
                return (int) Math.round(tdee - 500); // 500 kcal deficit
            case MUSCLE_GAIN:
                return (int) Math.round(tdee + 300); // 300 kcal lean surplus
            case STRENGTH:
                return (int) Math.round(tdee + 200);
            case MAINTENANCE:
            default:
                return (int) Math.round(tdee);
        }
    }

    /**
     * Suggests daily protein intake (grams) based on goal and weight.
     */
    public int calculateSuggestedProtein() {
        if (currentWeightKg <= 0) return 150;
        // 1.8g to 2.2g per kg bodyweight for active gym lifters
        if (fitnessGoal == FitnessGoalType.MUSCLE_GAIN || fitnessGoal == FitnessGoalType.STRENGTH) {
            return (int) Math.round(currentWeightKg * 2.0);
        } else if (fitnessGoal == FitnessGoalType.WEIGHT_LOSS) {
            return (int) Math.round(currentWeightKg * 2.2); // high protein to spare muscle in deficit
        }
        return (int) Math.round(currentWeightKg * 1.6);
    }

    /**
     * Calculates BMI = weight (kg) / (height in meters)^2
     */
    public double calculateBmi() {
        if (heightCm <= 0 || currentWeightKg <= 0) return 0.0;
        double heightInMeters = heightCm / 100.0;
        return currentWeightKg / (heightInMeters * heightInMeters);
    }

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getCurrentWeightKg() {
        return currentWeightKg;
    }

    public void setCurrentWeightKg(double currentWeightKg) {
        this.currentWeightKg = currentWeightKg;
    }

    public double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public FitnessGoalType getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(FitnessGoalType fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public int getDailyCalorieTarget() {
        return dailyCalorieTarget;
    }

    public void setDailyCalorieTarget(int dailyCalorieTarget) {
        this.dailyCalorieTarget = dailyCalorieTarget;
    }

    public int getDailyProteinTarget() {
        return dailyProteinTarget;
    }

    public void setDailyProteinTarget(int dailyProteinTarget) {
        this.dailyProteinTarget = dailyProteinTarget;
    }

    public int getDailyWaterTarget() {
        return dailyWaterTarget;
    }

    public void setDailyWaterTarget(int dailyWaterTarget) {
        this.dailyWaterTarget = dailyWaterTarget;
    }

    public double getDailySleepTarget() {
        return dailySleepTarget;
    }

    public void setDailySleepTarget(double dailySleepTarget) {
        this.dailySleepTarget = dailySleepTarget;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

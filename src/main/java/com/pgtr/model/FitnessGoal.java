package com.pgtr.model;

import java.time.LocalDate;

/**
 * Tracks measurable personal fitness goals (e.g., target weight, bench press PR, protein streak).
 */
public class FitnessGoal extends IdentifiableEntity {
    private String userId;
    private String title;
    private String description;
    private GoalCategory category;
    private double startingValue;
    private double targetValue;
    private double currentValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate deadline;
    private GoalStatus status;

    public FitnessGoal() {
        super();
        this.category = GoalCategory.WEIGHT;
        this.status = GoalStatus.IN_PROGRESS;
        this.startDate = LocalDate.now();
        this.deadline = LocalDate.now().plusMonths(3);
        this.unit = "kg";
    }

    public FitnessGoal(String id, String userId, String title, String description,
                       GoalCategory category, double startingValue, double targetValue,
                       double currentValue, String unit, LocalDate startDate,
                       LocalDate deadline, GoalStatus status) {
        super(id);
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category != null ? category : GoalCategory.WEIGHT;
        this.startingValue = startingValue;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit != null ? unit : "kg";
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.deadline = deadline != null ? deadline : LocalDate.now().plusMonths(3);
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
    }

    /**
     * Calculates completion percentage (0 - 100%).
     * Handles both increasing goals (e.g. lift 100kg from 60kg) and decreasing goals (e.g. drop from 90kg to 75kg).
     */
    public int getProgressPercentage() {
        if (status == GoalStatus.ACHIEVED) return 100;
        double totalDelta = Math.abs(targetValue - startingValue);
        if (totalDelta == 0) {
            return (currentValue >= targetValue) ? 100 : 0;
        }

        double progressDelta;
        if (targetValue >= startingValue) {
            // Increasing goal (e.g., muscle gain, bench press weight)
            progressDelta = currentValue - startingValue;
        } else {
            // Decreasing goal (e.g., fat loss)
            progressDelta = startingValue - currentValue;
        }

        if (progressDelta <= 0) return 0;
        int pct = (int) Math.round((progressDelta / totalDelta) * 100.0);
        return Math.min(100, Math.max(0, pct));
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GoalCategory getCategory() {
        return category;
    }

    public void setCategory(GoalCategory category) {
        this.category = category;
    }

    public double getStartingValue() {
        return startingValue;
    }

    public void setStartingValue(double startingValue) {
        this.startingValue = startingValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}

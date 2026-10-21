package com.pgtr.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a scheduled workout plan for a specific day of the week (Monday - Sunday)
 * supporting separate Cut Mode and Bulk Mode programs.
 */
public class WorkoutPlan extends IdentifiableEntity {
    private String userId;
    private String planMode; // "CUT" or "BULK"
    private DayOfWeek dayOfWeek;
    private String workoutName;
    private String muscleGroup;
    private String plannedExercises;
    private int restSeconds;
    private String cardio;
    private LocalTime scheduledTime;
    private String notes;
    private boolean active;
    private boolean completed;

    public WorkoutPlan() {
        super();
        this.planMode = "BULK";
        this.dayOfWeek = DayOfWeek.MONDAY;
        this.restSeconds = 90;
        this.cardio = "None";
        this.active = true;
        this.completed = false;
    }

    public WorkoutPlan(String id, String userId, String planMode, DayOfWeek dayOfWeek, String workoutName,
                       String muscleGroup, String plannedExercises, int restSeconds, String cardio,
                       LocalTime scheduledTime, String notes, boolean active, boolean completed) {
        super(id);
        this.userId = userId;
        this.planMode = planMode != null ? planMode.toUpperCase() : "BULK";
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : DayOfWeek.MONDAY;
        this.workoutName = workoutName;
        this.muscleGroup = muscleGroup;
        this.plannedExercises = plannedExercises;
        this.restSeconds = restSeconds > 0 ? restSeconds : 90;
        this.cardio = cardio != null ? cardio : "None";
        this.scheduledTime = scheduledTime;
        this.notes = notes;
        this.active = active;
        this.completed = completed;
    }

    // --- Legacy Constructor for backwards compatibility ---
    public WorkoutPlan(String id, String userId, DayOfWeek dayOfWeek, String workoutName,
                       String muscleGroup, String plannedExercises, LocalTime scheduledTime,
                       String notes, boolean active) {
        this(id, userId, "BULK", dayOfWeek, workoutName, muscleGroup, plannedExercises, 90, "None", scheduledTime, notes, active, false);
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlanMode() {
        return planMode != null ? planMode : "BULK";
    }

    public void setPlanMode(String planMode) {
        this.planMode = planMode != null ? planMode.toUpperCase() : "BULK";
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getPlannedExercises() {
        return plannedExercises;
    }

    public void setPlannedExercises(String plannedExercises) {
        this.plannedExercises = plannedExercises;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getCardio() {
        return cardio;
    }

    public void setCardio(String cardio) {
        this.cardio = cardio;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

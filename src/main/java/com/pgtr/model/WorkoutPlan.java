package com.pgtr.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a scheduled workout plan for a specific day of the week (Monday - Sunday).
 */
public class WorkoutPlan extends IdentifiableEntity {
    private String userId;
    private DayOfWeek dayOfWeek;
    private String workoutName;
    private String muscleGroup;
    private String plannedExercises;
    private LocalTime scheduledTime;
    private String notes;
    private boolean active;

    public WorkoutPlan() {
        super();
        this.dayOfWeek = DayOfWeek.MONDAY;
        this.active = true;
    }

    public WorkoutPlan(String id, String userId, DayOfWeek dayOfWeek, String workoutName,
                       String muscleGroup, String plannedExercises, LocalTime scheduledTime,
                       String notes, boolean active) {
        super(id);
        this.userId = userId;
        this.dayOfWeek = dayOfWeek != null ? dayOfWeek : DayOfWeek.MONDAY;
        this.workoutName = workoutName;
        this.muscleGroup = muscleGroup;
        this.plannedExercises = plannedExercises;
        this.scheduledTime = scheduledTime;
        this.notes = notes;
        this.active = active;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}

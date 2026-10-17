package com.pgtr.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Tracks sleep duration, sleep/wake timings, and perceived rest quality.
 */
public class SleepLog extends IdentifiableEntity {
    private String userId;
    private LocalDate sleepDate;
    private LocalTime sleepTime;
    private LocalTime wakeTime;
    private double durationHours;
    private String sleepQuality;
    private String notes;

    public SleepLog() {
        super();
        this.sleepDate = LocalDate.now();
        this.sleepTime = LocalTime.of(23, 0);
        this.wakeTime = LocalTime.of(7, 0);
        this.durationHours = 8.0;
        this.sleepQuality = "Good";
    }

    public SleepLog(String id, String userId, LocalDate sleepDate, LocalTime sleepTime,
                    LocalTime wakeTime, double durationHours, String sleepQuality, String notes) {
        super(id);
        this.userId = userId;
        this.sleepDate = sleepDate != null ? sleepDate : LocalDate.now();
        this.sleepTime = sleepTime;
        this.wakeTime = wakeTime;
        this.durationHours = durationHours;
        this.sleepQuality = sleepQuality != null ? sleepQuality : "Good";
        this.notes = notes;
    }

    /**
     * Auto-calculates duration in hours from sleepTime and wakeTime.
     */
    public static double calculateDuration(LocalTime sleep, LocalTime wake) {
        if (sleep == null || wake == null) return 8.0;
        long minutes;
        if (wake.isAfter(sleep)) {
            minutes = ChronoUnit.MINUTES.between(sleep, wake);
        } else {
            // crosses midnight
            minutes = (24 * 60 - ChronoUnit.MINUTES.between(LocalTime.MIN, sleep)) +
                      ChronoUnit.MINUTES.between(LocalTime.MIN, wake);
        }
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getSleepDate() {
        return sleepDate;
    }

    public void setSleepDate(LocalDate sleepDate) {
        this.sleepDate = sleepDate;
    }

    public LocalTime getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(LocalTime sleepTime) {
        this.sleepTime = sleepTime;
    }

    public LocalTime getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(LocalTime wakeTime) {
        this.wakeTime = wakeTime;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public String getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(String sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

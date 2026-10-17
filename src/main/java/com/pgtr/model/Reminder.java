package com.pgtr.model;

import java.time.LocalTime;

/**
 * In-app notification reminder entity for workouts, hydration, meals, and sleep.
 */
public class Reminder extends IdentifiableEntity {
    private String userId;
    private ReminderType reminderType;
    private String title;
    private String message;
    private LocalTime reminderTime;
    private boolean recurring;
    private boolean dismissed;

    public Reminder() {
        super();
        this.reminderType = ReminderType.WORKOUT;
        this.reminderTime = LocalTime.of(18, 0);
        this.recurring = true;
        this.dismissed = false;
    }

    public Reminder(String id, String userId, ReminderType reminderType, String title,
                    String message, LocalTime reminderTime, boolean recurring, boolean dismissed) {
        super(id);
        this.userId = userId;
        this.reminderType = reminderType != null ? reminderType : ReminderType.WORKOUT;
        this.title = title;
        this.message = message;
        this.reminderTime = reminderTime != null ? reminderTime : LocalTime.of(18, 0);
        this.recurring = recurring;
        this.dismissed = dismissed;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ReminderType getReminderType() {
        return reminderType;
    }

    public void setReminderType(ReminderType reminderType) {
        this.reminderType = reminderType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isDismissed() {
        return dismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }
}

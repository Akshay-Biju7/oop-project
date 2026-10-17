package com.pgtr.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Tracks individual water intake logs in millilitres.
 */
public class WaterLog extends IdentifiableEntity {
    private String userId;
    private LocalDate logDate;
    private int amountMl;
    private LocalTime loggedAt;

    public WaterLog() {
        super();
        this.logDate = LocalDate.now();
        this.loggedAt = LocalTime.now();
        this.amountMl = 250;
    }

    public WaterLog(String id, String userId, LocalDate logDate, int amountMl, LocalTime loggedAt) {
        super(id);
        this.userId = userId;
        this.logDate = logDate != null ? logDate : LocalDate.now();
        this.amountMl = amountMl;
        this.loggedAt = loggedAt != null ? loggedAt : LocalTime.now();
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public int getAmountMl() {
        return amountMl;
    }

    public void setAmountMl(int amountMl) {
        this.amountMl = amountMl;
    }

    public LocalTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}

package com.pgtr.model;

import java.time.LocalDate;

/**
 * Tracks anthropometric data, weight, circumferences, and calculated BMI.
 * Label: BMI is a screening metric only and does not differentiate muscle mass from fat mass.
 */
public class BodyMeasurement extends IdentifiableEntity {
    private String userId;
    private LocalDate logDate;
    private double weightKg;
    private double heightCm;
    private double bmi;
    private double waistCm;
    private double chestCm;
    private double armsCm;
    private double thighsCm;
    private double hipsCm;
    private double neckCm;
    private String notes;

    public BodyMeasurement() {
        super();
        this.logDate = LocalDate.now();
    }

    public BodyMeasurement(String id, String userId, LocalDate logDate, double weightKg,
                           double heightCm, double waistCm, double chestCm, double armsCm,
                           double thighsCm, double hipsCm, double neckCm, String notes) {
        super(id);
        this.userId = userId;
        this.logDate = logDate != null ? logDate : LocalDate.now();
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.waistCm = waistCm;
        this.chestCm = chestCm;
        this.armsCm = armsCm;
        this.thighsCm = thighsCm;
        this.hipsCm = hipsCm;
        this.neckCm = neckCm;
        this.notes = notes;
        this.bmi = calculateBmi();
    }

    /**
     * Calculates Body Mass Index (BMI).
     * Formula: weight (kg) / (height (m))^2
     */
    public double calculateBmi() {
        if (heightCm <= 0 || weightKg <= 0) return 0.0;
        double heightM = heightCm / 100.0;
        double calculated = weightKg / (heightM * heightM);
        return Math.round(calculated * 10.0) / 10.0;
    }

    public String getBmiCategory() {
        double val = (bmi > 0) ? bmi : calculateBmi();
        if (val < 18.5) return "Underweight (< 18.5)";
        if (val < 25.0) return "Normal Weight (18.5 - 24.9)";
        if (val < 30.0) return "Overweight (25.0 - 29.9)";
        return "Obese Range (≥ 30.0)";
    }

    public void recalculateBmi() {
        this.bmi = calculateBmi();
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

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
        recalculateBmi();
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
        recalculateBmi();
    }

    public double getBmi() {
        return bmi > 0 ? bmi : calculateBmi();
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getWaistCm() {
        return waistCm;
    }

    public void setWaistCm(double waistCm) {
        this.waistCm = waistCm;
    }

    public double getChestCm() {
        return chestCm;
    }

    public void setChestCm(double chestCm) {
        this.chestCm = chestCm;
    }

    public double getArmsCm() {
        return armsCm;
    }

    public void setArmsCm(double armsCm) {
        this.armsCm = armsCm;
    }

    public double getThighsCm() {
        return thighsCm;
    }

    public void setThighsCm(double thighsCm) {
        this.thighsCm = thighsCm;
    }

    public double getHipsCm() {
        return hipsCm;
    }

    public void setHipsCm(double hipsCm) {
        this.hipsCm = hipsCm;
    }

    public double getNeckCm() {
        return neckCm;
    }

    public void setNeckCm(double neckCm) {
        this.neckCm = neckCm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

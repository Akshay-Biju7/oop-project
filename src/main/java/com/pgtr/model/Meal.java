package com.pgtr.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a logged meal consumption record for nutrition and calorie tracking.
 */
public class Meal extends IdentifiableEntity {
    private String userId;
    private LocalDate mealDate;
    private LocalTime mealTime;
    private MealType mealType;
    private String foodName;
    private double servingQuantity;
    private double calories;
    private double proteinG;
    private double carbsG;
    private double fatG;
    private String notes;

    public Meal() {
        super();
        this.mealDate = LocalDate.now();
        this.mealTime = LocalTime.now();
        this.mealType = MealType.LUNCH;
        this.servingQuantity = 1.0;
    }

    public Meal(String id, String userId, LocalDate mealDate, LocalTime mealTime,
                MealType mealType, String foodName, double servingQuantity,
                double calories, double proteinG, double carbsG, double fatG, String notes) {
        super(id);
        this.userId = userId;
        this.mealDate = mealDate != null ? mealDate : LocalDate.now();
        this.mealTime = mealTime != null ? mealTime : LocalTime.now();
        this.mealType = mealType != null ? mealType : MealType.LUNCH;
        this.foodName = foodName;
        this.servingQuantity = servingQuantity;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.notes = notes;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public LocalTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(LocalTime mealTime) {
        this.mealTime = mealTime;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getServingQuantity() {
        return servingQuantity;
    }

    public void setServingQuantity(double servingQuantity) {
        this.servingQuantity = servingQuantity;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteinG() {
        return proteinG;
    }

    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    public double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(double carbsG) {
        this.carbsG = carbsG;
    }

    public double getFatG() {
        return fatG;
    }

    public void setFatG(double fatG) {
        this.fatG = fatG;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

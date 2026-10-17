package com.pgtr.model;

/**
 * Represents a nutritional item in the food library.
 * Note: Stored nutrition values are standardized estimates for guidance and tracking.
 */
public class FoodItem extends IdentifiableEntity {
    private String foodName;
    private String category;
    private String servingUnit;
    private double calories;
    private double proteinG;
    private double carbsG;
    private double fatG;
    private boolean isCustom;
    private String userId;

    public FoodItem() {
        super();
        this.servingUnit = "100g";
    }

    public FoodItem(String id, String foodName, String category, String servingUnit, 
                    double calories, double proteinG, double carbsG, double fatG, 
                    boolean isCustom, String userId) {
        super(id);
        this.foodName = foodName;
        this.category = category;
        this.servingUnit = servingUnit;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.isCustom = isCustom;
        this.userId = userId;
    }

    // --- Getters and Setters ---

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
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

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return foodName + " (" + servingUnit + ") - " + calories + " kcal | P:" + proteinG + "g C:" + carbsG + "g F:" + fatG + "g";
    }
}

package com.pgtr.model;

/**
 * Represents a planned food/meal item within an editable Cut Mode or Bulk Mode nutrition plan.
 */
public class NutritionPlanItem extends IdentifiableEntity {
    private String userId;
    private String planMode; // "CUT" or "BULK"
    private MealType mealType;
    private String foodName;
    private String servingSize;
    private double calories;
    private double proteinG;
    private double carbsG;
    private double fatG;
    private String mealTime;
    private String notes;
    private boolean consumed;

    public NutritionPlanItem() {
        super();
        this.planMode = "BULK";
        this.mealType = MealType.BREAKFAST;
        this.servingSize = "1 serving";
        this.consumed = false;
    }

    public NutritionPlanItem(String id, String userId, String planMode, MealType mealType,
                             String foodName, String servingSize, double calories,
                             double proteinG, double carbsG, double fatG,
                             String mealTime, String notes, boolean consumed) {
        super(id);
        this.userId = userId;
        this.planMode = planMode != null ? planMode.toUpperCase() : "BULK";
        this.mealType = mealType != null ? mealType : MealType.BREAKFAST;
        this.foodName = foodName;
        this.servingSize = servingSize != null ? servingSize : "1 serving";
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.mealTime = mealTime != null ? mealTime : "08:00 AM";
        this.notes = notes;
        this.consumed = consumed;
    }

    // --- Getters & Setters ---

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

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
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

    public String getMealTime() {
        return mealTime;
    }

    public void setMealTime(String mealTime) {
        this.mealTime = mealTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
}

package com.pgtr.model;

/**
 * Enumeration of meal categories across the daily nutrition cycle.
 */
public enum MealType {
    BREAKFAST("Breakfast"),
    MID_MORNING("Mid-Morning Snack"),
    LUNCH("Lunch"),
    EVENING_SNACK("Evening Snack"),
    DINNER("Dinner"),
    POST_WORKOUT("Post-Workout Fuel"),
    SNACK("Snack");

    private final String displayName;

    MealType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static MealType fromString(String text) {
        if (text == null) return BREAKFAST;
        for (MealType m : MealType.values()) {
            if (m.name().equalsIgnoreCase(text) || m.displayName.equalsIgnoreCase(text)) {
                return m;
            }
        }
        return BREAKFAST;
    }

    public String getDefaultTime() {
        switch (this) {
            case BREAKFAST: return "08:00 AM";
            case MID_MORNING: return "11:00 AM";
            case LUNCH: return "01:30 PM";
            case EVENING_SNACK: return "05:00 PM";
            case POST_WORKOUT: return "07:30 PM";
            case DINNER: return "08:45 PM";
            case SNACK:
            default:
                return "04:00 PM";
        }
    }
}

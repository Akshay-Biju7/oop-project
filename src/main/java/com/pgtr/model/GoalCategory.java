package com.pgtr.model;

public enum GoalCategory {
    WEIGHT("Body Weight", "kg"),
    STRENGTH("Lifting Strength", "kg"),
    MUSCLE("Muscle & Hypertrophy", "cm"),
    NUTRITION("Daily Nutrition", "g"),
    HABIT("Habits & Lifestyle", "days"),
    WORKOUT("Workout Volume / Frequency", "sessions");

    private final String displayName;
    private final String defaultUnit;

    GoalCategory(String displayName, String defaultUnit) {
        this.displayName = displayName;
        this.defaultUnit = defaultUnit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static GoalCategory fromString(String text) {
        if (text == null) return WEIGHT;
        for (GoalCategory c : GoalCategory.values()) {
            if (c.name().equalsIgnoreCase(text) || c.displayName.equalsIgnoreCase(text)) {
                return c;
            }
        }
        return WEIGHT;
    }
}

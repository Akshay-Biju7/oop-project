package com.pgtr.model;

public enum ActivityLevel {
    SEDENTARY("Sedentary", "Little or no exercise, desk job", 1.2),
    LIGHTLY_ACTIVE("Lightly Active", "Light exercise 1-3 days/week", 1.375),
    MODERATELY_ACTIVE("Moderately Active", "Moderate exercise 3-5 days/week", 1.55),
    VERY_ACTIVE("Very Active", "Hard exercise 6-7 days/week", 1.725),
    EXTREMELY_ACTIVE("Extremely Active", "Athletic training or physical job", 1.9);

    private final String displayName;
    private final String description;
    private final double multiplier;

    ActivityLevel(String displayName, String description, double multiplier) {
        this.displayName = displayName;
        this.description = description;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static ActivityLevel fromString(String text) {
        if (text == null) return MODERATELY_ACTIVE;
        for (ActivityLevel a : ActivityLevel.values()) {
            if (a.name().equalsIgnoreCase(text) || a.displayName.equalsIgnoreCase(text)) {
                return a;
            }
        }
        return MODERATELY_ACTIVE;
    }
}

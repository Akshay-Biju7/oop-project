package com.pgtr.model;

public enum FitnessGoalType {
    WEIGHT_LOSS("Weight Loss", "Focus on fat reduction and caloric deficit"),
    MUSCLE_GAIN("Muscle Gain", "Hypertrophy training with caloric surplus"),
    STRENGTH("Strength & Power", "Heavy compound lifting and neuromuscular gains"),
    MAINTENANCE("Maintenance & Health", "Sustaining current composition and conditioning");

    private final String displayName;
    private final String description;

    FitnessGoalType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static FitnessGoalType fromString(String text) {
        if (text == null) return MUSCLE_GAIN;
        for (FitnessGoalType b : FitnessGoalType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return MUSCLE_GAIN;
    }
}

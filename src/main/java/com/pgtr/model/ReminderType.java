package com.pgtr.model;

public enum ReminderType {
    WORKOUT("Gym Workout"),
    HYDRATION("Hydration / Water"),
    MEAL("Meal / Protein Intake"),
    SLEEP("Sleep & Recovery"),
    CUSTOM("Custom Reminder");

    private final String displayName;

    ReminderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static ReminderType fromString(String text) {
        if (text == null) return WORKOUT;
        for (ReminderType r : ReminderType.values()) {
            if (r.name().equalsIgnoreCase(text) || r.displayName.equalsIgnoreCase(text)) {
                return r;
            }
        }
        return WORKOUT;
    }
}

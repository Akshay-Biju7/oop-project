package com.pgtr.model;

public enum GoalStatus {
    IN_PROGRESS("In Progress"),
    ACHIEVED("Achieved"),
    ABANDONED("Abandoned");

    private final String displayName;

    GoalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static GoalStatus fromString(String text) {
        if (text == null) return IN_PROGRESS;
        for (GoalStatus s : GoalStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return IN_PROGRESS;
    }
}

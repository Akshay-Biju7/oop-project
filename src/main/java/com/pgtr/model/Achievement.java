package com.pgtr.model;

import java.io.Serializable;

/**
 * Catalog definition of available fitness achievements and badges.
 */
public class Achievement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String title;
    private String description;
    private String iconName;
    private String badgeTier; // BRONZE, SILVER, GOLD, PLATINUM

    public Achievement() {}

    public Achievement(String code, String title, String description, String iconName, String badgeTier) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.badgeTier = badgeTier;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getBadgeTier() {
        return badgeTier;
    }

    public void setBadgeTier(String badgeTier) {
        this.badgeTier = badgeTier;
    }
}

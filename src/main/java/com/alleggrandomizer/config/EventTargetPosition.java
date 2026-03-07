package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;

/**
 * Defines where an event should be triggered.
 * Used by the EVENT category to determine the target position for world events.
 */
public enum EventTargetPosition {
    /**
     * Trigger event at the player's position (egg thrower).
     */
    @SerializedName("PLAYER")
    PLAYER("玩家位置", "Event occurs at the player who threw the egg"),

    /**
     * Trigger event at the egg's collision position.
     */
    @SerializedName("EGG")
    EGG("鸡蛋落点", "Event occurs where the egg landed");

    private final String displayName;
    private final String description;

    EventTargetPosition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parse a string to EventTargetPosition, case-insensitive.
     * @param name the string to parse
     * @return the matching EventTargetPosition, or EGG as default
     */
    public static EventTargetPosition fromString(String name) {
        if (name == null) {
            return EGG;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EGG;
        }
    }
}

package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;

/**
 * Defines how a status effect should be applied.
 * Used by the EFFECT category to determine the application method.
 */
public enum EffectApplyMode {
    /**
     * Splash effect - creates an AreaEffectCloud at the target position.
     * Similar to splash potions, affects entities within a radius.
     */
    @SerializedName("SPLASH")
    SPLASH("喷溅型", "Creates an area effect cloud like splash potions"),

    /**
     * Direct effect - directly applies the effect to the nearest entity/player.
     * Instant application without area effect.
     */
    @SerializedName("DIRECT")
    DIRECT("直接给予型", "Directly applies effect to nearest target");

    private final String displayName;
    private final String description;

    EffectApplyMode(String displayName, String description) {
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
     * Parse a string to EffectApplyMode, case-insensitive.
     * @param name the string to parse
     * @return the matching EffectApplyMode, or SPLASH as default
     */
    public static EffectApplyMode fromString(String name) {
        if (name == null) {
            return SPLASH;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SPLASH;
        }
    }
}

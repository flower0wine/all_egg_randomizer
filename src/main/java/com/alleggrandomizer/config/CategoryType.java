package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;

/**
 * Enumeration of all possible output categories.
 * Each category represents a type of result that can be spawned when an egg hits something.
 */
public enum CategoryType {
    @SerializedName("ENTITY")
    ENTITY("生物", "Spawning mobs and creatures"),
    
    @SerializedName("ITEM")
    ITEM("物品", "Dropping items and blocks"),
    
    @SerializedName("EFFECT")
    EFFECT("效果", "Applying potion effects"),
    
    @SerializedName("EVENT")
    EVENT("事件", "Triggering world events");

    private final String displayName;
    private final String description;

    CategoryType(String displayName, String description) {
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
     * Parse a string to CategoryType, case-insensitive.
     * @param name the string to parse
     * @return the matching CategoryType, or null if not found
     */
    public static CategoryType fromString(String name) {
        if (name == null) {
            return null;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

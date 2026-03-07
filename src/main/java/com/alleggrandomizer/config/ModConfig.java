package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration model for the entire mod.
 * Contains version info, all category configurations, and global settings.
 */
public class ModConfig {

    @SerializedName("version")
    private String version = "1.0.0";

    @SerializedName("categories")
    private Map<String, CategoryConfig> categories = new HashMap<>();

    @SerializedName("globalSettings")
    private GlobalSettings globalSettings = new GlobalSettings();

    public ModConfig() {
        initializeDefaultCategories();
    }

    /**
     * Initialize default category configurations.
     */
    private void initializeDefaultCategories() {
        // ENTITY: enabled by default
        categories.put("ENTITY", new CategoryConfig(true, 1.0, createEntitySettings()));
        
        // ITEM: enabled by default
        categories.put("ITEM", new CategoryConfig(true, 1.0, createItemSettings()));
        
        // EFFECT: disabled by default
        categories.put("EFFECT", new CategoryConfig(false, 1.0, createEffectSettings()));
        
        // EVENT: disabled by default
        categories.put("EVENT", new CategoryConfig(false, 1.0, createEventSettings()));
    }

    private Map<String, Object> createEntitySettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("spawnCount", 1);
        settings.put("despawnTime", 600);
        return settings;
    }

    private Map<String, Object> createItemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("stackSize", 1);
        return settings;
    }

    private Map<String, Object> createEffectSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("duration", 300);              // Effect duration in ticks (15 seconds)
        settings.put("amplifier", 0);               // Effect level (0 = Level I)
        settings.put("splashRadius", 3.0);          // Splash cloud radius (for SPLASH mode)
        settings.put("splashDuration", 60);         // Splash cloud duration in ticks (3 seconds, for SPLASH mode)
        settings.put("targetType", "ANY");          // Target type for DIRECT mode: PLAYER, ENTITY, or ANY
        settings.put("searchRadius", 5.0);          // Search radius for DIRECT mode
        return settings;
    }

    private Map<String, Object> createEventSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("targetPosition", "EGG");      // Position: PLAYER or EGG
        settings.put("events", java.util.Arrays.asList("LIGHTNING")); // Enabled events list

        // Lightning-specific settings
        Map<String, Object> lightningSettings = new HashMap<>();
        lightningSettings.put("cosmetic", false);    // Cosmetic lightning (no damage/fire)
        settings.put("lightning", lightningSettings);

        return settings;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, CategoryConfig> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, CategoryConfig> categories) {
        this.categories = categories;
    }

    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }

    public void setGlobalSettings(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }

    /**
     * Get category config by type.
     * @param type the category type
     * @return the category config, or null if not found
     */
    public CategoryConfig getCategory(CategoryType type) {
        if (type == null) {
            return null;
        }
        return categories.get(type.name());
    }

    /**
     * Get category config by type name.
     * @param typeName the category type name (case-insensitive)
     * @return the category config, or null if not found
     */
    public CategoryConfig getCategory(String typeName) {
        CategoryType type = CategoryType.fromString(typeName);
        return getCategory(type);
    }

    /**
     * Set category config.
     * @param type the category type
     * @param config the config to set
     */
    public void setCategory(CategoryType type, CategoryConfig config) {
        if (type != null && config != null) {
            categories.put(type.name(), config);
        }
    }

    /**
     * Check if a category is enabled.
     * @param type the category type
     * @return true if enabled, false otherwise
     */
    public boolean isCategoryEnabled(CategoryType type) {
        CategoryConfig config = getCategory(type);
        return config != null && config.isEnabled();
    }

    /**
     * Validate all config values.
     */
    public void validate() {
        if (globalSettings != null) {
            globalSettings.validate();
        }
        
        for (CategoryConfig config : categories.values()) {
            if (config != null) {
                config.validate();
            }
        }
        
        // Ensure all category types exist
        for (CategoryType type : CategoryType.values()) {
            if (!categories.containsKey(type.name())) {
                categories.put(type.name(), new CategoryConfig());
            }
        }
    }
}

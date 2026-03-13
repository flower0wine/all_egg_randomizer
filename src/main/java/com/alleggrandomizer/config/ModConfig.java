package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration model for the entire mod.
 * Contains version info, all category configurations, and global settings.
 */
public class ModConfig {

    public static final Codec<ModConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("version").orElse("1.0.0").forGetter(ModConfig::getVersion),
            Codec.unboundedMap(Codec.STRING, CategoryConfig.CODEC)
                .fieldOf("categories")
                .orElseGet(HashMap::new)
                .forGetter(ModConfig::getCategories),
            GlobalSettings.CODEC.fieldOf("globalSettings")
                .orElseGet(GlobalSettings::new)
                .forGetter(ModConfig::getGlobalSettings)
        ).apply(instance, ModConfig::new)
    );

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
     * Constructor for codec deserialization.
     */
    private ModConfig(String version, Map<String, CategoryConfig> categories, GlobalSettings globalSettings) {
        this.version = version;
        this.categories = categories != null ? categories : new HashMap<>();
        this.globalSettings = globalSettings != null ? globalSettings : new GlobalSettings();
        
        // Ensure all default categories exist
        ensureDefaultCategories();
    }

    /**
     * Initialize default category configurations.
     */
    private void initializeDefaultCategories() {
        // ENTITY: disabled by default
        categories.put("ENTITY", new CategoryConfig(false, 5.0, createEntitySettings()));
        
        // ITEM: enabled by default
        categories.put("ITEM", new CategoryConfig(true, 85.0, createItemSettings()));
        
        // EFFECT: disabled by default
        categories.put("EFFECT", new CategoryConfig(false, 5.0, createEffectSettings()));
        
        // EVENT: disabled by default
        categories.put("EVENT", new CategoryConfig(false, 5.0, createEventSettings()));
    }

    /**
     * Ensure all default categories exist (for loaded configs).
     */
    private void ensureDefaultCategories() {
        if (!categories.containsKey("ENTITY")) {
            categories.put("ENTITY", new CategoryConfig(false, 5.0, createEntitySettings()));
        }
        if (!categories.containsKey("ITEM")) {
            categories.put("ITEM", new CategoryConfig(true, 85.0, createItemSettings()));
        }
        if (!categories.containsKey("EFFECT")) {
            categories.put("EFFECT", new CategoryConfig(false, 5.0, createEffectSettings()));
        }
        if (!categories.containsKey("EVENT")) {
            categories.put("EVENT", new CategoryConfig(false, 5.0, createEventSettings()));
        }
    }

    private Map<String, Object> createEntitySettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("spawnCount", 1);
        settings.put("despawnTime", 600);
        return settings;
    }

    private Map<String, Object> createItemSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("stackSize", 5);
        return settings;
    }

    private Map<String, Object> createEffectSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("duration", 300);
        settings.put("amplifier", 0);
        settings.put("splashRadius", 3.0);
        settings.put("splashDuration", 60);
        settings.put("targetType", "ANY");
        settings.put("searchRadius", 5.0);
        return settings;
    }

    private Map<String, Object> createEventSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("targetPosition", "EGG");
        settings.put("events", java.util.Arrays.asList("LIGHTNING"));

        Map<String, Object> lightningSettings = new HashMap<>();
        lightningSettings.put("cosmetic", false);
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
     */
    public CategoryConfig getCategory(CategoryType type) {
        if (type == null) {
            return null;
        }
        return categories.get(type.name());
    }

    /**
     * Get category config by type name.
     */
    public CategoryConfig getCategory(String typeName) {
        CategoryType type = CategoryType.fromString(typeName);
        return getCategory(type);
    }

    /**
     * Set category config.
     */
    public void setCategory(CategoryType type, CategoryConfig config) {
        if (type != null && config != null) {
            categories.put(type.name(), config);
        }
    }

    /**
     * Check if a category is enabled.
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

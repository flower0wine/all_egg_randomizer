package com.alleggrandomizer.config;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration model for a single category.
 * Contains enable/disable state, weight, and category-specific settings.
 */
public class CategoryConfig {

    // Codec for Map<String, Object> - we'll use a simplified approach storing as strings
    private static final Codec<Map<String, Object>> SETTINGS_CODEC = 
        Codec.unboundedMap(Codec.STRING, Codec.STRING)
            .xmap(
                // Convert Map<String, String> to Map<String, Object>
                map -> new HashMap<>(map),
                // Convert Map<String, Object> to Map<String, String>
                map -> {
                    Map<String, String> stringMap = new HashMap<>();
                    map.forEach((k, v) -> stringMap.put(k, v != null ? v.toString() : ""));
                    return stringMap;
                }
            );

    public static final Codec<CategoryConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("enabled").orElse(true).forGetter(CategoryConfig::isEnabled),
            Codec.DOUBLE.fieldOf("weight").orElse(1.0).forGetter(CategoryConfig::getWeight),
            SETTINGS_CODEC.fieldOf("specificSettings").orElse(new HashMap<>()).forGetter(CategoryConfig::getSpecificSettings)
        ).apply(instance, CategoryConfig::new)
    );

    @SerializedName("enabled")
    private boolean enabled = true;

    @SerializedName("weight")
    private double weight = 1.0;

    @SerializedName("specificSettings")
    private Map<String, Object> specificSettings = new HashMap<>();

    public CategoryConfig() {
    }

    public CategoryConfig(boolean enabled, double weight) {
        this.enabled = enabled;
        this.weight = weight;
    }

    public CategoryConfig(boolean enabled, double weight, Map<String, Object> specificSettings) {
        this.enabled = enabled;
        this.weight = weight;
        this.specificSettings = specificSettings != null ? specificSettings : new HashMap<>();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = Math.max(0, weight);
    }

    public Map<String, Object> getSpecificSettings() {
        return specificSettings;
    }

    public void setSpecificSettings(Map<String, Object> specificSettings) {
        this.specificSettings = specificSettings != null ? specificSettings : new HashMap<>();
    }

    /**
     * Get a specific setting value.
     * @param key the setting key
     * @param defaultValue default value if not found
     * @return the setting value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        Object value = specificSettings.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        // Handle type conversion for common cases
        if (defaultValue instanceof Number) {
            if (value instanceof Number) {
                return (T) value;
            } else if (value instanceof String) {
                try {
                    // Try to parse as integer
                    return (T) Integer.valueOf((String) value);
                } catch (NumberFormatException e) {
                    try {
                        // Try to parse as double
                        return (T) Double.valueOf((String) value);
                    } catch (NumberFormatException e2) {
                        return defaultValue;
                    }
                }
            }
        } else if (defaultValue instanceof Boolean) {
            if (value instanceof Boolean) {
                return (T) value;
            } else if (value instanceof String) {
                return (T) Boolean.valueOf((String) value);
            }
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Set a specific setting value.
     * @param key the setting key
     * @param value the value to set
     */
    public void setSetting(String key, Object value) {
        if (specificSettings == null) {
            specificSettings = new HashMap<>();
        }
        specificSettings.put(key, value);
    }

    /**
     * Validate and fix invalid values.
     */
    public void validate() {
        if (weight < 0) {
            weight = 0;
        }
        if (specificSettings == null) {
            specificSettings = new HashMap<>();
        }
    }
}

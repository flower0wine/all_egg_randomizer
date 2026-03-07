package com.alleggrandomizer.core.generator.effect;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Splash effect applier - creates an AreaEffectCloud at the target position.
 * Similar to splash potions, the effect cloud affects entities within a radius.
 *
 * Configuration parameters:
 * - duration (int): Effect duration in ticks (default: 300)
 * - amplifier (int): Effect amplifier/level (default: 0, which is level I)
 * - splashRadius (double): Radius of the effect cloud (default: 3.0)
 * - splashDuration (int): How long the cloud persists in ticks (default: 60)
 */
public class SplashEffectApplier implements EffectApplier {

    private static final String METHOD_NAME = "SPLASH";

    @Override
    public boolean apply(ServerWorld world, Vec3d position, RegistryEntry<StatusEffect> effect, Map<String, Object> config) {
        if (world == null || position == null || effect == null) {
            AllEggRandomizer.LOGGER.warn("Cannot apply splash effect: missing required parameters");
            return false;
        }

        try {
            // Read configuration
            int duration = getIntConfig(config, "duration", 300);
            int amplifier = getIntConfig(config, "amplifier", 0);
            float splashRadius = getFloatConfig(config, "splashRadius", 3.0f);
            int splashDuration = getIntConfig(config, "splashDuration", 60);

            // Create area effect cloud
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, position.x, position.y, position.z);

            // Add the status effect
            StatusEffectInstance effectInstance = new StatusEffectInstance(effect, duration, amplifier);
            cloud.addEffect(effectInstance);

            // Configure cloud properties
            cloud.setRadius(splashRadius);
            cloud.setDuration(splashDuration);

            // Set color based on effect (for visual feedback)
            // Note: AreaEffectCloud color is set automatically from the effect
            // No need to manually set color in newer Minecraft versions

            // Spawn the cloud
            world.spawnEntity(cloud);

            AllEggRandomizer.LOGGER.info("Splash effect applied: {} at ({}, {}, {}), radius: {}, duration: {}",
                    getEffectName(effect), position.x, position.y, position.z, splashRadius, duration);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error applying splash effect", e);
            return false;
        }
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public String getDescription() {
        return "Creates an area effect cloud at the target position";
    }

    /**
     * Helper method to safely extract integer config values.
     */
    private int getIntConfig(Map<String, Object> config, String key, int defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Helper method to safely extract float config values.
     */
    private float getFloatConfig(Map<String, Object> config, String key, float defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Get effect name for logging.
     */
    private String getEffectName(RegistryEntry<StatusEffect> effect) {
        try {
            return effect.getKey().map(key -> key.getValue().toString()).orElse("Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }
}

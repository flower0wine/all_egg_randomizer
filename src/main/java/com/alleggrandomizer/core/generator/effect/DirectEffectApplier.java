package com.alleggrandomizer.core.generator.effect;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

/**
 * Direct effect applier - directly applies the effect to the nearest entity/player.
 * Searches for the nearest valid target within a configurable range.
 *
 * Configuration parameters:
 * - duration (int): Effect duration in ticks (default: 300)
 * - amplifier (int): Effect amplifier/level (default: 0, which is level I)
 * - targetType (String): Target type - "PLAYER", "ENTITY", or "ANY" (default: "ANY")
 * - searchRadius (double): Maximum search radius for targets (default: 5.0)
 */
public class DirectEffectApplier implements EffectApplier {

    private static final String METHOD_NAME = "DIRECT";

    @Override
    public boolean apply(ServerWorld world, Vec3d position, RegistryEntry<StatusEffect> effect, Map<String, Object> config) {
        if (world == null || position == null || effect == null) {
            AllEggRandomizer.LOGGER.warn("Cannot apply direct effect: missing required parameters");
            return false;
        }

        try {
            // Read configuration
            int duration = getIntConfig(config, "duration", 300);
            int amplifier = getIntConfig(config, "amplifier", 0);
            String targetType = getStringConfig(config, "targetType", "ANY");
            double searchRadius = getDoubleConfig(config, "searchRadius", 5.0);

            // Find nearest target
            LivingEntity target = findNearestTarget(world, position, searchRadius, targetType);

            if (target == null) {
                AllEggRandomizer.LOGGER.debug("No valid target found for direct effect within radius {}", searchRadius);
                // Fallback: create a small splash effect as backup
                return createFallbackSplash(world, position, effect, duration, amplifier);
            }

            // Apply effect to target
            StatusEffectInstance effectInstance = new StatusEffectInstance(effect, duration, amplifier);
            boolean success = target.addStatusEffect(effectInstance);

            if (success) {
                AllEggRandomizer.LOGGER.info("Direct effect applied: {} to {} at ({}, {}, {}), duration: {}, amplifier: {}",
                        getEffectName(effect), target.getName().getString(),
                        target.getX(), target.getY(), target.getZ(), duration, amplifier);
            } else {
                AllEggRandomizer.LOGGER.warn("Failed to apply effect to target: {}", target.getName().getString());
            }

            return success;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error applying direct effect", e);
            return false;
        }
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public String getDescription() {
        return "Directly applies effect to the nearest entity or player";
    }

    /**
     * Find the nearest valid target within the search radius.
     */
    private LivingEntity findNearestTarget(ServerWorld world, Vec3d position, double radius, String targetType) {
        // Create search box
        Box searchBox = new Box(
                position.x - radius, position.y - radius, position.z - radius,
                position.x + radius, position.y + radius, position.z + radius
        );

        // Get all entities in range
        List<Entity> entities = world.getOtherEntities(null, searchBox);

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : entities) {
            // Filter by target type
            if (!isValidTarget(entity, targetType)) {
                continue;
            }

            // Check if it's a living entity
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            // Calculate distance
            double distance = entity.squaredDistanceTo(position);
            if (distance < nearestDistance) {
                nearest = livingEntity;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

    /**
     * Check if an entity is a valid target based on target type.
     */
    private boolean isValidTarget(Entity entity, String targetType) {
        if (entity == null) {
            return false;
        }

        return switch (targetType.toUpperCase()) {
            case "PLAYER" -> entity instanceof PlayerEntity;
            case "ENTITY" -> !(entity instanceof PlayerEntity) && entity instanceof LivingEntity;
            case "ANY" -> entity instanceof LivingEntity;
            default -> entity instanceof LivingEntity;
        };
    }

    /**
     * Create a small splash effect as fallback when no target is found.
     */
    private boolean createFallbackSplash(ServerWorld world, Vec3d position,
                                         RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        AllEggRandomizer.LOGGER.debug("Creating fallback splash effect");

        // TODO: SPEC-06 - Fallback to splash effect when no direct target found
        // This requires SplashEffectApplier, which should be available
        // For now, just log and return false
        AllEggRandomizer.LOGGER.info("No target found for direct effect, consider using SPLASH mode instead");
        return false;
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
     * Helper method to safely extract double config values.
     */
    private double getDoubleConfig(Map<String, Object> config, String key, double defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    /**
     * Helper method to safely extract string config values.
     */
    private String getStringConfig(Map<String, Object> config, String key, String defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof String) {
            return (String) value;
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

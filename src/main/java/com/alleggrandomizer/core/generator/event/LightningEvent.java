package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Lightning event implementation.
 * Spawns a lightning bolt at the specified position with configurable behavior.
 *
 * Configuration parameters:
 * - cosmetic (boolean): Whether the lightning is purely visual (default: true)
 *   - true: Only visual effect, no damage, no fire
 *   - false: Full lightning effect with damage and fire
 */
public class LightningEvent implements WorldEvent {

    private static final String EVENT_ID = "LIGHTNING";

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute lightning event: world or position is null");
            return false;
        }

        // Read configuration
        boolean cosmetic = getConfigValue(config, "cosmetic", false);
        
        try {
            // Create lightning entity
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
            if (lightning == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create lightning entity");
                return false;
            }

            // Set position
            lightning.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);

            // Configure lightning behavior
            lightning.setCosmetic(cosmetic);
            
            // Spawn the lightning
            world.spawnEntity(lightning);

            AllEggRandomizer.LOGGER.info("Lightning event triggered at ({}, {}, {}), cosmetic: {}",
                    position.x, position.y, position.z, cosmetic);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing lightning event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns a lightning bolt at the target position";
    }

    /**
     * Helper method to safely extract boolean config values.
     */
    private boolean getConfigValue(Map<String, Object> config, String key, boolean defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        // Handle string values
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return defaultValue;
    }
}

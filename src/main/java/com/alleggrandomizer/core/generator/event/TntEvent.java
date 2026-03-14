package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Map;

/**
 * TNT event implementation.
 * Spawns an activated TNT at the specified position.
 */
public class TntEvent implements WorldEvent {

    private static final String EVENT_ID = "TNT";
    private static final int DEFAULT_FUSE_TICKS = 80; // 4 seconds

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute TNT event: world or position is null");
            return false;
        }

        try {
            // Create TNT entity
            TntEntity tnt = EntityType.TNT.create(world, SpawnReason.TRIGGERED);
            if (tnt == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create TNT entity");
                return false;
            }

            // Set position
            tnt.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);

            // Get custom fuse time from config, or use default
            int fuseTicks = getConfigValue(config, "fuseTicks", DEFAULT_FUSE_TICKS);
            tnt.setFuse(fuseTicks);

            // Spawn the TNT
            world.spawnEntity(tnt);

            AllEggRandomizer.LOGGER.info("TNT event triggered at ({}, {}, {}) with {} ticks fuse",
                    position.x, position.y, position.z, fuseTicks);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing TNT event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns an activated TNT at the target position";
    }

    /**
     * Helper method to safely extract integer config values.
     */
    private int getConfigValue(Map<String, Object> config, String key, int defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return defaultValue;
    }
}

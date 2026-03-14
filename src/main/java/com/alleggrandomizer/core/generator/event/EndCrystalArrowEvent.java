package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Map;

/**
 * End Crystal with Arrow event implementation.
 * Spawns an end crystal at the position, and an arrow 6 blocks above that falls to hit it.
 */
public class EndCrystalArrowEvent implements WorldEvent {

    private static final String EVENT_ID = "END_CRYSTAL_ARROW";
    private static final int ARROW_HEIGHT_OFFSET = 6;
    private static final Random RANDOM = Random.create();

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute EndCrystalArrow event: world or position is null");
            return false;
        }

        try {
            // Get arrow height offset from config, or use default
            int heightOffset = getConfigValue(config, "arrowHeightOffset", ARROW_HEIGHT_OFFSET);

            // Spawn end crystal at the hit position
            var endCrystal = EntityType.END_CRYSTAL.create(world, SpawnReason.TRIGGERED);
            if (endCrystal == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create end crystal entity");
                return false;
            }

            // Set end crystal position (on the ground)
            endCrystal.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);

            world.spawnEntity(endCrystal);

            // Spawn arrow 6 blocks above the crystal
            ArrowEntity arrow = EntityType.ARROW.create(world, SpawnReason.TRIGGERED);
            if (arrow == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create arrow entity");
                return false;
            }

            // Set arrow position above the crystal
            double arrowY = position.y + heightOffset;
            arrow.refreshPositionAndAngles(position.x, arrowY, position.z, 0, -90);
            
            // Set arrow as critical (for more damage)
            arrow.setCritical(true);
            
            // Set velocity to make it fall down
            arrow.setVelocity(0, -2, 0);

            world.spawnEntity(arrow);

            AllEggRandomizer.LOGGER.info("EndCrystalArrow event triggered at ({}, {}, {}), arrow height: {}",
                    position.x, position.y, position.z, heightOffset);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing EndCrystalArrow event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns an end crystal with a falling arrow above it";
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

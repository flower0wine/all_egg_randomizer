package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Map;

/**
 * Bat and Witch event implementation.
 * Spawns a group of bats and a witch at the specified position.
 */
public class BatWitchEvent implements WorldEvent {

    private static final String EVENT_ID = "BAT_WITCH";
    private static final int DEFAULT_BAT_COUNT = 5;
    private static final Random RANDOM = Random.create();

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute BatWitch event: world or position is null");
            return false;
        }

        try {
            // Get bat count from config, or use default
            int batCount = getConfigValue(config, "batCount", DEFAULT_BAT_COUNT);

            // Spawn bats around the position
            for (int i = 0; i < batCount; i++) {
                BatEntity bat = EntityType.BAT.create(world, SpawnReason.TRIGGERED);
                if (bat != null) {
                    // Random offset for bat position
                    double offsetX = (RANDOM.nextDouble() - 0.5) * 4;
                    double offsetY = RANDOM.nextDouble() * 3 + 1;
                    double offsetZ = (RANDOM.nextDouble() - 0.5) * 4;

                    bat.refreshPositionAndAngles(
                            position.x + offsetX,
                            position.y + offsetY,
                            position.z + offsetZ,
                            RANDOM.nextFloat() * 360,
                            0
                    );

                    world.spawnEntity(bat);
                }
            }

            // Spawn witch at the center position
            WitchEntity witch = EntityType.WITCH.create(world, SpawnReason.TRIGGERED);
            if (witch != null) {
                witch.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);
                world.spawnEntity(witch);

                AllEggRandomizer.LOGGER.info("BatWitch event triggered at ({}, {}, {}): {} bats + 1 witch",
                        position.x, position.y, position.z, batCount);
            } else {
                AllEggRandomizer.LOGGER.warn("Failed to create witch entity");
                return false;
            }

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing BatWitch event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns a group of bats and a witch at the target position";
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

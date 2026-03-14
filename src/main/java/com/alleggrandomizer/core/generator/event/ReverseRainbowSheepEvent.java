package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.data.SheepDataKeys;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Map;

/**
 * Reverse Rainbow Sheep event implementation.
 * Spawns 1 upside-down rainbow sheep at the specified position.
 * 
 * Implementation:
 * - Rainbow effect: "jeb_" prefix (vanilla) - triggers rainbow wool rendering
 * - Upside-down effect: Custom TrackedData - triggers upside-down rendering
 * 
 * We use TrackedData because:
 * 1. TrackedData automatically syncs from server to client
 * 2. "jeb_" must be the exact name to trigger rainbow effect
 * 
 * The client mixin reads the TrackedData to determine if the sheep should be rendered upside-down.
 */
public class ReverseRainbowSheepEvent implements WorldEvent {

    private static final String EVENT_ID = "REVERSE_RAINBOW_SHEEP";
    private static final int MIN_SHEEP_COUNT = 1;
    private static final int MAX_SHEEP_COUNT = 1;
    private static final Random RANDOM = Random.create();
    
    // The magic name that triggers rainbow sheep effect in vanilla Minecraft
    private static final String RAINBOW_NAME = "jeb_";

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute ReverseRainbowSheep event: world or position is null");
            return false;
        }

        try {
            // Get min and max sheep count from config
            int minCount = getConfigValue(config, "minCount", MIN_SHEEP_COUNT);
            int maxCount = getConfigValue(config, "maxCount", MAX_SHEEP_COUNT);

            // Random count between min and max
            int sheepCount = RANDOM.nextInt(maxCount - minCount + 1) + minCount;

            // Spawn upside-down rainbow sheep
            for (int i = 0; i < sheepCount; i++) {
                SheepEntity sheep = EntityType.SHEEP.create(world, SpawnReason.TRIGGERED);
                if (sheep == null) {
                    AllEggRandomizer.LOGGER.warn("Failed to create sheep entity");
                    continue;
                }

                // Random offset for sheep position
                double offsetX = (RANDOM.nextDouble() - 0.5) * 6;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 6;

                // Set position
                sheep.refreshPositionAndAngles(
                        position.x + offsetX,
                        position.y,
                        position.z + offsetZ,
                        RANDOM.nextFloat() * 360,
                        0
                );

                // Set the name to "jeb_" to trigger rainbow effect
                // This MUST be exactly "jeb_" (with underscore) to work
                sheep.setCustomName(Text.literal(RAINBOW_NAME));
                
                // Set TrackedData to mark this sheep for upside-down rendering
                // This will automatically sync to nearby clients
                sheep.getDataTracker().set(SheepDataKeys.FLIP_UPSIDE_DOWN, true);

                // Set as baby for cuteness
                sheep.setBaby(true);

                // Spawn the sheep
                world.spawnEntity(sheep);
            }

            AllEggRandomizer.LOGGER.info("ReverseRainbowSheep event triggered at ({}, {}, {}): {} upside-down rainbow sheep",
                    position.x, position.y, position.z, sheepCount);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing ReverseRainbowSheep event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns 1 upside-down rainbow sheep at the target position";
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

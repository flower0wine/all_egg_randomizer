package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.DyeColor;

import java.util.Map;

/**
 * Rainbow Sheep event implementation.
 * Spawns 6-12 sheep with random colors at the specified position.
 */
public class RainbowSheepEvent implements WorldEvent {

    private static final String EVENT_ID = "RAINBOW_SHEEP";
    private static final int MIN_SHEEP_COUNT = 6;
    private static final int MAX_SHEEP_COUNT = 12;
    private static final Random RANDOM = Random.create();

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute RainbowSheep event: world or position is null");
            return false;
        }

        try {
            // Get min and max sheep count from config
            int minCount = getConfigValue(config, "minCount", MIN_SHEEP_COUNT);
            int maxCount = getConfigValue(config, "maxCount", MAX_SHEEP_COUNT);

            // Random count between min and max
            int sheepCount = RANDOM.nextInt(maxCount - minCount + 1) + minCount;

            // Get all possible dye colors
            DyeColor[] colors = DyeColor.values();

            // Spawn sheep with random colors
            for (int i = 0; i < sheepCount; i++) {
                SheepEntity sheep = EntityType.SHEEP.create(world, SpawnReason.TRIGGERED);
                if (sheep != null) {
                    // Random offset for sheep position
                    double offsetX = (RANDOM.nextDouble() - 0.5) * 6;
                    double offsetZ = (RANDOM.nextDouble() - 0.5) * 6;

                    sheep.refreshPositionAndAngles(
                            position.x + offsetX,
                            position.y,
                            position.z + offsetZ,
                            RANDOM.nextFloat() * 360,
                            0
                    );

                    // Set random color
                    DyeColor randomColor = colors[RANDOM.nextInt(colors.length)];
                    sheep.setColor(randomColor);

                    // Set as baby
                    sheep.setBaby(true);

                    world.spawnEntity(sheep);
                }
            }

            AllEggRandomizer.LOGGER.info("RainbowSheep event triggered at ({}, {}, {}): {} sheep",
                    position.x, position.y, position.z, sheepCount);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing RainbowSheep event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns 6-12 rainbow sheep at the target position";
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

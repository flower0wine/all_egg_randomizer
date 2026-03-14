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
 * Reverse Rainbow Sheep event implementation.
 * Spawns a rainbow sheep that moves backwards at the specified position.
 * Note: The sheep will have backward movement applied via velocity manipulation.
 */
public class ReverseRainbowSheepEvent implements WorldEvent {

    private static final String EVENT_ID = "REVERSE_RAINBOW_SHEEP";
    private static final Random RANDOM = Random.create();
    
    // Backward movement speed
    private static final double BACKWARD_SPEED = -0.15;

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
            // Get backward speed from config
            double speed = getConfigValue(config, "speed", BACKWARD_SPEED);

            // Create sheep entity
            SheepEntity sheep = EntityType.SHEEP.create(world, SpawnReason.TRIGGERED);
            if (sheep == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create sheep entity");
                return false;
            }

            // Set position
            sheep.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);

            // Set random color (rainbow)
            DyeColor[] colors = DyeColor.values();
            DyeColor randomColor = colors[RANDOM.nextInt(colors.length)];
            sheep.setColor(randomColor);

            // Set as baby
            sheep.setBaby(true);

            // Spawn the sheep first
            world.spawnEntity(sheep);

            // Apply initial backward velocity to make it start moving backwards
            // The sheep will continue moving in its facing direction but we'll give it backward velocity
            Vec3d backwardVelocity = new Vec3d(0, 0, BACKWARD_SPEED);
            sheep.setVelocity(backwardVelocity);

            AllEggRandomizer.LOGGER.info("ReverseRainbowSheep event triggered at ({}, {}, {}) with speed {}",
                    position.x, position.y, position.z, speed);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing ReverseRainbowSheep event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns a rainbow sheep that moves backwards at the target position";
    }

    /**
     * Helper method to safely extract double config values.
     */
    private double getConfigValue(Map<String, Object> config, String key, double defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }

        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        return defaultValue;
    }
}

package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * TNT Minecart event implementation.
 * Spawns a TNT minecart at the specified position.
 */
public class TntMinecartEvent implements WorldEvent {

    private static final String EVENT_ID = "TNT_MINECART";

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute TntMinecart event: world or position is null");
            return false;
        }

        try {
            // Create TNT minecart entity
            var minecart = EntityType.TNT_MINECART.create(world, SpawnReason.TRIGGERED);
            if (minecart == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create TNT minecart entity");
                return false;
            }

            // Set position
            minecart.refreshPositionAndAngles(position.x, position.y + 0.5, position.z, 0, 0);

            // Spawn the minecart
            world.spawnEntity(minecart);

            AllEggRandomizer.LOGGER.info("TntMinecart event triggered at ({}, {}, {})",
                    position.x, position.y, position.z);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing TntMinecart event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns a TNT minecart at the target position";
    }
}

package com.alleggrandomizer.core.generator.event;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Pig with Saddle event implementation.
 * Spawns a pig with a saddle at the specified position.
 */
public class PigWithSaddleEvent implements WorldEvent {

    private static final String EVENT_ID = "PIG_WITH_SADDLE";

    @Override
    public String getEventId() {
        return EVENT_ID;
    }

    @Override
    public boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config) {
        if (world == null || position == null) {
            AllEggRandomizer.LOGGER.warn("Cannot execute PigWithSaddle event: world or position is null");
            return false;
        }

        try {
            // Create pig entity
            PigEntity pig = EntityType.PIG.create(world, SpawnReason.TRIGGERED);
            if (pig == null) {
                AllEggRandomizer.LOGGER.warn("Failed to create pig entity");
                return false;
            }

            // Set position
            pig.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);

            // Set as adult (pigs need to be adult to be rideable)
            pig.setBaby(false);

            // Equip saddle on the pig using EquipmentSlot (1.21+ API)
            pig.equipStack(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));

            // Spawn the pig
            world.spawnEntity(pig);

            AllEggRandomizer.LOGGER.info("PigWithSaddle event triggered at ({}, {}, {})",
                    position.x, position.y, position.z);

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error executing PigWithSaddle event", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Spawns a pig with a saddle at the target position";
    }
}

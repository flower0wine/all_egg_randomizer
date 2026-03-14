package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for lightning creepers.
 * Creates a creeper and strikes it with lightning to transform it into a charged creeper.
 */
public class LightningCreeperConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!(entity instanceof CreeperEntity creeper)) {
            return false;
        }

        // Get position using getX(), getY(), getZ() (Yarn mapping)
        double x = creeper.getX();
        double y = creeper.getY();
        double z = creeper.getZ();

        // Spawn lightning to make creeper charged
        spawnLightningAndTransform(world, x, y, z);

        AllEggRandomizer.LOGGER.debug("Created lightning creeper at ({}, {}, {})", x, y, z);

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof CreeperEntity;
    }

    /**
     * Spawns lightning at position to create charged creeper.
     */
    public static void spawnLightningAndTransform(ServerWorld world, double x, double y, double z) {
        // Create lightning entity
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAndAngles(x, y, z, 0, 0);
        
        // Spawn the lightning (visual effect and will charge nearby creepers)
        world.spawnEntity(lightning);

        AllEggRandomizer.LOGGER.debug("Spawned lightning at ({}, {}, {})", x, y, z);
    }
}

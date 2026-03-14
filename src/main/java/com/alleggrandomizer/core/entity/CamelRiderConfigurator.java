package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for zombie riders on camels (normal or zombie camels).
 * Handles both adult and baby zombies with spears.
 */
public class CamelRiderConfigurator implements EntityConfigurator {

    private final EntityType<?> camelType;
    private final boolean isAdult;
    private final boolean giveSpear;

    public CamelRiderConfigurator(EntityType<?> camelType, boolean isAdult, boolean giveSpear) {
        this.camelType = camelType;
        this.isAdult = isAdult;
        this.giveSpear = giveSpear;
    }

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!(entity instanceof ZombieEntity zombie)) {
            return false;
        }

        // Create the camel mount
        Entity camel = camelType.create(world, SpawnReason.TRIGGERED);
        if (camel == null) {
            AllEggRandomizer.LOGGER.warn("Failed to create camel mount");
            return false;
        }

        // Set baby status if it's a camel
        if (camel instanceof CamelEntity camelEntity) {
            camelEntity.setBaby(!isAdult);
        }

        // Position the camel at the zombie's location
        camel.refreshPositionAndAngles(
            zombie.getX(),
            zombie.getY(),
            zombie.getZ(),
            zombie.getYaw(),
            zombie.getPitch()
        );

        // Spawn the mount first
        world.spawnEntity(camel);

        // Make zombie ride the camel
        zombie.startRiding(camel);

        // Give the zombie a spear if requested
        if (giveSpear) {
            giveSpearToZombie(zombie);
        }

        AllEggRandomizer.LOGGER.debug("Created zombie on camel with spear: adult={}, spear={}",
            isAdult, giveSpear);

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof ZombieEntity;
    }

    private void giveSpearToZombie(ZombieEntity zombie) {
        // Use the existing spear logic from ZombieEquipmentConfigurator
        new ZombieSpearConfigurator().giveRandomSpear(zombie);
    }
}

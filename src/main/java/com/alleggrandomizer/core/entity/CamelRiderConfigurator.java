package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.enchant.RandomEnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * Configurator for zombie riders on camels (normal or zombie camels).
 * Handles both adult and baby zombies with spears.
 * Has 30% chance to add a skeleton passenger behind the zombie.
 */
public class CamelRiderConfigurator implements EntityConfigurator {

    private static final double SKELETON_PASSENGER_CHANCE = 0.30;
    private static final Random RANDOM = new Random();

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

        // 30% chance to add skeleton passenger behind the zombie
        if (RANDOM.nextDouble() < SKELETON_PASSENGER_CHANCE) {
            addSkeletonPassenger(world, camel);
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

    private void addSkeletonPassenger(ServerWorld world, Entity camel) {
        // Create skeleton passenger
        Entity skeleton = EntityType.SKELETON.create(world, SpawnReason.TRIGGERED);
        if (skeleton == null) {
            AllEggRandomizer.LOGGER.warn("Failed to create skeleton passenger");
            return;
        }

        // Position behind the zombie (offset back on Z axis)
        skeleton.refreshPositionAndAngles(
            camel.getX(),
            camel.getY() + 1.5,
            camel.getZ() + 1.0,
            camel.getYaw(),
            camel.getPitch()
        );

        // Give skeleton a random weapon (sword or bow)
        if (skeleton instanceof SkeletonEntity skeletonEntity) {
            if (RANDOM.nextBoolean()) {
                skeletonEntity.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, 
                    RandomEnchantmentHelper.createRandomEnchantedSword(world));
            } else {
                skeletonEntity.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, 
                    RandomEnchantmentHelper.createRandomEnchantedBow(world));
            }
        }

        // Spawn the skeleton
        world.spawnEntity(skeleton);

        // Make skeleton ride the camel (not the zombie)
        skeleton.startRiding(camel);

        AllEggRandomizer.LOGGER.debug("Added skeleton passenger behind zombie on camel");
    }
}

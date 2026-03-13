package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * Configures entity age, making some entities spawn as babies.
 * Applies to passive entities and zombies with a 30% chance.
 */
public class AgeConfigurator implements EntityConfigurator {
    
    private static final double BABY_CHANCE = 0.3;
    private static final Random RANDOM = new Random();
    
    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }
        
        // 30% chance to spawn as baby
        if (RANDOM.nextDouble() < BABY_CHANCE) {
            if (entity instanceof PassiveEntity passiveEntity) {
                passiveEntity.setBaby(true);
                AllEggRandomizer.LOGGER.debug("Configured {} as baby (passive)", 
                    entity.getType().getName().getString());
                return true;
            } else if (entity instanceof ZombieEntity zombieEntity) {
                zombieEntity.setBaby(true);
                AllEggRandomizer.LOGGER.debug("Configured {} as baby (zombie)", 
                    entity.getType().getName().getString());
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof PassiveEntity || entity instanceof ZombieEntity;
    }
}

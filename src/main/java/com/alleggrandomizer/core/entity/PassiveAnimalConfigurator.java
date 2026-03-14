package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Base configurator for passive animals (donkey, sheep, etc.)
 * Sets them as babies with a certain probability.
 */
public class PassiveAnimalConfigurator implements EntityConfigurator {

    private final double babyChance;

    public PassiveAnimalConfigurator(double babyChance) {
        this.babyChance = babyChance;
    }

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!(entity instanceof AnimalEntity animal)) {
            return false;
        }

        // Randomly set as baby based on configured chance
        if (Math.random() < babyChance) {
            animal.setBaby(true);
        }

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof AnimalEntity;
    }
}

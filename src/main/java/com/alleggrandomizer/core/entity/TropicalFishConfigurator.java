package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Tropical Fish.
 */
public class TropicalFishConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Tropical fish have random patterns/colors generated on spawn

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof TropicalFishEntity;
    }
}

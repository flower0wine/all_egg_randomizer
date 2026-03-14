package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Snow Golems (Snowmen).
 */
public class SnowGolemConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Snow golems don't need special configuration
        // They are created by default with pumpkin head

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof SnowGolemEntity;
    }
}

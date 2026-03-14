package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Copper Golems.
 */
public class CopperGolemConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Copper golems don't need special configuration

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof CopperGolemEntity;
    }
}

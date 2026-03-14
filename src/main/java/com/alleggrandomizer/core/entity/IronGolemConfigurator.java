package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Iron Golems.
 */
public class IronGolemConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Iron golems don't need special configuration
        // They are created as adult by default

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof IronGolemEntity;
    }
}

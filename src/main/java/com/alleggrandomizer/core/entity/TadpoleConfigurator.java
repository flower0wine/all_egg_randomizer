package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TadpoleEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Tadpoles.
 */
public class TadpoleConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Tadpoles don't need special configuration

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof TadpoleEntity;
    }
}

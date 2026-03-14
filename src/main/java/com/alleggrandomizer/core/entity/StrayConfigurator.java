package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Strays (Frozen Skeletons).
 */
public class StrayConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Strays are variants of skeletons
        // They spawn with a bow and sometimes with tipped arrows

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof SkeletonEntity;
    }
}

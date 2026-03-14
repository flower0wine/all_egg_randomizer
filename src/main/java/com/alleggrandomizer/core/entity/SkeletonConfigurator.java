package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Configurator for Skeletons.
 */
public class SkeletonConfigurator implements EntityConfigurator {

    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }

        // Skeletons can have random equipment
        // They typically spawn with a bow

        return true;
    }

    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof SkeletonEntity;
    }
}

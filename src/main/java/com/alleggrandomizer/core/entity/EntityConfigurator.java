package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

/**
 * Interface for configuring spawned entities.
 * Implementations can modify entity properties after creation.
 */
public interface EntityConfigurator {
    
    /**
     * Configure the given entity.
     * 
     * @param entity the entity to configure
     * @param world the world the entity is in
     * @return true if configuration was applied, false otherwise
     */
    boolean configure(Entity entity, ServerWorld world);
    
    /**
     * Check if this configurator can handle the given entity.
     * 
     * @param entity the entity to check
     * @return true if this configurator can configure the entity
     */
    boolean canConfigure(Entity entity);
}

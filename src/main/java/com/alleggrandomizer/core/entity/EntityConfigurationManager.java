package com.alleggrandomizer.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages entity configurators and applies them to spawned entities.
 * Configurators are applied in registration order.
 */
public class EntityConfigurationManager {
    
    private final List<EntityConfigurator> configurators = new ArrayList<>();
    
    /**
     * Register a configurator.
     * 
     * @param configurator the configurator to register
     */
    public void registerConfigurator(EntityConfigurator configurator) {
        configurators.add(configurator);
    }
    
    /**
     * Apply all applicable configurators to an entity.
     * 
     * @param entity the entity to configure
     * @param world the world the entity is in
     */
    public void configureEntity(Entity entity, ServerWorld world) {
        for (EntityConfigurator configurator : configurators) {
            if (configurator.canConfigure(entity)) {
                configurator.configure(entity, world);
            }
        }
    }
}

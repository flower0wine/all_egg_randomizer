package com.alleggrandomizer.core.entity;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Configures zombie mounts, giving them rideable entities with a 30% chance.
 * Supports zombie horses, camel husks, and zombie nautiluses.
 */
public class ZombieMountConfigurator implements EntityConfigurator {
    
    private static final double MOUNT_CHANCE = 0.3;
    private static final Random RANDOM = new Random();
    
    // Available mount types for zombies
    private static final List<EntityType<?>> MOUNT_TYPES = new ArrayList<>();
    
    static {
        // Initialize mount types list
        addMountIfExists(EntityType.ZOMBIE_HORSE);
        addMountIfExists(EntityType.CAMEL_HUSK);
        addMountIfExists(EntityType.ZOMBIE_NAUTILUS);
    }
    
    private static void addMountIfExists(EntityType<?> entityType) {
        if (entityType != null) {
            MOUNT_TYPES.add(entityType);
        }
    }
    
    @Override
    public boolean configure(Entity entity, ServerWorld world) {
        if (!canConfigure(entity)) {
            return false;
        }
        
        ZombieEntity zombie = (ZombieEntity) entity;
        
        // 30% chance to spawn with a mount
        if (RANDOM.nextDouble() < MOUNT_CHANCE && !MOUNT_TYPES.isEmpty()) {
            EntityType<?> mountType = MOUNT_TYPES.get(RANDOM.nextInt(MOUNT_TYPES.size()));
            Entity mount = mountType.create(world, SpawnReason.TRIGGERED);
            
            if (mount != null) {
                // Position the mount at the same location as the zombie
                mount.refreshPositionAndAngles(
                    zombie.getX(), 
                    zombie.getY(), 
                    zombie.getZ(), 
                    zombie.getYaw(), 
                    zombie.getPitch()
                );
                
                // Spawn the mount first
                world.spawnEntity(mount);
                
                // Make the zombie ride the mount
                zombie.startRiding(mount);
                
                // Tame horses if applicable
                if (mount instanceof AbstractHorseEntity horse) {
                    horse.setTame(true);
                }
                
                AllEggRandomizer.LOGGER.debug("Mounted {} on {}", 
                    zombie.getType().getName().getString(),
                    mountType.getName().getString());
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean canConfigure(Entity entity) {
        return entity instanceof ZombieEntity;
    }
}

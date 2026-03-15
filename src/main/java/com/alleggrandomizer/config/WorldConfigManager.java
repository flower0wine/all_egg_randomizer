package com.alleggrandomizer.config;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;

/**
 * Manages per-world configuration instances.
 * Each world (save file) has its own independent configuration.
 * 
 * This ensures that configuration changes in one world do not affect other worlds.
 * 
 * Note: We do NOT use a static cache here because:
 * 1. PersistentStateManager already handles caching internally
 * 2. A static cache would cause config to be shared between worlds with the same name
 * 3. Each call to getWorldConfig() should fetch fresh data from the world's save file
 */
public class WorldConfigManager {
    
    /**
     * Get the configuration for a specific world.
     * Fetches directly from PersistentStateManager - no static cache.
     * 
     * @param server the Minecraft server
     * @return the world-specific configuration data
     */
    public static WorldConfigData getWorldConfig(MinecraftServer server) {
        if (server == null) {
            AllEggRandomizer.LOGGER.warn("Cannot get world config: server is null");
            return null;
        }
        
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            AllEggRandomizer.LOGGER.warn("Cannot get world config: overworld is null");
            return null;
        }
        
        // Get world name for logging
        String worldName = getWorldName(server);
        
        // Always fetch fresh from PersistentStateManager
        // It handles caching internally, so we don't need our own cache
        AllEggRandomizer.LOGGER.info("Loading configuration for world: {}", worldName);
        WorldConfigData config = overworld.getPersistentStateManager().getOrCreate(
            WorldConfigData.getPersistentStateType()
        );
        
        return config;
    }
    
    /**
     * Get the unique name/identifier for the current world.
     * 
     * @param server the Minecraft server
     * @return the world name
     */
    private static String getWorldName(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        if (overworld != null && overworld.getLevelProperties() instanceof ServerWorldProperties props) {
            return props.getLevelName();
        }
        return "unknown_world";
    }
}

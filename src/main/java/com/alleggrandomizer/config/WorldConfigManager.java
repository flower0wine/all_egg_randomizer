package com.alleggrandomizer.config;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-world configuration instances.
 * Each world (save file) has its own independent configuration.
 * 
 * This ensures that configuration changes in one world do not affect other worlds.
 */
public class WorldConfigManager {
    
    private static final Map<String, WorldConfigData> worldConfigs = new ConcurrentHashMap<>();
    
    /**
     * Get the configuration for a specific world.
     * Creates a new configuration if one doesn't exist for this world.
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
        
        // Get world name as unique identifier
        String worldName = getWorldName(server);
        
            // Get or create config for this world
            WorldConfigData config = worldConfigs.computeIfAbsent(worldName, name -> {
                AllEggRandomizer.LOGGER.info("Loading configuration for world: {}", name);
                return overworld.getPersistentStateManager().getOrCreate(
                    WorldConfigData.getPersistentStateType()
                );
            });
        
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
    
    /**
     * Clear cached configuration for a world.
     * Called when a world is unloaded.
     * 
     * @param server the Minecraft server
     */
    public static void clearWorldConfig(MinecraftServer server) {
        if (server == null) {
            return;
        }
        
        String worldName = getWorldName(server);
        WorldConfigData removed = worldConfigs.remove(worldName);
        if (removed != null) {
            AllEggRandomizer.LOGGER.info("Cleared configuration cache for world: {}", worldName);
        }
    }
    
    /**
     * Clear all cached configurations.
     * Typically called on mod shutdown.
     */
    public static void clearAllConfigs() {
        worldConfigs.clear();
        AllEggRandomizer.LOGGER.info("Cleared all world configuration caches");
    }
}

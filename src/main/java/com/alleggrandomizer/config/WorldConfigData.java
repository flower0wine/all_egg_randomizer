package com.alleggrandomizer.config;

import com.alleggrandomizer.AllEggRandomizer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

/**
 * World-specific configuration data stored using Minecraft's PersistentState system.
 * Each world (save file) has its own instance of this configuration.
 * 
 * This replaces the global singleton ConfigManager pattern with per-world storage.
 */
public class WorldConfigData extends PersistentState {
    
    private ModConfig config;
    
    private static final Codec<WorldConfigData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ModConfig.CODEC.fieldOf("config").forGetter(data -> data.config)
        ).apply(instance, WorldConfigData::new)
    );
    
    private static final PersistentStateType<WorldConfigData> TYPE = new PersistentStateType<>(
        AllEggRandomizer.MOD_ID,
        WorldConfigData::new,
        CODEC,
        null
    );
    
    /**
     * Create a new WorldConfigData with default configuration.
     */
    public WorldConfigData() {
        this.config = new ModConfig();
        AllEggRandomizer.LOGGER.debug("Created new world config with defaults");
    }
    
    /**
     * Create a WorldConfigData with existing configuration.
     * Used by codec for deserialization.
     */
    private WorldConfigData(ModConfig config) {
        this.config = config;
    }
    
    /**
     * Get the persistent state type for registration.
     */
    public static PersistentStateType<WorldConfigData> getPersistentStateType() {
        return TYPE;
    }
    
    /**
     * Get the current configuration.
     */
    public ModConfig getConfig() {
        return config;
    }
    
    /**
     * Get config for a specific category.
     */
    public CategoryConfig getCategoryConfig(CategoryType type) {
        return config != null ? config.getCategory(type) : null;
    }
    
    /**
     * Check if a category is enabled.
     */
    public boolean isCategoryEnabled(CategoryType type) {
        return config != null && config.isCategoryEnabled(type);
    }
    
    /**
     * Get weight for a category.
     */
    public double getCategoryWeight(CategoryType type) {
        CategoryConfig categoryConfig = getCategoryConfig(type);
        return categoryConfig != null ? categoryConfig.getWeight() : 1.0;
    }
    
    /**
     * Enable or disable a category.
     */
    public boolean setCategoryEnabled(CategoryType type, boolean enabled) {
        CategoryConfig categoryConfig = getCategoryConfig(type);
        if (categoryConfig != null) {
            categoryConfig.setEnabled(enabled);
            markDirty();
            return true;
        }
        return false;
    }
    
    /**
     * Set weight for a category.
     */
    public boolean setCategoryWeight(CategoryType type, double weight) {
        if (weight < 0) {
            return false;
        }
        
        CategoryConfig categoryConfig = getCategoryConfig(type);
        if (categoryConfig != null) {
            categoryConfig.setWeight(weight);
            markDirty();
            return true;
        }
        return false;
    }
    
    /**
     * Get global settings.
     */
    public GlobalSettings getGlobalSettings() {
        return config != null ? config.getGlobalSettings() : null;
    }
}

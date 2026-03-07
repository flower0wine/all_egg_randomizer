package com.alleggrandomizer.config;

import com.alleggrandomizer.AllEggRandomizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Configuration manager for the mod.
 * Handles loading, saving, and hot-reloading of configuration.
 * Uses singleton pattern for global access.
 */
public class ConfigManager {

    private static final String CONFIG_FILENAME = "alleggrandomizer.json";
    private static final String CONFIG_VERSION = "1.0.0";

    private static ConfigManager instance;

    private final Gson gson;
    private final Path configPath;
    private ModConfig config;
    private final List<Consumer<ModConfig>> changeListeners;

    private ConfigManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.configPath = configDir.resolve(CONFIG_FILENAME);
        this.changeListeners = new ArrayList<>();
        
        loadConfig();
    }

    /**
     * Get the singleton instance.
     * @return the config manager instance
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load configuration from file.
     * If file doesn't exist, creates default config.
     * If file is invalid, falls back to default config.
     */
    private void loadConfig() {
        try {
            if (Files.exists(configPath)) {
                String jsonContent = Files.readString(configPath);
                config = gson.fromJson(jsonContent, ModConfig.class);
                
                if (config == null) {
                    AllEggRandomizer.LOGGER.warn("Config file was empty, using defaults");
                    config = new ModConfig();
                }
                
                // Migrate config if needed
                migrateConfig();
                
                // Validate config
                config.validate();
                
                AllEggRandomizer.LOGGER.info("Configuration loaded from {}", configPath);
            } else {
                AllEggRandomizer.LOGGER.info("No config file found, creating default config");
                config = new ModConfig();
                saveConfig();
            }
        } catch (JsonSyntaxException e) {
            AllEggRandomizer.LOGGER.error("Failed to parse config file, using defaults: {}", e.getMessage());
            config = new ModConfig();
        } catch (IOException e) {
            AllEggRandomizer.LOGGER.error("Failed to read config file, using defaults: {}", e.getMessage());
            config = new ModConfig();
        }
    }

    /**
     * Migrate config to current version if needed.
     */
    private void migrateConfig() {
        if (config == null) {
            return;
        }
        
        String currentVersion = config.getVersion();
        if (currentVersion == null || !currentVersion.equals(CONFIG_VERSION)) {
            AllEggRandomizer.LOGGER.info("Migrating config from {} to {}", currentVersion, CONFIG_VERSION);
            config.setVersion(CONFIG_VERSION);
            // Future: add migration logic here
        }
    }

    /**
     * Save configuration to file.
     * @return true if saved successfully
     */
    public synchronized boolean saveConfig() {
        try {
            // Ensure parent directory exists
            Path parentDir = configPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            String jsonContent = gson.toJson(config);
            Files.writeString(configPath, jsonContent);
            
            AllEggRandomizer.LOGGER.info("Configuration saved to {}", configPath);
            return true;
        } catch (IOException e) {
            AllEggRandomizer.LOGGER.error("Failed to save config: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Reload configuration from file.
     * Notifies all change listeners after reload.
     * @return true if reload successful
     */
    public synchronized boolean reloadConfig() {
        ModConfig oldConfig = config;
        loadConfig();
        
        // Notify listeners
        notifyListeners(oldConfig, config);
        
        return true;
    }

    /**
     * Get current configuration.
     * @return the current config
     */
    public ModConfig getConfig() {
        return config;
    }

    /**
     * Get config for a specific category.
     * @param type the category type
     * @return the category config
     */
    public CategoryConfig getCategoryConfig(CategoryType type) {
        return config != null ? config.getCategory(type) : null;
    }

    /**
     * Check if a category is enabled.
     * @param type the category type
     * @return true if enabled
     */
    public boolean isCategoryEnabled(CategoryType type) {
        return config != null && config.isCategoryEnabled(type);
    }

    /**
     * Get weight for a category.
     * @param type the category type
     * @return the weight value
     */
    public double getCategoryWeight(CategoryType type) {
        CategoryConfig categoryConfig = getCategoryConfig(type);
        return categoryConfig != null ? categoryConfig.getWeight() : 1.0;
    }

    /**
     * Enable or disable a category.
     * @param type the category type
     * @param enabled true to enable, false to disable
     * @return true if successful
     */
    public synchronized boolean setCategoryEnabled(CategoryType type, boolean enabled) {
        CategoryConfig categoryConfig = getCategoryConfig(type);
        if (categoryConfig != null) {
            categoryConfig.setEnabled(enabled);
            notifyListeners(config, config);
            return saveConfig();
        }
        return false;
    }

    /**
     * Set weight for a category.
     * @param type the category type
     * @param weight the weight value (must be >= 0)
     * @return true if successful
     */
    public synchronized boolean setCategoryWeight(CategoryType type, double weight) {
        if (weight < 0) {
            return false;
        }
        
        CategoryConfig categoryConfig = getCategoryConfig(type);
        if (categoryConfig != null) {
            categoryConfig.setWeight(weight);
            notifyListeners(config, config);
            return saveConfig();
        }
        return false;
    }

    /**
     * Get global settings.
     * @return the global settings
     */
    public GlobalSettings getGlobalSettings() {
        return config != null ? config.getGlobalSettings() : null;
    }

    /**
     * Register a listener for config changes.
     * @param listener the listener to add
     */
    public void addChangeListener(Consumer<ModConfig> listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    /**
     * Remove a config change listener.
     * @param listener the listener to remove
     */
    public void removeChangeListener(Consumer<ModConfig> listener) {
        changeListeners.remove(listener);
    }

    /**
     * Notify all listeners of config change.
     */
    private void notifyListeners(ModConfig oldConfig, ModConfig newConfig) {
        for (Consumer<ModConfig> listener : changeListeners) {
            try {
                listener.accept(newConfig);
            } catch (Exception e) {
                AllEggRandomizer.LOGGER.error("Error notifying config change listener: {}", e.getMessage());
            }
        }
    }

    /**
     * Get the config file path.
     * @return the path to the config file
     */
    public Path getConfigPath() {
        return configPath;
    }
}

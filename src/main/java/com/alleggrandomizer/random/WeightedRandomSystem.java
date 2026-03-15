package com.alleggrandomizer.random;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.ModConfig;
import com.alleggrandomizer.core.classifier.ItemCategoryClassifier;
import com.alleggrandomizer.core.entity.SpecialEntityType;
import com.alleggrandomizer.core.item.BundleItemPopulator;
import com.alleggrandomizer.core.item.ChestItemPopulator;
import com.alleggrandomizer.core.item.ShulkerBoxItemPopulator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Main entry point for the weighted random system.
 * Provides a unified API for selecting random products based on configuration.
 * 
 * This class orchestrates the two-level random selection:
 * 1. Category selection (using CategorySelector)
 * 2. Product selection (using ProductSelector)
 * 
 * Usage:
 * <pre>
 * // Create with default seed provider
 * WeightedRandomSystem system = new WeightedRandomSystem();
 * 
 * // Select a category based on config
 * CategoryType category = system.selectCategory(config);
 * 
 * // Select a product within the category
 * EntityType selectedEntity = system.selectEntity(config, EntityType::getKey);
 * </pre>
 */
public class WeightedRandomSystem {
    
    private RandomSeedProvider seedProvider;
    private CategorySelector categorySelector;
    
    /**
     * Create a WeightedRandomSystem with default seed provider.
     */
    public WeightedRandomSystem() {
        this(RandomSeedProvider.createDefault());
    }
    
    /**
     * Create a WeightedRandomSystem with a custom seed provider.
     * 
     * @param seedProvider the seed provider to use
     */
    public WeightedRandomSystem(RandomSeedProvider seedProvider) {
        this.seedProvider = seedProvider;
        this.categorySelector = new CategorySelector();
    }
    
    /**
     * Generate a seed for the current throw context.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param worldSeed the world seed
     * @param playerId the player UUID (as string)
     * @param timeMillis current time in milliseconds
     * @return the generated seed
     */
    public long generateSeed(double x, double y, double z, long worldSeed, String playerId, long timeMillis) {
        return seedProvider.generateSeed(x, y, z, worldSeed, playerId, timeMillis);
    }
    
    /**
     * Select a category based on the mod configuration.
     * 
     * @param config the mod configuration
     * @return the selected category type, or null if none enabled
     */
    public CategoryType selectCategory(ModConfig config) {
        if (config == null) {
            AllEggRandomizer.LOGGER.warn("No config provided to WeightedRandomSystem.selectCategory");
            return null;
        }
        
        Map<CategoryType, CategoryConfig> categoryConfigs = new EnumMap<>(CategoryType.class);
        for (CategoryType type : CategoryType.values()) {
            CategoryConfig catConfig = config.getCategory(type);
            if (catConfig != null) {
                categoryConfigs.put(type, catConfig);
            }
        }
        
        return categorySelector.selectCategory(categoryConfigs);
    }
    
    /**
     * Select a category with a specific seed for deterministic results.
     * 
     * @param config the mod configuration
     * @param seed the seed for deterministic selection
     * @return the selected category type
     */
    public CategoryType selectCategory(ModConfig config, long seed) {
        categorySelector.setSeed(seed);
        return selectCategory(config);
    }
    
    /**
     * Select a random entity type within the ENTITY category.
     * 
     * @param config the mod configuration
     * @param availableEntities map of entity registry keys to weights
     * @return the selected entity type, or null if selection fails
     */
    public net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.EntityType<?>> 
    selectEntity(ModConfig config, Map<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.EntityType<?>>, Double> availableEntities) {
        
        if (availableEntities == null || availableEntities.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No available entities provided");
            return null;
        }
        
        CategoryConfig entityConfig = config.getCategory(CategoryType.ENTITY);
        if (entityConfig == null || !entityConfig.isEnabled()) {
            AllEggRandomizer.LOGGER.debug("ENTITY category is not enabled");
            return null;
        }
        
        // Use category weight as a modifier for entity weights
        double categoryWeight = entityConfig.getWeight();
        
        // Create adjusted weights
        Map<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.EntityType<?>>, Double> adjustedWeights = new HashMap<>();
        for (Map.Entry<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.EntityType<?>>, Double> entry : availableEntities.entrySet()) {
            adjustedWeights.put(entry.getKey(), entry.getValue() * categoryWeight);
        }
        
        ProductSelector<net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.EntityType<?>>> selector = 
            new ProductSelector<>();
        
        return selector.selectProduct(adjustedWeights);
    }
    
    /**
     * Select a special entity type from the special entity pool.
     * 
     * @param specialEntityWeights map of special entity types to weights
     * @return the selected special entity type, or null if selection fails
     */
    public SpecialEntityType selectSpecialEntity(Map<SpecialEntityType, Double> specialEntityWeights) {
        if (specialEntityWeights == null || specialEntityWeights.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No special entities provided");
            return null;
        }
        
        ProductSelector<SpecialEntityType> selector = new ProductSelector<>();
        return selector.selectProduct(specialEntityWeights);
    }
    
    /**
     * Select a random item within the ITEM category.
     * 
     * @param config the mod configuration
     * @param availableItems map of item registry keys to weights
     * @return the selected item, or null if selection fails
     */
    public ItemStack selectItem(ModConfig config, Map<Item, Double> availableItems) {
        
        if (availableItems == null || availableItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No available items provided");
            return null;
        }
        
        CategoryConfig itemConfig = config.getCategory(CategoryType.ITEM);
        if (itemConfig == null || !itemConfig.isEnabled()) {
            AllEggRandomizer.LOGGER.debug("ITEM category is not enabled");
            return null;
        }
        
        ProductSelector<Item> selector = new ProductSelector<>();
        
        Item selectedItem = selector.selectProduct(availableItems);
        if (selectedItem == null) {
            return null;
        }
        
        // Determine quantity based on item category
        // Weapons and equipment: 1
        // Other items: random 1-5
        ItemStack itemStack = new ItemStack(selectedItem);
        
        int quantity;
        if (ItemCategoryClassifier.isSingleQuantity(itemStack)) {
            quantity = 1;
        } else {
            // Get config stack size as max, or use default 1-8
            Number stackSizeObj = itemConfig.getSetting("stackSize", 8);
            int maxQuantity = 8;
            if (stackSizeObj != null) {
                maxQuantity = Math.max(1, stackSizeObj.intValue());
            }
            
            // Random between 1 and maxQuantity using Minecraft's Random.create()
            // This provides high-quality randomness for game use
            quantity = Random.create().nextInt(maxQuantity) + 1;
        }
        AllEggRandomizer.LOGGER.info("Selected item: {} (category: {}, quantity: {})",
            selectedItem, ItemCategoryClassifier.classify(itemStack), quantity);

        ItemStack resultStack = new ItemStack(selectedItem, quantity);
        
        // Bundles should always have quantity of 1 and be populated with random items
        if (BundleItemPopulator.isBundle(resultStack)) {
            resultStack.setCount(1);
            resultStack = BundleItemPopulator.populateBundle(resultStack);
            AllEggRandomizer.LOGGER.info("Bundle item generated with quantity: 1 and populated contents");
        }
        
        // Shulker boxes should always have quantity of 1 and be populated with random items
        if (ShulkerBoxItemPopulator.isShulkerBox(resultStack)) {
            resultStack.setCount(1);
            resultStack = ShulkerBoxItemPopulator.populateShulkerBox(resultStack);
            AllEggRandomizer.LOGGER.info("Shulker box generated with quantity: 1 and populated contents");
        }
        
        // Chests should always have quantity of 1 and be populated with random items
        if (ChestItemPopulator.isChest(resultStack)) {
            resultStack.setCount(1);
            resultStack = ChestItemPopulator.populateChest(resultStack);
            AllEggRandomizer.LOGGER.info("Chest generated with quantity: 1 and populated contents");
        }
        
        // Trap chests should always have quantity of 1 and be populated with random items
        if (ChestItemPopulator.isTrapChest(resultStack)) {
            resultStack.setCount(1);
            resultStack = ChestItemPopulator.populateTrapChest(resultStack);
            AllEggRandomizer.LOGGER.info("Trap chest generated with quantity: 1 and populated contents");
        }
        
        return resultStack;
    }
    
    /**
     * Get the probability distribution for all categories.
     * 
     * @param config the mod configuration
     * @return map of category types to their probabilities
     */
    public Map<CategoryType, Double> getCategoryProbabilities(ModConfig config) {
        Map<CategoryType, CategoryConfig> categoryConfigs = new EnumMap<>(CategoryType.class);
        for (CategoryType type : CategoryType.values()) {
            CategoryConfig catConfig = config.getCategory(type);
            if (catConfig != null) {
                categoryConfigs.put(type, catConfig);
            }
        }
        
        return categorySelector.getProbabilities(categoryConfigs);
    }
    
    /**
     * Get all currently enabled categories.
     * 
     * @param config the mod configuration
     * @return list of enabled category types
     */
    public List<CategoryType> getEnabledCategories(ModConfig config) {
        Map<CategoryType, CategoryConfig> categoryConfigs = new EnumMap<>(CategoryType.class);
        for (CategoryType type : CategoryType.values()) {
            CategoryConfig catConfig = config.getCategory(type);
            if (catConfig != null) {
                categoryConfigs.put(type, catConfig);
            }
        }
        
        return categorySelector.getEnabledCategories(categoryConfigs);
    }
    
    /**
     * Check if any category is enabled.
     * 
     * @param config the mod configuration
     * @return true if at least one category is enabled
     */
    public boolean isAnyCategoryEnabled(ModConfig config) {
        return !getEnabledCategories(config).isEmpty();
    }
    
    /**
     * Set a custom seed provider.
     * 
     * @param seedProvider the new seed provider
     */
    public void setSeedProvider(RandomSeedProvider seedProvider) {
        this.seedProvider = seedProvider;
    }
    
    /**
     * Get the current seed provider.
     * 
     * @return the seed provider
     */
    public RandomSeedProvider getSeedProvider() {
        return seedProvider;
    }
    
    /**
     * Get the category selector.
     * 
     * @return the category selector
     */
    public CategorySelector getCategorySelector() {
        return categorySelector;
    }
}

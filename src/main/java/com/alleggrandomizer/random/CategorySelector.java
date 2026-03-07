package com.alleggrandomizer.random;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Selector for choosing a category based on weights.
 * Only selects from enabled categories.
 * 
 * This implements the first level of two-level random selection:
 * Level 1: Category Selection (this class)
 * Level 2: Product Selection (ProductSelector)
 */
public class CategorySelector {
    
    private final WeightedRandom<CategoryType> weightedRandom;
    
    /**
     * Create a CategorySelector with a specific seed.
     * 
     * @param seed the seed for deterministic results
     */
    public CategorySelector(long seed) {
        this.weightedRandom = new WeightedRandom<>(seed);
    }
    
    /**
     * Create a CategorySelector with a default random.
     */
    public CategorySelector() {
        this.weightedRandom = new WeightedRandom<>();
    }
    
    /**
     * Select a category from the given configurations.
     * Only enabled categories are considered.
     * 
     * @param categoryConfigs map of category types to their configurations
     * @return the selected category type, or null if none enabled
     */
    public CategoryType selectCategory(Map<CategoryType, CategoryConfig> categoryConfigs) {
        if (categoryConfigs == null || categoryConfigs.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No category configurations provided to CategorySelector");
            return null;
        }
        
        // Filter to only enabled categories
        List<WeightedRandom.WeightedItem<CategoryType>> enabledCategories = categoryConfigs.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().isEnabled())
            .map(entry -> new WeightedRandom.WeightedItem<>(
                entry.getKey(),
                entry.getValue().getWeight()
            ))
            .collect(Collectors.toList());
        
        if (enabledCategories.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No enabled categories found in CategorySelector");
            return null;
        }
        
        CategoryType selected = weightedRandom.selectOne(enabledCategories);
        
        AllEggRandomizer.LOGGER.debug("Selected category: {}", selected);
        return selected;
    }
    
    /**
     * Get all enabled categories from the configuration.
     * 
     * @param categoryConfigs map of category types to their configurations
     * @return list of enabled category types
     */
    public List<CategoryType> getEnabledCategories(Map<CategoryType, CategoryConfig> categoryConfigs) {
        if (categoryConfigs == null) {
            return Collections.emptyList();
        }
        
        return categoryConfigs.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().isEnabled())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get the total weight of all enabled categories.
     * 
     * @param categoryConfigs map of category types to their configurations
     * @return total weight of enabled categories
     */
    public double getTotalWeight(Map<CategoryType, CategoryConfig> categoryConfigs) {
        if (categoryConfigs == null) {
            return 0.0;
        }
        
        return categoryConfigs.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null && entry.getValue().isEnabled())
            .mapToDouble(entry -> Math.max(entry.getValue().getWeight(), 0.0))
            .sum();
    }
    
    /**
     * Calculate the probability of each enabled category.
     * 
     * @param categoryConfigs map of category types to their configurations
     * @return map of category types to their probabilities (0.0 - 1.0)
     */
    public Map<CategoryType, Double> getProbabilities(Map<CategoryType, CategoryConfig> categoryConfigs) {
        Map<CategoryType, Double> probabilities = new EnumMap<>(CategoryType.class);
        
        double totalWeight = getTotalWeight(categoryConfigs);
        if (totalWeight <= 0) {
            return probabilities;
        }
        
        for (Map.Entry<CategoryType, CategoryConfig> entry : categoryConfigs.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isEnabled()) {
                double probability = entry.getValue().getWeight() / totalWeight;
                probabilities.put(entry.getKey(), probability);
            }
        }
        
        return probabilities;
    }
    
    /**
     * Set the seed for deterministic category selection.
     * 
     * @param seed the seed
     */
    public void setSeed(long seed) {
        weightedRandom.setSeed(seed);
    }
    
    /**
     * Get the underlying WeightedRandom instance.
     * 
     * @return the weighted random instance
     */
    public WeightedRandom<CategoryType> getWeightedRandom() {
        return weightedRandom;
    }
}

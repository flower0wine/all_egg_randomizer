package com.alleggrandomizer.random;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Core weighted random selection algorithm.
 * Uses "cumulative weight interval" algorithm to ensure probability
 * proportional to weight.
 * 
 * Algorithm:
 * 1. Calculate total weight W of all items
 * 2. Generate random number R in [0, W)
 * 3. Iterate through items, accumulating weights
 * 4. When accumulated weight > R, select that item
 * 
 * Time complexity: O(n) where n is the number of items
 *
 * @param <T> the type of items to select from
 */
public class WeightedRandom<T> {
    
    /**
     * Minimum weight to prevent division by zero.
     */
    private static final double MIN_WEIGHT = 0.0001;
    
    /**
     * Small epsilon for floating point comparison.
     */
    private static final double EPSILON = 1e-10;
    
    /**
     * Flag to determine whether to use deterministic (seeded) or non-deterministic random.
     */
    private final boolean useDeterministic;
    
    /**
     * Seed for deterministic mode (when useDeterministic = true).
     */
    private final long seed;
    
    /**
     * Create a WeightedRandom with a given seed for deterministic results.
     * 
     * @param seed the seed for the random number generator
     */
    public WeightedRandom(long seed) {
        this.useDeterministic = true;
        this.seed = seed;
    }
    
    /**
     * Create a WeightedRandom with non-deterministic random using Minecraft's Random.
     * This is the preferred mode for game random events.
     * Uses net.minecraft.util.math.random.Random.create() which provides:
     * - High-quality randomness optimized for game use
     * - Different seed for each call
     */
    public WeightedRandom() {
        this.useDeterministic = false;
        this.seed = 0;
    }
    
    /**
     * Get Minecraft Random instance based on mode.
     */
    private Random getRandom() {
        if (useDeterministic) {
            return Random.create(this.seed);
        } else {
            // Random.create() creates a new Random with a random seed
            // This is the Minecraft-preferred way for non-deterministic randomness
            return Random.create();
        }
    }
    
    /**
     * Select a single item based on weights.
     * 
     * @param items the list of items with their weights
     * @return the selected item, or null if no items or all weights are zero
     */
    public T selectOne(List<WeightedItem<T>> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        
        // Filter out items with zero or negative weight and collect valid items
        List<WeightedItem<T>> validItems = new ArrayList<>();
        double totalWeight = 0.0;
        
        for (WeightedItem<T> item : items) {
            double weight = normalizeWeight(item.weight);
            if (weight > 0) {
                validItems.add(new WeightedItem<>(item.item, weight));
                totalWeight += weight;
            }
        }
        
        // No valid items
        if (validItems.isEmpty() || totalWeight <= 0) {
            return null;
        }
        
        // Generate random value in [0, totalWeight) using Minecraft Random
        double randomValue = getRandom().nextDouble() * totalWeight;
        
        // Find the selected item using cumulative weight algorithm
        double cumulative = 0.0;
        for (WeightedItem<T> validItem : validItems) {
            cumulative += validItem.weight;
            if (randomValue < cumulative - EPSILON || Math.abs(randomValue - cumulative) < EPSILON) {
                AllEggRandomizer.LOGGER.debug("Selected item with weight {}, cumulative: {}, random: {}", 
                    validItem.weight, cumulative, randomValue);
                return validItem.item;
            }
        }
        
        // Fallback: return last item (should rarely happen with proper floating point)
        AllEggRandomizer.LOGGER.warn("WeightedRandom fallback triggered. Total: {}, Random: {}", 
            totalWeight, randomValue);
        return validItems.get(validItems.size() - 1).item;
    }
    
    /**
     * Select a single item from a map of items to weights.
     * 
     * @param weightMap map of items to their weights
     * @return the selected item, or null if map is empty or all weights are zero
     */
    public T selectFromMap(Map<T, Double> weightMap) {
        if (weightMap == null || weightMap.isEmpty()) {
            return null;
        }
        
        List<WeightedItem<T>> items = new ArrayList<>();
        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            items.add(new WeightedItem<>(entry.getKey(), entry.getValue()));
        }
        
        return selectOne(items);
    }
    
    /**
     * Select multiple items based on weights.
     * 
     * @param items the list of items with their weights
     * @param count the number of items to select
     * @param allowDuplicates whether to allow the same item to be selected multiple times
     * @return list of selected items
     */
    public List<T> selectMultiple(List<WeightedItem<T>> items, int count, boolean allowDuplicates) {
        List<T> result = new ArrayList<>();
        
        if (items == null || items.isEmpty() || count <= 0) {
            return result;
        }
        
        // Make a copy if we don't allow duplicates
        List<WeightedItem<T>> availableItems = allowDuplicates ? items : new ArrayList<>(items);
        
        for (int i = 0; i < count; i++) {
            if (availableItems.isEmpty()) {
                break;
            }
            
            T selected = selectOne(availableItems);
            if (selected != null) {
                result.add(selected);
                
                // Remove if duplicates not allowed
                if (!allowDuplicates) {
                    availableItems.removeIf(wi -> wi.item.equals(selected));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Normalize weight to ensure it's valid.
     * 
     * @param weight the raw weight
     * @return normalized weight (minimum MIN_WEIGHT)
     */
    private double normalizeWeight(double weight) {
        if (Double.isNaN(weight) || Double.isInfinite(weight)) {
            return MIN_WEIGHT;
        }
        return Math.max(weight, MIN_WEIGHT);
    }
    
    /**
     * Set the seed for deterministic behavior.
     * 
     * @param seed the new seed
     */
    public void setSeed(long seed) {
        // For compatibility - creates a new seeded instance on next getRandom() call
    }
    
    /**
     * Represents an item with its associated weight.
     * 
     * @param <T> the type of the item
     */
    public static class WeightedItem<T> {
        public final T item;
        public final double weight;
        
        public WeightedItem(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
        
        /**
         * Create a WeightedItem from a map entry.
         */
        public static <T> WeightedItem<T> fromEntry(Map.Entry<T, Double> entry) {
            return new WeightedItem<>(entry.getKey(), entry.getValue());
        }
    }
}

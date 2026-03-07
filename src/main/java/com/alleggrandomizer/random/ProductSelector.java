package com.alleggrandomizer.random;

import com.alleggrandomizer.AllEggRandomizer;

import java.util.*;

/**
 * Selector for choosing specific products within a category.
 * Uses weighted random selection to pick from available products.
 * 
 * This implements the second level of two-level random selection:
 * Level 1: Category Selection (CategorySelector)
 * Level 2: Product Selection (this class)
 * 
 * @param <T> the type of product (e.g., EntityType, ItemStack, StatusEffect)
 */
public class ProductSelector<T> {
    
    private final WeightedRandom<T> weightedRandom;
    
    /**
     * Create a ProductSelector with a specific seed.
     * 
     * @param seed the seed for deterministic results
     */
    public ProductSelector(long seed) {
        this.weightedRandom = new WeightedRandom<>(seed);
    }
    
    /**
     * Create a ProductSelector with a default random.
     */
    public ProductSelector() {
        this.weightedRandom = new WeightedRandom<>();
    }
    
    /**
     * Select a single product from the given map of products to weights.
     * 
     * @param products map of products to their weights
     * @return the selected product, or null if no valid products
     */
    public T selectProduct(Map<T, Double> products) {
        if (products == null || products.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No products provided to ProductSelector");
            return null;
        }
        
        // Filter out products with zero or negative weight
        Map<T, Double> validProducts = new HashMap<>();
        for (Map.Entry<T, Double> entry : products.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                validProducts.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (validProducts.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid products with positive weight found");
            return null;
        }
        
        T selected = weightedRandom.selectFromMap(validProducts);
        
        AllEggRandomizer.LOGGER.debug("Selected product: {}", selected);
        return selected;
    }
    
    /**
     * Select a single product from a list of weighted items.
     * 
     * @param weightedItems list of products with their weights
     * @return the selected product, or null if no valid products
     */
    public T selectProduct(List<WeightedRandom.WeightedItem<T>> weightedItems) {
        if (weightedItems == null || weightedItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No weighted items provided to ProductSelector");
            return null;
        }
        
        // Filter out invalid items
        List<WeightedRandom.WeightedItem<T>> validItems = new ArrayList<>();
        for (WeightedRandom.WeightedItem<T> item : weightedItems) {
            if (item.item != null && item.weight > 0) {
                validItems.add(item);
            }
        }
        
        if (validItems.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid weighted items found");
            return null;
        }
        
        T selected = weightedRandom.selectOne(validItems);
        
        AllEggRandomizer.LOGGER.debug("Selected product: {}", selected);
        return selected;
    }
    
    /**
     * Select multiple products from the available products.
     * 
     * @param products map of products to their weights
     * @param count number of products to select
     * @param allowDuplicates whether to allow the same product multiple times
     * @return list of selected products
     */
    public List<T> selectMultiple(Map<T, Double> products, int count, boolean allowDuplicates) {
        if (products == null || products.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        
        List<WeightedRandom.WeightedItem<T>> weightedItems = new ArrayList<>();
        for (Map.Entry<T, Double> entry : products.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                weightedItems.add(new WeightedRandom.WeightedItem<>(entry.getKey(), entry.getValue()));
            }
        }
        
        return weightedRandom.selectMultiple(weightedItems, count, allowDuplicates);
    }
    
    /**
     * Get the total weight of all products.
     * 
     * @param products map of products to their weights
     * @return total weight
     */
    public double getTotalWeight(Map<T, Double> products) {
        if (products == null) {
            return 0.0;
        }
        
        return products.entrySet()
            .stream()
            .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue() > 0)
            .mapToDouble(Map.Entry::getValue)
            .sum();
    }
    
    /**
     * Calculate the probability of each product.
     * 
     * @param products map of products to their weights
     * @return map of products to their probabilities (0.0 - 1.0)
     */
    public Map<T, Double> getProbabilities(Map<T, Double> products) {
        Map<T, Double> probabilities = new HashMap<>();
        
        if (products == null || products.isEmpty()) {
            return probabilities;
        }
        
        double totalWeight = getTotalWeight(products);
        if (totalWeight <= 0) {
            return probabilities;
        }
        
        for (Map.Entry<T, Double> entry : products.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                probabilities.put(entry.getKey(), entry.getValue() / totalWeight);
            }
        }
        
        return probabilities;
    }
    
    /**
     * Set the seed for deterministic product selection.
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
    public WeightedRandom<T> getWeightedRandom() {
        return weightedRandom;
    }
}

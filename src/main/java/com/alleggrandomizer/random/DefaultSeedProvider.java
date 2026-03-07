package com.alleggrandomizer.random;

import com.alleggrandomizer.AllEggRandomizer;

/**
 * Default implementation of RandomSeedProvider.
 * Generates seeds using: position coordinates + timestamp + player UUID.
 * 
 * This provides deterministic results when all parameters are the same,
 * which is useful for testing and debugging.
 */
public class DefaultSeedProvider implements RandomSeedProvider {
    
    /**
     * Multiplier for world seed to add more randomness.
     */
    private static final long WORLD_SEED_MULTIPLIER = 31L;
    
    @Override
    public long generateSeed(double x, double y, double z, long worldSeed, String playerId, long timeMillis) {
        // Start with world seed
        long seed = worldSeed;
        
        // Add position components
        seed = seed * WORLD_SEED_MULTIPLIER + (long) x;
        seed = seed * WORLD_SEED_MULTIPLIER + (long) y;
        seed = seed * WORLD_SEED_MULTIPLIER + (long) z;
        
        // Add timestamp for time-based variation
        long timeSeed = timeMillis / 100; // Change every 100ms
        seed = seed * WORLD_SEED_MULTIPLIER + timeSeed;
        
        // Add player UUID if available
        if (playerId != null && !playerId.isEmpty()) {
            int hashCode = playerId.hashCode();
            seed = seed * WORLD_SEED_MULTIPLIER + hashCode;
        }
        
        // Ensure we don't return 0
        if (seed == 0) {
            seed = 1;
        }
        
        AllEggRandomizer.LOGGER.debug("Generated seed: {} for position: ({}, {}, {})", seed, x, y, z);
        
        return seed;
    }
}

package com.alleggrandomizer.random;

/**
 * Interface for providing random seeds based on context.
 * Implementations can use different strategies to generate seeds
 * for deterministic or non-deterministic randomness.
 */
public interface RandomSeedProvider {
    
    /**
     * Generate a seed based on the throw context.
     * 
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param worldSeed the world seed
     * @param playerId the player UUID (as string, can be null)
     * @param timeMillis current time in milliseconds
     * @return a seed value for the random generator
     */
    long generateSeed(double x, double y, double z, long worldSeed, String playerId, long timeMillis);
    
    /**
     * Default implementation that uses position + time + player UUID.
     * This provides semi-deterministic results - same position at same time
     * with same player will produce the same result.
     */
    static RandomSeedProvider createDefault() {
        return new DefaultSeedProvider();
    }
}

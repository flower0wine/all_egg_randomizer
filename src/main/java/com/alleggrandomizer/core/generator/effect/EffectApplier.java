package com.alleggrandomizer.core.generator.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Interface for effect application strategies.
 * Implementations define different ways to apply status effects (splash, direct, etc.).
 *
 * Design pattern: Strategy pattern - each application method is a different strategy.
 */
public interface EffectApplier {

    /**
     * Apply a status effect at the specified position.
     *
     * @param world the server world
     * @param position the position where the effect should be applied
     * @param effect the status effect to apply
     * @param config effect-specific configuration (duration, amplifier, radius, etc.)
     * @return true if the effect was successfully applied, false otherwise
     */
    boolean apply(ServerWorld world, Vec3d position, RegistryEntry<StatusEffect> effect, Map<String, Object> config);

    /**
     * Get the name of this application method.
     * @return application method name (e.g., "SPLASH", "DIRECT")
     */
    String getMethodName();

    /**
     * Get a description of this application method.
     * @return method description
     */
    String getDescription();
}

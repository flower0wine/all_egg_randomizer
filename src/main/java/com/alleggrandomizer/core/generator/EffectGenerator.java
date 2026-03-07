package com.alleggrandomizer.core.generator;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.EffectApplyMode;
import com.alleggrandomizer.config.ModConfig;
import com.alleggrandomizer.core.generator.effect.DirectEffectApplier;
import com.alleggrandomizer.core.generator.effect.EffectApplier;
import com.alleggrandomizer.core.generator.effect.SplashEffectApplier;
import com.alleggrandomizer.random.ProductSelector;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Effect generator for the EFFECT category.
 * Manages effect selection and application based on configuration.
 *
 * Design pattern: Strategy + Factory pattern
 * - Strategy: Different effect appliers (splash, direct)
 * - Factory: Creates appropriate applier based on random selection
 *
 * Current implementation:
 * - Randomly selects from all available status effects using ProductSelector
 * - Randomly selects application mode (SPLASH or DIRECT)
 * - Configurable effect parameters (duration, amplifier, radius, etc.)
 */
public class EffectGenerator {

    // Effect applier registry
    private static final Map<EffectApplyMode, EffectApplier> APPLIER_REGISTRY = new EnumMap<>(EffectApplyMode.class);

    // Initialize default appliers
    static {
        registerApplier(EffectApplyMode.SPLASH, new SplashEffectApplier());
        registerApplier(EffectApplyMode.DIRECT, new DirectEffectApplier());
    }

    /**
     * Register a new effect applier.
     * @param mode the application mode
     * @param applier the applier implementation
     */
    public static void registerApplier(EffectApplyMode mode, EffectApplier applier) {
        if (mode != null && applier != null) {
            APPLIER_REGISTRY.put(mode, applier);
            AllEggRandomizer.LOGGER.debug("Registered effect applier: {} -> {}", mode, applier.getMethodName());
        }
    }

    /**
     * Generate and apply an effect based on configuration.
     *
     * @param world the server world
     * @param position the position where the effect should be applied
     * @param config the mod configuration
     */
    public static void generate(ServerWorld world, Vec3d position, ModConfig config) {
        if (world == null || position == null || config == null) {
            AllEggRandomizer.LOGGER.warn("Cannot generate effect: missing required parameters");
            return;
        }

        // Get EFFECT category config
        CategoryConfig effectConfig = config.getCategory(CategoryType.EFFECT);
        if (effectConfig == null || !effectConfig.isEnabled()) {
            AllEggRandomizer.LOGGER.debug("EFFECT category is not enabled");
            return;
        }

        // Get effect-specific settings
        Map<String, Object> specificSettings = effectConfig.getSpecificSettings();

        // Select a random effect from registry using ProductSelector
        RegistryEntry<StatusEffect> selectedEffect = selectRandomEffect();
        if (selectedEffect == null) {
            AllEggRandomizer.LOGGER.warn("No valid effect selected");
            return;
        }

        // Randomly select application mode (SPLASH or DIRECT) using ProductSelector
        EffectApplyMode applyMode = selectRandomApplyMode();

        // Get the appropriate applier
        EffectApplier applier = APPLIER_REGISTRY.get(applyMode);
        if (applier == null) {
            AllEggRandomizer.LOGGER.warn("No applier found for mode: {}", applyMode);
            return;
        }

        // Apply the effect
        boolean success = applier.apply(world, position, selectedEffect, specificSettings);

        if (success) {
            AllEggRandomizer.LOGGER.info("Effect generated successfully: {} using {} mode at ({}, {}, {})",
                    getEffectName(selectedEffect), applyMode, position.x, position.y, position.z);
        } else {
            AllEggRandomizer.LOGGER.warn("Effect generation failed: {} using {} mode",
                    getEffectName(selectedEffect), applyMode);
        }
    }

    /**
     * Select a random status effect from the registry using ProductSelector.
     * Filters out effects that shouldn't be randomly applied.
     */
    private static RegistryEntry<StatusEffect> selectRandomEffect() {
        // Build effect weights map (equal weight for all valid effects)
        Map<RegistryEntry<StatusEffect>, Double> effectWeights = new HashMap<>();

        // Iterate through all status effects in the registry
        for (StatusEffect effect : Registries.STATUS_EFFECT) {
            RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getEntry(effect);
            // Filter out unwanted effects
            if (isValidEffect(entry)) {
                effectWeights.put(entry, 1.0); // Equal weight for all effects
            }
        }

        if (effectWeights.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No valid effects found in registry");
            return null;
        }

        // Use ProductSelector to select random effect
        ProductSelector<RegistryEntry<StatusEffect>> selector = new ProductSelector<>();
        RegistryEntry<StatusEffect> selected = selector.selectProduct(effectWeights);

        AllEggRandomizer.LOGGER.debug("Selected effect: {} from {} available effects",
                getEffectName(selected), effectWeights.size());

        return selected;
    }

    /**
     * Randomly select an application mode using ProductSelector.
     * Gives equal weight to SPLASH and DIRECT modes.
     */
    private static EffectApplyMode selectRandomApplyMode() {
        // Build mode weights map (equal weight for all modes)
        Map<EffectApplyMode, Double> modeWeights = new HashMap<>();
        for (EffectApplyMode mode : EffectApplyMode.values()) {
            modeWeights.put(mode, 1.0); // Equal weight
        }

        // Use ProductSelector to select random mode
        ProductSelector<EffectApplyMode> selector = new ProductSelector<>();
        EffectApplyMode selected = selector.selectProduct(modeWeights);

        AllEggRandomizer.LOGGER.debug("Randomly selected effect apply mode: {}", selected);
        return selected != null ? selected : EffectApplyMode.SPLASH; // Fallback to SPLASH
    }

    /**
     * Check if an effect is valid for random selection.
     * Filters out effects that might be problematic or undesirable.
     */
    private static boolean isValidEffect(RegistryEntry<StatusEffect> effect) {
        if (effect == null) {
            return false;
        }

        try {
            StatusEffect statusEffect = effect.value();

            // TODO: SPEC-06 - Add filtering logic for specific effects if needed
            // For now, allow all effects
            // Future enhancements:
            // - Filter instant effects (Instant Health, Instant Damage)
            // - Filter effects that only work on specific mobs
            // - Add configurable blacklist/whitelist

            return true;

        } catch (Exception e) {
            AllEggRandomizer.LOGGER.warn("Error checking effect validity", e);
            return false;
        }
    }

    /**
     * Get effect name for logging.
     */
    private static String getEffectName(RegistryEntry<StatusEffect> effect) {
        if (effect == null) {
            return "Unknown";
        }
        try {
            return effect.getKey().map(key -> key.getValue().toString()).orElse("Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Get all registered application modes.
     * @return set of registered modes
     */
    public static Set<EffectApplyMode> getRegisteredModes() {
        return new HashSet<>(APPLIER_REGISTRY.keySet());
    }

    /**
     * Get the number of available effects in the registry.
     * @return count of valid effects
     */
    public static int getAvailableEffectCount() {
        int count = 0;
        for (StatusEffect effect : Registries.STATUS_EFFECT) {
            RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getEntry(effect);
            if (isValidEffect(entry)) {
                count++;
            }
        }
        return count;
    }
}

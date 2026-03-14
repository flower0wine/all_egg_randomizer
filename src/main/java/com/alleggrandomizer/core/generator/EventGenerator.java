package com.alleggrandomizer.core.generator;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.EventTargetPosition;
import com.alleggrandomizer.config.ModConfig;
import com.alleggrandomizer.core.generator.event.*;
import com.alleggrandomizer.random.ProductSelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Event generator for the EVENT category.
 * Manages event selection and execution based on configuration.
 *
 * Design pattern: Registry pattern - maintains a registry of available events.
 *
 * Current implementation:
 * - Supports LIGHTNING, TNT, BAT_WITCH, END_CRYSTAL_ARROW, RAINBOW_SHEEP,
 *   PIG_WITH_SADDLE, REVERSE_RAINBOW_SHEEP, TNT_MINECART, MATH_QUIZ events
 * - Configurable target position (PLAYER or EGG)
 * - Extensible for future event types
 */
public class EventGenerator {

    // Event registry - maps event IDs to event implementations
    private static final Map<String, WorldEvent> EVENT_REGISTRY = new HashMap<>();

    // Initialize default events
    static {
        // Basic events
        registerEvent(new LightningEvent());
        
        // New events
        registerEvent(new TntEvent());
        registerEvent(new BatWitchEvent());
        registerEvent(new EndCrystalArrowEvent());
        registerEvent(new RainbowSheepEvent());
        registerEvent(new PigWithSaddleEvent());
        registerEvent(new ReverseRainbowSheepEvent());
        registerEvent(new TntMinecartEvent());
        registerEvent(new MathQuizEvent());
    }

    /**
     * Register a new event type.
     * @param event the event implementation to register
     */
    public static void registerEvent(WorldEvent event) {
        if (event != null && event.getEventId() != null) {
            EVENT_REGISTRY.put(event.getEventId(), event);
            AllEggRandomizer.LOGGER.debug("Registered event: {}", event.getEventId());
        }
    }

    /**
     * Generate and execute an event based on configuration.
     *
     * @param world the server world
     * @param eggPosition the position where the egg hit
     * @param config the mod configuration
     * @param egg the egg entity (used to get player position)
     */
    public static void generate(ServerWorld world, Vec3d eggPosition, ModConfig config, EggEntity egg) {
        if (world == null || eggPosition == null || config == null) {
            AllEggRandomizer.LOGGER.warn("Cannot generate event: missing required parameters");
            return;
        }

        // Get EVENT category config
        CategoryConfig eventConfig = config.getCategory(CategoryType.EVENT);
        if (eventConfig == null || !eventConfig.isEnabled()) {
            AllEggRandomizer.LOGGER.debug("EVENT category is not enabled");
            return;
        }

        // Get event-specific settings
        Map<String, Object> specificSettings = eventConfig.getSpecificSettings();

        // Determine target position
        Vec3d targetPosition = determineTargetPosition(eggPosition, egg, specificSettings);

        // Get enabled events list
        List<String> enabledEvents = getEnabledEvents(specificSettings);
        if (enabledEvents.isEmpty()) {
            AllEggRandomizer.LOGGER.warn("No events enabled in configuration");
            return;
        }

        // Select a random event
        String selectedEventId = selectRandomEvent(enabledEvents);
        WorldEvent selectedEvent = EVENT_REGISTRY.get(selectedEventId);

        if (selectedEvent == null) {
            AllEggRandomizer.LOGGER.warn("Event not found in registry: {}", selectedEventId);
            return;
        }

        // Get event-specific configuration
        Map<String, Object> eventSpecificConfig = getEventConfig(specificSettings, selectedEventId);

        // Execute the event - use executeWithEgg if the event supports it
        boolean success = selectedEvent.executeWithEgg(world, targetPosition, eventSpecificConfig, egg);

        if (success) {
            AllEggRandomizer.LOGGER.info("Event executed successfully: {} at ({}, {}, {})",
                    selectedEventId, targetPosition.x, targetPosition.y, targetPosition.z);
        } else {
            AllEggRandomizer.LOGGER.warn("Event execution failed: {}", selectedEventId);
        }
    }

    /**
     * Determine the target position based on configuration.
     */
    private static Vec3d determineTargetPosition(Vec3d eggPosition, EggEntity egg, Map<String, Object> settings) {
        // Get target position setting
        String targetPosStr = getStringSetting(settings, "targetPosition", "EGG");
        EventTargetPosition targetPos = EventTargetPosition.fromString(targetPosStr);

        if (targetPos == EventTargetPosition.PLAYER && egg != null) {
            Entity owner = egg.getOwner();
            if (owner != null) {
                return owner.getBlockPos().toCenterPos();
            }
        }

        // Default to egg position
        return eggPosition;
    }

    /**
     * Get the list of enabled events from configuration.
     */
    @SuppressWarnings("unchecked")
    private static List<String> getEnabledEvents(Map<String, Object> settings) {
        if (settings == null || !settings.containsKey("events")) {
            // Default to LIGHTNING if not configured
            return Collections.singletonList("LIGHTNING");
        }

        Object eventsObj = settings.get("events");
        if (eventsObj instanceof List) {
            try {
                return (List<String>) eventsObj;
            } catch (ClassCastException e) {
                AllEggRandomizer.LOGGER.warn("Invalid events configuration format");
            }
        }

        return Collections.singletonList("LIGHTNING");
    }

    /**
     * Select a random event from the enabled events list.
     * Uses ProductSelector for consistent random selection with the rest of the system.
     */
    private static String selectRandomEvent(List<String> enabledEvents) {
        if (enabledEvents.isEmpty()) {
            return "LIGHTNING";
        }

        // Build weight map (equal weight for all enabled events)
        Map<String, Double> eventWeights = new HashMap<>();
        for (String eventId : enabledEvents) {
            eventWeights.put(eventId, 1.0);
        }

        // Use ProductSelector for weighted random selection
        com.alleggrandomizer.random.ProductSelector<String> selector =
            new com.alleggrandomizer.random.ProductSelector<>();

        String selected = selector.selectProduct(eventWeights);
        return selected != null ? selected : "LIGHTNING";
    }

    /**
     * Get event-specific configuration.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getEventConfig(Map<String, Object> settings, String eventId) {
        if (settings == null || !settings.containsKey(eventId.toLowerCase())) {
            return new HashMap<>();
        }

        Object configObj = settings.get(eventId.toLowerCase());
        if (configObj instanceof Map) {
            try {
                return (Map<String, Object>) configObj;
            } catch (ClassCastException e) {
                AllEggRandomizer.LOGGER.warn("Invalid event config format for: {}", eventId);
            }
        }

        return new HashMap<>();
    }

    /**
     * Helper method to safely get string settings.
     */
    private static String getStringSetting(Map<String, Object> settings, String key, String defaultValue) {
        if (settings == null || !settings.containsKey(key)) {
            return defaultValue;
        }

        Object value = settings.get(key);
        if (value instanceof String) {
            return (String) value;
        }

        return defaultValue;
    }

    /**
     * Get all registered event IDs.
     * @return set of registered event IDs
     */
    public static Set<String> getRegisteredEvents() {
        return new HashSet<>(EVENT_REGISTRY.keySet());
    }
}

package com.alleggrandomizer.core.generator;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.generator.event.LightningEvent;
import com.alleggrandomizer.core.generator.event.WorldEvent;

import java.util.*;

/**
 * Registry for all available world events.
 * Manages event registration and provides access to registered events.
 *
 * Design pattern: Registry pattern - centralized management of event types.
 */
public class EventRegistry {

    private static final Map<String, WorldEvent> EVENTS = new HashMap<>();

    static {
        // Register default events
        registerEvent(new LightningEvent());

        AllEggRandomizer.LOGGER.info("EventRegistry initialized with {} events", EVENTS.size());
    }

    /**
     * Register a new event type.
     *
     * @param event the event to register
     * @throws IllegalArgumentException if event with same ID already exists
     */
    public static void registerEvent(WorldEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        String eventId = event.getEventId();
        if (EVENTS.containsKey(eventId)) {
            AllEggRandomizer.LOGGER.warn("Event with ID '{}' is already registered, overwriting", eventId);
        }

        EVENTS.put(eventId, event);
        AllEggRandomizer.LOGGER.debug("Registered event: {} - {}", eventId, event.getDescription());
    }

    /**
     * Get an event by its ID.
     *
     * @param eventId the event ID
     * @return the event, or null if not found
     */
    public static WorldEvent getEvent(String eventId) {
        return EVENTS.get(eventId);
    }

    /**
     * Get all registered events.
     *
     * @return unmodifiable collection of all events
     */
    public static Collection<WorldEvent> getAllEvents() {
        return Collections.unmodifiableCollection(EVENTS.values());
    }

    /**
     * Get all registered event IDs.
     *
     * @return unmodifiable set of event IDs
     */
    public static Set<String> getAllEventIds() {
        return Collections.unmodifiableSet(EVENTS.keySet());
    }

    /**
     * Check if an event is registered.
     *
     * @param eventId the event ID to check
     * @return true if registered, false otherwise
     */
    public static boolean isRegistered(String eventId) {
        return EVENTS.containsKey(eventId);
    }

    /**
     * Get the number of registered events.
     *
     * @return event count
     */
    public static int getEventCount() {
        return EVENTS.size();
    }
}

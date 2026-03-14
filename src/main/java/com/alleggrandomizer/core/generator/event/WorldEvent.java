package com.alleggrandomizer.core.generator.event;

import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Interface for world events that can be triggered by egg hits.
 * Implementations define specific event behaviors (lightning, explosion, etc.).
 *
 * Design pattern: Strategy pattern - each event type is a different strategy.
 */
public interface WorldEvent {

    /**
     * Get the unique identifier for this event type.
     * @return event ID (e.g., "LIGHTNING", "EXPLOSION")
     */
    String getEventId();

    /**
     * Execute the event at the specified position.
     *
     * @param world the server world where the event occurs
     * @param position the position where the event should be triggered
     * @param config event-specific configuration parameters
     * @return true if the event was successfully executed, false otherwise
     */
    boolean execute(ServerWorld world, Vec3d position, Map<String, Object> config);

    /**
     * Execute the event at the specified position with access to the egg entity.
     * This is used for events that need player interaction (like MathQuiz).
     * Default implementation calls execute() for backward compatibility.
     *
     * @param world the server world where the event occurs
     * @param position the position where the event should be triggered
     * @param config event-specific configuration parameters
     * @param egg the egg entity (can be used to get the throwing player)
     * @return true if the event was successfully executed, false otherwise
     */
    default boolean executeWithEgg(ServerWorld world, Vec3d position, Map<String, Object> config, EggEntity egg) {
        return execute(world, position, config);
    }

    /**
     * Get a human-readable description of this event.
     * @return event description
     */
    String getDescription();
}

package com.alleggrandomizer.core;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.generator.event.MathQuizEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles chat events for math quiz functionality.
 * Intercepts player messages to check for quiz answers.
 */
public class ChatEventHandler {

    /**
     * Register chat event handlers.
     */
    public static void register() {
        // Register handler for when a player joins - we'll use message handling
        // Fabric doesn't have a direct chat event, so we use packet handling
        
        AllEggRandomizer.LOGGER.info("Chat event handler registered");
    }

    /**
     * Handle incoming chat message from player.
     * Called by mixin when player sends a chat message.
     * 
     * @param player the player who sent the message
     * @param message the message content
     * @return true if the message was handled by quiz system, false otherwise
     */
    public static boolean onPlayerChat(ServerPlayerEntity player, String message) {
        if (player == null || message == null) {
            return false;
        }
        
        // Check if this is a number (potential quiz answer)
        try {
            Integer.parseInt(message.trim());
            // Let MathQuizEvent handle it
            return MathQuizEvent.handlePlayerChat(player, message);
        } catch (NumberFormatException e) {
            // Not a number, let it pass through
            return false;
        }
    }
}

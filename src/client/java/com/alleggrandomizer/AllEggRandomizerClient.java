package com.alleggrandomizer;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side mod initializer.
 * 
 * Handles client-specific initialization including:
 * - Client-side event handlers
 * 
 * Note: All /allegg commands are handled by the server-side command system,
 * which works in both single-player (integrated server) and multiplayer.
 */
public class AllEggRandomizerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AllEggRandomizer.LOGGER.info("Client-side initialization complete");
    }
}

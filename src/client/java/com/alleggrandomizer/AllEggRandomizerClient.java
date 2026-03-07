package com.alleggrandomizer;

import com.alleggrandomizer.gui.ConfigGuiManager;
import com.alleggrandomizer.network.OpenGuiPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-side mod initializer.
 * 
 * Handles client-specific initialization including:
 * - Network packet receivers for opening GUI
 * - Client-side event handlers
 * 
 * Note: All /allegg commands are handled by the server-side command system,
 * which works in both single-player (integrated server) and multiplayer.
 */
public class AllEggRandomizerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerPacketReceivers();
        AllEggRandomizer.LOGGER.info("Client-side initialization complete");
    }

    /**
     * Register client-side packet receivers.
     */
    private void registerPacketReceivers() {
        // Register handler for opening GUI from server command
        ClientPlayNetworking.registerGlobalReceiver(OpenGuiPayload.ID, (payload, context) -> {
            context.client().execute(ConfigGuiManager::openScreen);
        });
    }
}

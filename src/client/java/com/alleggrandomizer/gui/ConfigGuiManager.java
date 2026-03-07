package com.alleggrandomizer.gui;

import net.minecraft.client.MinecraftClient;

/**
 * GUI Manager for opening and managing the configuration screen.
 * Provides a simple interface to open the config GUI from commands or other sources.
 * 
 * Implementation for SPEC-05: UI Configuration Panel System
 */
public class ConfigGuiManager {

    /**
     * Open the configuration screen.
     * This method should be called from the client side only.
     */
    public static void openScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(() -> {
                client.setScreen(new EggConfigScreen(client.currentScreen));
            });
        }
    }
}

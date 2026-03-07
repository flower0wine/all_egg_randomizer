package com.alleggrandomizer.network;

import com.alleggrandomizer.AllEggRandomizer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;

/**
 * Network handler for client-server communication.
 * Registers custom packets for the mod.
 * 
 * Implementation for SPEC-05: UI Configuration Panel System
 * Enables server commands to trigger client-side GUI opening.
 */
public class NetworkHandler {

    public static final Identifier OPEN_GUI_PACKET_ID = Identifier.of(AllEggRandomizer.MOD_ID, "open_gui");

    /**
     * Register all custom packets.
     * Called during mod initialization.
     */
    public static void registerPackets() {
        // Register the OpenGuiPayload type for both sending and receiving
        PayloadTypeRegistry.playS2C().register(OpenGuiPayload.ID, OpenGuiPayload.CODEC);
        
        AllEggRandomizer.LOGGER.info("Network packets registered");
    }
}

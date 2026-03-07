package com.alleggrandomizer.network;

import com.alleggrandomizer.AllEggRandomizer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Custom payload for opening the configuration GUI on the client.
 * This packet is sent from server to client when /allegg gui is executed.
 * 
 * Implementation for SPEC-05: UI Configuration Panel System
 */
public record OpenGuiPayload() implements CustomPayload {

    public static final CustomPayload.Id<OpenGuiPayload> ID = 
        new CustomPayload.Id<>(Identifier.of(AllEggRandomizer.MOD_ID, "open_gui"));

    public static final PacketCodec<RegistryByteBuf, OpenGuiPayload> CODEC = 
        PacketCodec.unit(new OpenGuiPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

package com.alleggrandomizer.mixin;

import com.alleggrandomizer.AllEggRandomizer;
import com.alleggrandomizer.core.ChatEventHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept chat messages for math quiz functionality.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Intercept chat messages and check if they're quiz answers.
     */
    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        if (player == null || player.getEntityWorld().isClient()) {
            return;
        }

        try {
            String message = packet.chatMessage();
            if (message == null || message.isEmpty()) {
                return;
            }

            // Check if this is a quiz answer
            boolean handled = ChatEventHandler.onPlayerChat(player, message);
            
            // If handled by quiz, cancel the message (don't show to others)
            if (handled) {
                ci.cancel();
            }
        } catch (Exception e) {
            AllEggRandomizer.LOGGER.error("Error processing chat message: {}", e.getMessage(), e);
        }
    }
}

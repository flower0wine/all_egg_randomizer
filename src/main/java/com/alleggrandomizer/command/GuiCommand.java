package com.alleggrandomizer.command;

import com.alleggrandomizer.network.OpenGuiPayload;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * GUI subcommand implementation.
 * Opens the configuration GUI panel for the player.
 * 
 * Implementation for SPEC-03: Command Control System
 * Integration with SPEC-05: UI Configuration Panel System
 * 
 * Sends a packet to the client to open the GUI screen.
 */
public class GuiCommand {

    /**
     * Register the gui subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("gui")
            .executes(GuiCommand::execute);
    }

    /**
     * Execute the gui command.
     * Sends a packet to the client to open the configuration GUI.
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // Check if the command source is a player
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            // Send packet to client to open GUI
            ServerPlayNetworking.send(player, new OpenGuiPayload());
            source.sendFeedback(() -> Text.literal("§a正在打开配置面板..."), false);
            return 1;
        } else {
            source.sendError(Text.literal("§c此命令只能由玩家执行"));
            return 0;
        }
    }
}

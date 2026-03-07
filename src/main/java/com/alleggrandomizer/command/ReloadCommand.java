package com.alleggrandomizer.command;

import com.alleggrandomizer.config.ConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Reload subcommand implementation.
 * Reloads the configuration from file.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class ReloadCommand {

    /**
     * Register the reload subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("reload")
            .requires(source -> {
                // Check if source has operator permissions (level 2 or higher)
                var player = source.getPlayer();
                if (player != null) {
                    return source.getServer().getPlayerManager().isOperator(player.getPlayerConfigEntry());
                }
                // Non-player sources (console, command blocks) are allowed
                return true;
            })
            .executes(ReloadCommand::execute);
    }

    /**
     * Execute the reload command.
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.reloadConfig();
        
        if (success) {
            source.sendFeedback(() -> Text.literal("§a配置已重载"), true);
            return 1;
        } else {
            source.sendError(Text.literal("§c重载配置失败"));
            return 0;
        }
    }
}

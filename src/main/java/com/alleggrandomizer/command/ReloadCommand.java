package com.alleggrandomizer.command;

import com.alleggrandomizer.config.WorldConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Reload subcommand implementation.
 * Reloads the world-specific configuration from persistent storage.
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
     * Note: With the new world-specific config system, configuration is automatically
     * persisted and loaded. This command clears the cache and reloads from disk.
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            // Clear the cached config for this world, forcing a reload on next access
            WorldConfigManager.clearWorldConfig(source.getServer());
            
            // Immediately reload by accessing it again
            var worldConfig = WorldConfigManager.getWorldConfig(source.getServer());
            
            if (worldConfig != null) {
                source.sendFeedback(() -> Text.literal("§a配置已重载"), true);
                return 1;
            } else {
                source.sendError(Text.literal("§c重载配置失败：无法获取世界配置"));
                return 0;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("§c重载配置时发生错误: " + e.getMessage()));
            return 0;
        }
    }
}

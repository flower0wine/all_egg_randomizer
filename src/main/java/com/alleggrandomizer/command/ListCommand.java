package com.alleggrandomizer.command;

import com.alleggrandomizer.config.CategoryConfig;
import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.WorldConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * List subcommand implementation.
 * Displays all categories with their enabled status and weights.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class ListCommand {

    /**
     * Register the list subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("list")
            .executes(ListCommand::execute);
    }

    /**
     * Execute the list command.
     */
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        var worldConfig = WorldConfigManager.getWorldConfig(source.getServer());
        
        if (worldConfig == null) {
            source.sendError(Text.literal("§c无法获取世界配置"));
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("§6=== All Egg Randomizer 配置 ==="), false);
        
        for (CategoryType type : CategoryType.values()) {
            CategoryConfig config = worldConfig.getCategoryConfig(type);
            if (config != null) {
                String displayName = type.getDisplayName();
                String enumName = type.name();
                boolean enabled = config.isEnabled();
                double weight = config.getWeight();
                
                String status = enabled ? "§a启用" : "§c禁用";
                String message = String.format("§e%s §7(%s): %s §7| 权重: §b%.1f", 
                    displayName, enumName, status, weight);
                
                source.sendFeedback(() -> Text.literal(message), false);
            }
        }
        
        return 1;
    }
}

package com.alleggrandomizer.command;

import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.ConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Arrays;

/**
 * Disable subcommand implementation.
 * Disables a specific category.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class DisableCommand {

    private static final SuggestionProvider<ServerCommandSource> CATEGORY_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(
            Arrays.stream(CategoryType.values()).map(Enum::name),
            builder
        );
    };

    /**
     * Register the disable subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("disable")
            .requires(source -> {
                // Check if source has operator permissions (level 2 or higher)
                var player = source.getPlayer();
                if (player != null) {
                    return source.getServer().getPlayerManager().isOperator(player.getPlayerConfigEntry());
                }
                // Non-player sources (console, command blocks) are allowed
                return true;
            })
            .then(CommandManager.argument("category", StringArgumentType.word())
                .suggests(CATEGORY_SUGGESTIONS)
                .executes(DisableCommand::execute)
            );
    }

    /**
     * Execute the disable command.
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String categoryName = StringArgumentType.getString(context, "category");
        
        CategoryType type = CategoryType.fromString(categoryName);
        if (type == null) {
            source.sendError(Text.literal("§c无效的分类名称: " + categoryName));
            source.sendError(Text.literal("§7可用分类: ENTITY, ITEM, EFFECT, EVENT"));
            return 0;
        }
        
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.setCategoryEnabled(type, false);
        
        if (success) {
            String displayName = type.getDisplayName();
            source.sendFeedback(() -> Text.literal("§c已禁用 " + displayName + " (" + type.name() + ")"), true);
            return 1;
        } else {
            source.sendError(Text.literal("§c禁用分类失败"));
            return 0;
        }
    }
}

package com.alleggrandomizer.command;

import com.alleggrandomizer.config.CategoryType;
import com.alleggrandomizer.config.ConfigManager;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
 * SetWeight subcommand implementation.
 * Sets the weight for a specific category.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class SetWeightCommand {

    private static final SuggestionProvider<ServerCommandSource> CATEGORY_SUGGESTIONS = (context, builder) -> {
        return CommandSource.suggestMatching(
            Arrays.stream(CategoryType.values()).map(Enum::name),
            builder
        );
    };

    /**
     * Register the setweight subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("setweight")
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
                .then(CommandManager.argument("weight", DoubleArgumentType.doubleArg(0.0))
                    .executes(SetWeightCommand::execute)
                )
            );
    }

    /**
     * Execute the setweight command.
     */
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String categoryName = StringArgumentType.getString(context, "category");
        double weight = DoubleArgumentType.getDouble(context, "weight");
        
        CategoryType type = CategoryType.fromString(categoryName);
        if (type == null) {
            source.sendError(Text.literal("§c无效的分类名称: " + categoryName));
            source.sendError(Text.literal("§7可用分类: ENTITY, ITEM, EFFECT, EVENT"));
            return 0;
        }
        
        if (weight < 0) {
            source.sendError(Text.literal("§c权重值必须为非负数"));
            return 0;
        }
        
        ConfigManager configManager = ConfigManager.getInstance();
        boolean success = configManager.setCategoryWeight(type, weight);
        
        if (success) {
            String displayName = type.getDisplayName();
            source.sendFeedback(() -> Text.literal(
                String.format("§a已将 %s (%s) 权重设置为 §b%.1f", displayName, type.name(), weight)
            ), true);
            return 1;
        } else {
            source.sendError(Text.literal("§c设置权重失败"));
            return 0;
        }
    }
}

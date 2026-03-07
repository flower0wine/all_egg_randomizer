package com.alleggrandomizer.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Help subcommand implementation.
 * Displays available commands and their usage.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class HelpCommand {

    /**
     * Register the help subcommand.
     */
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("help")
            .executes(HelpCommand::execute);
    }

    /**
     * Execute the help command.
     */
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendFeedback(() -> Text.literal("§6=== All Egg Randomizer 命令帮助 ==="), false);
        source.sendFeedback(() -> Text.literal("§e/allegg help §7- 显示此帮助信息"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg list §7- 列出所有分类及其状态"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg enable <category> §7- 启用指定分类 §c[需要OP]"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg disable <category> §7- 禁用指定分类 §c[需要OP]"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg setweight <category> <weight> §7- 设置分类权重 §c[需要OP]"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg reload §7- 重载配置文件 §c[需要OP]"), false);
        source.sendFeedback(() -> Text.literal("§e/allegg gui §7- 打开配置GUI面板"), false);
        source.sendFeedback(() -> Text.literal("§7可用分类: §aENTITY, ITEM, EFFECT, EVENT"), false);
        
        return 1;
    }
}

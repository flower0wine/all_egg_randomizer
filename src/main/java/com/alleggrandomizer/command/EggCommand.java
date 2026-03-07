package com.alleggrandomizer.command;

import com.alleggrandomizer.AllEggRandomizer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * Main command entry point for /allegg command.
 * Registers all subcommands and provides help information.
 * 
 * Implementation for SPEC-03: Command Control System
 */
public class EggCommand {

    /**
     * Register the main /allegg command and all its subcommands.
     * @param dispatcher the command dispatcher
     * @param dedicated whether this is a dedicated server
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("allegg")
            .executes(EggCommand::executeHelp)
            .then(HelpCommand.register())
            .then(ListCommand.register())
            .then(EnableCommand.register())
            .then(DisableCommand.register())
            .then(SetWeightCommand.register())
            .then(ReloadCommand.register())
            .then(GuiCommand.register())
        );
        
        AllEggRandomizer.LOGGER.info("Registered /allegg command with all subcommands");
    }

    /**
     * Execute help when /allegg is called without arguments.
     */
    private static int executeHelp(CommandContext<ServerCommandSource> context) {
        return HelpCommand.execute(context);
    }
}

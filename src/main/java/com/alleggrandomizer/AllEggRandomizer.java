package com.alleggrandomizer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alleggrandomizer.command.EggCommand;
import com.alleggrandomizer.network.NetworkHandler;

public class AllEggRandomizer implements ModInitializer {
	public static final String MOD_ID = "alleggrandomizer";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Register network packets
		NetworkHandler.registerPackets();

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			EggCommand.register(dispatcher, environment.dedicated);
		});

		LOGGER.info("All Egg Randomizer initialized!");
	}
}

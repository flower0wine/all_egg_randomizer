package com.alleggrandomizer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllEggRandomizer implements ModInitializer {
	public static final String MOD_ID = "alleggrandomizer";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("All Egg Randomizer initialized!");
	}
}

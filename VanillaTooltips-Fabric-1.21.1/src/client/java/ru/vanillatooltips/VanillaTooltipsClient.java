package ru.vanillatooltips;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client-only entry point. Rendering is deliberately limited to item tooltips. */
public final class VanillaTooltipsClient implements ClientModInitializer {
	public static final String MOD_ID = "vanilla_tooltips";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		VanillaTooltipsConfig.load();
		LOGGER.info("Vanilla Tooltips initialized");
	}
}

package ru.vanillatooltips;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client-only entry point. Rendering is deliberately limited to item tooltips. */
public final class VanillaTooltipsClient implements ClientModInitializer {
	public static final String MOD_ID = "vanilla_tooltips";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		VanillaTooltipsConfig.load();
		// Most container screens build item text before handing GuiGraphics a
		// generic component list. Keep this only as a candidate; the renderer
		// validates it against the tooltip that was actually scheduled.
		ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> {
			if (VanillaTooltipsConfig.isEnabled() && !stack.isEmpty() && !lines.isEmpty()) {
				TooltipTracker.offerCandidate(stack);
			}
		});
		LOGGER.info("Vanilla Tooltips initialized");
	}
}

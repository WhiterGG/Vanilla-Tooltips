package ru.vanillatooltips;

import com.mojang.logging.LogUtils;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import ru.vanillatooltips.client.TooltipRenderer;
import ru.vanillatooltips.client.VanillaTooltipsConfigScreen;

/** Client entry point for Vanilla Tooltips on Forge 1.20.1. */
@Mod(VanillaTooltips.MOD_ID)
public final class VanillaTooltips {
	public static final String MOD_ID = "vanilla_tooltips";
	public static final Logger LOGGER = LogUtils.getLogger();

	public VanillaTooltips() {
		VanillaTooltipsConfig.load();
		MinecraftForge.EVENT_BUS.addListener(TooltipRenderer::onTooltipRender);
		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(VanillaTooltipsConfigScreen::create));
		LOGGER.info("Vanilla Tooltips initialized for Forge 1.20.1");
	}
}

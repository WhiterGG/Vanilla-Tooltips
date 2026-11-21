package ru.vanillatooltips;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import ru.vanillatooltips.client.TooltipRenderer;
import ru.vanillatooltips.client.VanillaTooltipsConfigScreen;

/** Client-only entry point for Vanilla Tooltips on NeoForge. */
@Mod(value = VanillaTooltips.MOD_ID, dist = Dist.CLIENT)
public final class VanillaTooltips {
    public static final String MOD_ID = "vanilla_tooltips";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaTooltips(ModContainer container) {
        VanillaTooltipsConfig.load();
        NeoForge.EVENT_BUS.addListener(TooltipRenderer::onTooltipRender);
        IConfigScreenFactory screenFactory = (ignored, parent) -> VanillaTooltipsConfigScreen.create(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, screenFactory);
        LOGGER.info("Vanilla Tooltips initialized for NeoForge 1.21.1");
    }
}

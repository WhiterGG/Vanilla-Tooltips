package ru.vanillatooltips.mixin;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vanillatooltips.ArmorPreviewRenderer;
import ru.vanillatooltips.TooltipGradientRenderer;
import ru.vanillatooltips.TooltipTracker;
import ru.vanillatooltips.TooltipTextLayout;
import ru.vanillatooltips.VanillaTooltipsConfig;
import ru.vanillatooltips.VanillaTooltipsConfig.ArmorPreviewSide;
import ru.vanillatooltips.VanillaTooltipsConfig.ColorMode;
import ru.vanillatooltips.VanillaTooltipsConfig.TooltipSeparationStyle;

/** Replaces only tooltips reached through vanilla's exact ItemStack overload. */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
	@Unique
	private static final int VANILLA_TOOLTIPS_PREVIEW_SIZE = 22;
	@Unique
	private static final int VANILLA_TOOLTIPS_ARMOR_WIDTH = 54;
	@Unique
	private static final int VANILLA_TOOLTIPS_PADDING = 3;

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void vanillaTooltips$beginItemTooltip(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		TooltipTracker.beginItemTooltip(stack);
	}

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
	private void vanillaTooltips$endItemTooltip(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		TooltipTracker.endItemTooltip();
	}

	@Inject(method = "renderTooltipInternal", at = @At("HEAD"), cancellable = true)
	private void vanillaTooltips$renderItemTooltip(Font font, List<ClientTooltipComponent> components,
			int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
		ItemStack item = TooltipTracker.currentItem();
		if (!VanillaTooltipsConfig.isEnabled() || item.isEmpty() || components.isEmpty()) {
			return;
		}
		int titleLineCount = TooltipTextLayout.titleLineCount(item, components);
		if (titleLineCount == 0) {
			return;
		}

		GuiGraphics graphics = (GuiGraphics) (Object) this;
		EquipmentSlot armorSlot = ArmorPreviewRenderer.getArmorSlot(item);
		boolean showArmor = VanillaTooltipsConfig.isShowArmorPreview() && armorSlot != null;
		boolean armorPreviewOnRight = showArmor
				&& VanillaTooltipsConfig.getArmorPreviewSide() == ArmorPreviewSide.RIGHT;
		boolean showItem = VanillaTooltipsConfig.isShowItemPreview()
				&& (!showArmor
						|| VanillaTooltipsConfig.getArmorStandOpacity() == VanillaTooltipsConfig.MIN_ARMOR_STAND_OPACITY
						|| VanillaTooltipsConfig.isShowArmorItemPreview());
		boolean showRarity = VanillaTooltipsConfig.isShowRarity();
		TooltipSeparationStyle separationStyle = VanillaTooltipsConfig.getTooltipSeparationStyle();
		boolean separatePanels = separationStyle == TooltipSeparationStyle.PANELS && showArmor;

		Rarity rarity = item.getRarity();
		MutableComponent rarityLabel = Component.translatable("vanilla_tooltips.rarity." + rarity.getSerializedName());
		if (!VanillaTooltipsConfig.isRarityLabelGradient()) {
			int labelColor = VanillaTooltipsConfig.rarityLabelColor(rarity.name(), vanillaTooltips$rarityColor(rarity));
			rarityLabel = rarityLabel.withStyle(style -> style.withColor(TextColor.fromRgb(labelColor & 0xFFFFFF)));
		}
		int bodyWidth = 0;
		int bodyHeight = 0;
		int titleWidth = 0;
		for (int index = 0; index < components.size(); index++) {
			ClientTooltipComponent component = components.get(index);
			if (index < titleLineCount) {
				titleWidth = Math.max(titleWidth, component.getWidth(font));
			} else {
				bodyWidth = Math.max(bodyWidth, component.getWidth(font));
				bodyHeight += component.getHeight();
			}
		}

		ClientTooltipComponent title = components.getFirst();
		titleWidth = Math.max(titleWidth, showRarity ? font.width(rarityLabel) : 0);
		int itemPreviewWidth = showItem ? VANILLA_TOOLTIPS_PREVIEW_SIZE + 4 : 0;
		int textColumnWidth = Math.max(bodyWidth, titleWidth + itemPreviewWidth);
		int titleExtraHeight = (titleLineCount - 1) * title.getHeight();
		int headerHeight = (showItem || showRarity ? 28 : 16) + titleExtraHeight;
		int textContentHeight = headerHeight + bodyHeight + (bodyHeight > 0 ? 2 : 0);
		int armorPreviewHeight = showArmor ? VanillaTooltipsConfig.getArmorPreviewLayoutHeight() : 0;
		int lineContentGap = separationStyle == TooltipSeparationStyle.LINES && showArmor ? 5 : 0;
		int panelOuterPadding = 4;
		int panelGap = 12;
		int textPanelWidth = textColumnWidth + 8;
		int armorPanelWidth = VANILLA_TOOLTIPS_ARMOR_WIDTH + 8;
		int textPanelHeight = headerHeight + bodyHeight + 8;
		int armorPanelHeight = showArmor ? Math.max(armorPreviewHeight + 8, textPanelHeight) : 0;
		int width = separatePanels
				? panelOuterPadding * 2 + textPanelWidth + (showArmor ? panelGap + armorPanelWidth : 0)
				: (showArmor ? VANILLA_TOOLTIPS_ARMOR_WIDTH : 0) + textColumnWidth + 8 + lineContentGap;
		int height = separatePanels
				? Math.max(textPanelHeight, armorPanelHeight) + panelOuterPadding * 2
				: Math.max(textContentHeight, armorPreviewHeight);
		Component finalRarityLabel = rarityLabel;

		Vector2ic position = positioner.positionTooltip(
				graphics.guiWidth(), graphics.guiHeight(), mouseX, mouseY, width, height);
		int left = position.x();
		int top = position.y();

		graphics.drawManaged(() -> vanillaTooltips$renderTooltip(
				graphics, font, components, item, armorSlot, showArmor, armorPreviewOnRight,
				showItem, showRarity, separatePanels, separationStyle, left, top, width, height,
				textColumnWidth, headerHeight, textContentHeight, armorPreviewHeight,
				textPanelWidth, textPanelHeight, armorPanelWidth, armorPanelHeight, panelOuterPadding,
				panelGap, lineContentGap, rarity, finalRarityLabel));
		ci.cancel();
	}

	@Unique
	private static void vanillaTooltips$renderTooltip(GuiGraphics graphics, Font font,
			List<ClientTooltipComponent> components, ItemStack item, EquipmentSlot armorSlot,
			boolean showArmor, boolean armorPreviewOnRight, boolean showItem, boolean showRarity,
			boolean separatePanels, TooltipSeparationStyle separationStyle, int left, int top,
			int width, int height, int textColumnWidth, int headerHeight, int textContentHeight,
			int armorPreviewHeight, int textPanelWidth, int textPanelHeight,
			int armorPanelWidth, int armorPanelHeight, int panelOuterPadding, int panelGap,
			int lineContentGap,
			Rarity rarity, Component rarityLabel) {
		int textPanelLeft = left + panelOuterPadding
				+ (separatePanels && showArmor && !armorPreviewOnRight ? armorPanelWidth + panelGap : 0);
		int textColumnLeft = separatePanels ? textPanelLeft + 4
				: left + 4 + (showArmor && !armorPreviewOnRight
						? VANILLA_TOOLTIPS_ARMOR_WIDTH + lineContentGap : 0);
		int textPanelTop = top + panelOuterPadding;
		int panelTop = separatePanels ? textPanelTop + 4 : top + VANILLA_TOOLTIPS_PADDING;
		int panelLeft = textColumnLeft;
		int panelRight = panelLeft + VANILLA_TOOLTIPS_PREVIEW_SIZE;
		int panelBottom = panelTop + VANILLA_TOOLTIPS_PREVIEW_SIZE;
		int armorPanelLeft = left + panelOuterPadding
				+ (armorPreviewOnRight ? textPanelWidth + panelGap : 0);
		if (separatePanels) {
			TooltipRenderUtil.renderTooltipBackground(graphics, textPanelLeft, textPanelTop,
					textPanelWidth, textPanelHeight, 400);
			if (showArmor) {
				TooltipRenderUtil.renderTooltipBackground(graphics, armorPanelLeft, textPanelTop,
						armorPanelWidth, armorPanelHeight, 400);
			}
		} else {
			TooltipRenderUtil.renderTooltipBackground(graphics, left, top, width, height, 400);
		}
		int outlineColor = VanillaTooltipsConfig.tooltipOutlineColor(
				item.getRarity().name(), vanillaTooltips$rarityColor(rarity));
		if (VanillaTooltipsConfig.isRarityOutline()) {
			if (separatePanels) {
				vanillaTooltips$renderRarityOutline(graphics, textPanelLeft, textPanelTop,
						textPanelWidth, textPanelHeight, outlineColor, rarity.name());
				if (showArmor) {
					vanillaTooltips$renderRarityOutline(graphics, armorPanelLeft, textPanelTop,
							armorPanelWidth, armorPanelHeight, outlineColor, rarity.name());
				}
			} else {
				vanillaTooltips$renderRarityOutline(graphics, left, top, width, height, outlineColor, rarity.name());
			}
		}
		if (separationStyle == TooltipSeparationStyle.LINES && showArmor) {
			int separatorX = armorPreviewOnRight
					? left + textColumnWidth + 5 + lineContentGap
					: left + VANILLA_TOOLTIPS_ARMOR_WIDTH + 2;
			if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
				TooltipGradientRenderer.fillVertical(separatorX, top - 3, separatorX + 1, top + height + 3,
						progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarity.name(), progress),
						(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, 401, gradientColor));
			} else {
				graphics.fill(separatorX, top - 3, separatorX + 1, top + height + 3, 401, outlineColor);
			}
		}

		graphics.pose().pushPose();
		graphics.pose().translate(0.0F, 0.0F, 400.0F);

		if (showItem) {
			if (VanillaTooltipsConfig.isShowItemPreviewBackground()) {
				if (VanillaTooltipsConfig.isItemPreviewBackgroundGradient()) {
					TooltipGradientRenderer.fillVertical(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1,
							progress -> VanillaTooltipsConfig.itemPreviewBackgroundGradientColor(rarity.name(), progress),
							(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
				} else {
					graphics.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1,
							VanillaTooltipsConfig.resolveItemPreviewBackgroundColor(rarity.name()));
				}
			}
			if (VanillaTooltipsConfig.isShowItemFrame()) {
				if (VanillaTooltipsConfig.isItemFrameGradient()) {
					TooltipGradientRenderer.drawBorderVertical(panelLeft, panelTop, panelRight, panelBottom,
							progress -> VanillaTooltipsConfig.itemFrameGradientColor(rarity.name(), progress),
							(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
				} else {
					int border = VanillaTooltipsConfig.getItemFrameColor(rarity.name());
					graphics.fill(panelLeft + 1, panelTop, panelRight - 1, panelTop + 1, border);
					graphics.fill(panelLeft + 1, panelBottom - 1, panelRight - 1, panelBottom, border);
					graphics.fill(panelLeft, panelTop + 1, panelLeft + 1, panelBottom - 1, border);
					graphics.fill(panelRight - 1, panelTop + 1, panelRight, panelBottom - 1, border);
				}
			}

			TooltipTracker.track(item);
			float itemScale = TooltipTracker.itemAppearanceScale()
					* VanillaTooltipsConfig.getItemPreviewScale() / 100.0F;
			float centerX = panelLeft + VANILLA_TOOLTIPS_PREVIEW_SIZE / 2.0F;
			float centerY = panelTop + VANILLA_TOOLTIPS_PREVIEW_SIZE / 2.0F;
			graphics.enableScissor(panelLeft + 1, panelTop + 1,
					panelLeft + VANILLA_TOOLTIPS_PREVIEW_SIZE - 1,
					panelTop + VANILLA_TOOLTIPS_PREVIEW_SIZE - 1);
			graphics.pose().pushPose();
			graphics.pose().translate(centerX, centerY, 0.0F);
			graphics.pose().scale(itemScale, itemScale, 1.0F);
			graphics.pose().translate(-centerX, -centerY, 0.0F);
			graphics.renderItem(item, panelLeft + 3, panelTop + 3);
			graphics.pose().popPose();
			graphics.disableScissor();
		}

		if (showArmor) {
			int armorViewportHeight = separatePanels ? armorPanelHeight - 8 : armorPreviewHeight - 4;
			int armorPreviewTop = separatePanels ? textPanelTop + 4
					: top + (height - armorViewportHeight) / 2;
			int armorPreviewLeft = separatePanels ? armorPanelLeft + 4
					: armorPreviewOnRight
							? left + textColumnWidth + 7 + lineContentGap : left + 1;
			ArmorPreviewRenderer.render(graphics, item, armorSlot, armorPreviewLeft,
					armorPreviewTop, VANILLA_TOOLTIPS_ARMOR_WIDTH, armorViewportHeight,
					VanillaTooltipsConfig.getArmorStandOpacity());
		}

		int headerX = textColumnLeft + (showItem ? VANILLA_TOOLTIPS_PREVIEW_SIZE + 4 : 0);
		int headerY = separatePanels
				? textPanelTop + (showRarity ? 5 : showItem ? 10 : 2) : top + 6;
		ClientTooltipComponent title = components.getFirst();
		int titleLineCount = TooltipTextLayout.titleLineCount(item, components);
		int titleLineHeight = title.getHeight();
		for (int index = 0; index < titleLineCount; index++) {
			ClientTooltipComponent titleLine = components.get(index);
			int titleLineY = headerY + index * titleLineHeight;
			titleLine.renderText(font, headerX, titleLineY,
					graphics.pose().last().pose(), graphics.bufferSource());
			titleLine.renderImage(font, headerX, titleLineY, graphics);
		}
		int titleExtraHeight = (titleLineCount - 1) * titleLineHeight;
		if (showRarity) {
			int rarityLabelY = (separatePanels ? textPanelTop + 16 : top + 17) + titleExtraHeight;
			if (VanillaTooltipsConfig.isRarityLabelGradient()) {
				TooltipGradientRenderer.drawTextVertical(headerX, rarityLabelY, font.width(rarityLabel), font.lineHeight,
						progress -> VanillaTooltipsConfig.rarityLabelGradientColor(rarity.name(), progress),
						(clipLeft, clipTop, clipRight, clipBottom, draw) -> {
							graphics.flush();
							graphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
							try {
								draw.run();
								graphics.flush();
							} finally {
								graphics.disableScissor();
							}
						}, color -> graphics.drawString(font,
							rarityLabel.copy().withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))),
							headerX, rarityLabelY, 0xFFFFFFFF));
			} else {
				graphics.drawString(font, rarityLabel, headerX, rarityLabelY, 0xFFFFFFFF);
			}
		}

		int textY = separatePanels ? textPanelTop + headerHeight + 2 : top + headerHeight + 2;
		for (int index = titleLineCount; index < components.size(); index++) {
			ClientTooltipComponent component = components.get(index);
			component.renderText(font, textColumnLeft, textY,
					graphics.pose().last().pose(), graphics.bufferSource());
			component.renderImage(font, textColumnLeft, textY, graphics);
			textY += component.getHeight();
		}
		graphics.pose().popPose();
	}

	private static Component vanillaTooltips$rarityLabel(Rarity rarity) {
		ChatFormatting color = rarity == Rarity.COMMON ? ChatFormatting.GRAY : rarity.color();
		return Component.translatable("vanilla_tooltips.rarity." + rarity.getSerializedName()).withStyle(color);
	}

	@Unique
	private static int vanillaTooltips$rarityColor(Rarity rarity) {
		return switch (rarity) {
			case COMMON -> 0xFF90949A;
			case UNCOMMON -> 0xFFFFFF55;
			case RARE -> 0xFF55FFFF;
			case EPIC -> 0xFFFF55FF;
		};
	}

	@Unique
	private static void vanillaTooltips$renderRarityOutline(
			GuiGraphics graphics, int left, int top, int width, int height, int color, String rarity) {
		int outlineLeft = left - 3;
		int outlineTop = top - 3;
		int outlineRight = left + width + 3;
		int outlineBottom = top + height + 3;
		if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
			TooltipGradientRenderer.drawBorderVertical(outlineLeft, outlineTop, outlineRight, outlineBottom,
					progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarity, progress),
					(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, 401, gradientColor));
			return;
		}
		graphics.fill(outlineLeft + 1, outlineTop, outlineRight - 1, outlineTop + 1, 401, color);
		graphics.fill(outlineLeft + 1, outlineBottom - 1, outlineRight - 1, outlineBottom, 401, color);
		graphics.fill(outlineLeft, outlineTop + 1, outlineLeft + 1, outlineBottom - 1, 401, color);
		graphics.fill(outlineRight - 1, outlineTop + 1, outlineRight, outlineBottom - 1, 401, color);
	}
}

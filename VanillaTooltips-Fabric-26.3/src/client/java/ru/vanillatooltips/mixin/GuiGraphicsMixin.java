package ru.vanillatooltips.mixin;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import ru.vanillatooltips.ArmorPreviewRenderer;
import ru.vanillatooltips.TooltipGradientRenderer;
import ru.vanillatooltips.TooltipTracker;
import ru.vanillatooltips.TooltipTextLayout;
import ru.vanillatooltips.VanillaTooltipsConfig;
import ru.vanillatooltips.VanillaTooltipsConfig.ArmorPreviewSide;
import ru.vanillatooltips.VanillaTooltipsConfig.ColorMode;
import ru.vanillatooltips.VanillaTooltipsConfig.TooltipSeparationStyle;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces only item tooltips with a layout that has an internal preview panel. */
@Mixin(GuiGraphicsExtractor.class)
public abstract class GuiGraphicsMixin {
	@Shadow
	private Runnable deferredTooltip;

	@Unique
	private int vanillaTooltips$itemSetupDepth;
	@Unique
	private ItemStack vanillaTooltips$itemCandidate = ItemStack.EMPTY;
	@Unique
	private Runnable vanillaTooltips$previousDeferredTooltip;

	/** Marks the exact vanilla path that is currently preparing an item tooltip. */
	@Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void vanillaTooltips$beginItemTooltip(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		if (VanillaTooltipsConfig.isEnabled()) {
			this.vanillaTooltips$itemCandidate = stack.copy();
			this.vanillaTooltips$itemSetupDepth++;
		}
	}

	@Inject(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
	private void vanillaTooltips$endItemTooltip(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		if (VanillaTooltipsConfig.isEnabled() && this.vanillaTooltips$itemSetupDepth > 0) {
			this.vanillaTooltips$itemSetupDepth--;
			if (this.vanillaTooltips$itemSetupDepth == 0) {
				this.vanillaTooltips$itemCandidate = ItemStack.EMPTY;
			}
		}
	}

	@Inject(method = "setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ZZ)V", at = @At("HEAD"))
	private void vanillaTooltips$beforeTooltipScheduled(Font font, List<ClientTooltipComponent> components,
			int mouseX, int mouseY, ClientTooltipPositioner positioner, Identifier texture, boolean replace, boolean flag,
			CallbackInfo ci) {
		this.vanillaTooltips$previousDeferredTooltip = this.deferredTooltip;
	}

	@Inject(method = "setTooltipForNextFrameInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;ZZ)V", at = @At("RETURN"))
	private void vanillaTooltips$afterTooltipScheduled(Font font, List<ClientTooltipComponent> components,
			int mouseX, int mouseY, ClientTooltipPositioner positioner, Identifier texture, boolean replace, boolean flag,
			CallbackInfo ci) {
		// Associate an ItemStack only when this call actually installed the deferred
		// tooltip. Generic UI tooltips clear the association, while rejected tooltip
		// candidates leave the already scheduled tooltip untouched.
		if (this.deferredTooltip == this.vanillaTooltips$previousDeferredTooltip) {
			return;
		}
		ItemStack callbackCandidate = TooltipTracker.takeCandidate();
		ItemStack scheduledItem = ItemStack.EMPTY;
		if (this.vanillaTooltips$itemSetupDepth > 0 && !this.vanillaTooltips$itemCandidate.isEmpty()) {
			scheduledItem = this.vanillaTooltips$itemCandidate;
		} else if (!callbackCandidate.isEmpty()) {
			scheduledItem = callbackCandidate;
		}

		if (!scheduledItem.isEmpty() && !components.isEmpty()
				&& TooltipTextLayout.titleLineCount(scheduledItem, components) > 0) {
			TooltipTracker.remember(scheduledItem);
		} else {
			TooltipTracker.clearPending();
		}
	}

	@Inject(method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Z)V", at = @At("HEAD"), cancellable = true)
	private void vanillaTooltips$renderItemTooltip(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY,
			ClientTooltipPositioner positioner, Identifier texture, boolean flag, CallbackInfo ci) {
		ItemStack item = TooltipTracker.take();
		if (!VanillaTooltipsConfig.isEnabled() || item.isEmpty() || components.isEmpty()) {
			return;
		}
		int titleLineCount = TooltipTextLayout.titleLineCount(item, components);
		if (titleLineCount == 0) {
			return;
		}

		int bodyWidth = 0;
		int titleWidth = 0;
		int bodyHeight = 0;
		for (int index = 0; index < components.size(); index++) {
			ClientTooltipComponent component = components.get(index);
			if (index < titleLineCount) {
				titleWidth = Math.max(titleWidth, component.getWidth(font));
			} else {
				bodyWidth = Math.max(bodyWidth, component.getWidth(font));
				bodyHeight += component.getHeight(font);
			}
		}

		GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;
		EquipmentSlot armorSlot = ArmorPreviewRenderer.getArmorSlot(item);
		boolean showArmorPreview = VanillaTooltipsConfig.isShowArmorPreview() && armorSlot != null;
		boolean armorPreviewOnRight = showArmorPreview
				&& VanillaTooltipsConfig.getArmorPreviewSide() == ArmorPreviewSide.RIGHT;
		boolean showItemPreview = VanillaTooltipsConfig.isShowItemPreview()
				&& (!showArmorPreview
						|| VanillaTooltipsConfig.getArmorStandOpacity() == VanillaTooltipsConfig.MIN_ARMOR_STAND_OPACITY
						|| VanillaTooltipsConfig.isShowArmorItemPreview());
		boolean showRarity = VanillaTooltipsConfig.isShowRarity();
		TooltipSeparationStyle separationStyle = VanillaTooltipsConfig.getTooltipSeparationStyle();
		boolean separatePanels = separationStyle == TooltipSeparationStyle.PANELS && showArmorPreview;
		int previewSize = 22;
		int armorPreviewWidth = 54;
		int armorPreviewHeight = showArmorPreview
				? VanillaTooltipsConfig.getArmorPreviewLayoutHeight() : 0;
		int titleLineHeight = components.getFirst().getHeight(font);
		int titleExtraHeight = (titleLineCount - 1) * titleLineHeight;
		int headerHeight = (showItemPreview || showRarity ? 28 : 16) + titleExtraHeight;
		String rarityName = item.getRarity().name();
		MutableComponent rarityLabel = Component.translatable("vanilla_tooltips.rarity." + rarityName.toLowerCase());
		if (!VanillaTooltipsConfig.isRarityLabelGradient()) {
			int labelColor = VanillaTooltipsConfig.rarityLabelColor(rarityName, vanillaTooltips$rarityColor(item));
			rarityLabel = rarityLabel.withStyle(style -> style.withColor(TextColor.fromRgb(labelColor & 0xFFFFFF)));
		}
		titleWidth = Math.max(titleWidth, showRarity ? font.width(rarityLabel) : 0);
		int itemPreviewWidth = showItemPreview ? previewSize + 4 : 0;
		int textColumnWidth = Math.max(bodyWidth, titleWidth + itemPreviewWidth);
		int lineContentGap = separationStyle == TooltipSeparationStyle.LINES && showArmorPreview ? 5 : 0;
		int panelOuterPadding = 4;
		int panelGap = 12;
		int textPanelWidth = textColumnWidth + 8;
		int armorPanelWidth = armorPreviewWidth + 8;
		int textPanelHeight = headerHeight + bodyHeight + 8;
		int armorPanelHeight = showArmorPreview ? Math.max(armorPreviewHeight + 8, textPanelHeight) : 0;
		int width = separatePanels
				? panelOuterPadding * 2 + textPanelWidth + (showArmorPreview ? panelGap + armorPanelWidth : 0)
				: (showArmorPreview ? armorPreviewWidth : 0) + textColumnWidth + 8 + lineContentGap;
		int textContentHeight = headerHeight + bodyHeight + (bodyHeight > 0 ? 2 : 0);
		int height = separatePanels
				? Math.max(textPanelHeight, armorPanelHeight) + panelOuterPadding * 2
				: Math.max(textContentHeight, armorPreviewHeight);
		Vector2ic position = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), mouseX, mouseY, width, height);
		int left = position.x();
		int top = position.y();

		int textPanelLeft = left + panelOuterPadding
				+ (separatePanels && showArmorPreview && !armorPreviewOnRight ? armorPanelWidth + panelGap : 0);
		int textColumnLeft = separatePanels ? textPanelLeft + 4
				: left + 4 + (showArmorPreview && !armorPreviewOnRight ? armorPreviewWidth + lineContentGap : 0);
		int panelLeft = textColumnLeft;
		int textPanelTop = top + panelOuterPadding;
		int panelTop = separatePanels ? textPanelTop + 4 : top + 3;
		int panelRight = panelLeft + previewSize;
		int panelBottom = panelTop + previewSize;
		int armorPanelLeft = left + panelOuterPadding
				+ (armorPreviewOnRight ? textPanelWidth + panelGap : 0);
		if (separatePanels) {
			net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.extractTooltipBackground(
					graphics, textPanelLeft, textPanelTop, textPanelWidth, textPanelHeight, texture);
			if (showArmorPreview) {
				net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.extractTooltipBackground(
						graphics, armorPanelLeft, textPanelTop, armorPanelWidth, armorPanelHeight, texture);
			}
		} else {
			net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil.extractTooltipBackground(
					graphics, left, top, width, height, texture);
		}
		int outlineColor = VanillaTooltipsConfig.tooltipOutlineColor(
				item.getRarity().name(), vanillaTooltips$rarityColor(item));
		if (VanillaTooltipsConfig.isRarityOutline()) {
			if (separatePanels) {
				vanillaTooltips$renderPanelRarityOutline(
						graphics, textPanelLeft, textPanelTop, textPanelWidth, textPanelHeight, outlineColor, rarityName);
				if (showArmorPreview) {
					vanillaTooltips$renderPanelRarityOutline(
						graphics, armorPanelLeft, textPanelTop, armorPanelWidth, armorPanelHeight, outlineColor, rarityName);
				}
			} else {
				vanillaTooltips$renderRarityOutline(graphics, left, top, width, height, outlineColor, rarityName);
			}
		}
		if (separationStyle == TooltipSeparationStyle.LINES && showArmorPreview) {
			int outlineTop = top - 3;
			int outlineBottom = top + height + 3;
			int separatorX = armorPreviewOnRight
					? left + textColumnWidth + 5 + lineContentGap
					: left + armorPreviewWidth + 2;
			if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
				TooltipGradientRenderer.fillVertical(separatorX, outlineTop, separatorX + 1, outlineBottom,
						progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarityName, progress),
						(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
			} else {
				graphics.fill(separatorX, outlineTop, separatorX + 1, outlineBottom, outlineColor);
			}
		}

		if (showItemPreview) {
			if (VanillaTooltipsConfig.isShowItemPreviewBackground()) {
				if (VanillaTooltipsConfig.isItemPreviewBackgroundGradient()) {
					TooltipGradientRenderer.fillVertical(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1,
							progress -> VanillaTooltipsConfig.itemPreviewBackgroundGradientColor(rarityName, progress),
							(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
				} else {
					graphics.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1,
							VanillaTooltipsConfig.resolveItemPreviewBackgroundColor(item.getRarity().name()));
				}
			}
			if (VanillaTooltipsConfig.isShowItemFrame()) {
				if (VanillaTooltipsConfig.isItemFrameGradient()) {
					TooltipGradientRenderer.drawBorderVertical(panelLeft, panelTop, panelRight, panelBottom,
							progress -> VanillaTooltipsConfig.itemFrameGradientColor(rarityName, progress),
							(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
				} else {
					int border = VanillaTooltipsConfig.getItemFrameColor(item.getRarity().name());
					graphics.fill(panelLeft + 1, panelTop, panelRight - 1, panelTop + 1, border);
					graphics.fill(panelLeft + 1, panelBottom - 1, panelRight - 1, panelBottom, border);
					graphics.fill(panelLeft, panelTop + 1, panelLeft + 1, panelBottom - 1, border);
					graphics.fill(panelRight - 1, panelTop + 1, panelRight, panelBottom - 1, border);
				}
			}
			float itemScale = TooltipTracker.itemAppearanceScale()
					* VanillaTooltipsConfig.getItemPreviewScale() / 100.0F;
			float itemCenterX = panelLeft + 11.0F;
			float itemCenterY = panelTop + 11.0F;
			graphics.enableScissor(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1);
			graphics.pose().pushMatrix();
			graphics.pose().translateLocal(-itemCenterX, -itemCenterY);
			graphics.pose().scaleLocal(itemScale, itemScale);
			graphics.pose().translateLocal(itemCenterX, itemCenterY);
			graphics.item(item, panelLeft + 3, panelTop + 3);
			graphics.pose().popMatrix();
			graphics.disableScissor();
		}
		if (showArmorPreview) {
			int armorViewportHeight = separatePanels ? armorPanelHeight - 8 : armorPreviewHeight - 4;
			int armorViewportTop = separatePanels ? textPanelTop + 4
					: top + (height - armorViewportHeight) / 2;
			int armorPreviewLeft = separatePanels ? armorPanelLeft + 4
					: armorPreviewOnRight ? left + textColumnWidth + 7 + lineContentGap : left + 1;
			ArmorPreviewRenderer.render(graphics, item, armorSlot, armorPreviewLeft, armorViewportTop,
					armorPreviewWidth, armorViewportHeight,
					VanillaTooltipsConfig.getArmorStandOpacity());
		}

		int headerX = showItemPreview ? panelRight + 4 : textColumnLeft;
		int headerTextY = separatePanels
				? textPanelTop + (showRarity ? 5 : showItemPreview ? 10 : 2)
				: top + 6;
		ClientTooltipComponent title = components.getFirst();
		for (int index = 0; index < titleLineCount; index++) {
			ClientTooltipComponent titleLine = components.get(index);
			int titleLineY = headerTextY + index * titleLineHeight;
			titleLine.extractText(graphics, font, headerX, titleLineY);
			titleLine.extractImage(font, headerX, titleLineY, width, height, graphics);
		}
		if (showRarity) {
			int rarityLabelY = (separatePanels ? textPanelTop + 16 : top + 17) + titleExtraHeight;
			if (VanillaTooltipsConfig.isRarityLabelGradient()) {
				Component gradientLabel = rarityLabel;
				TooltipGradientRenderer.drawTextVertical(headerX, rarityLabelY, font.width(rarityLabel), font.lineHeight,
						progress -> VanillaTooltipsConfig.rarityLabelGradientColor(rarityName, progress),
						(clipLeft, clipTop, clipRight, clipBottom, draw) -> {
							graphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
							try { draw.run(); } finally { graphics.disableScissor(); }
						}, color -> graphics.text(font,
							gradientLabel.copy().withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))),
							headerX, rarityLabelY, 0xFFFFFFFF));
			} else {
				graphics.text(font, rarityLabel, headerX, rarityLabelY, 0xFFFFFFFF);
			}
		}

		int textX = textColumnLeft;
		int textY = separatePanels ? textPanelTop + headerHeight + 2 : top + headerHeight + 2;
		for (int index = titleLineCount; index < components.size(); index++) {
			ClientTooltipComponent component = components.get(index);
			component.extractText(graphics, font, textX, textY);
			component.extractImage(font, textX, textY, width, height, graphics);
			textY += component.getHeight(font);
		}
		ci.cancel();
	}

	private static int vanillaTooltips$rarityColor(ItemStack item) {
		return switch (item.getRarity()) {
			case COMMON -> 0xFF90949A;
			case UNCOMMON -> 0xFFFFFF55;
			case RARE -> 0xFF55FFFF;
			case EPIC -> 0xFFFF55FF;
		};
	}

	private static void vanillaTooltips$renderRarityOutline(
			GuiGraphicsExtractor graphics, int left, int top, int width, int height, int color, String rarity) {
		int outlineLeft = left - 3;
		int outlineTop = top - 3;
		int outlineRight = left + width + 3;
		int outlineBottom = top + height + 3;
		if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
			TooltipGradientRenderer.drawBorderVertical(outlineLeft, outlineTop, outlineRight, outlineBottom,
					progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarity, progress),
						(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
			return;
		}
		// Match the one-pixel cut corners used by the preview panel.
		graphics.fill(outlineLeft + 1, outlineTop, outlineRight - 1, outlineTop + 1, color);
		graphics.fill(outlineLeft + 1, outlineBottom - 1, outlineRight - 1, outlineBottom, color);
		graphics.fill(outlineLeft, outlineTop + 1, outlineLeft + 1, outlineBottom - 1, color);
		graphics.fill(outlineRight - 1, outlineTop + 1, outlineRight, outlineBottom - 1, color);
	}

	private static void vanillaTooltips$renderPanelRarityOutline(
			GuiGraphicsExtractor graphics, int left, int top, int width, int height, int color, String rarity) {
		int outlineLeft = left - 3;
		int outlineTop = top - 3;
		int outlineRight = left + width + 3;
		int outlineBottom = top + height + 3;
		if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
			TooltipGradientRenderer.drawBorderVertical(outlineLeft, outlineTop, outlineRight, outlineBottom,
					progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarity, progress),
					(x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, gradientColor));
			return;
		}
		graphics.fill(outlineLeft + 1, outlineTop, outlineRight - 1, outlineTop + 1, color);
		graphics.fill(outlineLeft + 1, outlineBottom - 1, outlineRight - 1, outlineBottom, color);
		graphics.fill(outlineLeft, outlineTop + 1, outlineLeft + 1, outlineBottom - 1, color);
		graphics.fill(outlineRight - 1, outlineTop + 1, outlineRight, outlineBottom - 1, color);
	}

}

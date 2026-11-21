package ru.vanillatooltips.client;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.joml.Vector2ic;
import ru.vanillatooltips.TooltipTextLayout;
import ru.vanillatooltips.TooltipGradientRenderer;
import ru.vanillatooltips.VanillaTooltipsConfig;
import ru.vanillatooltips.VanillaTooltipsConfig.ArmorPreviewSide;
import ru.vanillatooltips.VanillaTooltipsConfig.ColorMode;
import ru.vanillatooltips.VanillaTooltipsConfig.TooltipSeparationStyle;

/** Replaces only tooltips that Forge associates with a real item stack. */
public final class TooltipRenderer {
    private static final int PREVIEW_SIZE = 22;
    private static final int ARMOR_PREVIEW_WIDTH = 54;
    private static final int VERTICAL_PADDING = 3;
    private static ItemStack animatedItem = ItemStack.EMPTY;
    private static long animationStartedNanos;
    private static long lastTooltipNanos;

    private TooltipRenderer() {}

    public static void onTooltipRender(RenderTooltipEvent.Pre event) {
        ItemStack item = event.getItemStack();
        List<ClientTooltipComponent> components = event.getComponents();
        if (!VanillaTooltipsConfig.isEnabled() || item.isEmpty() || components.isEmpty()) return;

        EquipmentSlot armorSlot = ArmorPreviewRenderer.getArmorSlot(item);
        boolean showArmorPreview = VanillaTooltipsConfig.isShowArmorPreview() && armorSlot != null;
        boolean armorPreviewOnRight = showArmorPreview
                && VanillaTooltipsConfig.getArmorPreviewSide() == ArmorPreviewSide.RIGHT;
        boolean showItemPreview = VanillaTooltipsConfig.isShowItemPreview()
                && (!showArmorPreview
                        || VanillaTooltipsConfig.getArmorStandOpacity() == VanillaTooltipsConfig.MIN_ARMOR_STAND_OPACITY
                        || VanillaTooltipsConfig.isShowArmorItemPreview());
        boolean showRarity = VanillaTooltipsConfig.isShowRarity();
        boolean showOutline = VanillaTooltipsConfig.isRarityOutline();
        if (!showArmorPreview && !showItemPreview && !showRarity && !showOutline) return;

        event.setCanceled(true);
        render(event, item, components, armorSlot, showArmorPreview, armorPreviewOnRight,
                showItemPreview, showRarity, showOutline);
    }

    private static void render(RenderTooltipEvent.Pre event, ItemStack item,
            List<ClientTooltipComponent> components, EquipmentSlot armorSlot,
            boolean showArmorPreview, boolean armorPreviewOnRight, boolean showItemPreview,
            boolean showRarity, boolean showOutline) {
        GuiGraphics graphics = event.getGraphics();
        Font font = event.getFont();
        ClientTooltipComponent title = components.get(0);
        int titleLineCount = TooltipTextLayout.titleLineCount(item, components);
        if (titleLineCount == 0) titleLineCount = 1;
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

        Rarity rarity = item.getRarity();
        MutableComponent rarityLabel = rarityLabel(rarity);
        if (!VanillaTooltipsConfig.isRarityLabelGradient()) {
            int labelColor = VanillaTooltipsConfig.rarityLabelColor(rarity.name(), rarityColor(rarity));
            rarityLabel = rarityLabel.withStyle(style -> style.withColor(TextColor.fromRgb(labelColor & 0xFFFFFF)));
        }
        titleWidth = Math.max(titleWidth, showRarity ? font.width(rarityLabel) : 0);
        int itemPreviewWidth = showItemPreview ? PREVIEW_SIZE + 4 : 0;
        int textColumnWidth = Math.max(bodyWidth, titleWidth + itemPreviewWidth);
        int titleLineHeight = title.getHeight();
        int titleExtraHeight = (titleLineCount - 1) * titleLineHeight;
        int headerHeight = (showItemPreview || showRarity ? 28 : 16) + titleExtraHeight;
        int textContentHeight = headerHeight + bodyHeight + (bodyHeight > 0 ? 2 : 0);
        int armorPreviewHeight = showArmorPreview ? VanillaTooltipsConfig.getArmorPreviewLayoutHeight() : 0;
        TooltipSeparationStyle separationStyle = VanillaTooltipsConfig.getTooltipSeparationStyle();
        boolean separatePanels = separationStyle == TooltipSeparationStyle.PANELS && showArmorPreview;
        int lineContentGap = separationStyle == TooltipSeparationStyle.LINES && showArmorPreview ? 5 : 0;
        int panelOuterPadding = 4;
        int panelGap = 12;
        int textPanelWidth = textColumnWidth + 8;
        int armorPanelWidth = ARMOR_PREVIEW_WIDTH + 8;
        int textPanelHeight = headerHeight + bodyHeight + 8;
        int armorPanelHeight = showArmorPreview ? Math.max(armorPreviewHeight + 8, textPanelHeight) : 0;
        int width = separatePanels
                ? panelOuterPadding * 2 + textPanelWidth + (showArmorPreview ? panelGap + armorPanelWidth : 0)
                : (showArmorPreview ? ARMOR_PREVIEW_WIDTH : 0) + textColumnWidth + 8 + lineContentGap;
        int height = separatePanels
                ? Math.max(textPanelHeight, armorPanelHeight) + panelOuterPadding * 2
                : Math.max(textContentHeight, armorPreviewHeight);
        Vector2ic position = event.getTooltipPositioner().positionTooltip(
                event.getScreenWidth(), event.getScreenHeight(), event.getX(), event.getY(), width, height);
        int left = position.x();
        int top = position.y();

        int textPanelLeft = left + panelOuterPadding
                + (separatePanels && showArmorPreview && !armorPreviewOnRight ? armorPanelWidth + panelGap : 0);
        int textColumnLeft = separatePanels ? textPanelLeft + 4
                : left + 4 + (showArmorPreview && !armorPreviewOnRight ? ARMOR_PREVIEW_WIDTH + lineContentGap : 0);
        int textPanelTop = top + panelOuterPadding;
        int panelTop = separatePanels ? textPanelTop + 4 : top + VERTICAL_PADDING;
        int panelLeft = textColumnLeft;
        int panelRight = panelLeft + PREVIEW_SIZE;
        int panelBottom = panelTop + PREVIEW_SIZE;
        int armorPanelLeft = left + panelOuterPadding
                + (armorPreviewOnRight ? textPanelWidth + panelGap : 0);

        graphics.pose().pushPose();
        if (separatePanels) {
            graphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(
                    graphics, textPanelLeft, textPanelTop, textPanelWidth, textPanelHeight, 400));
            if (showArmorPreview) {
                graphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(
                        graphics, armorPanelLeft, textPanelTop, armorPanelWidth, armorPanelHeight, 400));
            }
        } else {
            graphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(
                    graphics, left, top, width, height, 400));
        }
        int outlineColor = VanillaTooltipsConfig.tooltipOutlineColor(rarity.name(), rarityColor(rarity));
        if (showOutline) {
            if (separatePanels) {
                renderRarityOutline(graphics, textPanelLeft, textPanelTop, textPanelWidth, textPanelHeight, outlineColor, rarity.name());
                if (showArmorPreview) {
                    renderRarityOutline(graphics, armorPanelLeft, textPanelTop, armorPanelWidth, armorPanelHeight, outlineColor, rarity.name());
                }
            } else {
                renderRarityOutline(graphics, left, top, width, height, outlineColor, rarity.name());
            }
        }
        if (separationStyle == TooltipSeparationStyle.LINES && showArmorPreview) {
            int separatorX = armorPreviewOnRight
                    ? left + textColumnWidth + 5 + lineContentGap
                    : left + ARMOR_PREVIEW_WIDTH + 2;
            if (VanillaTooltipsConfig.isTooltipOutlineGradient()) {
                TooltipGradientRenderer.fillVertical(separatorX, top - 3, separatorX + 1, top + height + 3,
                        progress -> VanillaTooltipsConfig.tooltipOutlineGradientColor(rarity.name(), progress),
                        (x1, y1, x2, y2, gradientColor) -> graphics.fill(x1, y1, x2, y2, 401, gradientColor));
            } else {
                graphics.fill(separatorX, top - 3, separatorX + 1, top + height + 3, 401, outlineColor);
            }
        }
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        if (showItemPreview) {
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
            renderAnimatedItem(graphics, item, panelLeft + 3, panelTop + 3);
        }
        if (showArmorPreview) {
            int armorViewportHeight = separatePanels ? armorPanelHeight - 8 : armorPreviewHeight - 4;
            int armorPreviewTop = separatePanels ? textPanelTop + 4
                    : top + (height - armorViewportHeight) / 2;
            int armorPreviewLeft = separatePanels ? armorPanelLeft + 4
                    : armorPreviewOnRight ? left + textColumnWidth + 7 + lineContentGap : left + 1;
            ArmorPreviewRenderer.render(graphics, item, armorSlot, armorPreviewLeft, armorPreviewTop,
                    ARMOR_PREVIEW_WIDTH, armorViewportHeight, VanillaTooltipsConfig.getArmorStandOpacity());
        }

        int headerX = showItemPreview ? panelRight + 4 : textColumnLeft;
        int headerY = separatePanels
                ? textPanelTop + (showRarity ? 5 : showItemPreview ? 10 : 2) : top + 6;
        for (int index = 0; index < titleLineCount; index++) {
            ClientTooltipComponent titleLine = components.get(index);
            int titleLineY = headerY + index * titleLineHeight;
            titleLine.renderText(font, headerX, titleLineY,
                    graphics.pose().last().pose(), graphics.bufferSource());
            titleLine.renderImage(font, headerX, titleLineY, graphics);
        }
        if (showRarity) {
            int rarityLabelY = (separatePanels ? textPanelTop + 16 : top + 17) + titleExtraHeight;
            if (VanillaTooltipsConfig.isRarityLabelGradient()) {
                Component gradientLabel = rarityLabel;
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
                                gradientLabel.copy().withStyle(style -> style.withColor(TextColor.fromRgb(color & 0xFFFFFF))),
                                headerX, rarityLabelY, 0xFFFFFFFF, true));
            } else {
                graphics.drawString(font, rarityLabel, headerX, rarityLabelY, 0xFFFFFFFF, true);
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

    private static void renderAnimatedItem(GuiGraphics graphics, ItemStack item, int x, int y) {
        float scale = itemAppearanceScale(item)
                * VanillaTooltipsConfig.getItemPreviewScale() / 100.0F;
        float centerX = x + 8.0F;
        float centerY = y + 8.0F;
        graphics.enableScissor(x - 2, y - 2, x + PREVIEW_SIZE - 4, y + PREVIEW_SIZE - 4);
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-centerX, -centerY, 0.0F);
        graphics.renderItem(item, x, y);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private static float itemAppearanceScale(ItemStack item) {
        if (!VanillaTooltipsConfig.isAnimateItemPreview()) return 1.0F;
        long now = System.nanoTime();
        boolean tooltipWasGone = lastTooltipNanos == 0L || now - lastTooltipNanos > 120_000_000L;
        if (tooltipWasGone || animatedItem.isEmpty()
                || !ItemStack.isSameItemSameTags(animatedItem, item)) {
            animatedItem = item.copy();
            animationStartedNanos = now;
        }
        lastTooltipNanos = now;
        float progress = Math.min(1.0F, (now - animationStartedNanos)
                / (VanillaTooltipsConfig.getAnimationDurationMs() * 1_000_000.0F));
        float shifted = progress - 1.0F;
        float eased = 1.0F + 2.70158F * shifted * shifted * shifted
                + 1.70158F * shifted * shifted;
        return 0.65F + 0.35F * eased;
    }

    private static MutableComponent rarityLabel(Rarity rarity) {
        String name = rarity.name().toLowerCase(Locale.ROOT);
        String fallback = name.isEmpty() ? "Common"
                : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        MutableComponent label = Component.translatableWithFallback(
                "vanilla_tooltips.rarity." + name, fallback);
        return rarity == Rarity.COMMON
                ? label.withStyle(ChatFormatting.GRAY)
                : label.withStyle(rarity.getStyleModifier());
    }

    private static int rarityColor(Rarity rarity) {
        if (rarity == Rarity.COMMON) return 0xFF90949A;
        Style style = rarity.getStyleModifier().apply(Style.EMPTY);
        return style.getColor() == null ? 0xFF90949A : 0xFF000000 | style.getColor().getValue();
    }

    private static void renderRarityOutline(GuiGraphics graphics, int left, int top,
            int width, int height, int color, String rarity) {
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

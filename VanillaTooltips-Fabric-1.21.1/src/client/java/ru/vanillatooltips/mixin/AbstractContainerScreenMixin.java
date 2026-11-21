package ru.vanillatooltips.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vanillatooltips.TooltipTracker;

/** Supplies the exact hovered stack before Minecraft converts it to tooltip text. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
	@Shadow
	protected Slot hoveredSlot;

	@Shadow
	@Final
	protected AbstractContainerMenu menu;

	@Inject(method = "renderTooltip", at = @At("HEAD"))
	private void vanillaTooltips$beginContainerItemTooltip(
			GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
		ItemStack stack = ItemStack.EMPTY;
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			stack = this.hoveredSlot.getItem();
		}
		TooltipTracker.beginItemTooltip(stack);
	}

	@Inject(method = "renderTooltip", at = @At("RETURN"))
	private void vanillaTooltips$endContainerItemTooltip(
			GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
		TooltipTracker.endItemTooltip();
	}
}

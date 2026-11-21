package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Passes a click outside YACL's color picker through to the underlying settings screen. */
@Mixin(value = PopupControllerScreen.class, remap = false)
public abstract class YACLPopupControllerScreenMixin {
	@Shadow
	@Final
	private YACLScreen backgroundYaclScreen;

	@Inject(method = "mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z", at = @At("RETURN"), cancellable = true, remap = true)
	private void vanillaTooltips$forwardClickOutsidePopup(MouseButtonEvent event, boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir) {
		if (Boolean.FALSE.equals(cir.getReturnValue())) {
			cir.setReturnValue(backgroundYaclScreen.mouseClicked(event, doubleClick));
		}
	}
}
package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.OptionListWidget;
import dev.isxander.yacl3.gui.controllers.ColorController;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets YACL's color reset button receive clicks before its greedy text field. */
@Mixin(value = OptionListWidget.OptionEntry.class, remap = false)
public abstract class YACLColorResetPriorityMixin {
	@Shadow
	@Final
	public AbstractWidget widget;

	@Inject(method = "children", at = @At("RETURN"), cancellable = true, remap = true)
	private void vanillaTooltips$prioritizeColorResetButton(
			CallbackInfoReturnable<List<? extends GuiEventListener>> cir) {
		if (!(this.widget instanceof ColorController.ColorControllerElement)) {
			return;
		}

		List<? extends GuiEventListener> children = cir.getReturnValue();
		if (children.size() == 2 && children.get(0) == this.widget) {
			cir.setReturnValue(List.of(children.get(1), children.get(0)));
		}
	}
}

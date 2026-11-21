package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.utils.OptionUtils;
import dev.isxander.yacl3.gui.YACLScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vanillatooltips.compat.VanillaTooltipsConfigScreen;

/** Keeps YACL's reset action separate from its dedicated undo action. */
@Mixin(value = YACLScreen.class, remap = false)
public abstract class YACLScreenMixin {
	@Shadow
	@Final
	public YetAnotherConfigLib config;

	@Inject(method = "cancelOrReset", at = @At("HEAD"), cancellable = true)
	private void vanillaTooltips$alwaysReset(CallbackInfo ci) {
		if (!VanillaTooltipsConfigScreen.owns(this.config)) {
			return;
		}
		OptionUtils.forEachOptions(this.config, Option::requestSetDefault);
		ci.cancel();
	}
}

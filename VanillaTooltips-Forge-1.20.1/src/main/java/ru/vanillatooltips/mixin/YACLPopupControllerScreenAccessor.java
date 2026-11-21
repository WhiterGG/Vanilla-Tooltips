package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PopupControllerScreen.class, remap = false)
public interface YACLPopupControllerScreenAccessor {
	@Accessor(value = "backgroundYaclScreen", remap = false)
	YACLScreen vanillaTooltips$getBackgroundYaclScreen();
}
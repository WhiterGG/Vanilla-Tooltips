package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.PopupControllerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Closes a Forge YACL popup and forwards outside clicks to its settings screen. */
@Mixin(Screen.class)
public abstract class YACLPopupClickForwardingMixin extends AbstractContainerEventHandler {
	@Unique
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean handled = super.mouseClicked(mouseX, mouseY, button);
		if (handled || !((Object) this instanceof PopupControllerScreen popup)) {
			return handled;
		}

		YACLScreen background = ((YACLPopupControllerScreenAccessor) popup).vanillaTooltips$getBackgroundYaclScreen();
		popup.onClose();
		return background.mouseClicked(mouseX, mouseY, button);
	}
}
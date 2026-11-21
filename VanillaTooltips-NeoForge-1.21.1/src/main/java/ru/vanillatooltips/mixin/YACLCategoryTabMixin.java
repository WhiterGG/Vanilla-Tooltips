package ru.vanillatooltips.mixin;

import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.SearchFieldWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vanillatooltips.client.VanillaTooltipsConfigScreen;

/** Prevents YACL's reset button from changing its label to Cancel. */
@Mixin(value = YACLScreen.CategoryTab.class, remap = false)
public abstract class YACLCategoryTabMixin {
	@Shadow
	@Final
	private YACLScreen screen;

	@Shadow
	@Final
	private SearchFieldWidget searchField;

	@Shadow
	@Final
	public Button cancelResetButton;

	@Inject(method = "updateButtons", at = @At("RETURN"))
	private void vanillaTooltips$keepResetButton(CallbackInfo ci) {
		if (!VanillaTooltipsConfigScreen.owns(this.screen.config)) {
			return;
		}
		this.cancelResetButton.setMessage(Component.translatable("controls.reset"));
		this.cancelResetButton.setTooltip(Tooltip.create(
				Component.translatable("vanilla_tooltips.config.reset.tooltip")));
		this.searchField.setEmptyText(Component.empty());
		this.searchField.setHint(Component.translatable("vanilla_tooltips.config.search"));
	}
}

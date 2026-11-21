package ru.vanillatooltips.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vanillatooltips.ArmorPreviewRenderer;

/** Applies configurable opacity only to the base armor stand in our preview. */
@Mixin(LivingEntityRenderer.class)
public abstract class ArmorStandOpacityMixin {
	@Shadow
	protected abstract Identifier getTextureLocation(LivingEntityRenderState state);

	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
	private void vanillaTooltips$useTranslucentPreviewTexture(LivingEntityRenderState state,
			boolean bodyVisible, boolean translucent, boolean glowing,
			CallbackInfoReturnable<RenderType> cir) {
		if (ArmorPreviewRenderer.isPreviewState(state)) {
			cir.setReturnValue(RenderTypes.entityTranslucent(this.getTextureLocation(state), false));
		}
	}

	@Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
	private void vanillaTooltips$applyPreviewOpacity(LivingEntityRenderState state,
			CallbackInfoReturnable<Integer> cir) {
		Integer opacity = ArmorPreviewRenderer.opacityForState(state);
		if (opacity != null) {
			int tint = cir.getReturnValue();
			int alpha = (tint >>> 24) * opacity / 255;
			cir.setReturnValue((tint & 0x00FFFFFF) | (alpha << 24));
		}
	}
}

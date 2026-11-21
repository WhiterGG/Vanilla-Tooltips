package ru.vanillatooltips.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.vanillatooltips.client.ArmorPreviewRenderer;

/** Applies configurable opacity only to the base armor stand in our preview. */
@Mixin(LivingEntityRenderer.class)
public abstract class ArmorStandOpacityMixin {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void vanillaTooltips$useTranslucentPreviewTexture(LivingEntity entity,
            boolean bodyVisible, boolean translucent, boolean glowing,
            CallbackInfoReturnable<RenderType> cir) {
        if (entity instanceof ArmorStand stand && ArmorPreviewRenderer.opacityForPreview(stand) != null) {
            ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
            cir.setReturnValue(RenderType.entityTranslucent(texture));
        }
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            ordinal = 0), index = 4)
    private int vanillaTooltips$applyPreviewOpacity(int tint) {
        int opacity = ArmorPreviewRenderer.activePreviewOpacity();
        int alpha = (tint >>> 24) * opacity / 100;
        return (tint & 0x00FFFFFF) | (alpha << 24);
    }
}

package ru.vanillatooltips.mixin;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Provides the vanilla render-state extraction used by the armor preview. */
@Mixin(InventoryScreen.class)
public interface InventoryScreenAccessor {
	@Invoker("extractRenderState")
	static EntityRenderState vanillaTooltips$extractRenderState(LivingEntity entity) {
		throw new AssertionError();
	}
}

package ru.vanillatooltips;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.vanillatooltips.mixin.InventoryScreenAccessor;

/** Renders hovered armor on a cached client-side armor stand. */
public final class ArmorPreviewRenderer {
	private static final Map<LivingEntityRenderState, Integer> PREVIEW_OPACITY_BY_STATE =
			Collections.synchronizedMap(new WeakHashMap<>());
	private static ClientLevel cachedLevel;
	private static ArmorStand armorStand;

	private ArmorPreviewRenderer() {}

	public static EquipmentSlot getArmorSlot(ItemStack stack) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable == null || !isHumanoidArmorSlot(equippable.slot())) {
			return null;
		}
		return equippable.slot();
	}

	private static boolean isHumanoidArmorSlot(EquipmentSlot slot) {
		return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
				|| slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
	}

	public static void render(GuiGraphics graphics, ItemStack stack, EquipmentSlot slot,
			int left, int top, int width, int height, int standOpacity) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) {
			return;
		}

		if (armorStand == null || cachedLevel != level) {
			cachedLevel = level;
			armorStand = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
			armorStand.setShowArms(true);
		}

		armorStand.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
		armorStand.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
		armorStand.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
		armorStand.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
		armorStand.setItemSlot(slot, stack.copy());
		armorStand.setInvisible(false);

		EntityRenderState state = InventoryScreenAccessor.vanillaTooltips$extractRenderState(armorStand);
		if (state instanceof LivingEntityRenderState livingState) {
			float rotation = VanillaTooltipsConfig.isAnimateArmorPreview()
					? (float) ((System.nanoTime() / 1_000_000_000.0D
							* VanillaTooltipsConfig.getArmorRotationSpeed()) % 360.0D)
							* VanillaTooltipsConfig.getArmorRotationDirection().multiplier()
					: 0.0F;
			livingState.bodyRot = 180.0F + rotation;
			livingState.yRot = rotation;
			livingState.xRot = 0.0F;
			livingState.boundingBoxWidth /= livingState.scale;
			livingState.boundingBoxHeight /= livingState.scale;
			livingState.scale = 1.0F;
			PREVIEW_OPACITY_BY_STATE.put(livingState,
					Math.round(255.0F * Math.max(0, Math.min(100, standOpacity)) / 100.0F));
		}
		if (state instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState humanoidState) {
			// A cached preview entity does not tick, so give elytra the same open pose
			// vanilla animation would produce while the player is moving.
			humanoidState.elytraRotX = (float) (Math.PI / 12.0D);
			humanoidState.elytraRotY = 0.0F;
			humanoidState.elytraRotZ = (float) (-Math.PI / 12.0D);
		}

		Quaternionf modelRotation = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf cameraRotation = new Quaternionf();
		modelRotation.mul(cameraRotation);
		float previewScale = getPreviewScale(height);
	Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
		graphics.submitEntityRenderState(state, previewScale, translation, modelRotation, cameraRotation,
				left, top, left + width, top + height);
	}

	private static float getPreviewScale(int viewportHeight) {
		float requestedScale = 30.0F * VanillaTooltipsConfig.getArmorPreviewScale() / 100.0F;
		float maxScale = Math.max(1.0F, (viewportHeight - 2.0F) / 2.125F);
		return Math.min(requestedScale, maxScale);
	}

	public static boolean isPreviewState(LivingEntityRenderState state) {
		return PREVIEW_OPACITY_BY_STATE.containsKey(state);
	}

	public static Integer opacityForState(LivingEntityRenderState state) {
		return PREVIEW_OPACITY_BY_STATE.get(state);
	}
}

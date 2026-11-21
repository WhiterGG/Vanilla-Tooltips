package ru.vanillatooltips;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Renders human armor and clothing on a cached client-side armor stand. */
public final class ArmorPreviewRenderer {
	private static final Map<ArmorStand, Integer> PREVIEW_OPACITY =
			Collections.synchronizedMap(new WeakHashMap<>());
	private static final ThreadLocal<Integer> ACTIVE_PREVIEW_OPACITY = new ThreadLocal<>();
	private static ClientLevel cachedLevel;
	private static ArmorStand armorStand;

	private ArmorPreviewRenderer() {}

	public static EquipmentSlot getArmorSlot(ItemStack stack) {
		Equipable equipable = Equipable.get(stack);
		if (equipable == null || !isHumanoidArmorSlot(equipable.getEquipmentSlot())) return null;
		return equipable.getEquipmentSlot();
	}

	private static boolean isHumanoidArmorSlot(EquipmentSlot slot) {
		return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST
				|| slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
	}

	public static void render(GuiGraphics graphics, ItemStack stack, EquipmentSlot slot,
			int left, int top, int width, int height, int standOpacity) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null) return;

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
		PREVIEW_OPACITY.put(armorStand, Math.max(0, Math.min(100, standOpacity)));

		float rotation = VanillaTooltipsConfig.isAnimateArmorPreview()
				? (float) ((System.nanoTime() / 1_000_000_000.0D
						* VanillaTooltipsConfig.getArmorRotationSpeed()
						* -VanillaTooltipsConfig.getArmorRotationDirection().multiplier()) % 360.0D)
				: 0.0F;
		armorStand.setYBodyRot(0.0F);
		armorStand.setYRot(0.0F);
		armorStand.yRotO = 0.0F;
		armorStand.setYHeadRot(0.0F);
		armorStand.setXRot(0.0F);
		armorStand.xRotO = 0.0F;

		float centerX = left + width / 2.0F;
		float centerY = top + height / 2.0F;
		float entityScale = armorStand.getScale();
		float previewScale = getPreviewScale(height);
		Vector3f translation = new Vector3f(
			0.0F, armorStand.getBbHeight() / 2.0F + 0.0625F * entityScale, 0.0F);
		Quaternionf modelRotation = new Quaternionf()
				.rotateZ((float) Math.PI)
				.rotateY((float) Math.toRadians(rotation + 180.0F));
		Quaternionf cameraRotation = new Quaternionf();

		graphics.enableScissor(left, top, left + width, top + height);
		ACTIVE_PREVIEW_OPACITY.set(Math.max(0, Math.min(100, standOpacity)));
		try {
			InventoryScreen.renderEntityInInventory(graphics, centerX, centerY,
					previewScale / entityScale,
					translation, modelRotation, cameraRotation, armorStand);
		} finally {
			ACTIVE_PREVIEW_OPACITY.remove();
			graphics.disableScissor();
		}
	}

	private static float getPreviewScale(int viewportHeight) {
		float requestedScale = 30.0F * VanillaTooltipsConfig.getArmorPreviewScale() / 100.0F;
		float maxScale = Math.max(1.0F, (viewportHeight - 2.0F) / 2.125F);
		return Math.min(requestedScale, maxScale);
	}

	public static Integer opacityForPreview(ArmorStand stand) {
		return PREVIEW_OPACITY.get(stand);
	}

	public static int activePreviewOpacity() {
		Integer opacity = ACTIVE_PREVIEW_OPACITY.get();
		return opacity == null ? 100 : opacity;
	}
}

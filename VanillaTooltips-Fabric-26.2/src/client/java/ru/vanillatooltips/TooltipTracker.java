package ru.vanillatooltips;

import net.minecraft.world.item.ItemStack;

/** Bridges Fabric's item-only callback to the vanilla tooltip renderer. */
public final class TooltipTracker {
	private static ItemStack pending = ItemStack.EMPTY;
	private static ItemStack candidate = ItemStack.EMPTY;
	private static ItemStack lastItem = ItemStack.EMPTY;
	private static long appearedAt;
	private static long lastSeenAt;
	private static boolean suppressNextTooltip;

	private TooltipTracker() {}

 	public static void remember(ItemStack stack) {
		pending = stack.copy();
		updateAppearance(stack);
	}

	private static void updateAppearance(ItemStack stack) {
		long now = System.currentTimeMillis();
		if (!ItemStack.isSameItemSameComponents(lastItem, stack) || now - lastSeenAt > 75L) {
			lastItem = stack.copy();
			appearedAt = now;
		}
		lastSeenAt = now;
	}

	public static ItemStack take() {
		if (suppressNextTooltip) {
			suppressNextTooltip = false;
			pending = ItemStack.EMPTY;
			return ItemStack.EMPTY;
		}
		ItemStack result = pending;
		pending = ItemStack.EMPTY;
		return result;
	}

	public static void offerCandidate(ItemStack stack) {
		candidate = stack.copy();
	}

	public static ItemStack takeCandidate() {
		ItemStack result = candidate;
		candidate = ItemStack.EMPTY;
		return result;
	}

	public static void suppressNextTooltip() {
		pending = ItemStack.EMPTY;
		candidate = ItemStack.EMPTY;
		suppressNextTooltip = true;
	}

	public static void clearPending() {
		pending = ItemStack.EMPTY;
	}

	public static float itemAppearanceScale() {
		if (!VanillaTooltipsConfig.isAnimateItemPreview()) {
			return 1.0F;
		}
		float duration = VanillaTooltipsConfig.getAnimationDurationMs();
		float progress = Math.min(1.0F, (System.currentTimeMillis() - appearedAt) / duration);
		if (progress < 0.65F) {
			float phase = progress / 0.65F;
			float eased = phase * phase * (3.0F - 2.0F * phase);
			return 0.65F + 0.50F * eased;
		}
		float phase = (progress - 0.65F) / 0.35F;
		float eased = phase * phase * (3.0F - 2.0F * phase);
		return 1.15F - 0.15F * eased;
	}
}

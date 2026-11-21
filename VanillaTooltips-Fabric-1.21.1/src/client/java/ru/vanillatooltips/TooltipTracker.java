package ru.vanillatooltips;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.item.ItemStack;

/** Tracks the exact item rendering context and its appearance animation. */
public final class TooltipTracker {
	private static final Deque<ItemStack> ACTIVE_ITEMS = new ArrayDeque<>();
	private static ItemStack lastItem = ItemStack.EMPTY;
	private static long appearedAt;
	private static long lastSeenAt;

	private TooltipTracker() {}

	public static void beginItemTooltip(ItemStack stack) {
		ACTIVE_ITEMS.push(stack == null ? ItemStack.EMPTY : stack.copy());
	}

	public static void endItemTooltip() {
		if (!ACTIVE_ITEMS.isEmpty()) {
			ACTIVE_ITEMS.pop();
		}
	}

	public static ItemStack currentItem() {
		return ACTIVE_ITEMS.isEmpty() ? ItemStack.EMPTY : ACTIVE_ITEMS.peek();
	}

	public static void track(ItemStack stack) {
		long now = System.currentTimeMillis();
		if (!ItemStack.isSameItemSameComponents(lastItem, stack) || now - lastSeenAt > 75L) {
			lastItem = stack.copy();
			appearedAt = now;
		}
		lastSeenAt = now;
	}

	public static float itemAppearanceScale() {
		if (!VanillaTooltipsConfig.isAnimateItemPreview()) return 1.0F;
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

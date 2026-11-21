package ru.vanillatooltips;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntConsumer;

/** Shared drawing helpers for vertical, two-color rarity gradients. */
public final class TooltipGradientRenderer {
	private TooltipGradientRenderer() {}

	@FunctionalInterface
	public interface Fill {
		void fill(int left, int top, int right, int bottom, int color);
	}

	@FunctionalInterface
	public interface Scissor {
		void clip(int left, int top, int right, int bottom, Runnable draw);
	}

	public static void fillVertical(int left, int top, int right, int bottom,
			DoubleToIntFunction colorAt, Fill fill) {
		int height = bottom - top;
		for (int y = top; y < bottom; y++) {
			double progress = height <= 1 ? 0.5D : (y - top) / (double) (height - 1);
			fill.fill(left, y, right, y + 1, colorAt.applyAsInt(progress));
		}
	}

	public static void drawBorderVertical(int left, int top, int right, int bottom,
			DoubleToIntFunction colorAt, Fill fill) {
		int height = bottom - top;
		for (int y = top; y < bottom; y++) {
			double progress = height <= 1 ? 0.5D : (y - top) / (double) (height - 1);
			int color = colorAt.applyAsInt(progress);
			if (y == top || y == bottom - 1) {
				fill.fill(left + 1, y, right - 1, y + 1, color);
			} else {
				fill.fill(left, y, left + 1, y + 1, color);
				fill.fill(right - 1, y, right, y + 1, color);
			}
		}
	}

	public static void drawTextVertical(int left, int top, int width, int height,
			DoubleToIntFunction colorAt, Scissor scissor, IntConsumer drawWithColor) {
		if (width <= 0 || height <= 0) return;
		for (int row = 0; row < height; row++) {
			int y = top + row;
			double progress = height <= 1 ? 0.5D : row / (double) (height - 1);
			scissor.clip(left, y, left + width, y + 1,
					() -> drawWithColor.accept(colorAt.applyAsInt(progress)));
		}
	}
}

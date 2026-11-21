package ru.vanillatooltips;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import ru.vanillatooltips.mixin.ClientTextTooltipAccessor;

/** Finds title rows that vanilla split into separate tooltip components. */
public final class TooltipTextLayout {
	private TooltipTextLayout() {}

	public static int titleLineCount(ItemStack item, List<ClientTooltipComponent> components) {
		String title = item.getHoverName().getString();
		int offset = 0;
		int lines = 0;

		for (ClientTooltipComponent component : components) {
			if (!(component instanceof ClientTextTooltipAccessor accessor)) {
				break;
			}
			String line = text(accessor.vanillaTooltips$getText()).strip();
			if (line.isEmpty()) {
				break;
			}
			while (offset < title.length() && Character.isWhitespace(title.charAt(offset))) {
				offset++;
			}
			if (!title.startsWith(line, offset)) {
				return 0;
			}
			offset += line.length();
			lines++;
			if (offset == title.length()) {
				return lines;
			}
		}
		return 0;
	}

	private static String text(FormattedCharSequence sequence) {
		StringBuilder text = new StringBuilder();
		sequence.accept((index, style, codePoint) -> {
			text.appendCodePoint(codePoint);
			return true;
		});
		return text.toString();
	}
}

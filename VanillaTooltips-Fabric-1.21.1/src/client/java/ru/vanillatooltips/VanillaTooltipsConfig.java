package ru.vanillatooltips;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Persistent client settings shared by the renderer and Mod Menu screen. */
public final class VanillaTooltipsConfig {
	public static final int MIN_ANIMATION_SPEED_PERCENT = 50;
	public static final int MAX_ANIMATION_SPEED_PERCENT = 100;
	public static final int ANIMATION_SPEED_STEP_PERCENT = 25;
	public static final int DEFAULT_ANIMATION_SPEED_PERCENT = 75;
	private static final int ANIMATION_DURATION_AT_100_PERCENT_MS = 100;
	private static final int ANIMATION_DURATION_AT_75_PERCENT_MS = 200;
	private static final int ANIMATION_DURATION_AT_50_PERCENT_MS = 300;
	public static final int MIN_ARMOR_ROTATION_SPEED = 45;
	public static final int MAX_ARMOR_ROTATION_SPEED = 180;
	public static final int DEFAULT_ARMOR_ROTATION_SPEED = 45;
	public static final int MIN_ITEM_PREVIEW_SCALE = 50;
	public static final int MAX_ITEM_PREVIEW_SCALE = 100;
	public static final int DEFAULT_ITEM_PREVIEW_SCALE = 100;
	public static final int MIN_ARMOR_PREVIEW_SCALE = 50;
	public static final int MAX_ARMOR_PREVIEW_SCALE = 100;
	public static final int DEFAULT_ARMOR_PREVIEW_SCALE = 75;
	public static final int MIN_ARMOR_STAND_OPACITY = 0;
	public static final int MAX_ARMOR_STAND_OPACITY = 100;
	public static final int DEFAULT_ARMOR_STAND_OPACITY = 100;
	public static final int DEFAULT_TOOLTIP_OUTLINE_COLOR = 0xFF899098;
	public static final int DEFAULT_ITEM_FRAME_COLOR = 0xD0899098;
	public static final int DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR = 0xDD202020;
	private static final String[] OUTLINE_RARITIES = { "common", "uncommon", "rare", "epic" };
	private static final int[] DEFAULT_TOOLTIP_OUTLINE_COLORS = {
			0xFF90949A, 0xFFFFFF55, 0xFF55FFFF, 0xFFFF55FF
	};
	private static final int[] DEFAULT_RARITY_LABEL_COLORS = {
			0xFFAAAAAA, 0xFFFFFF55, 0xFF55FFFF, 0xFFFF55FF
	};
	private static final int[] DEFAULT_ITEM_FRAME_COLORS = {
			0xD090949A, 0xD0FFFF55, 0xD055FFFF, 0xD0FF55FF
	};
	private static final int[] DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS = {
			0xDD3B3C3D, 0xDD56562D, 0xDD2D5656, 0xDD562D56
	};
	private static final int[] DEFAULT_RARITY_LABEL_GRADIENT_END_COLORS = {
			0xFFF1F4FA, 0xFFFFB347, 0xFF4C7DFF, 0xFFB65CFF
	};
	private static final int[] DEFAULT_TOOLTIP_OUTLINE_GRADIENT_END_COLORS = {
			0xFFD8DEE9, 0xFFFFB347, 0xFF4C7DFF, 0xFFB65CFF
	};
	private static final int[] DEFAULT_ITEM_FRAME_GRADIENT_END_COLORS = {
			0xD0D8DEE9, 0xD0FFB347, 0xD04C7DFF, 0xD0B65CFF
	};
	private static final int[] DEFAULT_ITEM_PREVIEW_BACKGROUND_GRADIENT_END_COLORS = {
			0xDD5D6675, 0xDD8A662B, 0xDD273F7A, 0xDD3E1F70
	};

	private static final int ITEM_PREVIEW_SCALE_CONFIG_VERSION = 1;
	private static final int ARMOR_PREVIEW_SCALE_CONFIG_VERSION = 1;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("vanilla_tooltips.json");
	private static boolean enabled = true;
	private static boolean showRarity = true;
	private static boolean showItemPreview = true;
	private static boolean animateItemPreview = true;
	private static int itemPreviewScale = DEFAULT_ITEM_PREVIEW_SCALE;
	private static boolean showArmorPreview = true;
	private static boolean showArmorItemPreview = true;
	private static int armorPreviewScale = DEFAULT_ARMOR_PREVIEW_SCALE;
	private static int armorStandOpacity = DEFAULT_ARMOR_STAND_OPACITY;
	private static boolean animateArmorPreview = true;
	private static int armorRotationSpeed = DEFAULT_ARMOR_ROTATION_SPEED;
	private static ArmorRotationDirection armorRotationDirection = ArmorRotationDirection.RIGHT;
	private static ArmorPreviewSide armorPreviewSide = ArmorPreviewSide.LEFT;
	private static TooltipSeparationStyle tooltipSeparationStyle = TooltipSeparationStyle.OFF;
	private static ColorMode rarityLabelColorMode = ColorMode.GRADIENT;
	private static int[] rarityLabelColors = DEFAULT_RARITY_LABEL_COLORS.clone();
	private static int rarityLabelCustomColor = DEFAULT_RARITY_LABEL_COLORS[0];
	private static boolean rarityOutline = true;
	private static ColorMode tooltipOutlineColorMode = ColorMode.GRADIENT;
	private static int[] tooltipOutlineColors = DEFAULT_TOOLTIP_OUTLINE_COLORS.clone();
	private static int tooltipOutlineCustomColor = DEFAULT_TOOLTIP_OUTLINE_COLOR;
	private static PreviewColorMode itemFrameColorMode = PreviewColorMode.GRADIENT;
	private static boolean showItemFrame = true;
	private static int itemFrameCustomColor = DEFAULT_ITEM_FRAME_COLOR;
	private static int[] itemFrameColors = DEFAULT_ITEM_FRAME_COLORS.clone();
	private static PreviewColorMode itemPreviewBackgroundColorMode = PreviewColorMode.GRADIENT;
	private static boolean showItemPreviewBackground = true;
	private static int[] itemPreviewBackgroundColors = DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS.clone();
	private static int itemPreviewBackgroundColor = DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR;
	private static int animationSpeedPercent = DEFAULT_ANIMATION_SPEED_PERCENT;

	private VanillaTooltipsConfig() {}

	public static void load() {
		if (!Files.isRegularFile(PATH)) {
			save();
			return;
		}

		boolean saveMigratedConfig = false;
		try (Reader reader = Files.newBufferedReader(PATH)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			boolean migrateItemPreviewScale = !json.has("itemPreviewScaleVersion")
					|| json.get("itemPreviewScaleVersion").getAsInt() < ITEM_PREVIEW_SCALE_CONFIG_VERSION;
			boolean migrateArmorPreviewScale = !json.has("armorPreviewScaleVersion")
					|| json.get("armorPreviewScaleVersion").getAsInt() < ARMOR_PREVIEW_SCALE_CONFIG_VERSION;
			boolean hasPerRarityOutlineModes = false;
			boolean hasCustomPerRarityOutlineMode = false;
			boolean migrateOutlineColors = !json.has("tooltipOutlineMode");
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				String propertyPrefix = outlineColorPropertyPrefix(index);
				String modeKey = propertyPrefix + "Mode";
				hasPerRarityOutlineModes |= json.has(modeKey);
				if (json.has(modeKey)) {
					hasCustomPerRarityOutlineMode |= ColorMode.fromSerializedName(
							json.get(modeKey).getAsString(), ColorMode.RARITY) == ColorMode.CUSTOM;
				}
				migrateOutlineColors |= json.has(modeKey) || !json.has(propertyPrefix + "Color");
			}
			if (json.has("enabled")) {
				enabled = json.get("enabled").getAsBoolean();
			}
			if (json.has("showRarity")) {
				showRarity = json.get("showRarity").getAsBoolean();
			}
			if (json.has("showItemPreview")) {
				showItemPreview = json.get("showItemPreview").getAsBoolean();
			} else if (json.has("showPreview")) {
				// Migrate the preview setting used by versions 0.6.0-0.8.0.
				showItemPreview = json.get("showPreview").getAsBoolean();
			}
			if (json.has("animateItemPreview")) {
				animateItemPreview = json.get("animateItemPreview").getAsBoolean();
			}
			if (json.has("itemPreviewScale")) {
				int configuredScale = json.get("itemPreviewScale").getAsInt();
				if (migrateItemPreviewScale) {
					configuredScale -= 25;
				}
				itemPreviewScale = clampItemPreviewScale(configuredScale);
			}
			if (json.has("showArmorPreview")) {
				showArmorPreview = json.get("showArmorPreview").getAsBoolean();
			}
			if (json.has("showArmorItemPreview")) {
				showArmorItemPreview = json.get("showArmorItemPreview").getAsBoolean();
			}
			if (json.has("armorPreviewScale")) {
				int configuredScale = json.get("armorPreviewScale").getAsInt();
				if (migrateArmorPreviewScale) {
					configuredScale -= 25;
				}
				armorPreviewScale = clampArmorPreviewScale(configuredScale);
			}
			if (json.has("showArmorStand")) {
				// Preserve the old on/off setting when upgrading from 1.1.0.
				armorStandOpacity = json.get("showArmorStand").getAsBoolean()
						? MAX_ARMOR_STAND_OPACITY : MIN_ARMOR_STAND_OPACITY;
			}
			if (json.has("armorStandOpacity")) {
				armorStandOpacity = clampArmorStandOpacity(json.get("armorStandOpacity").getAsInt());
			}
			if (json.has("animateArmorPreview")) {
				animateArmorPreview = json.get("animateArmorPreview").getAsBoolean();
			}
			if (json.has("armorRotationSpeed")) {
				armorRotationSpeed = clampArmorRotationSpeed(json.get("armorRotationSpeed").getAsInt());
			}
			if (json.has("armorRotationDirection")) {
				armorRotationDirection = ArmorRotationDirection.fromSerializedName(
						json.get("armorRotationDirection").getAsString());
			}
			if (json.has("armorPreviewSide")) {
				armorPreviewSide = ArmorPreviewSide.fromSerializedName(
						json.get("armorPreviewSide").getAsString());
			}
			if (json.has("tooltipSeparationStyle")) {
				tooltipSeparationStyle = TooltipSeparationStyle.fromSerializedName(
						json.get("tooltipSeparationStyle").getAsString());
			}
			boolean migrateColorModes = !json.has("rarityLabelCustomColor")
					|| !json.has("tooltipOutlineCustomColor");
			String rarityLabelModeName = json.has("rarityLabelColorMode")
					? json.get("rarityLabelColorMode").getAsString() : "standard";
			if (migrateColorModes) {
				rarityLabelModeName = rarityLabelModeName.equalsIgnoreCase("custom") ? "rarity"
						: rarityLabelModeName.equalsIgnoreCase("rarity") ? "standard" : rarityLabelModeName;
			}
			rarityLabelColorMode = ColorMode.fromSerializedName(
					rarityLabelModeName, ColorMode.STANDARD);
			rarityLabelCustomColor = json.has("rarityLabelCustomColor")
					? forceOpaque(json.get("rarityLabelCustomColor").getAsInt()) : DEFAULT_RARITY_LABEL_COLORS[0];
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				String colorKey = rarityLabelColorPropertyPrefix(index) + "Color";
				rarityLabelColors[index] = json.has(colorKey)
						? forceOpaque(json.get(colorKey).getAsInt()) : DEFAULT_RARITY_LABEL_COLORS[index];
			}
			if (json.has("rarityOutline")) {
				rarityOutline = json.get("rarityOutline").getAsBoolean();
			}
			String legacyOutlineModeName = json.has("tooltipOutlineColorMode")
					? json.get("tooltipOutlineColorMode").getAsString() : "standard";
			if (migrateColorModes) {
				legacyOutlineModeName = legacyOutlineModeName.equalsIgnoreCase("custom") ? "rarity"
						: legacyOutlineModeName.equalsIgnoreCase("rarity") ? "standard" : legacyOutlineModeName;
			}
			ColorMode legacyOutlineMode = ColorMode.fromSerializedName(
					legacyOutlineModeName,
					ColorMode.STANDARD);
			String outlineModeName = json.has("tooltipOutlineMode")
					? json.get("tooltipOutlineMode").getAsString() : null;
			if (outlineModeName != null && migrateColorModes) {
				outlineModeName = outlineModeName.equalsIgnoreCase("custom") ? "rarity"
						: outlineModeName.equalsIgnoreCase("rarity") ? "standard" : outlineModeName;
			}
			tooltipOutlineColorMode = outlineModeName != null
					? ColorMode.fromSerializedName(outlineModeName, ColorMode.STANDARD)
					: hasPerRarityOutlineModes
							? (hasCustomPerRarityOutlineMode ? ColorMode.RARITY : ColorMode.STANDARD)
							: legacyOutlineMode;
			tooltipOutlineCustomColor = json.has("tooltipOutlineCustomColor")
					? forceOpaque(json.get("tooltipOutlineCustomColor").getAsInt()) : DEFAULT_TOOLTIP_OUTLINE_COLOR;
			int legacyOutlineColor = json.has("tooltipOutlineColor")
					? forceOpaque(json.get("tooltipOutlineColor").getAsInt())
					: DEFAULT_TOOLTIP_OUTLINE_COLOR;
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				String propertyPrefix = outlineColorPropertyPrefix(index);
				String modeKey = propertyPrefix + "Mode";
				String colorKey = propertyPrefix + "Color";
				ColorMode previousMode = json.has(modeKey)
						? ColorMode.fromSerializedName(json.get(modeKey).getAsString(), legacyOutlineMode)
						: legacyOutlineMode;
				if (hasPerRarityOutlineModes && previousMode == ColorMode.RARITY) {
					// In 1.2.4, standard mode used the renderer's rarity palette. Copy it
					// into the custom palette when migrating mixed per-rarity settings.
					tooltipOutlineColors[index] = DEFAULT_TOOLTIP_OUTLINE_COLORS[index];
				} else {
					tooltipOutlineColors[index] = json.has(colorKey)
							? forceOpaque(json.get(colorKey).getAsInt())
							: legacyOutlineMode == ColorMode.CUSTOM
									? legacyOutlineColor : DEFAULT_TOOLTIP_OUTLINE_COLORS[index];
				}
			}
			boolean hasLegacyFrameColor = json.has("itemFrameColor");
			int legacyFrameColor = hasLegacyFrameColor
					? json.get("itemFrameColor").getAsInt() : DEFAULT_ITEM_FRAME_COLOR;
			boolean hasPerRarityFrameColors = false;
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				String colorKey = itemFrameColorPropertyPrefix(index) + "Color";
				hasPerRarityFrameColors |= json.has(colorKey);
				itemFrameColors[index] = json.has(colorKey)
						? json.get(colorKey).getAsInt()
						: DEFAULT_ITEM_FRAME_COLORS[index];
			}
			itemFrameColorMode = PreviewColorMode.fromSerializedName(
					json.has("itemFrameColorMode") ? json.get("itemFrameColorMode").getAsString()
							: hasPerRarityFrameColors ? "rarity"
							: hasLegacyFrameColor && legacyFrameColor != DEFAULT_ITEM_FRAME_COLOR ? "custom" : "standard",
					PreviewColorMode.STANDARD);
			itemFrameCustomColor = json.has("itemFrameCustomColor")
					? json.get("itemFrameCustomColor").getAsInt() : legacyFrameColor;
			showItemFrame = !json.has("showItemFrame") || json.get("showItemFrame").getAsBoolean();
			if (json.has("itemPreviewBackgroundColor")) {
				itemPreviewBackgroundColor = json.get("itemPreviewBackgroundColor").getAsInt();
			}
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				String colorKey = itemPreviewBackgroundColorPropertyPrefix(index) + "Color";
				itemPreviewBackgroundColors[index] = json.has(colorKey)
						? json.get(colorKey).getAsInt() : DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS[index];
			}
			itemPreviewBackgroundColorMode = PreviewColorMode.fromSerializedName(
					json.has("itemPreviewBackgroundColorMode")
							? json.get("itemPreviewBackgroundColorMode").getAsString()
							: itemPreviewBackgroundColor != DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR ? "custom" : "standard",
					PreviewColorMode.STANDARD);
			showItemPreviewBackground = !json.has("showItemPreviewBackground")
					|| json.get("showItemPreviewBackground").getAsBoolean();
			if (json.has("animationSpeedPercent")) {
				animationSpeedPercent = clampAnimationSpeedPercent(json.get("animationSpeedPercent").getAsInt());
			} else {
				animationSpeedPercent = json.has("animationDurationMs")
						? animationSpeedPercentFromDuration(json.get("animationDurationMs").getAsInt())
						: DEFAULT_ANIMATION_SPEED_PERCENT;
				saveMigratedConfig = true;
			}
			saveMigratedConfig |= migrateItemPreviewScale || migrateArmorPreviewScale
					|| migrateOutlineColors
					|| hasLegacyFrameColor || !json.has("showArmorItemPreview")
					|| !json.has("armorPreviewSide") || !json.has("tooltipSeparationStyle")
					|| !json.has("rarityLabelColorMode")
					|| !json.has("rarityLabelCustomColor") || !json.has("tooltipOutlineCustomColor")
					|| !json.has("showItemFrame") || !json.has("showItemPreviewBackground")
					|| !json.has(rarityLabelColorPropertyPrefix(0) + "Color")
					|| !json.has(rarityLabelColorPropertyPrefix(1) + "Color")
					|| !json.has(rarityLabelColorPropertyPrefix(2) + "Color")
					|| !json.has(rarityLabelColorPropertyPrefix(3) + "Color")
					|| !json.has(itemFrameColorPropertyPrefix(0) + "Color")
					|| !json.has(itemFrameColorPropertyPrefix(1) + "Color")
					|| !json.has(itemFrameColorPropertyPrefix(2) + "Color")
					|| !json.has(itemFrameColorPropertyPrefix(3) + "Color")
					|| !json.has("itemFrameColorMode") || !json.has("itemFrameCustomColor")
					|| !json.has("itemPreviewBackgroundColor") || !json.has("itemPreviewBackgroundColorMode")
					|| !json.has(itemPreviewBackgroundColorPropertyPrefix(0) + "Color")
					|| !json.has(itemPreviewBackgroundColorPropertyPrefix(1) + "Color")
					|| !json.has(itemPreviewBackgroundColorPropertyPrefix(2) + "Color")
					|| !json.has(itemPreviewBackgroundColorPropertyPrefix(3) + "Color");
		} catch (Exception exception) {
			VanillaTooltipsClient.LOGGER.warn("Could not read {}; using default settings", PATH, exception);
		}
		if (saveMigratedConfig) {
			save();
		}
	}

	public static void save() {
		JsonObject json = new JsonObject();
		json.addProperty("enabled", enabled);
		json.addProperty("showRarity", showRarity);
		json.addProperty("showItemPreview", showItemPreview);
		json.addProperty("animateItemPreview", animateItemPreview);
		json.addProperty("itemPreviewScale", itemPreviewScale);
		json.addProperty("itemPreviewScaleVersion", ITEM_PREVIEW_SCALE_CONFIG_VERSION);
		json.addProperty("showArmorPreview", showArmorPreview);
		json.addProperty("showArmorItemPreview", showArmorItemPreview);
		json.addProperty("armorPreviewScale", armorPreviewScale);
		json.addProperty("armorPreviewScaleVersion", ARMOR_PREVIEW_SCALE_CONFIG_VERSION);
		json.addProperty("armorStandOpacity", armorStandOpacity);
		json.addProperty("animateArmorPreview", animateArmorPreview);
		json.addProperty("armorRotationSpeed", armorRotationSpeed);
		json.addProperty("armorRotationDirection", armorRotationDirection.serializedName());
		json.addProperty("armorPreviewSide", armorPreviewSide.serializedName());
		json.addProperty("tooltipSeparationStyle", tooltipSeparationStyle.serializedName());
		json.addProperty("rarityLabelColorMode", rarityLabelColorMode.serializedName());
		json.addProperty("rarityLabelCustomColor", rarityLabelCustomColor);
		for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
			json.addProperty(rarityLabelColorPropertyPrefix(index) + "Color", rarityLabelColors[index]);
		}
		json.addProperty("rarityOutline", rarityOutline);
		json.addProperty("tooltipOutlineMode", tooltipOutlineColorMode.serializedName());
		json.addProperty("tooltipOutlineCustomColor", tooltipOutlineCustomColor);
		for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
			String propertyPrefix = outlineColorPropertyPrefix(index);
			json.addProperty(propertyPrefix + "Color", tooltipOutlineColors[index]);
		}
		json.addProperty("itemFrameColorMode", itemFrameColorMode.serializedName());
		json.addProperty("showItemFrame", showItemFrame);
		json.addProperty("itemFrameCustomColor", itemFrameCustomColor);
		for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
			json.addProperty(itemFrameColorPropertyPrefix(index) + "Color", itemFrameColors[index]);
		}
		json.addProperty("itemPreviewBackgroundColorMode", itemPreviewBackgroundColorMode.serializedName());
		json.addProperty("showItemPreviewBackground", showItemPreviewBackground);
		for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
			json.addProperty(itemPreviewBackgroundColorPropertyPrefix(index) + "Color",
					itemPreviewBackgroundColors[index]);
		}
		json.addProperty("itemPreviewBackgroundColor", itemPreviewBackgroundColor);
		json.addProperty("animationSpeedPercent", animationSpeedPercent);
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(json, writer);
			}
		} catch (IOException exception) {
			VanillaTooltipsClient.LOGGER.error("Could not save {}", PATH, exception);
		}
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

	public static boolean isShowRarity() {
		return showRarity;
	}

	public static void setShowRarity(boolean value) {
		showRarity = value;
	}

	public static boolean isShowItemPreview() {
		return showItemPreview;
	}

	public static void setShowItemPreview(boolean value) {
		showItemPreview = value;
	}

	public static boolean isAnimateItemPreview() {
		return animateItemPreview;
	}

	public static void setAnimateItemPreview(boolean value) {
		animateItemPreview = value;
	}

	public static int getItemPreviewScale() {
		return itemPreviewScale;
	}

	public static void setItemPreviewScale(int value) {
		itemPreviewScale = clampItemPreviewScale(value);
	}

	public static boolean isShowArmorPreview() {
		return showArmorPreview;
	}

	public static void setShowArmorPreview(boolean value) {
		showArmorPreview = value;
	}

	public static boolean isShowArmorItemPreview() {
		return showArmorItemPreview;
	}

	public static void setShowArmorItemPreview(boolean value) {
		showArmorItemPreview = value;
	}

	public static int getArmorPreviewScale() {
		return armorPreviewScale;
	}

	/** Tooltip-space height of the armor preview, including its small breathing room. */
	public static int getArmorPreviewLayoutHeight() {
		return Math.round(60.0F * armorPreviewScale / 100.0F) + 8;
	}

	public static void setArmorPreviewScale(int value) {
		armorPreviewScale = clampArmorPreviewScale(value);
	}

	public static int getArmorStandOpacity() {
		return armorStandOpacity;
	}

	public static void setArmorStandOpacity(int value) {
		armorStandOpacity = clampArmorStandOpacity(value);
	}

	/** Kept for integrations compiled against the 1.1.0 config API. */
	public static boolean isShowArmorStand() {
		return armorStandOpacity > MIN_ARMOR_STAND_OPACITY;
	}

	/** Kept for integrations compiled against the 1.1.0 config API. */
	public static void setShowArmorStand(boolean value) {
		armorStandOpacity = value ? MAX_ARMOR_STAND_OPACITY : MIN_ARMOR_STAND_OPACITY;
	}

	public static boolean isAnimateArmorPreview() {
		return animateArmorPreview;
	}

	public static void setAnimateArmorPreview(boolean value) {
		animateArmorPreview = value;
	}

	public static int getArmorRotationSpeed() {
		return armorRotationSpeed;
	}

	public static void setArmorRotationSpeed(int value) {
		armorRotationSpeed = clampArmorRotationSpeed(value);
	}

	public static ArmorRotationDirection getArmorRotationDirection() {
		return armorRotationDirection;
	}

	public static void setArmorRotationDirection(ArmorRotationDirection value) {
		armorRotationDirection = value == null ? ArmorRotationDirection.RIGHT : value;
	}

	public static ArmorPreviewSide getArmorPreviewSide() {
		return armorPreviewSide;
	}

	public static TooltipSeparationStyle getTooltipSeparationStyle() {
		return tooltipSeparationStyle;
	}

	public static void setTooltipSeparationStyle(TooltipSeparationStyle value) {
		tooltipSeparationStyle = value == null ? TooltipSeparationStyle.OFF : value;
	}

	public static void setArmorPreviewSide(ArmorPreviewSide value) {
		armorPreviewSide = value == null ? ArmorPreviewSide.LEFT : value;
	}

	public static boolean isRarityOutline() {
		return rarityOutline;
	}

	public static void setRarityOutline(boolean value) {
		rarityOutline = value;
	}

	public static ColorMode getTooltipOutlineColorMode() {
		return tooltipOutlineColorMode;
	}

	public static void setTooltipOutlineColorMode(ColorMode value) {
		tooltipOutlineColorMode = value == null ? ColorMode.STANDARD : value;
	}

	public static int getTooltipOutlineCustomColor() { return tooltipOutlineCustomColor; }
	public static void setTooltipOutlineCustomColor(int value) { tooltipOutlineCustomColor = forceOpaque(value); }

	public static ColorMode getRarityLabelColorMode() {
		return rarityLabelColorMode;
	}

	public static void setRarityLabelColorMode(ColorMode value) {
		rarityLabelColorMode = value == null ? ColorMode.STANDARD : value;
	}

	public static int getRarityLabelCustomColor() { return rarityLabelCustomColor; }
	public static void setRarityLabelCustomColor(int value) { rarityLabelCustomColor = forceOpaque(value); }

	public static int getRarityLabelColor(String rarity) {
		return rarityLabelColors[outlineRarityIndex(rarity)];
	}

	public static int defaultRarityLabelColor(String rarity) {
		return DEFAULT_RARITY_LABEL_COLORS[outlineRarityIndex(rarity)];
	}

	public static void setRarityLabelColor(String rarity, int value) {
		rarityLabelColors[outlineRarityIndex(rarity)] = forceOpaque(value);
	}

	public static int rarityLabelColor(String rarity, int rarityColor) {
		return switch (rarityLabelColorMode) {
			case STANDARD -> rarityColor;
			case RARITY -> rarityLabelColors[outlineRarityIndex(rarity)];
			case CUSTOM -> rarityLabelCustomColor;
			case GRADIENT -> rarityLabelGradientColor(rarity, 0.5D);
		};
	}

	public static int getTooltipOutlineColor(String rarity) {
		return tooltipOutlineColors[outlineRarityIndex(rarity)];
	}

	public static void setTooltipOutlineColor(String rarity, int value) {
		tooltipOutlineColors[outlineRarityIndex(rarity)] = forceOpaque(value);
	}

	public static int defaultTooltipOutlineColor(String rarity) {
		return DEFAULT_TOOLTIP_OUTLINE_COLORS[outlineRarityIndex(rarity)];
	}

	public static int tooltipOutlineColor(String rarity, int rarityColor) {
		return switch (tooltipOutlineColorMode) {
			case STANDARD -> DEFAULT_TOOLTIP_OUTLINE_COLOR;
			case RARITY -> tooltipOutlineColors[outlineRarityIndex(rarity)];
			case CUSTOM -> tooltipOutlineCustomColor;
			case GRADIENT -> tooltipOutlineGradientColor(rarity, 0.5D);
		};
	}

	public static boolean isShowItemFrame() { return showItemFrame; }
	public static void setShowItemFrame(boolean value) { showItemFrame = value; }
	public static boolean isShowItemPreviewBackground() { return showItemPreviewBackground; }
	public static void setShowItemPreviewBackground(boolean value) { showItemPreviewBackground = value; }

	public static PreviewColorMode getItemFrameColorMode() {
		return itemFrameColorMode;
	}

	public static void setItemFrameColorMode(PreviewColorMode value) {
		itemFrameColorMode = value == null ? PreviewColorMode.STANDARD : value;
	}

	public static int getItemFrameCustomColor() {
		return itemFrameCustomColor;
	}

	public static void setItemFrameCustomColor(int value) {
		itemFrameCustomColor = value;
	}

	public static int getRarityItemFrameColor(String rarity) {
		return itemFrameColors[outlineRarityIndex(rarity)];
	}

	public static int getItemFrameColor(String rarity) {
		return switch (itemFrameColorMode) {
			case STANDARD -> DEFAULT_ITEM_FRAME_COLOR;
			case RARITY -> getRarityItemFrameColor(rarity);
			case CUSTOM -> itemFrameCustomColor;
			case GRADIENT -> itemFrameGradientColor(rarity, 0.5D);
		};
	}

	public static void setItemFrameColor(String rarity, int value) {
		itemFrameColors[outlineRarityIndex(rarity)] = value;
	}

	public static int defaultItemFrameColor(String rarity) {
		return DEFAULT_ITEM_FRAME_COLORS[outlineRarityIndex(rarity)];
	}

	public static int getItemPreviewBackgroundColor() {
		return itemPreviewBackgroundColor;
	}

	public static PreviewColorMode getItemPreviewBackgroundColorMode() {
		return itemPreviewBackgroundColorMode;
	}

	public static void setItemPreviewBackgroundColorMode(PreviewColorMode value) {
		itemPreviewBackgroundColorMode = value == null ? PreviewColorMode.STANDARD : value;
	}

	public static int getItemPreviewBackgroundRarityColor(String rarity) {
		return itemPreviewBackgroundColors[outlineRarityIndex(rarity)];
	}

	public static int defaultItemPreviewBackgroundRarityColor(String rarity) {
		return DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS[outlineRarityIndex(rarity)];
	}

	public static void setItemPreviewBackgroundRarityColor(String rarity, int value) {
		itemPreviewBackgroundColors[outlineRarityIndex(rarity)] = value;
	}

	public static int resolveItemPreviewBackgroundColor(String rarity) {
		return switch (itemPreviewBackgroundColorMode) {
			case STANDARD -> DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR;
			case RARITY -> getItemPreviewBackgroundRarityColor(rarity);
			case CUSTOM -> itemPreviewBackgroundColor;
			case GRADIENT -> itemPreviewBackgroundGradientColor(rarity, 0.5D);
		};
	}

	public static void setItemPreviewBackgroundColor(int value) {
		itemPreviewBackgroundColor = value;
	}

	public static int getAnimationSpeedPercent() {
		return animationSpeedPercent;
	}

	public static void setAnimationSpeedPercent(int value) {
		animationSpeedPercent = clampAnimationSpeedPercent(value);
	}

	public static int getAnimationDurationMs() {
		return switch (animationSpeedPercent) {
			case 100 -> ANIMATION_DURATION_AT_100_PERCENT_MS;
			case 75 -> ANIMATION_DURATION_AT_75_PERCENT_MS;
			default -> ANIMATION_DURATION_AT_50_PERCENT_MS;
		};
	}

	public static boolean isRarityLabelGradient() { return rarityLabelColorMode == ColorMode.GRADIENT; }
	public static int rarityLabelGradientColor(String rarity, double progress) {
		int index = outlineRarityIndex(rarity);
		return rarityGradientColor(rarityLabelColors[index], DEFAULT_RARITY_LABEL_COLORS[index],
				DEFAULT_RARITY_LABEL_GRADIENT_END_COLORS[index], progress);
	}
	public static boolean isTooltipOutlineGradient() { return tooltipOutlineColorMode == ColorMode.GRADIENT; }
	public static int tooltipOutlineGradientColor(String rarity, double progress) {
		int index = outlineRarityIndex(rarity);
		return rarityGradientColor(tooltipOutlineColors[index], DEFAULT_TOOLTIP_OUTLINE_COLORS[index],
				DEFAULT_TOOLTIP_OUTLINE_GRADIENT_END_COLORS[index], progress);
	}
	public static boolean isItemFrameGradient() { return itemFrameColorMode == PreviewColorMode.GRADIENT; }
	public static int itemFrameGradientColor(String rarity, double progress) {
		int index = outlineRarityIndex(rarity);
		return rarityGradientColor(itemFrameColors[index], DEFAULT_ITEM_FRAME_COLORS[index],
				DEFAULT_ITEM_FRAME_GRADIENT_END_COLORS[index], progress);
	}
	public static boolean isItemPreviewBackgroundGradient() {
		return itemPreviewBackgroundColorMode == PreviewColorMode.GRADIENT;
	}
	public static int itemPreviewBackgroundGradientColor(String rarity, double progress) {
		int index = outlineRarityIndex(rarity);
		return rarityGradientColor(itemPreviewBackgroundColors[index], DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS[index],
				DEFAULT_ITEM_PREVIEW_BACKGROUND_GRADIENT_END_COLORS[index], progress);
	}

	public static Snapshot snapshot() {
		return new Snapshot(enabled, showRarity, showItemPreview, animateItemPreview,
				itemPreviewScale, showArmorPreview, showArmorItemPreview, armorPreviewScale, armorStandOpacity,
				animateArmorPreview, armorRotationSpeed, armorRotationDirection, armorPreviewSide,
				tooltipSeparationStyle, rarityLabelColorMode, rarityLabelColors.clone(), rarityLabelCustomColor, rarityOutline,
				tooltipOutlineColorMode, tooltipOutlineColors.clone(), tooltipOutlineCustomColor, itemFrameColorMode,
				showItemFrame, itemFrameCustomColor,
				itemFrameColors.clone(), itemPreviewBackgroundColorMode, itemPreviewBackgroundColors.clone(),
				showItemPreviewBackground, itemPreviewBackgroundColor,
				animationSpeedPercent);
	}

	public static void restore(Snapshot snapshot) {
		enabled = snapshot.enabled();
		showRarity = snapshot.showRarity();
		showItemPreview = snapshot.showItemPreview();
		animateItemPreview = snapshot.animateItemPreview();
		itemPreviewScale = snapshot.itemPreviewScale();
		showArmorPreview = snapshot.showArmorPreview();
		showArmorItemPreview = snapshot.showArmorItemPreview();
		armorPreviewScale = snapshot.armorPreviewScale();
		armorStandOpacity = snapshot.armorStandOpacity();
		animateArmorPreview = snapshot.animateArmorPreview();
		armorRotationSpeed = snapshot.armorRotationSpeed();
		armorRotationDirection = snapshot.armorRotationDirection();
		armorPreviewSide = snapshot.armorPreviewSide();
		tooltipSeparationStyle = snapshot.tooltipSeparationStyle();
		rarityLabelColorMode = snapshot.rarityLabelColorMode();
		rarityLabelColors = snapshot.rarityLabelColors().clone();
		rarityLabelCustomColor = snapshot.rarityLabelCustomColor();
		rarityOutline = snapshot.rarityOutline();
		tooltipOutlineColorMode = snapshot.tooltipOutlineColorMode();
		tooltipOutlineColors = snapshot.tooltipOutlineColors().clone();
		tooltipOutlineCustomColor = snapshot.tooltipOutlineCustomColor();
		itemFrameColorMode = snapshot.itemFrameColorMode();
		showItemFrame = snapshot.showItemFrame();
		itemFrameCustomColor = snapshot.itemFrameCustomColor();
		itemFrameColors = snapshot.itemFrameColors().clone();
		itemPreviewBackgroundColorMode = snapshot.itemPreviewBackgroundColorMode();
		itemPreviewBackgroundColors = snapshot.itemPreviewBackgroundColors().clone();
		showItemPreviewBackground = snapshot.showItemPreviewBackground();
		itemPreviewBackgroundColor = snapshot.itemPreviewBackgroundColor();
		animationSpeedPercent = snapshot.animationSpeedPercent();
	}

	public static void resetDefaults() {
		enabled = true;
		showRarity = true;
		showItemPreview = true;
		animateItemPreview = true;
		itemPreviewScale = DEFAULT_ITEM_PREVIEW_SCALE;
		showArmorPreview = true;
		showArmorItemPreview = true;
		armorPreviewScale = DEFAULT_ARMOR_PREVIEW_SCALE;
		armorStandOpacity = DEFAULT_ARMOR_STAND_OPACITY;
		animateArmorPreview = true;
		armorRotationSpeed = DEFAULT_ARMOR_ROTATION_SPEED;
		armorRotationDirection = ArmorRotationDirection.RIGHT;
		armorPreviewSide = ArmorPreviewSide.LEFT;
		tooltipSeparationStyle = TooltipSeparationStyle.OFF;
		rarityLabelColorMode = ColorMode.GRADIENT;
		rarityLabelColors = DEFAULT_RARITY_LABEL_COLORS.clone();
		rarityLabelCustomColor = DEFAULT_RARITY_LABEL_COLORS[0];
		rarityOutline = true;
		tooltipOutlineColorMode = ColorMode.GRADIENT;
		tooltipOutlineColors = DEFAULT_TOOLTIP_OUTLINE_COLORS.clone();
		tooltipOutlineCustomColor = DEFAULT_TOOLTIP_OUTLINE_COLOR;
		itemFrameColorMode = PreviewColorMode.GRADIENT;
		showItemFrame = true;
		itemFrameCustomColor = DEFAULT_ITEM_FRAME_COLOR;
		itemFrameColors = DEFAULT_ITEM_FRAME_COLORS.clone();
		itemPreviewBackgroundColorMode = PreviewColorMode.GRADIENT;
		showItemPreviewBackground = true;
		itemPreviewBackgroundColors = DEFAULT_ITEM_PREVIEW_BACKGROUND_COLORS.clone();
		itemPreviewBackgroundColor = DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR;
		animationSpeedPercent = DEFAULT_ANIMATION_SPEED_PERCENT;
	}

	public record Snapshot(boolean enabled, boolean showRarity, boolean showItemPreview,
			boolean animateItemPreview, int itemPreviewScale, boolean showArmorPreview,
			boolean showArmorItemPreview, int armorPreviewScale, int armorStandOpacity, boolean animateArmorPreview,
			int armorRotationSpeed, ArmorRotationDirection armorRotationDirection, ArmorPreviewSide armorPreviewSide,
			TooltipSeparationStyle tooltipSeparationStyle, ColorMode rarityLabelColorMode, int[] rarityLabelColors,
			int rarityLabelCustomColor, boolean rarityOutline, ColorMode tooltipOutlineColorMode, int[] tooltipOutlineColors,
			int tooltipOutlineCustomColor, PreviewColorMode itemFrameColorMode, boolean showItemFrame,
			int itemFrameCustomColor, int[] itemFrameColors,
			PreviewColorMode itemPreviewBackgroundColorMode, int[] itemPreviewBackgroundColors,
			boolean showItemPreviewBackground, int itemPreviewBackgroundColor,
			int animationSpeedPercent) {}

	public enum PreviewColorMode {
		STANDARD("standard"),
		RARITY("rarity"),
		GRADIENT("gradient"),
		CUSTOM("custom");

		private final String serializedName;

		PreviewColorMode(String serializedName) {
			this.serializedName = serializedName;
		}

		public String serializedName() {
			return serializedName;
		}

		private static PreviewColorMode fromSerializedName(String name, PreviewColorMode fallback) {
			for (PreviewColorMode mode : values()) {
				if (mode.serializedName.equalsIgnoreCase(name)) {
					return mode;
				}
			}
			return fallback;
		}
	}

	public enum ColorMode {
		STANDARD("standard"),
		RARITY("rarity"),
		GRADIENT("gradient"),
		CUSTOM("custom");

		private final String serializedName;

		ColorMode(String serializedName) {
			this.serializedName = serializedName;
		}

		public String serializedName() {
			return serializedName;
		}

		private static ColorMode fromSerializedName(String name, ColorMode fallback) {
			for (ColorMode mode : values()) {
				if (mode.serializedName.equalsIgnoreCase(name)) {
					return mode;
				}
			}
			return fallback;
		}
	}

	public enum TooltipSeparationStyle {
		OFF("off"),
		LINES("lines"),
		PANELS("panels");

		private final String serializedName;

		TooltipSeparationStyle(String serializedName) {
			this.serializedName = serializedName;
		}

		public String serializedName() {
			return serializedName;
		}

		private static TooltipSeparationStyle fromSerializedName(String name) {
			for (TooltipSeparationStyle style : values()) {
				if (style.serializedName.equalsIgnoreCase(name)) {
					return style;
				}
			}
			return OFF;
		}
	}

	public enum ArmorPreviewSide {
		LEFT("left"),
		RIGHT("right");

		private final String serializedName;

		ArmorPreviewSide(String serializedName) {
			this.serializedName = serializedName;
		}

		public String serializedName() {
			return serializedName;
		}

		private static ArmorPreviewSide fromSerializedName(String name) {
			for (ArmorPreviewSide side : values()) {
				if (side.serializedName.equalsIgnoreCase(name)) {
					return side;
				}
			}
			return LEFT;
		}
	}

	public enum ArmorRotationDirection {
		RIGHT("right", 1.0F),
		LEFT("left", -1.0F);

		private final String serializedName;
		private final float multiplier;

		ArmorRotationDirection(String serializedName, float multiplier) {
			this.serializedName = serializedName;
			this.multiplier = multiplier;
		}

		public String serializedName() {
			return serializedName;
		}

		public float multiplier() {
			return multiplier;
		}

		private static ArmorRotationDirection fromSerializedName(String name) {
			if ("clockwise".equalsIgnoreCase(name)) {
				return RIGHT;
			}
			if ("counterclockwise".equalsIgnoreCase(name)) {
				return LEFT;
			}
			for (ArmorRotationDirection direction : values()) {
				if (direction.serializedName.equalsIgnoreCase(name)) {
					return direction;
				}
			}
			return RIGHT;
		}
	}

	private static String outlineColorPropertyPrefix(int index) {
		String rarity = OUTLINE_RARITIES[index];
		return "tooltipOutline" + Character.toUpperCase(rarity.charAt(0)) + rarity.substring(1);
	}

	private static String itemFrameColorPropertyPrefix(int index) {
		String rarity = OUTLINE_RARITIES[index];
		return "itemFrame" + Character.toUpperCase(rarity.charAt(0)) + rarity.substring(1);
	}

	private static String itemPreviewBackgroundColorPropertyPrefix(int index) {
		String rarity = OUTLINE_RARITIES[index];
		return "itemPreviewBackground" + Character.toUpperCase(rarity.charAt(0)) + rarity.substring(1);
	}

	private static String rarityLabelColorPropertyPrefix(int index) {
		String rarity = OUTLINE_RARITIES[index];
		return "rarityLabel" + Character.toUpperCase(rarity.charAt(0)) + rarity.substring(1);
	}

	private static int outlineRarityIndex(String rarity) {
		if (rarity != null) {
			for (int index = 0; index < OUTLINE_RARITIES.length; index++) {
				if (OUTLINE_RARITIES[index].equalsIgnoreCase(rarity)) {
					return index;
				}
			}
		}
		return 0;
	}

	private static int clampAnimationSpeedPercent(int value) {
		int clamped = Math.max(MIN_ANIMATION_SPEED_PERCENT, Math.min(MAX_ANIMATION_SPEED_PERCENT, value));
		int steps = Math.round((clamped - MIN_ANIMATION_SPEED_PERCENT) / (float) ANIMATION_SPEED_STEP_PERCENT);
		return MIN_ANIMATION_SPEED_PERCENT + steps * ANIMATION_SPEED_STEP_PERCENT;
	}

	private static int animationSpeedPercentFromDuration(int durationMs) {
		if (durationMs <= 150) {
			return 100;
		}
		if (durationMs <= 250) {
			return 75;
		}
		return 50;
	}

	private static int clampArmorRotationSpeed(int value) {
		return Math.max(MIN_ARMOR_ROTATION_SPEED, Math.min(MAX_ARMOR_ROTATION_SPEED, value));
	}

	private static int clampItemPreviewScale(int value) {
		return Math.max(MIN_ITEM_PREVIEW_SCALE, Math.min(MAX_ITEM_PREVIEW_SCALE, value));
	}

	private static int clampArmorPreviewScale(int value) {
		return Math.max(MIN_ARMOR_PREVIEW_SCALE, Math.min(MAX_ARMOR_PREVIEW_SCALE, value));
	}

	private static int clampArmorStandOpacity(int value) {
		return Math.max(MIN_ARMOR_STAND_OPACITY, Math.min(MAX_ARMOR_STAND_OPACITY, value));
	}

	private static int rarityGradientColor(int baseColor, int defaultBaseColor, int defaultEndColor, double progress) {
		int endColor = (baseColor & 0xFF000000)
				| shiftGradientChannel(baseColor, defaultBaseColor, defaultEndColor, 16) << 16
				| shiftGradientChannel(baseColor, defaultBaseColor, defaultEndColor, 8) << 8
				| shiftGradientChannel(baseColor, defaultBaseColor, defaultEndColor, 0);
		return interpolateGradientColor(baseColor, endColor, progress);
	}

	private static int shiftGradientChannel(int baseColor, int defaultBaseColor, int defaultEndColor, int shift) {
		int baseChannel = (baseColor >>> shift) & 0xFF;
		int defaultBaseChannel = (defaultBaseColor >>> shift) & 0xFF;
		int defaultEndChannel = (defaultEndColor >>> shift) & 0xFF;
		return Math.max(0, Math.min(255, baseChannel + defaultEndChannel - defaultBaseChannel));
	}

	private static int interpolateGradientColor(int startColor, int endColor, double progress) {
		double clampedProgress = Math.max(0.0D, Math.min(1.0D, progress));
		double amount = clampedProgress;
		return interpolateColorChannel(startColor, endColor, 24, amount) << 24
				| interpolateColorChannel(startColor, endColor, 16, amount) << 16
				| interpolateColorChannel(startColor, endColor, 8, amount) << 8
				| interpolateColorChannel(startColor, endColor, 0, amount);
	}

	private static int interpolateColorChannel(int startColor, int endColor, int shift, double amount) {
		int startChannel = (startColor >>> shift) & 0xFF;
		int endChannel = (endColor >>> shift) & 0xFF;
		return (int) Math.round(startChannel + (endChannel - startChannel) * amount);
	}

	private static int forceOpaque(int color) {
		return color | 0xFF000000;
	}
}

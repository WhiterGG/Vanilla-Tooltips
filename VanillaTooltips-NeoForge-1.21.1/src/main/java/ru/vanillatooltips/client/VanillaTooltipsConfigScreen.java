package ru.vanillatooltips.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionEventListener;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import java.awt.Color;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.IntFunction;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.vanillatooltips.VanillaTooltipsConfig;
import ru.vanillatooltips.VanillaTooltipsConfig.ArmorPreviewSide;
import ru.vanillatooltips.VanillaTooltipsConfig.ArmorRotationDirection;
import ru.vanillatooltips.VanillaTooltipsConfig.ColorMode;
import ru.vanillatooltips.VanillaTooltipsConfig.PreviewColorMode;
import ru.vanillatooltips.VanillaTooltipsConfig.TooltipSeparationStyle;

/** YACL-powered settings screen shared conceptually by every loader port. */
public final class VanillaTooltipsConfigScreen {
	private static final Set<YetAnotherConfigLib> OWN_CONFIGS =
			Collections.newSetFromMap(new WeakHashMap<>());

	private VanillaTooltipsConfigScreen() {}

	public static Screen create(Screen parent) {
		Option<Boolean> enabled = booleanOption("enabled", true,
				VanillaTooltipsConfig::isEnabled, VanillaTooltipsConfig::setEnabled);
		Option<Boolean> showRarity = booleanOption("show_rarity", true,
				VanillaTooltipsConfig::isShowRarity, VanillaTooltipsConfig::setShowRarity);
		Option<Boolean> rarityOutline = booleanOption("rarity_outline", true,
				VanillaTooltipsConfig::isRarityOutline, VanillaTooltipsConfig::setRarityOutline);
		Option<ColorMode> outlineColorMode = colorModeOption("tooltip_outline_color_mode",
				ColorMode.GRADIENT, VanillaTooltipsConfig::getTooltipOutlineColorMode,
				VanillaTooltipsConfig::setTooltipOutlineColorMode);
		Option<Color> commonOutlineColor = outlineColorOption("common");
		Option<Color> uncommonOutlineColor = outlineColorOption("uncommon");
		Option<Color> rareOutlineColor = outlineColorOption("rare");
		Option<Color> epicOutlineColor = outlineColorOption("epic");
		Option<Color> customOutlineColor = colorOption("tooltip_outline_custom_color",
				VanillaTooltipsConfig.DEFAULT_TOOLTIP_OUTLINE_COLOR,
				VanillaTooltipsConfig::getTooltipOutlineCustomColor,
				VanillaTooltipsConfig::setTooltipOutlineCustomColor, false);
		Option<ColorMode> rarityLabelColorMode = colorModeOption("rarity_label_color_mode",
				ColorMode.GRADIENT, VanillaTooltipsConfig::getRarityLabelColorMode,
				VanillaTooltipsConfig::setRarityLabelColorMode);
		Option<Color> commonRarityLabelColor = rarityLabelColorOption("common");
		Option<Color> uncommonRarityLabelColor = rarityLabelColorOption("uncommon");
		Option<Color> rareRarityLabelColor = rarityLabelColorOption("rare");
		Option<Color> epicRarityLabelColor = rarityLabelColorOption("epic");
		Option<Color> customRarityLabelColor = colorOption("rarity_label_custom_color",
				VanillaTooltipsConfig.defaultRarityLabelColor("common"),
				VanillaTooltipsConfig::getRarityLabelCustomColor,
				VanillaTooltipsConfig::setRarityLabelCustomColor, false);
		Option<TooltipSeparationStyle> tooltipSeparation = Option.<TooltipSeparationStyle>createBuilder()
				.name(text("tooltip_separation"))
				.description(description("tooltip_separation"))
				.binding(TooltipSeparationStyle.OFF,
						VanillaTooltipsConfig::getTooltipSeparationStyle,
						VanillaTooltipsConfig::setTooltipSeparationStyle)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(TooltipSeparationStyle.class)
						.formatValue(style -> Component.translatable(
								"vanilla_tooltips.config.tooltip_separation." + style.serializedName())))
				.build();
		Option<Color> commonFrameColor = itemFrameColorOption("common");
		Option<Color> uncommonFrameColor = itemFrameColorOption("uncommon");
		Option<Color> rareFrameColor = itemFrameColorOption("rare");
		Option<Color> epicFrameColor = itemFrameColorOption("epic");
		Option<PreviewColorMode> itemFrameColorMode = previewColorModeOption("item_frame_color_mode",
				VanillaTooltipsConfig::getItemFrameColorMode, VanillaTooltipsConfig::setItemFrameColorMode);
		Option<Boolean> showItemFrame = booleanOption("show_item_frame", true,
				VanillaTooltipsConfig::isShowItemFrame, VanillaTooltipsConfig::setShowItemFrame);
		Option<Color> customItemFrameColor = colorOption("item_frame_custom_color",
				VanillaTooltipsConfig.DEFAULT_ITEM_FRAME_COLOR,
				VanillaTooltipsConfig::getItemFrameCustomColor,
				VanillaTooltipsConfig::setItemFrameCustomColor, true);

		Option<Boolean> showItem = booleanOption("show_preview", true,
				VanillaTooltipsConfig::isShowItemPreview, VanillaTooltipsConfig::setShowItemPreview);
		Option<Boolean> animateItem = booleanOption("animate_item_preview", true,
				VanillaTooltipsConfig::isAnimateItemPreview, VanillaTooltipsConfig::setAnimateItemPreview);
		Option<Integer> itemScale = integerSliderOption("item_preview_scale",
				VanillaTooltipsConfig.DEFAULT_ITEM_PREVIEW_SCALE,
				VanillaTooltipsConfig::getItemPreviewScale, VanillaTooltipsConfig::setItemPreviewScale,
				VanillaTooltipsConfig.MIN_ITEM_PREVIEW_SCALE,
				VanillaTooltipsConfig.MAX_ITEM_PREVIEW_SCALE, 5,
				value -> Component.translatable("vanilla_tooltips.config.value.percent", value));
		Option<Integer> itemSpeed = integerSliderOption("animation_speed",
				VanillaTooltipsConfig.DEFAULT_ANIMATION_SPEED_PERCENT,
				VanillaTooltipsConfig::getAnimationSpeedPercent,
				VanillaTooltipsConfig::setAnimationSpeedPercent,
				VanillaTooltipsConfig.MIN_ANIMATION_SPEED_PERCENT,
				VanillaTooltipsConfig.MAX_ANIMATION_SPEED_PERCENT,
				VanillaTooltipsConfig.ANIMATION_SPEED_STEP_PERCENT,
				value -> Component.translatable("vanilla_tooltips.config.value.percent", value));
		Option<Color> itemPreviewBackgroundColor = colorOption("item_preview_background_color",
				VanillaTooltipsConfig.DEFAULT_ITEM_PREVIEW_BACKGROUND_COLOR,
				VanillaTooltipsConfig::getItemPreviewBackgroundColor,
				VanillaTooltipsConfig::setItemPreviewBackgroundColor, true);
		Option<Color> commonBackgroundColor = itemPreviewBackgroundColorOption("common");
		Option<Color> uncommonBackgroundColor = itemPreviewBackgroundColorOption("uncommon");
		Option<Color> rareBackgroundColor = itemPreviewBackgroundColorOption("rare");
		Option<Color> epicBackgroundColor = itemPreviewBackgroundColorOption("epic");
		Option<PreviewColorMode> itemPreviewBackgroundColorMode = previewColorModeOption(
				"item_preview_background_color_mode", VanillaTooltipsConfig::getItemPreviewBackgroundColorMode,
				VanillaTooltipsConfig::setItemPreviewBackgroundColorMode);
		Option<Boolean> showItemPreviewBackground = booleanOption("show_item_preview_background", true,
				VanillaTooltipsConfig::isShowItemPreviewBackground,
				VanillaTooltipsConfig::setShowItemPreviewBackground);

		Option<Boolean> showArmor = booleanOption("show_armor_preview", true,
				VanillaTooltipsConfig::isShowArmorPreview, VanillaTooltipsConfig::setShowArmorPreview);
		Option<Boolean> showArmorItem = booleanOption("show_armor_item_preview", true,
				VanillaTooltipsConfig::isShowArmorItemPreview, VanillaTooltipsConfig::setShowArmorItemPreview);
		Option<ArmorPreviewSide> armorPreviewSide = Option.<ArmorPreviewSide>createBuilder()
				.name(text("armor_preview_side"))
				.description(description("armor_preview_side"))
				.binding(ArmorPreviewSide.LEFT,
						VanillaTooltipsConfig::getArmorPreviewSide,
						VanillaTooltipsConfig::setArmorPreviewSide)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(ArmorPreviewSide.class)
						.formatValue(side -> Component.translatable(
								"vanilla_tooltips.config.side." + side.serializedName())))
				.build();
		Option<Integer> armorScale = integerSliderOption("armor_preview_scale",
				VanillaTooltipsConfig.DEFAULT_ARMOR_PREVIEW_SCALE,
				VanillaTooltipsConfig::getArmorPreviewScale,
				VanillaTooltipsConfig::setArmorPreviewScale,
				VanillaTooltipsConfig.MIN_ARMOR_PREVIEW_SCALE,
				VanillaTooltipsConfig.MAX_ARMOR_PREVIEW_SCALE, 5,
				value -> Component.translatable("vanilla_tooltips.config.value.percent", value));
		Option<Integer> standOpacity = integerSliderOption("armor_stand_opacity",
				VanillaTooltipsConfig.DEFAULT_ARMOR_STAND_OPACITY,
				VanillaTooltipsConfig::getArmorStandOpacity,
				VanillaTooltipsConfig::setArmorStandOpacity,
				VanillaTooltipsConfig.MIN_ARMOR_STAND_OPACITY,
				VanillaTooltipsConfig.MAX_ARMOR_STAND_OPACITY, 5,
				value -> Component.translatable("vanilla_tooltips.config.value.percent", value));
		Option<Boolean> animateArmor = booleanOption("animate_armor_preview", true,
				VanillaTooltipsConfig::isAnimateArmorPreview, VanillaTooltipsConfig::setAnimateArmorPreview);
		Option<Integer> rotationSpeed = integerSliderOption("armor_rotation_speed",
				VanillaTooltipsConfig.DEFAULT_ARMOR_ROTATION_SPEED,
				VanillaTooltipsConfig::getArmorRotationSpeed,
				VanillaTooltipsConfig::setArmorRotationSpeed,
				VanillaTooltipsConfig.MIN_ARMOR_ROTATION_SPEED,
				VanillaTooltipsConfig.MAX_ARMOR_ROTATION_SPEED, 5,
				value -> Component.translatable(
						"vanilla_tooltips.config.value.degrees_per_second", value));
		Option<ArmorRotationDirection> rotationDirection = Option.<ArmorRotationDirection>createBuilder()
				.name(text("armor_rotation_direction"))
				.description(description("armor_rotation_direction"))
				.binding(ArmorRotationDirection.RIGHT,
						VanillaTooltipsConfig::getArmorRotationDirection,
						VanillaTooltipsConfig::setArmorRotationDirection)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(ArmorRotationDirection.class)
						.formatValue(direction -> Component.translatable(
								"vanilla_tooltips.config.direction." + direction.serializedName())))
				.build();

		Runnable updateItemColorAvailability = () -> {
			boolean preview = showItem.pendingValue();
			PreviewColorMode frameMode = itemFrameColorMode.pendingValue();
			PreviewColorMode backgroundMode = itemPreviewBackgroundColorMode.pendingValue();
			setAvailablePreservingValue(showItemFrame, preview);
			boolean frameVisible = preview && showItemFrame.pendingValue();
			setAvailablePreservingValue(itemFrameColorMode, frameVisible);
			boolean frameByRarity = frameVisible && (frameMode == PreviewColorMode.RARITY
					|| frameMode == PreviewColorMode.GRADIENT);
			setAvailablePreservingValue(commonFrameColor, frameByRarity);
			setAvailablePreservingValue(uncommonFrameColor, frameByRarity);
			setAvailablePreservingValue(rareFrameColor, frameByRarity);
			setAvailablePreservingValue(epicFrameColor, frameByRarity);
			setAvailablePreservingValue(customItemFrameColor,
					frameVisible && frameMode == PreviewColorMode.CUSTOM);
			setAvailablePreservingValue(showItemPreviewBackground, preview);
			boolean backgroundVisible = preview && showItemPreviewBackground.pendingValue();
			setAvailablePreservingValue(itemPreviewBackgroundColorMode, backgroundVisible);
			boolean backgroundByRarity = backgroundVisible && (backgroundMode == PreviewColorMode.RARITY
					|| backgroundMode == PreviewColorMode.GRADIENT);
			setAvailablePreservingValue(commonBackgroundColor, backgroundByRarity);
			setAvailablePreservingValue(uncommonBackgroundColor, backgroundByRarity);
			setAvailablePreservingValue(rareBackgroundColor, backgroundByRarity);
			setAvailablePreservingValue(epicBackgroundColor, backgroundByRarity);
			setAvailablePreservingValue(itemPreviewBackgroundColor,
					backgroundVisible && backgroundMode == PreviewColorMode.CUSTOM);
		};
		OptionEventListener<Boolean> itemAvailability = (option, event) -> {
			boolean preview = showItem.pendingValue();
			setAvailablePreservingValue(animateItem, preview);
			setAvailablePreservingValue(itemScale, preview);
			setAvailablePreservingValue(itemSpeed, preview && animateItem.pendingValue());
			updateItemColorAvailability.run();
		};
		showItem.addEventListener(itemAvailability);
		animateItem.addEventListener(itemAvailability);
		itemAvailability.onEvent(showItem, OptionEventListener.Event.INITIAL);
		OptionEventListener<PreviewColorMode> itemColorModeAvailability = (option, event) ->
				updateItemColorAvailability.run();
		itemFrameColorMode.addEventListener(itemColorModeAvailability);
		itemPreviewBackgroundColorMode.addEventListener(itemColorModeAvailability);
		showItemFrame.addEventListener((option, event) -> updateItemColorAvailability.run());
		showItemPreviewBackground.addEventListener((option, event) -> updateItemColorAvailability.run());

		OptionEventListener<ColorMode> outlineModeAvailability = (option, event) -> {
			boolean visible = rarityOutline.pendingValue();
			boolean byRarity = visible && (outlineColorMode.pendingValue() == ColorMode.RARITY
					|| outlineColorMode.pendingValue() == ColorMode.GRADIENT);
			setAvailablePreservingValue(commonOutlineColor, byRarity);
			setAvailablePreservingValue(uncommonOutlineColor, byRarity);
			setAvailablePreservingValue(rareOutlineColor, byRarity);
			setAvailablePreservingValue(epicOutlineColor, byRarity);
			setAvailablePreservingValue(customOutlineColor, visible
					&& outlineColorMode.pendingValue() == ColorMode.CUSTOM);
		};
		outlineColorMode.addEventListener(outlineModeAvailability);
		outlineModeAvailability.onEvent(outlineColorMode, OptionEventListener.Event.INITIAL);

		OptionEventListener<Boolean> outlineAvailability = (option, event) -> {
			boolean visible = rarityOutline.pendingValue();
			setAvailablePreservingValue(outlineColorMode, visible);
			boolean byRarity = visible && (outlineColorMode.pendingValue() == ColorMode.RARITY
					|| outlineColorMode.pendingValue() == ColorMode.GRADIENT);
			setAvailablePreservingValue(commonOutlineColor, byRarity);
			setAvailablePreservingValue(uncommonOutlineColor, byRarity);
			setAvailablePreservingValue(rareOutlineColor, byRarity);
			setAvailablePreservingValue(epicOutlineColor, byRarity);
			setAvailablePreservingValue(customOutlineColor, visible
					&& outlineColorMode.pendingValue() == ColorMode.CUSTOM);
		};
		rarityOutline.addEventListener(outlineAvailability);
		outlineAvailability.onEvent(rarityOutline, OptionEventListener.Event.INITIAL);

		Runnable updateRarityLabelColorAvailability = () -> {
			boolean visible = showRarity.pendingValue();
			boolean byRarity = visible && (rarityLabelColorMode.pendingValue() == ColorMode.RARITY
					|| rarityLabelColorMode.pendingValue() == ColorMode.GRADIENT);
			setAvailablePreservingValue(rarityLabelColorMode, visible);
			setAvailablePreservingValue(commonRarityLabelColor, byRarity);
			setAvailablePreservingValue(uncommonRarityLabelColor, byRarity);
			setAvailablePreservingValue(rareRarityLabelColor, byRarity);
			setAvailablePreservingValue(epicRarityLabelColor, byRarity);
			setAvailablePreservingValue(customRarityLabelColor, visible
					&& rarityLabelColorMode.pendingValue() == ColorMode.CUSTOM);
		};
		OptionEventListener<Boolean> rarityLabelVisibility = (option, event) -> updateRarityLabelColorAvailability.run();
		OptionEventListener<ColorMode> rarityLabelModeAvailability = (option, event) -> updateRarityLabelColorAvailability.run();
		showRarity.addEventListener(rarityLabelVisibility);
		rarityLabelColorMode.addEventListener(rarityLabelModeAvailability);
		rarityLabelVisibility.onEvent(showRarity, OptionEventListener.Event.INITIAL);

		Runnable updateArmorAvailability = () -> {
			boolean preview = showArmor.pendingValue();
			boolean animation = preview && animateArmor.pendingValue();
			setAvailablePreservingValue(armorScale, preview);
			setAvailablePreservingValue(standOpacity, preview);
			setAvailablePreservingValue(showArmorItem,
					preview && standOpacity.pendingValue() > VanillaTooltipsConfig.MIN_ARMOR_STAND_OPACITY);
			setAvailablePreservingValue(armorPreviewSide, preview);
			setAvailablePreservingValue(animateArmor, preview);
			setAvailablePreservingValue(rotationSpeed, animation);
			setAvailablePreservingValue(rotationDirection, animation);
		};
		OptionEventListener<Boolean> armorAvailability = (option, event) -> updateArmorAvailability.run();
		OptionEventListener<Integer> standOpacityAvailability = (option, event) -> updateArmorAvailability.run();
		showArmor.addEventListener(armorAvailability);
		animateArmor.addEventListener(armorAvailability);
		standOpacity.addEventListener(standOpacityAvailability);
		armorAvailability.onEvent(showArmor, OptionEventListener.Event.INITIAL);

		YetAnotherConfigLib config = YetAnotherConfigLib.createBuilder()
			.title(Component.translatable("vanilla_tooltips.config.title"))
			.category(ConfigCategory.createBuilder()
					.name(category("general"))
					.group(OptionGroup.createBuilder()
							.name(group("general"))
							.option(enabled)
							.build())
					.group(OptionGroup.createBuilder()
							.name(group("rarity"))
							.option(showRarity)
							.option(rarityLabelColorMode)
							.option(commonRarityLabelColor)
							.option(uncommonRarityLabelColor)
							.option(rareRarityLabelColor)
							.option(epicRarityLabelColor)
							.option(customRarityLabelColor)
							.build())
					.group(OptionGroup.createBuilder()
							.name(group("outline_colors"))
							.option(rarityOutline)
							.option(outlineColorMode)
							.option(commonOutlineColor)
							.option(uncommonOutlineColor)
							.option(rareOutlineColor)
							.option(epicOutlineColor)
							.option(customOutlineColor)
							.build())
					.build())
			.category(ConfigCategory.createBuilder()
					.name(category("item"))
					.group(OptionGroup.createBuilder()
							.name(group("item_preview"))
							.option(showItem)
							.option(itemScale)
							.option(animateItem)
							.option(itemSpeed)
							.build())
					.group(OptionGroup.createBuilder()
							.name(group("item_frame_colors"))
							.option(showItemFrame)
							.option(itemFrameColorMode)
							.option(commonFrameColor)
							.option(uncommonFrameColor)
							.option(rareFrameColor)
							.option(epicFrameColor)
							.option(customItemFrameColor)
							.build())
					.group(OptionGroup.createBuilder()
							.name(group("item_frame"))
							.option(showItemPreviewBackground)
							.option(itemPreviewBackgroundColorMode)
							.option(commonBackgroundColor)
							.option(uncommonBackgroundColor)
							.option(rareBackgroundColor)
							.option(epicBackgroundColor)
							.option(itemPreviewBackgroundColor)
							.build())
					.build())
			.category(ConfigCategory.createBuilder()
					.name(category("armor"))
					.group(OptionGroup.createBuilder()
							.name(group("armor_preview"))
							.option(showArmor)
							.option(showArmorItem)
							.option(armorPreviewSide)
							.option(tooltipSeparation)
							.option(armorScale)
							.option(standOpacity)
							.build())
					.group(OptionGroup.createBuilder()
							.name(group("armor_animation"))
							.option(animateArmor)
							.option(rotationSpeed)
							.option(rotationDirection)
							.build())
					.build())
			.save(VanillaTooltipsConfig::save)
			.build();
		OWN_CONFIGS.add(config);
		return config.generateScreen(parent);
	}

	public static boolean owns(YetAnotherConfigLib config) {
		return OWN_CONFIGS.contains(config);
	}

	private static Option<Color> outlineColorOption(String rarity) {
		return colorOption("tooltip_outline_" + rarity + "_color",
				VanillaTooltipsConfig.defaultTooltipOutlineColor(rarity),
				() -> VanillaTooltipsConfig.getTooltipOutlineColor(rarity),
				value -> VanillaTooltipsConfig.setTooltipOutlineColor(rarity, value), false);
	}

	private static Option<Color> rarityLabelColorOption(String rarity) {
		return colorOption("rarity_label_" + rarity + "_color",
				VanillaTooltipsConfig.defaultRarityLabelColor(rarity),
				() -> VanillaTooltipsConfig.getRarityLabelColor(rarity),
				value -> VanillaTooltipsConfig.setRarityLabelColor(rarity, value), false);
	}

	private static Option<Color> itemFrameColorOption(String rarity) {
		return colorOption("item_frame_" + rarity + "_color",
				VanillaTooltipsConfig.defaultItemFrameColor(rarity),
				() -> VanillaTooltipsConfig.getRarityItemFrameColor(rarity),
				value -> VanillaTooltipsConfig.setItemFrameColor(rarity, value), true);
	}

	private static Option<Color> itemPreviewBackgroundColorOption(String rarity) {
		String rarityColorKey = "item_frame_" + rarity + "_color";
		String descriptionKey = "item_preview_background_" + rarity + "_color";
		return Option.<Color>createBuilder()
				.name(text(rarityColorKey))
				.description(description(descriptionKey))
				.binding(new Color(VanillaTooltipsConfig.defaultItemPreviewBackgroundRarityColor(rarity), true),
						() -> new Color(VanillaTooltipsConfig.getItemPreviewBackgroundRarityColor(rarity), true),
						color -> VanillaTooltipsConfig.setItemPreviewBackgroundRarityColor(rarity, color.getRGB()))
				.controller(option -> ColorControllerBuilder.create(option).allowAlpha(true))
				.build();
	}

	private static Option<Boolean> booleanOption(String key, boolean defaultValue,
			java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(text(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> BooleanControllerBuilder.create(option).coloured(true).onOffFormatter())
				.build();
	}

	private static Option<Integer> integerSliderOption(String key, int defaultValue,
			IntSupplier getter, IntConsumer setter, int minimum, int maximum, int step,
			IntFunction<Component> formatter) {
		return Option.<Integer>createBuilder()
				.name(text(key))
				.description(description(key))
				.binding(defaultValue, getter::getAsInt, setter::accept)
				.controller(option -> IntegerSliderControllerBuilder.create(option)
						.range(minimum, maximum)
						.step(step)
						.formatValue(value -> formatter.apply(value)))
				.build();
	}

	private static Option<ColorMode> colorModeOption(String key, ColorMode defaultValue,
			java.util.function.Supplier<ColorMode> getter,
			java.util.function.Consumer<ColorMode> setter) {
		return Option.<ColorMode>createBuilder()
				.name(text(key))
				.description(description(key))
				.binding(defaultValue, getter, setter)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(ColorMode.class)
						.formatValue(mode -> Component.translatable(
								"vanilla_tooltips.config.color_mode." + mode.serializedName())))
				.build();
	}

	private static Option<PreviewColorMode> previewColorModeOption(String key,
			java.util.function.Supplier<PreviewColorMode> getter,
			java.util.function.Consumer<PreviewColorMode> setter) {
		return Option.<PreviewColorMode>createBuilder()
				.name(text(key))
				.description(description(key))
				.binding(PreviewColorMode.GRADIENT, getter, setter)
				.controller(option -> EnumControllerBuilder.create(option)
						.enumClass(PreviewColorMode.class)
						.formatValue(mode -> Component.translatable(
								"vanilla_tooltips.config.preview_color_mode." + mode.serializedName())))
				.build();
	}

	private static Option<Color> colorOption(String key, int defaultValue,
			IntSupplier getter, IntConsumer setter, boolean allowAlpha) {
		return Option.<Color>createBuilder()
				.name(text(key))
				.description(description(key))
				.binding(new Color(defaultValue, true),
						() -> new Color(getter.getAsInt(), true),
						color -> setter.accept(allowAlpha ? color.getRGB() : color.getRGB() | 0xFF000000))
				.controller(option -> ColorControllerBuilder.create(option).allowAlpha(allowAlpha))
				.build();
	}

	private static Component text(String key) {
		return Component.translatable("vanilla_tooltips.config." + key);
	}

	private static Component category(String key) {
		return Component.translatable("vanilla_tooltips.config.category." + key);
	}

	private static Component group(String key) {
		return Component.translatable("vanilla_tooltips.config.group." + key);
	}

	private static OptionDescription description(String key) {
		return OptionDescription.of(Component.translatable("vanilla_tooltips.config.description." + key));
	}

	private static <T> void setAvailablePreservingValue(Option<T> option, boolean available) {
		T pendingValue = option.pendingValue();
		option.setAvailable(available);
		if (!Objects.equals(pendingValue, option.pendingValue())) {
			option.requestSet(pendingValue);
		}
	}
}

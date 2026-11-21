# Vanilla Tooltips 3.0.0

**Vanilla Tooltips** is a lightweight, client-side cosmetic mod that enhances item tooltips while keeping Minecraft’s familiar visual style.

It adds animated item previews, rarity styling, and a 3D armor preview. Customize their appearance, colors, layout, and animations, or turn visual features off individually.

Get the latest version on [Modrinth](https://modrinth.com/mod/vanilla-tooltips).

![Before and after](https://cdn.modrinth.com/data/cached_images/84ce95a6fe0660418312759e0dacc5ed8fbebf49_0.webp)

![Animated item previews with rarity styling](https://cdn.modrinth.com/data/cached_images/40ada8523f85635b09cd6831584f7ad92ba9b166.gif)

## Features

- Animated item previews with adjustable size and animation speed
- Customizable item preview frames and backgrounds with Standard, By Rarity, Custom, and Gradient color modes
- Two-color vertical gradients that transition from top to bottom
- Options to hide the item preview frame or background
- Rarity labels and tooltip outlines with configurable colors and separate colors for each rarity
- Options to show or hide rarity labels and tooltip outlines
- 3D armor previews with adjustable size, position, armor stand opacity, and rotation speed and direction
- Optional armor preview separation from the item and its stats, with Line and Window styles, or no divider
- Independent settings for each visual feature

![Rotating 3D armor preview](https://cdn.modrinth.com/data/cached_images/cd17c1d02fb6ef6a5dc91b057e422e46d2eff46a.gif)

## Compatibility

Vanilla Tooltips is client-side and does not need to be installed on a server.

| Loader | Minecraft versions |
|:---|:---|
| Fabric | 26.3, 26.2, 1.21.11, 1.21.1 |
| Forge | 1.20.1 |
| NeoForge | 1.21.1 |

Fabric projects require Fabric API and YetAnotherConfigLib (YACL). Mod Menu is optional and adds the in-game configuration screen. Forge and NeoForge projects require YACL.

## Building

Each loader and Minecraft version has its own project folder. Open the folder for the target version and run `gradlew.bat build --no-daemon` on Windows, or `./gradlew build --no-daemon` on Linux and macOS. Use the Java version required by that Minecraft version. The built mod JAR is written to that project’s `build/libs` folder.

## License

The Vanilla Tooltips source code is available under the [MIT License](LICENSE). The NeoForge MDK template retains its separate license notice in `VanillaTooltips-NeoForge-1.21.1/TEMPLATE_LICENSE.txt`.

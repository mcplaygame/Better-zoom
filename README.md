# Better Zoom Pro

**A configurable client-side zoom mod for Minecraft Java Edition 1.8.9 (Forge).**

Better Zoom Pro adds zoom presets, hold/toggle behavior, smooth transitions, configurable mouse sensitivity, custom keyboard shortcuts, and a searchable responsive settings screen.

> **Build status:** GitHub Actions builds the JAR from the repository. A successful compile does not replace testing the mod inside a live Minecraft client; test it in a separate Forge 1.8.9 profile first.

## Features

- **Hold or Toggle Zoom** — default C; choose whether to hold the key or press it to toggle zoom.
- **Settings menu** — default V, with Zoom, Controls, and Advanced tabs.
- **Quick zoom presets** — 2x, 4x, 8x, and 16x.
- **Adjustable zoom range** — choose a maximum from 2x to 32x.
- **Zoom up/down** — default = and -; configured step size is shared with mouse-wheel zoom.
- **Reset Zoom** — default R, resets the current level to 4x (or the configured maximum if lower).
- **Smooth animation** — enable/disable and adjust transition speed.
- **Mouse-wheel zoom** — optionally adjust zoom while zoom is active.
- **Mouse sensitivity** — optionally reduce sensitivity while zooming and choose the multiplier.
- **Search and tooltips** — search settings by name/description and hover for explanations.
- **Responsive settings GUI** — tabs, scrollable settings, and a live zoom-level preview.
- **Custom keybinds** — rebind all five shortcuts; duplicate assignments show a warning.
- **Config profiles** — export and import settings profiles.
- **Persistent settings** — settings are stored in config/betterzoom.cfg.

## Default controls

| Action | Key |
| --- | --- |
| Hold / Toggle Zoom | C |
| Open Better Zoom Pro Settings | V |
| Increase Zoom Level | = |
| Decrease Zoom Level | - |
| Reset Zoom Level | R |

Keybindings are also listed under Minecraft Options → Controls.

## Settings

- Current zoom: 2x up to the selected maximum
- Maximum zoom: 2x–32x
- Zoom step: 0.1–4.0
- Smooth animation speed: 0.05–1.00
- Sensitivity multiplier: 5%–100%
- Smooth zoom, mouse wheel, reduced sensitivity, and hold/toggle mode: on/off or mode selection

Use **Reset Defaults** to restore the settings and keyboard shortcuts.

## Import and export profiles

Open V → **Advanced**:
1. Choose **Export** to create config/betterzoom-export.cfg.
2. To import a profile, copy the profile file to config/betterzoom-import.cfg.
3. Choose **Import** in the Advanced tab.

The import action reads settings only; it does not change your keyboard shortcuts. Back up your config before replacing it.

## Requirements

- Minecraft Java Edition 1.8.9
- Minecraft Forge 11.15.1.2318-1.8.9
- Java 8 runtime

## Download the built JAR

1. Open the repository's [GitHub Actions](https://github.com/mcplaygame/Better-zoom/actions).
2. Open the latest successful **Build Better Zoom Pro** run.
3. Under **Artifacts**, download BetterZoom-Pro-2.0.0 and extract the ZIP.
4. Put the mod JAR into your Minecraft 1.8.9 Forge mods folder.
5. Launch a separate Forge 1.8.9 profile and test before using it in your main game.

GitHub Actions artifacts expire after 14 days. Re-run the workflow to produce a fresh artifact.

## Build locally on Windows

1. Download the matching Forge 1.8.9 MDK.
2. Copy gradlew, gradlew.bat, and the full gradle/wrapper/ directory from the MDK into the repository root.
3. Open Command Prompt or PowerShell in the project folder.
4. Run:

   ~~~bat
   gradlew.bat setupDecompWorkspace
   gradlew.bat build
   ~~~

5. If the build succeeds, the JAR should appear in build/libs/.

## Project structure

~~~
src/main/java/com/predex/betterzoom/
  BetterZoom.java
  ZoomHandler.java
  GuiBetterZoom.java
src/main/resources/
  mcmod.info
  assets/betterzoom/lang/en_US.lang
.github/workflows/build.yml
build.gradle
settings.gradle
gradle.properties
~~~

## License

No license has been specified yet. Please ask the repository owner before redistributing the code.

## Contributing

Bug reports and pull requests are welcome. When reporting a problem, include your Minecraft version, Forge version, Java version, and relevant crash-log details.

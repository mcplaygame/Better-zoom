# Better Zoom

**A configurable client-side zoom mod for Minecraft Java Edition 1.8.9 (Forge).**

Better Zoom provides hold-to-zoom, smooth animation, mouse-wheel adjustment, separate zoom-up/down shortcuts, and a compact dark settings screen with cyan and magenta accents.

> **Build status:** The latest Java source revision compiled successfully with GitHub Actions, and a JAR artifact was produced. It has not yet been tested inside a live Minecraft client, so in-game behavior still needs testing.

## Features

- **Hold to Zoom** — default `C`.
- **Open settings** — default `V`.
- **Increase zoom level** — default `=`.
- **Decrease zoom level** — default `-`.
- **Mouse-wheel zoom** — optionally change zoom strength with the wheel while zooming.
- **Smooth zoom animation** — toggle it and adjust its speed.
- **Adjustable zoom step** — choose how much the wheel or zoom-up/down keys change the zoom.
- **Reduced mouse sensitivity** — toggle it and adjust the sensitivity multiplier.
- **Friendly settings GUI** — simple ON/OFF controls, plus/minus steppers, key rebinding, Reset Defaults, and Done.
- **Controls menu integration** — keybinds also appear under `Options → Controls`.
- **Saved settings** — values are written to `config/betterzoom.cfg`.

## Default controls

| Action | Key |
| --- | --- |
| Hold to Zoom | `C` |
| Open Better Zoom Settings | `V` |
| Increase Zoom Level | `=` |
| Decrease Zoom Level | `-` |

To change a key, open the Better Zoom menu and click its key field, then press your preferred keyboard key. You can also change the bindings from Minecraft's `Options → Controls`.

## Settings you can change

- Zoom strength: **2.0×–10.0×**
- Smooth zoom: **on/off**
- Animation speed: **0.05–1.00**
- Mouse-wheel zoom adjustment: **on/off**
- Zoom change per step: **0.1–2.0**
- Reduced mouse sensitivity: **on/off**
- Sensitivity multiplier: **5%–100%**
- All four keyboard shortcuts

Use **Reset Defaults** to restore the original settings and keybinds.

## Requirements

- Minecraft Java Edition **1.8.9**
- Minecraft Forge **11.15.1.2318-1.8.9**
- **Java 8 JDK**

## Download the built JAR

1. Open the repository's [GitHub Actions](https://github.com/mcplaygame/Better-zoom/actions).
2. Open the latest successful **Build Better Zoom** run.
3. Under **Artifacts**, download `BetterZoom-1.2.0` and extract the ZIP.
4. Put `BetterZoom-1.2.0.jar` into your Minecraft 1.8.9 Forge `mods` folder.
5. Test in a separate Forge 1.8.9 profile before using it in your main game.

GitHub Actions artifacts are temporary and expire after 14 days; build again from the Actions page if needed.

## Build locally on Windows

1. Download the matching Forge 1.8.9 MDK.
2. Copy `gradlew`, `gradlew.bat`, and the full `gradle/wrapper/` directory from the MDK into the repository root.
3. Open Command Prompt or PowerShell in the project folder.
4. Run:

   ```bat
   gradlew.bat setupDecompWorkspace
   gradlew.bat build
   ```

5. If the build succeeds, the JAR should appear in `build/libs/`.

## Project structure

```
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
```

## License

No license has been specified yet. Please ask the repository owner before redistributing the code.

## Contributing

Bug reports and pull requests are welcome. When reporting a problem, include your Minecraft version, Forge version, Java version, and relevant crash-log details.

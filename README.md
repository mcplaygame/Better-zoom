# Better Zoom

**A configurable client-side zoom mod for Minecraft Java Edition 1.8.9 (Forge).**

Better Zoom adds smooth hold-to-zoom, adjustable zoom strength, mouse-wheel control, customizable keyboard shortcuts, and a compact dark settings screen with cyan and magenta accents.

> **Project status:** Source code is available; the mod has not yet been verified with a successful build or live Minecraft test. Contributions and testing reports are welcome.

## Features

- **Hold to Zoom** — hold `C` by default.
- **Settings menu** — press `V` by default.
- **Zoom level up/down** — press `=` or `-` by default.
- **Mouse-wheel adjustment** — adjust zoom strength while zooming; toggle this in settings.
- **Smooth zoom** — enable or disable animated zoom transitions.
- **Reduced mouse sensitivity** — optionally lower sensitivity while zooming.
- **Custom settings GUI** — change zoom strength, sensitivity, toggles, and keybinds.
- **Minecraft Controls integration** — keybinds also appear under `Options → Controls`.
- **Persistent settings** — configuration is saved to `config/betterzoom.cfg`.

## Default controls

| Action | Default key |
| --- | --- |
| Hold to Zoom | `C` |
| Open Better Zoom Settings | `V` |
| Increase Zoom Level | `=` |
| Decrease Zoom Level | `-` |

Change keybinds from the Better Zoom settings screen or from Minecraft's `Options → Controls` menu.

## Requirements

- Minecraft Java Edition **1.8.9**
- Minecraft Forge **11.15.1.2318-1.8.9**
- **Java 8 JDK**
- The matching Minecraft Forge 1.8.9 MDK

## Build on Windows

1. Download the matching Forge 1.8.9 MDK.
2. Copy `gradlew`, `gradlew.bat`, and the full `gradle/wrapper/` directory from the MDK into this repository's root folder.
3. Open Command Prompt or PowerShell in the project directory.
4. Run:

   ```bat
   gradlew.bat setupDecompWorkspace
   gradlew.bat build
   ```

5. If the build succeeds, find the mod JAR in `build/libs/`.
6. Test it in a separate Minecraft 1.8.9 Forge profile before using it in your main game.

## Configuration

The mod writes settings to `config/betterzoom.cfg`. The in-game menu lets you adjust:

- Zoom multiplier
- Smooth zoom
- Mouse-wheel zoom adjustment
- Reduced mouse sensitivity and its multiplier
- Zoom, settings, zoom-in, and zoom-out keybinds

## Repository layout

```
src/main/java/com/predex/betterzoom/
  BetterZoom.java
  ZoomHandler.java
  GuiBetterZoom.java
src/main/resources/
  mcmod.info
  assets/betterzoom/lang/en_US.lang
build.gradle
settings.gradle
gradle.properties
```

## License

No license has been specified yet. Until a license is added, please ask the repository owner before redistributing modified or unmodified copies.

## Contributing

Bug reports, compatibility notes, and pull requests are welcome. Please include your Minecraft version, Forge version, Java version, and relevant crash-log details when reporting a problem.

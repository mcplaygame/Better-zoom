# Better Zoom — Minecraft Forge 1.8.9

A client-side zoom mod for Minecraft 1.8.9 using Forge `11.15.1.2318`.

## Features
- Hold-to-zoom (default: `C`)
- Open the Better Zoom settings menu (default: `V`)
- Increase zoom level with a configurable key (default: `=`)
- Decrease zoom level with a configurable key (default: `-`)
- Mouse-wheel zoom adjustment while zooming (can be toggled)
- Smooth zoom transition toggle
- Reduced mouse sensitivity while zooming, with adjustable multiplier
- Custom dark settings GUI; all four keybinds can be changed in the GUI or `Options → Controls`
- Settings saved to `config/betterzoom.cfg`

## Build requirements
- Java 8 JDK
- Minecraft Forge 1.8.9 MDK, version `11.15.1.2318`

## Build steps (Windows)
1. Copy `gradlew`, `gradlew.bat`, and the `gradle/wrapper/` directory from the matching Forge 1.8.9 MDK into this project root.
2. Open Command Prompt or PowerShell in the project folder.
3. Run `gradlew.bat setupDecompWorkspace`.
4. Run `gradlew.bat build`.
5. Find the mod JAR in `build/libs/`.

## Important
This repository contains the project source. It has not been compiled or tested in a live Minecraft client in this environment. Test it in a separate Forge 1.8.9 profile first.

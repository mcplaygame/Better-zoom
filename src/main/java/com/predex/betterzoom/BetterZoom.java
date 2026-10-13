package com.predex.betterzoom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

@Mod(
    modid = BetterZoom.MODID,
    name = BetterZoom.NAME,
    version = BetterZoom.VERSION,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true
)
public class BetterZoom {
    public static final String MODID = "betterzoom";
    public static final String NAME = "Better Zoom Pro";
    public static final String VERSION = "2.0.0";
    public static final float DEFAULT_ZOOM = 4.0F;
    public static final float MIN_ZOOM = 2.0F;
    public static final float ABSOLUTE_MAX_ZOOM = 32.0F;

    public static Configuration CONFIG;
    public static File configDirectory;

    public static float zoomFactor = DEFAULT_ZOOM;
    public static float maxZoomFactor = 16.0F;
    public static float zoomStep = 0.5F;
    public static float smoothSpeed = 0.22F;
    public static float sensitivityMultiplier = 0.45F;
    public static boolean smoothZoomEnabled = true;
    public static boolean scrollZoomEnabled = true;
    public static boolean reducedSensitivityEnabled = true;
    public static boolean holdToZoomEnabled = true;
    public static boolean zoomToggleState = false;
    public static String lastMessage = "";

    public static KeyBinding zoomKey;
    public static KeyBinding settingsKey;
    public static KeyBinding zoomInKey;
    public static KeyBinding zoomOutKey;
    public static KeyBinding resetZoomKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDirectory = event.getModConfigurationDirectory();
        CONFIG = new Configuration(new File(configDirectory, "betterzoom.cfg"));
        loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        zoomKey = new KeyBinding("key.betterzoom.zoom", Keyboard.KEY_C, "key.categories.betterzoom");
        settingsKey = new KeyBinding("key.betterzoom.settings", Keyboard.KEY_V, "key.categories.betterzoom");
        zoomInKey = new KeyBinding("key.betterzoom.zoom_in", Keyboard.KEY_EQUALS, "key.categories.betterzoom");
        zoomOutKey = new KeyBinding("key.betterzoom.zoom_out", Keyboard.KEY_MINUS, "key.categories.betterzoom");
        resetZoomKey = new KeyBinding("key.betterzoom.reset_zoom", Keyboard.KEY_R, "key.categories.betterzoom");

        ClientRegistry.registerKeyBinding(zoomKey);
        ClientRegistry.registerKeyBinding(settingsKey);
        ClientRegistry.registerKeyBinding(zoomInKey);
        ClientRegistry.registerKeyBinding(zoomOutKey);
        ClientRegistry.registerKeyBinding(resetZoomKey);

        ZoomHandler handler = new ZoomHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        FMLCommonHandler.instance().bus().register(this);
    }

    public static void loadConfig() {
        CONFIG.load();
        zoomFactor = CONFIG.getFloat("zoomFactor", "general", DEFAULT_ZOOM, MIN_ZOOM, ABSOLUTE_MAX_ZOOM,
            "Current zoom multiplier.");
        maxZoomFactor = CONFIG.getFloat("maxZoomFactor", "general", 16.0F, MIN_ZOOM, ABSOLUTE_MAX_ZOOM,
            "Maximum allowed zoom multiplier.");
        zoomStep = CONFIG.getFloat("zoomStep", "general", 0.5F, 0.1F, 4.0F,
            "How much zoom level changes per key press or mouse-wheel notch.");
        smoothSpeed = CONFIG.getFloat("smoothSpeed", "general", 0.22F, 0.05F, 1.0F,
            "Smooth transition speed. Higher is faster.");
        sensitivityMultiplier = CONFIG.getFloat("sensitivityMultiplier", "general", 0.45F, 0.05F, 1.0F,
            "Mouse sensitivity multiplier while zooming.");
        smoothZoomEnabled = CONFIG.getBoolean("smoothZoomEnabled", "general", true,
            "Enable smooth zoom transitions.");
        scrollZoomEnabled = CONFIG.getBoolean("scrollZoomEnabled", "general", true,
            "Allow the mouse wheel to change zoom level while zooming.");
        reducedSensitivityEnabled = CONFIG.getBoolean("reducedSensitivityEnabled", "general", true,
            "Reduce mouse sensitivity while zooming.");
        holdToZoomEnabled = CONFIG.getBoolean("holdToZoomEnabled", "general", true,
            "When true, hold the zoom key. When false, press it to toggle zoom.");
        clampZoom();
        if (CONFIG.hasChanged()) CONFIG.save();
    }

    public static void saveConfig() {
        if (CONFIG == null) return;
        CONFIG.get("general", "zoomFactor", DEFAULT_ZOOM).set(zoomFactor);
        CONFIG.get("general", "maxZoomFactor", 16.0F).set(maxZoomFactor);
        CONFIG.get("general", "zoomStep", 0.5F).set(zoomStep);
        CONFIG.get("general", "smoothSpeed", 0.22F).set(smoothSpeed);
        CONFIG.get("general", "sensitivityMultiplier", 0.45F).set(sensitivityMultiplier);
        CONFIG.get("general", "smoothZoomEnabled", true).set(smoothZoomEnabled);
        CONFIG.get("general", "scrollZoomEnabled", true).set(scrollZoomEnabled);
        CONFIG.get("general", "reducedSensitivityEnabled", true).set(reducedSensitivityEnabled);
        CONFIG.get("general", "holdToZoomEnabled", true).set(holdToZoomEnabled);
        CONFIG.save();
    }

    public static void clampZoom() {
        if (maxZoomFactor < MIN_ZOOM) maxZoomFactor = MIN_ZOOM;
        if (maxZoomFactor > ABSOLUTE_MAX_ZOOM) maxZoomFactor = ABSOLUTE_MAX_ZOOM;
        if (zoomFactor < MIN_ZOOM) zoomFactor = MIN_ZOOM;
        if (zoomFactor > maxZoomFactor) zoomFactor = maxZoomFactor;
    }

    public static void setZoom(float value) {
        zoomFactor = Math.max(MIN_ZOOM, Math.min(maxZoomFactor, value));
        saveConfig();
    }

    public static void resetZoomLevel() {
        setZoom(Math.min(DEFAULT_ZOOM, maxZoomFactor));
        lastMessage = "Zoom reset to " + String.format(java.util.Locale.ROOT, "%.1fx", zoomFactor);
    }

    public static void resetKeyBindings() {
        if (zoomKey == null || settingsKey == null || zoomInKey == null || zoomOutKey == null || resetZoomKey == null) return;
        zoomKey.setKeyCode(Keyboard.KEY_C);
        settingsKey.setKeyCode(Keyboard.KEY_V);
        zoomInKey.setKeyCode(Keyboard.KEY_EQUALS);
        zoomOutKey.setKeyCode(Keyboard.KEY_MINUS);
        resetZoomKey.setKeyCode(Keyboard.KEY_R);
        KeyBinding.resetKeyBindingArrayAndHash();
        Minecraft.getMinecraft().gameSettings.saveOptions();
    }

    public static void resetDefaults() {
        zoomFactor = DEFAULT_ZOOM;
        maxZoomFactor = 16.0F;
        zoomStep = 0.5F;
        smoothSpeed = 0.22F;
        sensitivityMultiplier = 0.45F;
        smoothZoomEnabled = true;
        scrollZoomEnabled = true;
        reducedSensitivityEnabled = true;
        holdToZoomEnabled = true;
        zoomToggleState = false;
        resetKeyBindings();
        saveConfig();
        lastMessage = "All settings restored to defaults.";
    }

    public static void exportConfig() {
        try {
            saveConfig();
            if (configDirectory == null) throw new IOException("Config directory is unavailable");
            File source = new File(configDirectory, "betterzoom.cfg");
            File destination = new File(configDirectory, "betterzoom-export.cfg");
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            lastMessage = "Exported: config/betterzoom-export.cfg";
        } catch (IOException ex) {
            lastMessage = "Export failed: " + ex.getMessage();
        }
    }

    public static void importConfig() {
        if (configDirectory == null) {
            lastMessage = "Import failed: config directory unavailable.";
            return;
        }
        File source = new File(configDirectory, "betterzoom-import.cfg");
        if (!source.isFile()) {
            lastMessage = "Place a profile at config/betterzoom-import.cfg first.";
            return;
        }
        Configuration imported = new Configuration(source);
        imported.load();
        zoomFactor = imported.getFloat("zoomFactor", "general", DEFAULT_ZOOM, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "");
        maxZoomFactor = imported.getFloat("maxZoomFactor", "general", 16.0F, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "");
        zoomStep = imported.getFloat("zoomStep", "general", 0.5F, 0.1F, 4.0F, "");
        smoothSpeed = imported.getFloat("smoothSpeed", "general", 0.22F, 0.05F, 1.0F, "");
        sensitivityMultiplier = imported.getFloat("sensitivityMultiplier", "general", 0.45F, 0.05F, 1.0F, "");
        smoothZoomEnabled = imported.getBoolean("smoothZoomEnabled", "general", true, "");
        scrollZoomEnabled = imported.getBoolean("scrollZoomEnabled", "general", true, "");
        reducedSensitivityEnabled = imported.getBoolean("reducedSensitivityEnabled", "general", true, "");
        holdToZoomEnabled = imported.getBoolean("holdToZoomEnabled", "general", true, "");
        clampZoom();
        saveConfig();
        lastMessage = "Imported settings from config/betterzoom-import.cfg";
    }

    public static KeyBinding keyForId(int id) {
        if (id == 1) return zoomKey;
        if (id == 2) return settingsKey;
        if (id == 3) return zoomInKey;
        if (id == 4) return zoomOutKey;
        if (id == 5) return resetZoomKey;
        return null;
    }

    public static String keyConflictMessage(int changedId) {
        KeyBinding changed = keyForId(changedId);
        if (changed == null || changed.getKeyCode() == Keyboard.KEY_NONE) return "";
        for (int id = 1; id <= 5; id++) {
            if (id == changedId) continue;
            KeyBinding other = keyForId(id);
            if (other != null && other.getKeyCode() == changed.getKeyCode()) {
                return "Key conflict: " + Keyboard.getKeyName(changed.getKeyCode()) + " is assigned more than once.";
            }
        }
        return "";
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (settingsKey != null && settingsKey.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new GuiBetterZoom());
            return;
        }
        if (!holdToZoomEnabled && zoomKey != null && zoomKey.isPressed() && mc.currentScreen == null) {
            zoomToggleState = !zoomToggleState;
        }
    }
}

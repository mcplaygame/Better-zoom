package com.predex.betterzoom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

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

@Mod(modid = BetterZoom.MODID, name = BetterZoom.NAME, version = BetterZoom.VERSION,
     acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class BetterZoom {
    public static final String MODID = "betterzoom";
    public static final String NAME = "Better Zoom Pro";
    public static final String VERSION = "2.0.0"; // Keep this version fixed unless the owner explicitly approves a bump.
    public static final float DEFAULT_ZOOM = 4.0F;
    public static final float MIN_ZOOM = 2.0F;
    public static final float ABSOLUTE_MAX_ZOOM = 32.0F;

    public static Configuration CONFIG;
    public static File configDirectory;
    public static float zoomFactor = DEFAULT_ZOOM, maxZoomFactor = 16.0F, zoomStep = 0.5F;
    public static float smoothSpeed = 0.22F, sensitivityMultiplier = 0.45F;
    public static boolean smoothZoomEnabled = true, scrollZoomEnabled = true;
    public static boolean reducedSensitivityEnabled = true, holdToZoomEnabled = true;
    public static boolean invertWheelEnabled = false, showZoomIndicator = true;
    public static boolean zoomToggleState = false;
    public static int indicatorCorner = 1; // 0=top-left, 1=top-right, 2=bottom-left, 3=bottom-right
    public static String lastMessage = "";
    public static KeyBinding zoomKey, settingsKey, zoomInKey, zoomOutKey, resetZoomKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDirectory = event.getModConfigurationDirectory();
        File configFile = new File(configDirectory, "betterzoom.cfg");
        if (configFile.isFile() && configFile.length() > 0L) {
            try {
                Files.copy(configFile.toPath(), new File(configDirectory, "betterzoom-backup.cfg").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) { }
        }
        CONFIG = new Configuration(configFile);
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
        if (CONFIG == null) return;
        try {
            CONFIG.load();
            zoomFactor = CONFIG.getFloat("zoomFactor", "general", DEFAULT_ZOOM, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "Current zoom multiplier.");
            maxZoomFactor = CONFIG.getFloat("maxZoomFactor", "general", 16.0F, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "Maximum zoom multiplier.");
            zoomStep = CONFIG.getFloat("zoomStep", "general", 0.5F, 0.1F, 4.0F, "Zoom change per wheel notch or key press.");
            smoothSpeed = CONFIG.getFloat("smoothSpeed", "general", 0.22F, 0.05F, 1.0F, "Zoom animation speed.");
            sensitivityMultiplier = CONFIG.getFloat("sensitivityMultiplier", "general", 0.45F, 0.05F, 1.0F, "Mouse sensitivity multiplier while zoomed.");
            smoothZoomEnabled = CONFIG.getBoolean("smoothZoomEnabled", "general", true, "Enable smooth zoom.");
            scrollZoomEnabled = CONFIG.getBoolean("scrollZoomEnabled", "general", true, "Allow wheel zoom while zooming.");
            reducedSensitivityEnabled = CONFIG.getBoolean("reducedSensitivityEnabled", "general", true, "Reduce mouse sensitivity while zoomed.");
            holdToZoomEnabled = CONFIG.getBoolean("holdToZoomEnabled", "general", true, "Hold key to zoom; off means toggle.");
            invertWheelEnabled = CONFIG.getBoolean("invertWheelEnabled", "controls", false, "Invert wheel zoom direction.");
            showZoomIndicator = CONFIG.getBoolean("showZoomIndicator", "hud", true, "Show zoom indicator in-game.");
            indicatorCorner = CONFIG.getInt("indicatorCorner", "hud", 1, 0, 3, "HUD corner: 0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right.");
            clampZoom();
            if (CONFIG.hasChanged()) CONFIG.save();
        } catch (RuntimeException ex) {
            recoverConfigBackup();
            lastMessage = "Config was invalid; attempted backup recovery.";
        }
    }

    public static void saveConfig() {
        if (CONFIG == null) return;
        clampZoom();
        CONFIG.get("general", "zoomFactor", DEFAULT_ZOOM).set(zoomFactor);
        CONFIG.get("general", "maxZoomFactor", 16.0F).set(maxZoomFactor);
        CONFIG.get("general", "zoomStep", 0.5F).set(zoomStep);
        CONFIG.get("general", "smoothSpeed", 0.22F).set(smoothSpeed);
        CONFIG.get("general", "sensitivityMultiplier", 0.45F).set(sensitivityMultiplier);
        CONFIG.get("general", "smoothZoomEnabled", true).set(smoothZoomEnabled);
        CONFIG.get("general", "scrollZoomEnabled", true).set(scrollZoomEnabled);
        CONFIG.get("general", "reducedSensitivityEnabled", true).set(reducedSensitivityEnabled);
        CONFIG.get("general", "holdToZoomEnabled", true).set(holdToZoomEnabled);
        CONFIG.get("controls", "invertWheelEnabled", false).set(invertWheelEnabled);
        CONFIG.get("hud", "showZoomIndicator", true).set(showZoomIndicator);
        CONFIG.get("hud", "indicatorCorner", 1).set(Math.max(0, Math.min(3, indicatorCorner)));
        CONFIG.save();
    }

    public static void clampZoom() {
        maxZoomFactor = clamp(maxZoomFactor, MIN_ZOOM, ABSOLUTE_MAX_ZOOM);
        zoomFactor = clamp(zoomFactor, MIN_ZOOM, maxZoomFactor);
        zoomStep = clamp(zoomStep, 0.1F, 4.0F);
        smoothSpeed = clamp(smoothSpeed, 0.05F, 1.0F);
        sensitivityMultiplier = clamp(sensitivityMultiplier, 0.05F, 1.0F);
        indicatorCorner = Math.max(0, Math.min(3, indicatorCorner));
    }

    private static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    public static void setZoom(float value) {
        zoomFactor = clamp(value, MIN_ZOOM, maxZoomFactor);
        saveConfig();
    }

    public static void resetZoomLevel() {
        setZoom(Math.min(DEFAULT_ZOOM, maxZoomFactor));
        lastMessage = "Zoom reset to " + String.format(Locale.ROOT, "%.1fx", zoomFactor);
    }

    public static void resetKeyBindings() {
        if (zoomKey == null || settingsKey == null || zoomInKey == null || zoomOutKey == null || resetZoomKey == null) return;
        zoomKey.setKeyCode(Keyboard.KEY_C); settingsKey.setKeyCode(Keyboard.KEY_V);
        zoomInKey.setKeyCode(Keyboard.KEY_EQUALS); zoomOutKey.setKeyCode(Keyboard.KEY_MINUS);
        resetZoomKey.setKeyCode(Keyboard.KEY_R);
        KeyBinding.resetKeyBindingArrayAndHash();
        Minecraft.getMinecraft().gameSettings.saveOptions();
    }

    public static void resetDefaults() {
        zoomFactor = DEFAULT_ZOOM; maxZoomFactor = 16.0F; zoomStep = 0.5F;
        smoothSpeed = 0.22F; sensitivityMultiplier = 0.45F;
        smoothZoomEnabled = true; scrollZoomEnabled = true; reducedSensitivityEnabled = true;
        holdToZoomEnabled = true; invertWheelEnabled = false; showZoomIndicator = true;
        indicatorCorner = 1; zoomToggleState = false;
        resetKeyBindings(); saveConfig(); lastMessage = "All settings restored to defaults.";
    }

    public static File profileFile(String name) {
        if (configDirectory == null || name == null) return null;
        String safe = name.trim().replaceAll("[^A-Za-z0-9_-]", "_");
        if (safe.length() == 0 || safe.length() > 32 || safe.equals(".") || safe.equals("..")) return null;
        return new File(configDirectory, "betterzoom-profile-" + safe + ".cfg");
    }

    public static void saveProfile(String name) {
        File target = profileFile(name);
        if (target == null) { lastMessage = "Profile name: use 1-32 letters, numbers, _ or -."; return; }
        try {
            saveConfig();
            Files.copy(new File(configDirectory, "betterzoom.cfg").toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            lastMessage = "Saved profile: " + target.getName();
        } catch (IOException ex) { lastMessage = "Profile save failed: " + ex.getMessage(); }
    }

    public static void loadProfile(String name) {
        File source = profileFile(name);
        if (source == null || !source.isFile()) { lastMessage = "Profile not found. Save it first."; return; }
        if (!validateProfile(source)) { lastMessage = "Profile rejected: invalid or empty config."; return; }
        backupCurrentConfig();
        applyConfigFile(source);
    }

    public static void deleteProfile(String name) {
        File target = profileFile(name);
        if (target == null || !target.isFile()) { lastMessage = "Profile not found."; return; }
        if (target.delete()) lastMessage = "Deleted profile: " + target.getName();
        else lastMessage = "Could not delete profile.";
    }

    private static boolean validateProfile(File file) {
        try {
            if (!file.isFile() || file.length() < 8L || file.length() > 1024L * 1024L) return false;
            String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (!text.contains("general") || !(text.contains("zoomFactor") || text.contains("maxZoomFactor"))) return false;
            Configuration test = new Configuration(file);
            test.load();
            test.getFloat("zoomFactor", "general", DEFAULT_ZOOM, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "");
            test.getFloat("maxZoomFactor", "general", 16.0F, MIN_ZOOM, ABSOLUTE_MAX_ZOOM, "");
            test.getFloat("zoomStep", "general", 0.5F, 0.1F, 4.0F, "");
            return true;
        } catch (Exception ex) { return false; }
    }

    public static void importConfig() {
        if (configDirectory == null) { lastMessage = "Import failed: config directory unavailable."; return; }
        File source = new File(configDirectory, "betterzoom-import.cfg");
        if (!validateProfile(source)) { lastMessage = "Import rejected: missing, empty, or invalid profile."; return; }
        backupCurrentConfig();
        applyConfigFile(source);
    }

    private static void applyConfigFile(File source) {
        try {
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
            invertWheelEnabled = imported.getBoolean("invertWheelEnabled", "controls", false, "");
            showZoomIndicator = imported.getBoolean("showZoomIndicator", "hud", true, "");
            indicatorCorner = imported.getInt("indicatorCorner", "hud", 1, 0, 3, "");
            clampZoom(); saveConfig();
            lastMessage = "Loaded settings from " + source.getName() + ". Backup: betterzoom-backup.cfg";
        } catch (Exception ex) { recoverConfigBackup(); lastMessage = "Profile load failed; backup recovery attempted."; }
    }

    public static void exportConfig() {
        try {
            saveConfig();
            File destination = new File(configDirectory, "betterzoom-export.cfg");
            Files.copy(new File(configDirectory, "betterzoom.cfg").toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            lastMessage = "Exported to config/betterzoom-export.cfg";
        } catch (IOException ex) { lastMessage = "Export failed: " + ex.getMessage(); }
    }

    public static void backupCurrentConfig() {
        if (configDirectory == null) return;
        try {
            File current = new File(configDirectory, "betterzoom.cfg");
            if (current.isFile() && current.length() > 0L)
                Files.copy(current.toPath(), new File(configDirectory, "betterzoom-backup.cfg").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) { lastMessage = "Backup failed: " + ex.getMessage(); }
    }

    public static void recoverConfigBackup() {
        if (configDirectory == null) { lastMessage = "Backup recovery unavailable."; return; }
        File backup = new File(configDirectory, "betterzoom-backup.cfg");
        if (!validateProfile(backup)) { lastMessage = "No valid backup found; defaults will be used."; resetMemoryDefaults(); return; }
        try {
            File current = new File(configDirectory, "betterzoom.cfg");
            Files.copy(backup.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
            CONFIG = new Configuration(current);
            loadConfig();
            lastMessage = "Recovered settings from betterzoom-backup.cfg";
        } catch (Exception ex) { resetMemoryDefaults(); lastMessage = "Backup recovery failed; defaults loaded."; }
    }

    private static void resetMemoryDefaults() {
        zoomFactor = DEFAULT_ZOOM; maxZoomFactor = 16.0F; zoomStep = 0.5F; smoothSpeed = 0.22F;
        sensitivityMultiplier = 0.45F; smoothZoomEnabled = true; scrollZoomEnabled = true;
        reducedSensitivityEnabled = true; holdToZoomEnabled = true; invertWheelEnabled = false;
        showZoomIndicator = true; indicatorCorner = 1;
    }

    public static KeyBinding keyForId(int id) {
        if (id == 1) return zoomKey; if (id == 2) return settingsKey;
        if (id == 3) return zoomInKey; if (id == 4) return zoomOutKey;
        if (id == 5) return resetZoomKey; return null;
    }

    public static String keyConflictMessage(int changedId) {
        KeyBinding changed = keyForId(changedId);
        if (changed == null || changed.getKeyCode() == Keyboard.KEY_NONE) return "";
        for (int id = 1; id <= 5; id++) {
            if (id == changedId) continue;
            KeyBinding other = keyForId(id);
            if (other != null && other.getKeyCode() == changed.getKeyCode())
                return "Key conflict: " + Keyboard.getKeyName(changed.getKeyCode()) + " is assigned more than once.";
        }
        return "";
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (settingsKey != null && settingsKey.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new GuiBetterZoom()); return;
        }
        if (!holdToZoomEnabled && zoomKey != null && zoomKey.isPressed() && mc.currentScreen == null)
            zoomToggleState = !zoomToggleState;
    }
}

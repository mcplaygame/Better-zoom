package com.predex.betterzoom;

import java.io.File;

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
    public static final String NAME = "Better Zoom";
    public static final String VERSION = "1.1.0";

    public static Configuration CONFIG;

    public static float zoomFactor = 4.0F;
    public static float smoothSpeed = 0.22F;
    public static float sensitivityMultiplier = 0.45F;
    public static boolean smoothZoomEnabled = true;
    public static boolean scrollZoomEnabled = true;
    public static boolean reducedSensitivityEnabled = true;

    public static KeyBinding zoomKey;
    public static KeyBinding settingsKey;
    public static KeyBinding zoomInKey;
    public static KeyBinding zoomOutKey;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CONFIG = new Configuration(new File(event.getModConfigurationDirectory(), "betterzoom.cfg"));
        loadConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        zoomKey = new KeyBinding("key.betterzoom.zoom", Keyboard.KEY_C, "key.categories.betterzoom");
        settingsKey = new KeyBinding("key.betterzoom.settings", Keyboard.KEY_V, "key.categories.betterzoom");
        zoomInKey = new KeyBinding("key.betterzoom.zoom_in", Keyboard.KEY_EQUALS, "key.categories.betterzoom");
        zoomOutKey = new KeyBinding("key.betterzoom.zoom_out", Keyboard.KEY_MINUS, "key.categories.betterzoom");
        ClientRegistry.registerKeyBinding(zoomKey);
        ClientRegistry.registerKeyBinding(settingsKey);
        ClientRegistry.registerKeyBinding(zoomInKey);
        ClientRegistry.registerKeyBinding(zoomOutKey);

        ZoomHandler handler = new ZoomHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        FMLCommonHandler.instance().bus().register(this);
    }

    public static void loadConfig() {
        CONFIG.load();

        zoomFactor = CONFIG.getFloat("zoomFactor", "general", 4.0F, 2.0F, 10.0F,
            "Zoom multiplier. Scroll wheel or keybinds can adjust it.");
        smoothSpeed = CONFIG.getFloat("smoothSpeed", "general", 0.22F, 0.05F, 1.0F,
            "Zoom transition speed. Higher is faster.");
        sensitivityMultiplier = CONFIG.getFloat("sensitivityMultiplier", "general", 0.45F, 0.05F, 1.0F,
            "Mouse sensitivity multiplier while zooming.");
        smoothZoomEnabled = CONFIG.getBoolean("smoothZoomEnabled", "general", true,
            "Enable smooth zoom transitions.");
        scrollZoomEnabled = CONFIG.getBoolean("scrollZoomEnabled", "general", true,
            "Allow the mouse wheel to change zoom level while zooming.");
        reducedSensitivityEnabled = CONFIG.getBoolean("reducedSensitivityEnabled", "general", true,
            "Reduce mouse sensitivity while zooming.");

        if (CONFIG.hasChanged()) {
            CONFIG.save();
        }
    }

    public static void saveConfig() {
        if (CONFIG == null) return;
        CONFIG.get("general", "zoomFactor", 4.0F).set(zoomFactor);
        CONFIG.get("general", "smoothSpeed", 0.22F).set(smoothSpeed);
        CONFIG.get("general", "sensitivityMultiplier", 0.45F).set(sensitivityMultiplier);
        CONFIG.get("general", "smoothZoomEnabled", true).set(smoothZoomEnabled);
        CONFIG.get("general", "scrollZoomEnabled", true).set(scrollZoomEnabled);
        CONFIG.get("general", "reducedSensitivityEnabled", true).set(reducedSensitivityEnabled);
        CONFIG.save();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (settingsKey != null && settingsKey.isPressed() && mc.currentScreen == null) {
            mc.displayGuiScreen(new GuiBetterZoom());
        }
    }
}

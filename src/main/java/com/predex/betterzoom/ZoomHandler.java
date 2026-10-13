package com.predex.betterzoom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ZoomHandler {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float zoomProgress = 0.0F;
    private float originalSensitivity = 0.5F;
    private boolean sensitivityApplied = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (mc.thePlayer == null || mc.theWorld == null) {
            zoomProgress = 0.0F;
            restoreSensitivity();
            return;
        }

        // Dedicated zoom-level hotkeys work independently of the mouse wheel.
        // Only adjust the level while in-game and with no GUI open.
        if (mc.currentScreen == null) {
            if (BetterZoom.zoomInKey != null && BetterZoom.zoomInKey.isPressed()) {
                changeZoomFactor(0.5F);
            }
            if (BetterZoom.zoomOutKey != null && BetterZoom.zoomOutKey.isPressed()) {
                changeZoomFactor(-0.5F);
            }
        }

        boolean holdingZoom = BetterZoom.zoomKey != null && BetterZoom.zoomKey.isKeyDown();
        if (BetterZoom.smoothZoomEnabled) {
            float speed = BetterZoom.smoothSpeed;
            zoomProgress += holdingZoom ? speed : -speed;
            if (zoomProgress < 0.0F) zoomProgress = 0.0F;
            if (zoomProgress > 1.0F) zoomProgress = 1.0F;
        } else {
            zoomProgress = holdingZoom ? 1.0F : 0.0F;
        }

        updateSensitivity(zoomProgress > 0.0F && BetterZoom.reducedSensitivityEnabled);
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (zoomProgress <= 0.0F) return;
        float multiplier = 1.0F + ((BetterZoom.zoomFactor - 1.0F) * zoomProgress);
        event.setFOV(event.getFOV() / multiplier);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!BetterZoom.scrollZoomEnabled || zoomProgress <= 0.0F || event.dwheel == 0) return;

        changeZoomFactor(event.dwheel > 0 ? 0.5F : -0.5F);
        event.setCanceled(true);
    }

    private void changeZoomFactor(float amount) {
        float next = BetterZoom.zoomFactor + amount;
        if (next < 2.0F) next = 2.0F;
        if (next > 10.0F) next = 10.0F;
        if (next != BetterZoom.zoomFactor) {
            BetterZoom.zoomFactor = next;
            BetterZoom.saveConfig();
        }
    }

    private void updateSensitivity(boolean zoomIsActive) {
        if (zoomIsActive) {
            if (!sensitivityApplied) {
                originalSensitivity = mc.gameSettings.mouseSensitivity;
                sensitivityApplied = true;
            }
            mc.gameSettings.mouseSensitivity = originalSensitivity * BetterZoom.sensitivityMultiplier;
        } else {
            restoreSensitivity();
        }
    }

    private void restoreSensitivity() {
        if (sensitivityApplied && mc.gameSettings != null) {
            mc.gameSettings.mouseSensitivity = originalSensitivity;
        }
        sensitivityApplied = false;
    }
}

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
            BetterZoom.zoomToggleState = false;
            restoreSensitivity();
            return;
        }

        if (mc.currentScreen == null) {
            if (BetterZoom.resetZoomKey != null && BetterZoom.resetZoomKey.isPressed()) {
                BetterZoom.resetZoomLevel();
            }
            if (BetterZoom.zoomInKey != null && BetterZoom.zoomInKey.isPressed()) {
                BetterZoom.setZoom(BetterZoom.zoomFactor + BetterZoom.zoomStep);
            }
            if (BetterZoom.zoomOutKey != null && BetterZoom.zoomOutKey.isPressed()) {
                BetterZoom.setZoom(BetterZoom.zoomFactor - BetterZoom.zoomStep);
            }
        }

        boolean keyRequestsZoom = BetterZoom.holdToZoomEnabled
            ? BetterZoom.zoomKey != null && BetterZoom.zoomKey.isKeyDown()
            : BetterZoom.zoomToggleState;
        boolean zoomRequested = mc.currentScreen == null && keyRequestsZoom;

        if (BetterZoom.smoothZoomEnabled) {
            float speed = BetterZoom.smoothSpeed;
            zoomProgress += zoomRequested ? speed : -speed;
            if (zoomProgress < 0.0F) zoomProgress = 0.0F;
            if (zoomProgress > 1.0F) zoomProgress = 1.0F;
        } else {
            zoomProgress = zoomRequested ? 1.0F : 0.0F;
        }

        updateSensitivity(zoomProgress > 0.0F && BetterZoom.reducedSensitivityEnabled);
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (zoomProgress <= 0.0F) return;
        BetterZoom.clampZoom();
        float multiplier = 1.0F + ((BetterZoom.zoomFactor - 1.0F) * zoomProgress);
        event.setFOV(event.getFOV() / multiplier);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!BetterZoom.scrollZoomEnabled || zoomProgress <= 0.0F || event.dwheel == 0) return;
        if (mc.currentScreen != null) return;

        BetterZoom.setZoom(event.dwheel > 0
            ? BetterZoom.zoomFactor + BetterZoom.zoomStep
            : BetterZoom.zoomFactor - BetterZoom.zoomStep);
        event.setCanceled(true);
    }

    private void updateSensitivity(boolean zoomIsActive) {
        if (mc.gameSettings == null) return;
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

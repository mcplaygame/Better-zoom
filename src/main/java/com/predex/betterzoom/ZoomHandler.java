package com.predex.betterzoom;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ZoomHandler {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float zoomProgress = 0.0F;
    private float originalSensitivity = 0.5F;
    private float lastAppliedSensitivity = -1.0F;
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
            if (BetterZoom.resetZoomKey != null && BetterZoom.resetZoomKey.isPressed()) BetterZoom.resetZoomLevel();
            if (BetterZoom.zoomInKey != null && BetterZoom.zoomInKey.isPressed()) BetterZoom.setZoom(BetterZoom.zoomFactor + BetterZoom.zoomStep);
            if (BetterZoom.zoomOutKey != null && BetterZoom.zoomOutKey.isPressed()) BetterZoom.setZoom(BetterZoom.zoomFactor - BetterZoom.zoomStep);
        }
        boolean keyRequestsZoom = BetterZoom.holdToZoomEnabled
            ? BetterZoom.zoomKey != null && BetterZoom.zoomKey.isKeyDown()
            : BetterZoom.zoomToggleState;
        boolean zoomRequested = mc.currentScreen == null && keyRequestsZoom;
        if (BetterZoom.smoothZoomEnabled) {
            float speed = BetterZoom.smoothSpeed;
            zoomProgress += zoomRequested ? speed : -speed;
            zoomProgress = Math.max(0.0F, Math.min(1.0F, zoomProgress));
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
        if (!BetterZoom.scrollZoomEnabled || zoomProgress <= 0.0F || event.dwheel == 0 || mc.currentScreen != null) return;
        boolean wheelUp = event.dwheel > 0;
        if (BetterZoom.invertWheelEnabled) wheelUp = !wheelUp;
        BetterZoom.setZoom(BetterZoom.zoomFactor + (wheelUp ? BetterZoom.zoomStep : -BetterZoom.zoomStep));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !BetterZoom.showZoomIndicator || zoomProgress <= 0.02F) return;
        if (mc.fontRendererObj == null) return;
        String label = String.format(java.util.Locale.ROOT, "ZOOM %.1fx", 1.0F + (BetterZoom.zoomFactor - 1.0F) * zoomProgress);
        int padding = 6;
        int boxWidth = mc.fontRendererObj.getStringWidth(label) + padding * 2;
        int boxHeight = 18;
        int sw = event.resolution.getScaledWidth();
        int sh = event.resolution.getScaledHeight();
        int corner = BetterZoom.indicatorCorner;
        int left = (corner == 0 || corner == 2) ? 8 : sw - boxWidth - 8;
        int top = (corner == 0 || corner == 1) ? 8 : sh - boxHeight - 8;
        net.minecraft.client.gui.Gui.drawRect(left, top, left + boxWidth, top + boxHeight, 0xB0101826);
        net.minecraft.client.gui.Gui.drawRect(left, top, left + boxWidth, top + 2, 0xFF32D9FF);
        mc.fontRendererObj.drawStringWithShadow(label, left + padding, top + 5, 0xFFFFFFFF);
    }

    private void updateSensitivity(boolean zoomIsActive) {
        if (mc.gameSettings == null) return;
        if (zoomIsActive) {
            if (!sensitivityApplied) {
                originalSensitivity = mc.gameSettings.mouseSensitivity;
                sensitivityApplied = true;
            } else if (lastAppliedSensitivity >= 0.0F
                && Math.abs(mc.gameSettings.mouseSensitivity - lastAppliedSensitivity) > 0.0001F) {
                // Respect external changes made while zoomed, then apply the zoom multiplier again.
                originalSensitivity = BetterZoom.sensitivityMultiplier > 0.0F
                    ? Math.min(1.0F, mc.gameSettings.mouseSensitivity / BetterZoom.sensitivityMultiplier)
                    : mc.gameSettings.mouseSensitivity;
            }
            mc.gameSettings.mouseSensitivity = originalSensitivity * BetterZoom.sensitivityMultiplier;
            lastAppliedSensitivity = mc.gameSettings.mouseSensitivity;
        } else {
            restoreSensitivity();
        }
    }

    private void restoreSensitivity() {
        if (sensitivityApplied && mc.gameSettings != null) mc.gameSettings.mouseSensitivity = originalSensitivity;
        sensitivityApplied = false;
        lastAppliedSensitivity = -1.0F;
    }
}

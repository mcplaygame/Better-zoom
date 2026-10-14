package com.predex.betterzoom;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ZoomHandler {
    private final Minecraft mc = Minecraft.getMinecraft();
    private float zoomProgress = 0.0F;
    private float currentZoomLevel = 1.0F;
    private float originalSensitivity = 0.5F;
    private float lastAppliedSensitivity = -1.0F;
    private boolean sensitivityApplied = false;
    private boolean optifineNoticeSent = false;
    private long lastTickNanos = System.nanoTime();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.nanoTime();
        float deltaSeconds = (now - lastTickNanos) / 1000000000.0F;
        lastTickNanos = now;
        deltaSeconds = Math.max(0.001F, Math.min(0.1F, deltaSeconds));

        if (mc.thePlayer == null || mc.theWorld == null) {
            zoomProgress = 0.0F;
            currentZoomLevel = 1.0F;
            BetterZoom.zoomToggleState = false;
            restoreSensitivity();
            return;
        }

        if (BetterZoom.optifineDetected && !optifineNoticeSent) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[Better Zoom] " + EnumChatFormatting.YELLOW
                + "OptiFine detected. Better Zoom uses B by default."));
            mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[Better Zoom] " + EnumChatFormatting.WHITE
                + "To avoid double zoom, disable OptiFine Zoom (usually C) in Options > Controls."));
            optifineNoticeSent = true;
        }

        if (mc.currentScreen == null) {
            if (BetterZoom.resetZoomKey != null && BetterZoom.resetZoomKey.isPressed()) BetterZoom.resetZoomLevel();
            if (BetterZoom.zoomInKey != null && BetterZoom.zoomInKey.isPressed()) {
                BetterZoom.setZoom(BetterZoom.zoomFactor + BetterZoom.zoomStep);
                BetterZoom.saveConfig();
            }
            if (BetterZoom.zoomOutKey != null && BetterZoom.zoomOutKey.isPressed()) {
                BetterZoom.setZoom(BetterZoom.zoomFactor - BetterZoom.zoomStep);
                BetterZoom.saveConfig();
            }
        }

        boolean keyRequestsZoom = BetterZoom.holdToZoomEnabled
            ? BetterZoom.zoomKey != null && BetterZoom.zoomKey.isKeyDown()
            : BetterZoom.zoomToggleState;
        boolean zoomRequested = mc.currentScreen == null && keyRequestsZoom;

        if (BetterZoom.smoothZoomEnabled) {
            // Directly ease the actual magnification toward its target, rather than mixing
            // two separate progress values. This gives a predictable, responsive transition.
            float response = 8.0F + BetterZoom.smoothSpeed * 28.0F;
            float alpha = (float) (1.0D - Math.exp(-response * deltaSeconds));
            float targetZoom = zoomRequested ? BetterZoom.zoomFactor : 1.0F;
            currentZoomLevel += (targetZoom - currentZoomLevel) * alpha;
            zoomProgress += ((zoomRequested ? 1.0F : 0.0F) - zoomProgress) * alpha;
            if (Math.abs(targetZoom - currentZoomLevel) < 0.005F) currentZoomLevel = targetZoom;
            if (Math.abs((zoomRequested ? 1.0F : 0.0F) - zoomProgress) < 0.005F)
                zoomProgress = zoomRequested ? 1.0F : 0.0F;
        } else {
            currentZoomLevel = zoomRequested ? BetterZoom.zoomFactor : 1.0F;
            zoomProgress = zoomRequested ? 1.0F : 0.0F;
        }

        currentZoomLevel = Math.max(1.0F, Math.min(BetterZoom.maxZoomFactor, currentZoomLevel));
        zoomProgress = Math.max(0.0F, Math.min(1.0F, zoomProgress));
        updateSensitivity(currentZoomLevel > 1.02F && BetterZoom.reducedSensitivityEnabled);
    }

    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        if (currentZoomLevel <= 1.001F) return;
        BetterZoom.clampZoom();
        event.setFOV(event.getFOV() / currentZoomLevel);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!BetterZoom.scrollZoomEnabled || currentZoomLevel <= 1.02F || event.dwheel == 0 || mc.currentScreen != null) return;
        boolean wheelUp = event.dwheel > 0;
        if (BetterZoom.invertWheelEnabled) wheelUp = !wheelUp;
        BetterZoom.setZoom(BetterZoom.zoomFactor + (wheelUp ? BetterZoom.zoomStep : -BetterZoom.zoomStep));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !BetterZoom.showZoomIndicator || currentZoomLevel <= 1.02F) return;
        if (mc.fontRendererObj == null) return;
        String label = String.format(java.util.Locale.ROOT, "ZOOM %.1fx", currentZoomLevel);
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

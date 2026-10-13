package com.predex.betterzoom;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class GuiBetterZoom extends GuiScreen {
    private int panelX, panelY, panelW = 390, panelH = 390;
    private boolean waitingForKey = false;
    private int waitingFor = 0; // 1 = zoom, 2 = settings, 3 = zoom in, 4 = zoom out

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        buttonList.clear();
        buttonList.add(new GuiButton(99, -1000, -1000, 1, 1, ""));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        BetterZoom.saveConfig();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        Gui.drawRect(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFF101A2B);
        Gui.drawRect(panelX, panelY, panelX + panelW, panelY + panelH, 0xF0182234);
        Gui.drawRect(panelX, panelY, panelX + panelW, panelY + 3, 0xFF32D9FF);
        Gui.drawRect(panelX, panelY + 3, panelX + 5, panelY + panelH, 0xFF32D9FF);
        Gui.drawRect(panelX + panelW - 5, panelY + 3, panelX + panelW, panelY + panelH, 0xFFFF4FBD);

        drawCenteredString(fontRendererObj, "BETTER ZOOM", width / 2, panelY + 15, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "CLIENT SETTINGS  •  MINECRAFT 1.8.9", width / 2, panelY + 29, 0xFF9DB2D1);
        Gui.drawRect(panelX + 18, panelY + 45, panelX + panelW - 18, panelY + 46, 0xFF35445C);

        drawLabel("Zoom strength", panelX + 20, panelY + 58);
        drawValueButton(panelX + 230, panelY + 53, 32, 22, "−", mouseX, mouseY);
        drawValueButton(panelX + 264, panelY + 53, 58, 22, String.format("%.1fx", BetterZoom.zoomFactor), mouseX, mouseY);
        drawValueButton(panelX + 324, panelY + 53, 32, 22, "+", mouseX, mouseY);

        drawLabel("Smooth zoom", panelX + 20, panelY + 91);
        drawToggle(panelX + 280, panelY + 86, BetterZoom.smoothZoomEnabled, mouseX, mouseY);

        drawLabel("Scroll to adjust zoom", panelX + 20, panelY + 123);
        drawToggle(panelX + 280, panelY + 118, BetterZoom.scrollZoomEnabled, mouseX, mouseY);

        drawLabel("Reduced mouse sensitivity", panelX + 20, panelY + 155);
        drawToggle(panelX + 280, panelY + 150, BetterZoom.reducedSensitivityEnabled, mouseX, mouseY);

        drawLabel("Sensitivity while zooming", panelX + 20, panelY + 187);
        drawValueButton(panelX + 230, panelY + 182, 32, 22, "−", mouseX, mouseY);
        drawValueButton(panelX + 264, panelY + 182, 58, 22,
            Math.round(BetterZoom.sensitivityMultiplier * 100) + "%", mouseX, mouseY);
        drawValueButton(panelX + 324, panelY + 182, 32, 22, "+", mouseX, mouseY);

        drawLabel("Zoom key", panelX + 20, panelY + 222);
        drawKeyButton(panelX + 230, panelY + 216, 126, 24,
            waitingForKey && waitingFor == 1 ? "PRESS A KEY..." : keyName(BetterZoom.zoomKey), mouseX, mouseY);

        drawLabel("Open settings key", panelX + 20, panelY + 253);
        drawKeyButton(panelX + 230, panelY + 247, 126, 24,
            waitingForKey && waitingFor == 2 ? "PRESS A KEY..." : keyName(BetterZoom.settingsKey), mouseX, mouseY);

        drawLabel("Zoom level up key", panelX + 20, panelY + 284);
        drawKeyButton(panelX + 230, panelY + 278, 126, 24,
            waitingForKey && waitingFor == 3 ? "PRESS A KEY..." : keyName(BetterZoom.zoomInKey), mouseX, mouseY);

        drawLabel("Zoom level down key", panelX + 20, panelY + 315);
        drawKeyButton(panelX + 230, panelY + 309, 126, 24,
            waitingForKey && waitingFor == 4 ? "PRESS A KEY..." : keyName(BetterZoom.zoomOutKey), mouseX, mouseY);

        if (waitingForKey) {
            drawCenteredString(fontRendererObj, "Press a key to bind • ESC cancels", width / 2, panelY + 340, 0xFFFFD66B);
        } else {
            drawCenteredString(fontRendererObj, "All four keys are also in Options → Controls", width / 2, panelY + 340, 0xFF9DB2D1);
        }

        drawValueButton(panelX + 82, panelY + 360, 100, 20, "RESET", mouseX, mouseY);
        drawValueButton(panelX + 208, panelY + 360, 100, 20, "DONE", mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLabel(String label, int x, int y) {
        fontRendererObj.drawString(label, x, y + 6, 0xFFE6EDF8);
    }

    private void drawValueButton(int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        int bg = hover ? 0xFF304A67 : 0xFF202F45;
        Gui.drawRect(x, y, x + w, y + h, bg);
        Gui.drawRect(x, y, x + w, y + 1, hover ? 0xFF32D9FF : 0xFF435772);
        drawCenteredString(fontRendererObj, text, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
    }

    private void drawToggle(int x, int y, boolean enabled, int mx, int my) {
        boolean hover = mx >= x && mx < x + 76 && my >= y && my < y + 22;
        int bg = enabled ? 0xFF174B50 : 0xFF483047;
        if (hover) bg = enabled ? 0xFF20666B : 0xFF623C5A;
        Gui.drawRect(x, y, x + 76, y + 22, bg);
        Gui.drawRect(x, y, x + 76, y + 1, enabled ? 0xFF32D9FF : 0xFFFF4FBD);
        drawCenteredString(fontRendererObj, enabled ? "ON" : "OFF", x + 38, y + 7, 0xFFFFFFFF);
    }

    private void drawKeyButton(int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
        Gui.drawRect(x, y, x + w, y + h, hover ? 0xFF304A67 : 0xFF202F45);
        Gui.drawRect(x, y, x + w, y + 1, hover ? 0xFFFF4FBD : 0xFF435772);
        drawCenteredString(fontRendererObj, text, x + w / 2, y + 8, 0xFFFFFFFF);
    }

    private String keyName(net.minecraft.client.settings.KeyBinding key) {
        if (key == null) return "UNBOUND";
        String name = Keyboard.getKeyName(key.getKeyCode());
        return name == null ? "UNBOUND" : name;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (hit(mouseX, mouseY, panelX + 230, panelY + 53, 32, 22)) {
            BetterZoom.zoomFactor = Math.max(2.0F, BetterZoom.zoomFactor - 0.5F);
        } else if (hit(mouseX, mouseY, panelX + 324, panelY + 53, 32, 22)) {
            BetterZoom.zoomFactor = Math.min(10.0F, BetterZoom.zoomFactor + 0.5F);
        } else if (hit(mouseX, mouseY, panelX + 280, panelY + 86, 76, 22)) {
            BetterZoom.smoothZoomEnabled = !BetterZoom.smoothZoomEnabled;
        } else if (hit(mouseX, mouseY, panelX + 280, panelY + 118, 76, 22)) {
            BetterZoom.scrollZoomEnabled = !BetterZoom.scrollZoomEnabled;
        } else if (hit(mouseX, mouseY, panelX + 280, panelY + 150, 76, 22)) {
            BetterZoom.reducedSensitivityEnabled = !BetterZoom.reducedSensitivityEnabled;
        } else if (hit(mouseX, mouseY, panelX + 230, panelY + 182, 32, 22)) {
            BetterZoom.sensitivityMultiplier = Math.max(0.05F, BetterZoom.sensitivityMultiplier - 0.05F);
        } else if (hit(mouseX, mouseY, panelX + 324, panelY + 182, 32, 22)) {
            BetterZoom.sensitivityMultiplier = Math.min(1.0F, BetterZoom.sensitivityMultiplier + 0.05F);
        } else if (hit(mouseX, mouseY, panelX + 230, panelY + 216, 126, 24)) {
            waitingForKey = true; waitingFor = 1;
        } else if (hit(mouseX, mouseY, panelX + 230, panelY + 247, 126, 24)) {
            waitingForKey = true; waitingFor = 2;
        } else if (hit(mouseX, mouseY, panelX + 230, panelY + 278, 126, 24)) {
            waitingForKey = true; waitingFor = 3;
        } else if (hit(mouseX, mouseY, panelX + 230, panelY + 309, 126, 24)) {
            waitingForKey = true; waitingFor = 4;
        } else if (hit(mouseX, mouseY, panelX + 82, panelY + 360, 100, 20)) {
            BetterZoom.zoomFactor = 4.0F;
            BetterZoom.smoothSpeed = 0.22F;
            BetterZoom.sensitivityMultiplier = 0.45F;
            BetterZoom.smoothZoomEnabled = true;
            BetterZoom.scrollZoomEnabled = true;
            BetterZoom.reducedSensitivityEnabled = true;
            BetterZoom.saveConfig();
        } else if (hit(mouseX, mouseY, panelX + 208, panelY + 360, 100, 20)) {
            BetterZoom.saveConfig();
            mc.displayGuiScreen(null);
        }
    }

    private boolean hit(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (waitingForKey) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                waitingForKey = false;
                waitingFor = 0;
                return;
            }
            if (waitingFor == 1 && BetterZoom.zoomKey != null) {
                BetterZoom.zoomKey.setKeyCode(keyCode);
            } else if (waitingFor == 2 && BetterZoom.settingsKey != null) {
                BetterZoom.settingsKey.setKeyCode(keyCode);
            } else if (waitingFor == 3 && BetterZoom.zoomInKey != null) {
                BetterZoom.zoomInKey.setKeyCode(keyCode);
            } else if (waitingFor == 4 && BetterZoom.zoomOutKey != null) {
                BetterZoom.zoomOutKey.setKeyCode(keyCode);
            }
            net.minecraft.client.settings.KeyBinding.resetKeyBindingArrayAndHash();
            mc.gameSettings.saveOptions();
            waitingForKey = false;
            waitingFor = 0;
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            BetterZoom.saveConfig();
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}

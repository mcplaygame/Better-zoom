package com.predex.betterzoom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GuiBetterZoom extends GuiScreen {
    private static final int ROW_HEIGHT = 29;
    private int x, y, w, h, scrollOffset;
    private int activeTab = 0;
    private boolean waitingForKey = false;
    private int waitingTarget = 0;
    private GuiTextField searchField;

    private static class SettingRow {
        final String id, label, description, type;
        final float min, max, step;
        SettingRow(String id, String label, String description, String type, float min, float max, float step) {
            this.id = id;
            this.label = label;
            this.description = description;
            this.type = type;
            this.min = min;
            this.max = max;
            this.step = step;
        }
    }

    @Override
    public void initGui() {
        w = Math.min(500, Math.max(280, width - 16));
        h = Math.min(560, Math.max(230, height - 16));
        x = (width - w) / 2;
        y = (height - h) / 2;
        scrollOffset = 0;
        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(0, fontRendererObj, x + 16, y + 49, w - 32, 18);
        searchField.setMaxStringLength(48);
        searchField.setFocused(false);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        BetterZoom.saveConfig();
    }

    private List<SettingRow> rowsForTab() {
        List<SettingRow> rows = new ArrayList<SettingRow>();
        if (activeTab == 0) {
            rows.add(new SettingRow("zoom", "Current zoom level", "Current magnification. Use the preset buttons for common levels.", "step", 2F, BetterZoom.maxZoomFactor, 0.5F));
            rows.add(new SettingRow("max", "Maximum zoom limit", "The zoom level cannot exceed this value. Range: 2x to 32x.", "step", 2F, 32F, 2F));
            rows.add(new SettingRow("presets", "Quick zoom presets", "Choose a common zoom level instantly. Presets above your maximum are clamped.", "presets", 0, 0, 0));
            rows.add(new SettingRow("smooth", "Smooth zoom animation", "Fade smoothly between normal view and zoomed view.", "toggle", 0, 0, 0));
            rows.add(new SettingRow("speed", "Animation speed", "Higher values make zoom transitions faster.", "step", 0.05F, 1F, 0.05F));
            rows.add(new SettingRow("scroll", "Mouse-wheel zoom", "Allow the wheel to adjust zoom while zoom is active.", "toggle", 0, 0, 0));
            rows.add(new SettingRow("step", "Zoom change per step", "How much the wheel or zoom-up/down keys change magnification.", "step", 0.1F, 4F, 0.1F));
        } else if (activeTab == 1) {
            rows.add(new SettingRow("hold", "Zoom key behavior", "Hold mode requires holding the key. Toggle mode switches zoom on/off with each press.", "togglemode", 0, 0, 0));
            rows.add(new SettingRow("keyzoom", "Zoom key", "Main zoom shortcut. Click the button and press a keyboard key. Backspace unbinds; Escape cancels.", "key1", 0, 0, 0));
            rows.add(new SettingRow("keysettings", "Open settings", "Shortcut to open this settings menu.", "key2", 0, 0, 0));
            rows.add(new SettingRow("keyin", "Increase zoom", "Increase the zoom level by one configured step.", "key3", 0, 0, 0));
            rows.add(new SettingRow("keyout", "Decrease zoom", "Decrease the zoom level by one configured step.", "key4", 0, 0, 0));
            rows.add(new SettingRow("keyreset", "Reset zoom level", "Return to the default 4x zoom level.", "key5", 0, 0, 0));
        } else {
            rows.add(new SettingRow("sensitivity", "Reduced mouse sensitivity", "Reduce mouse sensitivity while zooming.", "toggle", 0, 0, 0));
            rows.add(new SettingRow("multiplier", "Sensitivity multiplier", "Lower values make camera movement slower while zoomed.", "step", 0.05F, 1F, 0.05F));
            rows.add(new SettingRow("export", "Export current profile", "Writes your current settings to config/betterzoom-export.cfg.", "export", 0, 0, 0));
            rows.add(new SettingRow("import", "Import profile", "Reads config/betterzoom-import.cfg. Copy an exported profile to that filename first.", "import", 0, 0, 0));
            rows.add(new SettingRow("defaults", "Restore all defaults", "Reset settings and all five keyboard shortcuts.", "defaults", 0, 0, 0));
        }
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(java.util.Locale.ROOT);
        if (query.length() == 0) return rows;
        List<SettingRow> filtered = new ArrayList<SettingRow>();
        for (SettingRow row : rows) {
            if (row.label.toLowerCase(java.util.Locale.ROOT).contains(query)
                || row.description.toLowerCase(java.util.Locale.ROOT).contains(query)) filtered.add(row);
        }
        return filtered;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        Gui.drawRect(x - 2, y - 2, x + w + 2, y + h + 2, 0xFF070B12);
        Gui.drawRect(x, y, x + w, y + h, 0xF0182234);
        Gui.drawRect(x, y, x + w, y + 3, 0xFF32D9FF);
        Gui.drawRect(x, y + 3, x + 4, y + h, 0xFF32D9FF);
        Gui.drawRect(x + w - 4, y + 3, x + w, y + h, 0xFFFF4FBD);
        drawCenteredString(fontRendererObj, "BETTER ZOOM PRO 2.0", width / 2, y + 10, 0xFFFFFFFF);
        drawCenteredString(fontRendererObj, "LIVE PREVIEW  •  " + String.format(java.util.Locale.ROOT, "%.1fx", BetterZoom.zoomFactor)
            + "  •  MAX " + String.format(java.util.Locale.ROOT, "%.0fx", BetterZoom.maxZoomFactor), width / 2, y + 27, 0xFF9DB2D1);

        searchField.drawTextBox();
        int tabY = y + 73;
        int tabGap = 4;
        int tabW = (w - 32 - tabGap * 2) / 3;
        drawTab(x + 16, tabY, tabW, "ZOOM", 0, mouseX, mouseY);
        drawTab(x + 16 + tabW + tabGap, tabY, tabW, "CONTROLS", 1, mouseX, mouseY);
        drawTab(x + 16 + (tabW + tabGap) * 2, tabY, tabW, "ADVANCED", 2, mouseX, mouseY);

        int contentTop = y + 101;
        int contentBottom = y + h - 55;
        Gui.drawRect(x + 12, contentTop, x + w - 12, contentBottom, 0xAA0D1522);
        List<SettingRow> rows = rowsForTab();
        int viewHeight = Math.max(1, contentBottom - contentTop);
        int maxScroll = Math.max(0, rows.size() * ROW_HEIGHT - viewHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;
        int controlX = x + w - 170;

        boolean tooltipDrawn = false;
        for (int i = 0; i < rows.size(); i++) {
            SettingRow row = rows.get(i);
            int rowY = contentTop + 4 + i * ROW_HEIGHT - scrollOffset;
            if (rowY < contentTop || rowY + 24 > contentBottom) continue;
            if (i % 2 == 0) Gui.drawRect(x + 16, rowY, x + w - 16, rowY + 25, 0x182F4761);
            String shortLabel = fontRendererObj.trimStringToWidth(row.label, Math.max(24, controlX - (x + 19) - 8));
            fontRendererObj.drawString(shortLabel, x + 19, rowY + 8, 0xFFE6EDF8);
            drawRowControl(row, controlX, rowY, mouseX, mouseY);
            if (!tooltipDrawn && mouseX >= x + 14 && mouseX <= x + w - 14
                && mouseY >= rowY && mouseY <= rowY + 25) {
                drawHoveringText(Arrays.asList(row.description), mouseX, mouseY);
                tooltipDrawn = true;
            }
        }
        if (rows.isEmpty()) drawCenteredString(fontRendererObj, "No matching settings", width / 2, contentTop + 20, 0xFF9DB2D1);
        if (maxScroll > 0) {
            fontRendererObj.drawString("Scroll for more settings", x + 18, contentBottom - 10, 0xFF7187A5);
        }

        if (BetterZoom.lastMessage != null && !BetterZoom.lastMessage.isEmpty()) {
            String status = fontRendererObj.trimStringToWidth(BetterZoom.lastMessage, w - 28);
            drawCenteredString(fontRendererObj, status, width / 2, y + h - 48, status.startsWith("Key conflict") ? 0xFFFFD66B : 0xFF9DB2D1);
        }
        drawAction(x + 16, y + h - 34, 108, "RESET DEFAULTS", mouseX, mouseY, false);
        drawAction(x + w - 108, y + h - 34, 92, "DONE", mouseX, mouseY, true);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawTab(int bx, int by, int bw, String label, int tab, int mx, int my) {
        boolean active = activeTab == tab;
        Gui.drawRect(bx, by, bx + bw, by + 22, active ? 0xFF174B50 : (hit(mx, my, bx, by, bw, 22) ? 0xFF304A67 : 0xFF202F45));
        Gui.drawRect(bx, by, bx + bw, by + 1, active ? 0xFF32D9FF : 0xFF435772);
        drawCenteredString(fontRendererObj, label, bx + bw / 2, by + 7, 0xFFFFFFFF);
    }

    private void drawRowControl(SettingRow row, int bx, int by, int mx, int my) {
        if ("step".equals(row.type)) {
            float value = getStepValue(row.id);
            drawButton(bx, by + 2, 25, 21, "−", hit(mx, my, bx, by + 2, 25, 21));
            drawValue(bx + 28, by + 2, 78, formatValue(row.id, value), mx, my);
            drawButton(bx + 109, by + 2, 25, 21, "+", hit(mx, my, bx + 109, by + 2, 25, 21));
        } else if ("toggle".equals(row.type) || "togglemode".equals(row.type)) {
            boolean on = "togglemode".equals(row.type) ? BetterZoom.holdToZoomEnabled : getBoolean(row.id);
            String text = "togglemode".equals(row.type) ? (on ? "HOLD" : "TOGGLE") : (on ? "ON" : "OFF");
            drawButton(bx + 37, by + 2, 96, 21, text, hit(mx, my, bx + 37, by + 2, 96, 21));
        } else if (row.type.startsWith("key")) {
            int id = Integer.parseInt(row.type.substring(3));
            KeyBinding key = BetterZoom.keyForId(id);
            String name = waitingForKey && waitingTarget == id ? "PRESS KEY..." : keyName(key);
            drawButton(bx - 4, by + 2, 138, 21, name, hit(mx, my, bx - 4, by + 2, 138, 21));
        } else if ("presets".equals(row.type)) {
            int[] levels = new int[] {2, 4, 8, 16};
            for (int i = 0; i < levels.length; i++) {
                int px = bx + i * 34;
                drawButton(px, by + 2, 31, 21, levels[i] + "x", hit(mx, my, px, by + 2, 31, 21));
            }
        } else if ("export".equals(row.type) || "import".equals(row.type) || "defaults".equals(row.type)) {
            String label = "export".equals(row.type) ? "EXPORT" : ("import".equals(row.type) ? "IMPORT" : "RESET");
            drawButton(bx + 27, by + 2, 106, 21, label, hit(mx, my, bx + 27, by + 2, 106, 21));
        }
    }

    private void drawButton(int bx, int by, int bw, int bh, String label, boolean hovered) {
        Gui.drawRect(bx, by, bx + bw, by + bh, hovered ? 0xFF304A67 : 0xFF202F45);
        Gui.drawRect(bx, by, bx + bw, by + 1, 0xFF32D9FF);
        drawCenteredString(fontRendererObj, label, bx + bw / 2, by + 7, 0xFFFFFFFF);
    }

    private void drawValue(int bx, int by, int bw, String label, int mx, int my) {
        Gui.drawRect(bx, by, bx + bw, by + 21, hit(mx, my, bx, by, bw, 21) ? 0xFF304A67 : 0xFF182538);
        Gui.drawRect(bx, by, bx + bw, by + 1, 0xFF435772);
        drawCenteredString(fontRendererObj, label, bx + bw / 2, by + 7, 0xFFFFFFFF);
    }

    private void drawAction(int bx, int by, int bw, String label, int mx, int my, boolean primary) {
        boolean hovered = hit(mx, my, bx, by, bw, 22);
        Gui.drawRect(bx, by, bx + bw, by + 22, hovered ? 0xFF304A67 : (primary ? 0xFF174B50 : 0xFF202F45));
        Gui.drawRect(bx, by, bx + bw, by + 1, primary ? 0xFF32D9FF : 0xFFFF4FBD);
        drawCenteredString(fontRendererObj, label, bx + bw / 2, by + 7, 0xFFFFFFFF);
    }

    private String keyName(KeyBinding key) {
        if (key == null || key.getKeyCode() == Keyboard.KEY_NONE) return "UNBOUND";
        String name = Keyboard.getKeyName(key.getKeyCode());
        return name == null ? "UNBOUND" : name;
    }

    private float getStepValue(String id) {
        if ("zoom".equals(id)) return BetterZoom.zoomFactor;
        if ("max".equals(id)) return BetterZoom.maxZoomFactor;
        if ("speed".equals(id)) return BetterZoom.smoothSpeed;
        if ("step".equals(id)) return BetterZoom.zoomStep;
        return BetterZoom.sensitivityMultiplier;
    }

    private String formatValue(String id, float value) {
        if ("zoom".equals(id) || "max".equals(id)) return String.format(java.util.Locale.ROOT, "%.1fx", value);
        if ("multiplier".equals(id)) return Math.round(value * 100F) + "%";
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private boolean getBoolean(String id) {
        if ("smooth".equals(id)) return BetterZoom.smoothZoomEnabled;
        if ("scroll".equals(id)) return BetterZoom.scrollZoomEnabled;
        return BetterZoom.reducedSensitivityEnabled;
    }

    private boolean hit(int mx, int my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        int tabY = y + 73;
        int tabGap = 4;
        int tabW = (w - 32 - tabGap * 2) / 3;
        if (hit(mouseX, mouseY, x + 16, tabY, tabW, 22)) { activeTab = 0; scrollOffset = 0; return; }
        if (hit(mouseX, mouseY, x + 16 + tabW + tabGap, tabY, tabW, 22)) { activeTab = 1; scrollOffset = 0; return; }
        if (hit(mouseX, mouseY, x + 16 + (tabW + tabGap) * 2, tabY, tabW, 22)) { activeTab = 2; scrollOffset = 0; return; }
        if (hit(mouseX, mouseY, x + 16, y + h - 34, 108, 22)) { BetterZoom.resetDefaults(); scrollOffset = 0; return; }
        if (hit(mouseX, mouseY, x + w - 108, y + h - 34, 92, 22)) { BetterZoom.saveConfig(); mc.displayGuiScreen(null); return; }

        int contentTop = y + 101;
        int contentBottom = y + h - 55;
        if (mouseY < contentTop || mouseY >= contentBottom) return;
        List<SettingRow> rows = rowsForTab();
        int index = (mouseY - contentTop - 4 + scrollOffset) / ROW_HEIGHT;
        if (index < 0 || index >= rows.size()) return;
        SettingRow row = rows.get(index);
        int rowY = contentTop + 4 + index * ROW_HEIGHT - scrollOffset;
        int bx = x + w - 170;
        if (mouseY < rowY || mouseY > rowY + 25) return;

        if ("step".equals(row.type)) {
            if (hit(mouseX, mouseY, bx, rowY + 2, 25, 21)) changeStep(row.id, -row.step);
            else if (hit(mouseX, mouseY, bx + 109, rowY + 2, 25, 21)) changeStep(row.id, row.step);
        } else if ("toggle".equals(row.type)) {
            if (hit(mouseX, mouseY, bx + 37, rowY + 2, 96, 21)) toggle(row.id);
        } else if ("togglemode".equals(row.type)) {
            if (hit(mouseX, mouseY, bx + 37, rowY + 2, 96, 21)) {
                BetterZoom.holdToZoomEnabled = !BetterZoom.holdToZoomEnabled;
                BetterZoom.zoomToggleState = false;
                BetterZoom.lastMessage = BetterZoom.holdToZoomEnabled ? "Hold-to-zoom mode enabled." : "Toggle-zoom mode enabled.";
            }
        } else if (row.type.startsWith("key")) {
            int id = Integer.parseInt(row.type.substring(3));
            if (hit(mouseX, mouseY, bx - 4, rowY + 2, 138, 21)) {
                waitingForKey = true;
                waitingTarget = id;
                BetterZoom.lastMessage = "Press a key. Backspace unbinds; Escape cancels.";
            }
        } else if ("presets".equals(row.type)) {
            int[] levels = new int[] {2, 4, 8, 16};
            for (int i = 0; i < levels.length; i++) {
                int px = bx + i * 34;
                if (hit(mouseX, mouseY, px, rowY + 2, 31, 21)) {
                    BetterZoom.setZoom(levels[i]);
                    BetterZoom.lastMessage = "Zoom preset set to " + String.format(java.util.Locale.ROOT, "%.1fx", BetterZoom.zoomFactor);
                    break;
                }
            }
        } else if ("export".equals(row.type)) {
            if (hit(mouseX, mouseY, bx + 27, rowY + 2, 106, 21)) BetterZoom.exportConfig();
        } else if ("import".equals(row.type)) {
            if (hit(mouseX, mouseY, bx + 27, rowY + 2, 106, 21)) BetterZoom.importConfig();
        } else if ("defaults".equals(row.type)) {
            if (hit(mouseX, mouseY, bx + 27, rowY + 2, 106, 21)) BetterZoom.resetDefaults();
        }
        BetterZoom.clampZoom();
        BetterZoom.saveConfig();
    }

    private void changeStep(String id, float amount) {
        if ("zoom".equals(id)) BetterZoom.setZoom(BetterZoom.zoomFactor + amount);
        else if ("max".equals(id)) {
            BetterZoom.maxZoomFactor = Math.max(2F, Math.min(32F, BetterZoom.maxZoomFactor + amount));
            BetterZoom.clampZoom();
        } else if ("speed".equals(id)) BetterZoom.smoothSpeed = Math.max(0.05F, Math.min(1F, BetterZoom.smoothSpeed + amount));
        else if ("step".equals(id)) BetterZoom.zoomStep = Math.max(0.1F, Math.min(4F, BetterZoom.zoomStep + amount));
        else if ("multiplier".equals(id)) BetterZoom.sensitivityMultiplier = Math.max(0.05F, Math.min(1F, BetterZoom.sensitivityMultiplier + amount));
    }

    private void toggle(String id) {
        if ("smooth".equals(id)) BetterZoom.smoothZoomEnabled = !BetterZoom.smoothZoomEnabled;
        else if ("scroll".equals(id)) BetterZoom.scrollZoomEnabled = !BetterZoom.scrollZoomEnabled;
        else BetterZoom.reducedSensitivityEnabled = !BetterZoom.reducedSensitivityEnabled;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (waitingForKey) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                waitingForKey = false;
                waitingTarget = 0;
                BetterZoom.lastMessage = "Key binding cancelled.";
                return;
            }
            KeyBinding key = BetterZoom.keyForId(waitingTarget);
            if (key != null) {
                key.setKeyCode(keyCode == Keyboard.KEY_BACK ? Keyboard.KEY_NONE : keyCode);
                KeyBinding.resetKeyBindingArrayAndHash();
                mc.gameSettings.saveOptions();
                String conflict = BetterZoom.keyConflictMessage(waitingTarget);
                BetterZoom.lastMessage = conflict.isEmpty() ? "Key binding saved." : conflict;
            }
            waitingForKey = false;
            waitingTarget = 0;
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            BetterZoom.saveConfig();
            mc.displayGuiScreen(null);
            return;
        }
        searchField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        while (Mouse.next()) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            int wheel = Mouse.getEventDWheel();
            int contentTop = y + 101;
            int contentBottom = y + h - 55;
            if (wheel != 0 && hit(mouseX, mouseY, x + 12, contentTop, w - 24, Math.max(1, contentBottom - contentTop))) {
                scrollOffset += wheel > 0 ? -ROW_HEIGHT : ROW_HEIGHT;
                int maxScroll = Math.max(0, rowsForTab().size() * ROW_HEIGHT - (contentBottom - contentTop));
                if (scrollOffset < 0) scrollOffset = 0;
                if (scrollOffset > maxScroll) scrollOffset = maxScroll;
            }
            int button = Mouse.getEventButton();
            if (button >= 0 && Mouse.getEventButtonState()) {
                mouseClicked(mouseX, mouseY, button);
            } else if (button >= 0) {
                mouseReleased(mouseX, mouseY, button);
            }
        }
    }
}

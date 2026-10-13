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
    private int rowHeight() { return w < 360 ? 44 : ROW_HEIGHT; }
    private int x, y, w, h, scrollOffset;
    private boolean waitingForKey = false;
    private int waitingTarget = 0;
    private GuiTextField profileField;

    private static class SettingRow {
        final String id, label, description, type;
        final float min, max, step;
        SettingRow(String id, String label, String description, String type, float min, float max, float step) {
            this.id=id; this.label=label; this.description=description; this.type=type;
            this.min=min; this.max=max; this.step=step;
        }
    }

    @Override
    public void initGui() {
        w = Math.min(520, Math.max(220, width - 12));
        h = Math.min(500, Math.max(170, height - 12));
        x = (width - w) / 2; y = (height - h) / 2;
        scrollOffset = Math.max(0, scrollOffset);
        Keyboard.enableRepeatEvents(true);
        profileField = new GuiTextField(1, fontRendererObj, x + w - 168, y + 140, 132, 20);
        profileField.setMaxStringLength(32);
        profileField.setText("default");
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        BetterZoom.saveConfig();
    }

    private List<SettingRow> rowsForTab() {
        List<SettingRow> rows = new ArrayList<SettingRow>();
        rows.add(new SettingRow("zoom","Current zoom level","Current magnification. Use quick presets or + / −.","step",2F,BetterZoom.maxZoomFactor,0.5F));
        rows.add(new SettingRow("max","Maximum zoom limit","Range: 2x to 32x.","step",2F,32F,2F));
        rows.add(new SettingRow("presets","Quick zoom presets","Apply 2x, 4x, 8x or 16x.","presets",0,0,0));
        rows.add(new SettingRow("smooth","Smooth zoom animation","Turn animation off for instant zoom.","toggle",0,0,0));
        rows.add(new SettingRow("speed","Animation speed","Higher values make transitions faster.","step",0.05F,1F,0.05F));
        rows.add(new SettingRow("scroll","Mouse-wheel zoom","Allow the wheel to adjust zoom while zoom is active.","toggle",0,0,0));
        rows.add(new SettingRow("step","Zoom change per step","Wheel and zoom-up/down key step size.","step",0.1F,4F,0.1F));
        rows.add(new SettingRow("hold","Zoom key behavior","Choose hold or toggle mode.","togglemode",0,0,0));
        rows.add(new SettingRow("keyzoom","Zoom key","Click and press a key. Backspace unbinds; Escape cancels.","key1",0,0,0));
        rows.add(new SettingRow("keysettings","Open settings","Shortcut to open settings.","key2",0,0,0));
        rows.add(new SettingRow("keyin","Increase zoom","Increase magnification by one step.","key3",0,0,0));
        rows.add(new SettingRow("keyout","Decrease zoom","Decrease magnification by one step.","key4",0,0,0));
        rows.add(new SettingRow("keyreset","Reset zoom level","Return to default zoom level.","key5",0,0,0));
        rows.add(new SettingRow("invert","Invert wheel direction","Reverse zoom wheel direction.","toggle",0,0,0));
        rows.add(new SettingRow("sensitivity","Reduced mouse sensitivity","Reduce sensitivity while zooming.","toggle",0,0,0));
        rows.add(new SettingRow("multiplier","Sensitivity multiplier","Lower values slow camera movement while zoomed.","step",0.05F,1F,0.05F));
        rows.add(new SettingRow("hud","In-game zoom indicator","Show zoom level while zoom is active.","toggle",0,0,0));
        rows.add(new SettingRow("corner","HUD position","Cycle through four screen corners.","corner",0,3,1));
        rows.add(new SettingRow("profileName","Profile name","Letters, numbers, underscore or hyphen; up to 32 characters.","profileName",0,0,0));
        rows.add(new SettingRow("profileSave","Save named profile","Save current settings to a profile file.","profileSave",0,0,0));
        rows.add(new SettingRow("profileLoad","Load named profile","Load a saved profile.","profileLoad",0,0,0));
        rows.add(new SettingRow("profileDelete","Delete named profile","Delete the named profile file.","profileDelete",0,0,0));
        rows.add(new SettingRow("export","Export current profile","Write settings to config/betterzoom-export.cfg.","export",0,0,0));
        rows.add(new SettingRow("import","Import profile","Import config/betterzoom-import.cfg.","import",0,0,0));
        rows.add(new SettingRow("recover","Recover backup","Restore config/betterzoom-backup.cfg.","recover",0,0,0));
        rows.add(new SettingRow("defaults","Restore all defaults","Reset settings and keyboard shortcuts.","defaults",0,0,0));
        return rows;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        Gui.drawRect(x-2,y-2,x+w+2,y+h+2,0xFF000000);
        Gui.drawRect(x,y,x+w,y+h,0xFFC6C6C6);
        Gui.drawRect(x+2,y+2,x+w-2,y+h-2,0xFF373737);
        Gui.drawRect(x+4,y+4,x+w-4,y+h-4,0xFF202020);
        drawCenteredString(fontRendererObj,"Better Zoom Pro 2.0.0",width/2,y+8,0xFFFFFFFF);
        drawCenteredString(fontRendererObj,"Zoom: "+String.format(java.util.Locale.ROOT,"%.1fx",BetterZoom.zoomFactor)
            +"    Maximum: "+String.format(java.util.Locale.ROOT,"%.0fx",BetterZoom.maxZoomFactor),width/2,y+20,0xFFCCCCCC);
        int contentTop=y+36, contentBottom=y+h-43;
        Gui.drawRect(x+8,contentTop,x+w-8,contentBottom,0xFF101010);
        List<SettingRow> rows=rowsForTab();
        int viewHeight=Math.max(1,contentBottom-contentTop);
        int maxScroll=Math.max(0,rows.size()*rowHeight()-viewHeight);
        scrollOffset=Math.max(0,Math.min(scrollOffset,maxScroll));
        int controlX=x+w-146;
        boolean tooltipDrawn=false;
        for(int i=0;i<rows.size();i++){
            SettingRow row=rows.get(i);
            int rowY=contentTop+3+i*rowHeight()-scrollOffset;
            if(rowY<contentTop||rowY+rowHeight()-1>contentBottom) continue;
            if(i%2==0) Gui.drawRect(x+10,rowY,x+w-10,rowY+rowHeight()-1,0xFF191919);
            int labelWidth=w<360?w-24:Math.max(24,controlX-(x+16)-8);
            String shortLabel=fontRendererObj.trimStringToWidth(row.label,labelWidth);
            fontRendererObj.drawString(shortLabel,x+13,rowY+(w<360?2:7),0xFFFFFFFF);
            int rowControlX=w<360?x+(w-134)/2:controlX;
            drawRowControl(row,rowControlX,w<360?rowY+13:rowY,mouseX,mouseY);
            if(!tooltipDrawn&&mouseX>=x+9&&mouseX<=x+w-9&&mouseY>=rowY&&mouseY<rowY+rowHeight()-1){
                drawHoveringText(Arrays.asList(row.description),mouseX,mouseY);tooltipDrawn=true;
            }
        }
        if(maxScroll>0) drawCenteredString(fontRendererObj,"Scroll for more settings",width/2,contentBottom-10,0xFFAAAAAA);
        if(BetterZoom.lastMessage!=null&&!BetterZoom.lastMessage.isEmpty()){
            String status=fontRendererObj.trimStringToWidth(BetterZoom.lastMessage,w-24);
            drawCenteredString(fontRendererObj,status,width/2,y+h-36,status.startsWith("Key conflict")?0xFFFFCC55:0xFFDDDDDD);
        }
        drawAction(x+12,y+h-27,108,"Reset defaults",mouseX,mouseY,false);
        drawAction(x+w-104,y+h-27,92,"Done",mouseX,mouseY,true);
        super.drawScreen(mouseX,mouseY,partialTicks);
    }

    private void drawTab(int bx,int by,int bw,String label,int tab,int mx,int my) { }

    private void drawRowControl(SettingRow row,int bx,int by,int mx,int my){
        if("step".equals(row.type)){
            float value=getStepValue(row.id);
            drawButton(bx,by+2,25,21,"−",hit(mx,my,bx,by+2,25,21));
            drawValue(bx+28,by+2,78,formatValue(row.id,value),mx,my);
            drawButton(bx+109,by+2,25,21,"+",hit(mx,my,bx+109,by+2,25,21));
        }else if("toggle".equals(row.type)||"togglemode".equals(row.type)){
            boolean on="togglemode".equals(row.type)?BetterZoom.holdToZoomEnabled:getBoolean(row.id);
            String label="togglemode".equals(row.type)?(on?"HOLD":"TOGGLE"):(on?"ON":"OFF");
            drawButton(bx+37,by+2,96,21,label,hit(mx,my,bx+37,by+2,96,21));
        }else if(row.type.startsWith("key")){
            int id=Integer.parseInt(row.type.substring(3)); KeyBinding key=BetterZoom.keyForId(id);
            String name=waitingForKey&&waitingTarget==id?"PRESS KEY...":keyName(key);
            boolean conflict=!BetterZoom.keyConflictMessage(id).isEmpty();
            drawButton(bx-4,by+2,138,21,name,hit(mx,my,bx-4,by+2,138,21),conflict);
        }else if("presets".equals(row.type)){
            int[] levels={2,4,8,16};
            for(int i=0;i<levels.length;i++){int px=bx+i*34;drawButton(px,by+2,31,21,levels[i]+"x",hit(mx,my,px,by+2,31,21));}
        }else if("corner".equals(row.type)){
            String[] names={"TOP LEFT","TOP RIGHT","BOTTOM LEFT","BOTTOM RIGHT"};
            drawButton(bx+5,by+2,128,21,names[BetterZoom.indicatorCorner],hit(mx,my,bx+5,by+2,128,21));
        }else if("profileName".equals(row.type)){
            profileField.xPosition=bx; profileField.yPosition=by+2; profileField.width=134;
            profileField.drawTextBox();
        }else{
            String label;
            if("export".equals(row.type)) label="EXPORT";
            else if("import".equals(row.type)) label="IMPORT";
            else if("recover".equals(row.type)) label="RECOVER";
            else if("profileSave".equals(row.type)) label="SAVE";
            else if("profileLoad".equals(row.type)) label="LOAD";
            else if("profileDelete".equals(row.type)) label="DELETE";
            else label="RESET";
            drawButton(bx+27,by+2,106,21,label,hit(mx,my,bx+27,by+2,106,21));
        }
    }

    private void drawButton(int bx,int by,int bw,int bh,String label,boolean hovered){drawButton(bx,by,bw,bh,label,hovered,false);}
    private void drawButton(int bx,int by,int bw,int bh,String label,boolean hovered,boolean warning){
        int fill=hovered?0xFF6B6B6B:0xFF555555;
        int top=hovered?0xFFFFFFFF:0xFFB8B8B8;
        int bottom=hovered?0xFFB8B8B8:0xFF252525;
        Gui.drawRect(bx,by,bx+bw,by+bh,fill);
        Gui.drawRect(bx,by,bx+bw,by+1,warning?0xFFFFCC55:top);
        Gui.drawRect(bx,by,bx+1,by+bh,bottom);
        Gui.drawRect(bx,by+bh-1,bx+bw,by+bh,0xFF252525);
        drawCenteredString(fontRendererObj,label,bx+bw/2,by+(bh-8)/2,0xFFFFFFFF);
    }
    private void drawValue(int bx,int by,int bw,String label,int mx,int my){
        int fill=hit(mx,my,bx,by,bw,21)?0xFF4B4B4B:0xFF303030;
        Gui.drawRect(bx,by,bx+bw,by+21,fill);
        Gui.drawRect(bx,by,bx+bw,by+1,0xFF777777);
        Gui.drawRect(bx,by,bx+1,by+21,0xFF777777);
        drawCenteredString(fontRendererObj,label,bx+bw/2,by+7,0xFFFFFFFF);
    }
    private void drawAction(int bx,int by,int bw,String label,int mx,int my,boolean primary){
        drawButton(bx,by,bw,22,label,hit(mx,my,bx,by,bw,22));
    }

    private String keyName(KeyBinding key){
        if(key==null||key.getKeyCode()==Keyboard.KEY_NONE)return "UNBOUND";
        String name=Keyboard.getKeyName(key.getKeyCode());return name==null?"UNBOUND":name;
    }
    private float getStepValue(String id){
        if("zoom".equals(id))return BetterZoom.zoomFactor;
        if("max".equals(id))return BetterZoom.maxZoomFactor;
        if("speed".equals(id))return BetterZoom.smoothSpeed;
        if("step".equals(id))return BetterZoom.zoomStep;
        return BetterZoom.sensitivityMultiplier;
    }
    private String formatValue(String id,float value){
        if("zoom".equals(id)||"max".equals(id))return String.format(java.util.Locale.ROOT,"%.1fx",value);
        if("multiplier".equals(id))return Math.round(value*100F)+"%";
        return String.format(java.util.Locale.ROOT,"%.2f",value);
    }
    private boolean getBoolean(String id){
        if("smooth".equals(id))return BetterZoom.smoothZoomEnabled;
        if("scroll".equals(id))return BetterZoom.scrollZoomEnabled;
        if("invert".equals(id))return BetterZoom.invertWheelEnabled;
        if("hud".equals(id))return BetterZoom.showZoomIndicator;
        return BetterZoom.reducedSensitivityEnabled;
    }
    private boolean hit(int mx,int my,int bx,int by,int bw,int bh){return mx>=bx&&mx<bx+bw&&my>=by&&my<by+bh;}

    @Override
    protected void mouseClicked(int mouseX,int mouseY,int mouseButton)throws IOException{
        if(mouseButton!=0){super.mouseClicked(mouseX,mouseY,mouseButton);return;}
        profileField.mouseClicked(mouseX,mouseY,mouseButton);
        if(hit(mouseX,mouseY,x+12,y+h-27,108,22)){BetterZoom.resetDefaults();scrollOffset=0;return;}
        if(hit(mouseX,mouseY,x+w-104,y+h-27,92,22)){BetterZoom.saveConfig();mc.displayGuiScreen(null);return;}
        int contentTop=y+36,contentBottom=y+h-43;
        if(mouseY<contentTop||mouseY>=contentBottom)return;
        List<SettingRow> rows=rowsForTab();
        int index=(mouseY-contentTop-3+scrollOffset)/rowHeight();
        if(index<0||index>=rows.size())return;
        SettingRow row=rows.get(index);int rowY=contentTop+3+index*rowHeight()-scrollOffset;int bx=w < 360 ? x + (w - 134) / 2 : x+w-146;int controlY=rowY+(w < 360 ? 13 : 0);
        if(mouseY<rowY||mouseY>rowY+rowHeight()-2)return;
        if("step".equals(row.type)){
            if(hit(mouseX,mouseY,bx,controlY+2,25,21))changeStep(row.id,-row.step);
            else if(hit(mouseX,mouseY,bx+109,controlY+2,25,21))changeStep(row.id,row.step);
        }else if("toggle".equals(row.type)){
            if(hit(mouseX,mouseY,bx+37,controlY+2,96,21))toggle(row.id);
        }else if("togglemode".equals(row.type)){
            if(hit(mouseX,mouseY,bx+37,controlY+2,96,21)){BetterZoom.holdToZoomEnabled=!BetterZoom.holdToZoomEnabled;BetterZoom.zoomToggleState=false;}
        }else if(row.type.startsWith("key")){
            int id=Integer.parseInt(row.type.substring(3));
            if(hit(mouseX,mouseY,bx-4,controlY+2,138,21)){waitingForKey=true;waitingTarget=id;BetterZoom.lastMessage="Press a key. Backspace unbinds; Escape cancels.";}
        }else if("presets".equals(row.type)){
            int[] levels={2,4,8,16};
            for(int i=0;i<levels.length;i++){int px=bx+i*34;if(hit(mouseX,mouseY,px,controlY+2,31,21)){BetterZoom.setZoom(levels[i]);BetterZoom.lastMessage="Zoom preset set to "+String.format(java.util.Locale.ROOT,"%.1fx",BetterZoom.zoomFactor);break;}}
        }else if("corner".equals(row.type)){
            if(hit(mouseX,mouseY,bx+5,controlY+2,128,21)){BetterZoom.indicatorCorner=(BetterZoom.indicatorCorner+1)%4;BetterZoom.saveConfig();}
        }else if("profileSave".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.saveProfile(profileField.getText());}
        else if("profileLoad".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.loadProfile(profileField.getText());}
        else if("profileDelete".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.deleteProfile(profileField.getText());}
        else if("export".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.exportConfig();}
        else if("import".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.importConfig();}
        else if("recover".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.recoverConfigBackup();}
        else if("defaults".equals(row.type)){if(hit(mouseX,mouseY,bx+27,controlY+2,106,21))BetterZoom.resetDefaults();}
        BetterZoom.clampZoom();BetterZoom.saveConfig();
    }

    private void changeStep(String id,float amount){
        if("zoom".equals(id))BetterZoom.setZoom(BetterZoom.zoomFactor+amount);
        else if("max".equals(id)){BetterZoom.maxZoomFactor=Math.max(2F,Math.min(32F,BetterZoom.maxZoomFactor+amount));BetterZoom.clampZoom();}
        else if("speed".equals(id))BetterZoom.smoothSpeed=Math.max(0.05F,Math.min(1F,BetterZoom.smoothSpeed+amount));
        else if("step".equals(id))BetterZoom.zoomStep=Math.max(0.1F,Math.min(4F,BetterZoom.zoomStep+amount));
        else if("multiplier".equals(id))BetterZoom.sensitivityMultiplier=Math.max(0.05F,Math.min(1F,BetterZoom.sensitivityMultiplier+amount));
    }
    private void toggle(String id){
        if("smooth".equals(id))BetterZoom.smoothZoomEnabled=!BetterZoom.smoothZoomEnabled;
        else if("scroll".equals(id))BetterZoom.scrollZoomEnabled=!BetterZoom.scrollZoomEnabled;
        else if("invert".equals(id))BetterZoom.invertWheelEnabled=!BetterZoom.invertWheelEnabled;
        else if("hud".equals(id))BetterZoom.showZoomIndicator=!BetterZoom.showZoomIndicator;
        else BetterZoom.reducedSensitivityEnabled=!BetterZoom.reducedSensitivityEnabled;
    }

    @Override
    protected void keyTyped(char typedChar,int keyCode)throws IOException{
        if(waitingForKey){
            if(keyCode==Keyboard.KEY_ESCAPE){waitingForKey=false;waitingTarget=0;BetterZoom.lastMessage="Key binding cancelled.";return;}
            KeyBinding key=BetterZoom.keyForId(waitingTarget);
            if(key!=null){key.setKeyCode(keyCode==Keyboard.KEY_BACK?Keyboard.KEY_NONE:keyCode);KeyBinding.resetKeyBindingArrayAndHash();mc.gameSettings.saveOptions();
                String conflict=BetterZoom.keyConflictMessage(waitingTarget);BetterZoom.lastMessage=conflict.isEmpty()?"Key binding saved.":conflict;}
            waitingForKey=false;waitingTarget=0;return;
        }
        if(keyCode==Keyboard.KEY_ESCAPE){BetterZoom.saveConfig();mc.displayGuiScreen(null);return;}
        if(profileField.isFocused()){profileField.textboxKeyTyped(typedChar,keyCode);return;}
        super.keyTyped(typedChar,keyCode);
    }

    @Override
    public void handleMouseInput()throws IOException{
        while(Mouse.next()){
            int mouseX=Mouse.getEventX()*width/mc.displayWidth;
            int mouseY=height-Mouse.getEventY()*height/mc.displayHeight-1;
            int wheel=Mouse.getEventDWheel();
            int contentTop=y+36,contentBottom=y+h-43;
            if(wheel!=0&&hit(mouseX,mouseY,x+12,contentTop,w-24,Math.max(1,contentBottom-contentTop))){
                scrollOffset+=wheel>0?-rowHeight():rowHeight();
                int maxScroll=Math.max(0,rowsForTab().size()*rowHeight()-(contentBottom-contentTop));
                scrollOffset=Math.max(0,Math.min(scrollOffset,maxScroll));
            }
            int button=Mouse.getEventButton();
            if(button>=0&&Mouse.getEventButtonState())mouseClicked(mouseX,mouseY,button);
            else if(button>=0)mouseReleased(mouseX,mouseY,button);
        }
    }
}

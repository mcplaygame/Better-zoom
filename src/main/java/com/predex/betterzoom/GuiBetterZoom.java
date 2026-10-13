package com.predex.betterzoom;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class GuiBetterZoom extends GuiScreen {
 private int x,y,w=420,h=420;
 private boolean waiting=false;
 private int target=0;
 @Override public void initGui(){Keyboard.enableRepeatEvents(true);x=(width-w)/2;y=Math.max(6,(height-h)/2);}
 @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);BetterZoom.saveConfig();}
 @Override public void drawScreen(int mx,int my,float pt){
  drawDefaultBackground();
  Gui.drawRect(x-2,y-2,x+w+2,y+h+2,0xFF0A101C);
  Gui.drawRect(x,y,x+w,y+h,0xF0182234);
  Gui.drawRect(x,y,x+w,y+3,0xFF32D9FF);
  Gui.drawRect(x,y+3,x+4,y+h,0xFF32D9FF);
  Gui.drawRect(x+w-4,y+3,x+w,y+h,0xFFFF4FBD);
  drawCenteredString(fontRendererObj,"BETTER ZOOM",width/2,y+12,0xFFFFFFFF);
  drawCenteredString(fontRendererObj,"PERSONALIZE YOUR ZOOM EXPERIENCE",width/2,y+27,0xFF9DB2D1);
  Gui.drawRect(x+16,y+43,x+w-16,y+44,0xFF35445C);
  label("Zoom strength",x+18,y+50); step(x+250,y+48,"-",mx,my); val(x+284,y+48,60,String.format("%.1fx",BetterZoom.zoomFactor),mx,my); step(x+346,y+48,"+",mx,my);
  label("Smooth zoom animation",x+18,y+77); toggle(x+290,y+74,BetterZoom.smoothZoomEnabled,mx,my);
  label("Animation speed",x+18,y+103); step(x+250,y+100,"-",mx,my); val(x+284,y+100,60,String.format("%.2f",BetterZoom.smoothSpeed),mx,my); step(x+346,y+100,"+",mx,my);
  label("Mouse-wheel zoom control",x+18,y+129); toggle(x+290,y+126,BetterZoom.scrollZoomEnabled,mx,my);
  label("Zoom change per step",x+18,y+155); step(x+250,y+152,"-",mx,my); val(x+284,y+152,60,String.format("%.1f",BetterZoom.zoomStep),mx,my); step(x+346,y+152,"+",mx,my);
  label("Reduced mouse sensitivity",x+18,y+181); toggle(x+290,y+178,BetterZoom.reducedSensitivityEnabled,mx,my);
  label("Sensitivity while zooming",x+18,y+207); step(x+250,y+204,"-",mx,my); val(x+284,y+204,60,Math.round(BetterZoom.sensitivityMultiplier*100)+"%",mx,my); step(x+346,y+204,"+",mx,my);
  Gui.drawRect(x+16,y+232,x+w-16,y+233,0xFF35445C);
  label("KEYBOARD SHORTCUTS",x+18,y+237);
  label("Hold to zoom",x+18,y+259); keyButton(x+250,y+254,keyName(BetterZoom.zoomKey,1),mx,my);
  label("Open settings",x+18,y+285); keyButton(x+250,y+280,keyName(BetterZoom.settingsKey,2),mx,my);
  label("Increase zoom",x+18,y+311); keyButton(x+250,y+306,keyName(BetterZoom.zoomInKey,3),mx,my);
  label("Decrease zoom",x+18,y+337); keyButton(x+250,y+332,keyName(BetterZoom.zoomOutKey,4),mx,my);
  drawCenteredString(fontRendererObj,waiting?"Press a key • ESC cancels":"Keys can also be changed in Options > Controls",width/2,y+361,waiting?0xFFFFD66B:0xFF9DB2D1);
  action(x+80,y+388,110,"RESET DEFAULTS",mx,my,false); action(x+230,y+388,110,"DONE",mx,my,true);
  super.drawScreen(mx,my,pt);
 }
 private void label(String s,int a,int b){fontRendererObj.drawString(s,a,b+7,0xFFE6EDF8);}
 private boolean hit(int mx,int my,int a,int b,int ww,int hh){return mx>=a&&mx<a+ww&&my>=b&&my<b+hh;}
 private void val(int a,int b,int ww,String s,int mx,int my){Gui.drawRect(a,b,a+ww,b+22,hit(mx,my,a,b,ww,22)?0xFF304A67:0xFF202F45);Gui.drawRect(a,b,a+ww,b+1,0xFF435772);drawCenteredString(fontRendererObj,s,a+ww/2,b+7,0xFFFFFFFF);}
 private void step(int a,int b,String s,int mx,int my){Gui.drawRect(a,b,a+32,b+22,hit(mx,my,a,b,32,22)?0xFF20666B:0xFF174B50);Gui.drawRect(a,b,a+32,b+1,0xFF32D9FF);drawCenteredString(fontRendererObj,s,a+16,b+7,0xFFFFFFFF);}
 private void toggle(int a,int b,boolean on,int mx,int my){Gui.drawRect(a,b,a+98,b+22,hit(mx,my,a,b,98,22)?0xFF304A67:(on?0xFF174B50:0xFF483047));Gui.drawRect(a,b,a+98,b+1,on?0xFF32D9FF:0xFFFF4FBD);drawCenteredString(fontRendererObj,on?"ON":"OFF",a+49,b+7,0xFFFFFFFF);}
 private void keyButton(int a,int b,String s,int mx,int my){Gui.drawRect(a,b,a+138,b+22,hit(mx,my,a,b,138,22)?0xFF304A67:0xFF202F45);Gui.drawRect(a,b,a+138,b+1,0xFFFF4FBD);drawCenteredString(fontRendererObj,s,a+69,b+7,0xFFFFFFFF);}
 private void action(int a,int b,int ww,String s,int mx,int my,boolean primary){Gui.drawRect(a,b,a+ww,b+22,hit(mx,my,a,b,ww,22)?0xFF304A67:(primary?0xFF174B50:0xFF202F45));Gui.drawRect(a,b,a+ww,b+1,primary?0xFF32D9FF:0xFFFF4FBD);drawCenteredString(fontRendererObj,s,a+ww/2,b+7,0xFFFFFFFF);}
 private String keyName(KeyBinding k,int id){if(waiting&&target==id)return "PRESS KEY...";if(k==null||k.getKeyCode()==Keyboard.KEY_NONE)return "UNBOUND";String s=Keyboard.getKeyName(k.getKeyCode());return s==null?"UNBOUND":s;}
 @Override protected void mouseClicked(int mx,int my,int button){
  if(button!=0){super.mouseClicked(mx,my,button);return;}
  if(hit(mx,my,x+250,y+48,32,22))BetterZoom.zoomFactor=Math.max(2F,BetterZoom.zoomFactor-.5F);
  else if(hit(mx,my,x+346,y+48,32,22))BetterZoom.zoomFactor=Math.min(10F,BetterZoom.zoomFactor+.5F);
  else if(hit(mx,my,x+290,y+74,98,22))BetterZoom.smoothZoomEnabled=!BetterZoom.smoothZoomEnabled;
  else if(hit(mx,my,x+250,y+100,32,22))BetterZoom.smoothSpeed=Math.max(.05F,BetterZoom.smoothSpeed-.05F);
  else if(hit(mx,my,x+346,y+100,32,22))BetterZoom.smoothSpeed=Math.min(1F,BetterZoom.smoothSpeed+.05F);
  else if(hit(mx,my,x+290,y+126,98,22))BetterZoom.scrollZoomEnabled=!BetterZoom.scrollZoomEnabled;
  else if(hit(mx,my,x+250,y+152,32,22))BetterZoom.zoomStep=Math.max(.1F,BetterZoom.zoomStep-.1F);
  else if(hit(mx,my,x+346,y+152,32,22))BetterZoom.zoomStep=Math.min(2F,BetterZoom.zoomStep+.1F);
  else if(hit(mx,my,x+290,y+178,98,22))BetterZoom.reducedSensitivityEnabled=!BetterZoom.reducedSensitivityEnabled;
  else if(hit(mx,my,x+250,y+204,32,22))BetterZoom.sensitivityMultiplier=Math.max(.05F,BetterZoom.sensitivityMultiplier-.05F);
  else if(hit(mx,my,x+346,y+204,32,22))BetterZoom.sensitivityMultiplier=Math.min(1F,BetterZoom.sensitivityMultiplier+.05F);
  else if(hit(mx,my,x+250,y+254,138,22)){waiting=true;target=1;}
  else if(hit(mx,my,x+250,y+280,138,22)){waiting=true;target=2;}
  else if(hit(mx,my,x+250,y+306,138,22)){waiting=true;target=3;}
  else if(hit(mx,my,x+250,y+332,138,22)){waiting=true;target=4;}
  else if(hit(mx,my,x+80,y+388,110,22))resetDefaults();
  else if(hit(mx,my,x+230,y+388,110,22)){BetterZoom.saveConfig();mc.displayGuiScreen(null);}
  BetterZoom.saveConfig();
 }
 private void resetDefaults(){BetterZoom.zoomFactor=4F;BetterZoom.zoomStep=.5F;BetterZoom.smoothSpeed=.22F;BetterZoom.sensitivityMultiplier=.45F;BetterZoom.smoothZoomEnabled=true;BetterZoom.scrollZoomEnabled=true;BetterZoom.reducedSensitivityEnabled=true;BetterZoom.resetKeyBindings();BetterZoom.saveConfig();}
 @Override protected void keyTyped(char c,int code){
  if(waiting){if(code==Keyboard.KEY_ESCAPE){waiting=false;target=0;return;}
   if(target==1)BetterZoom.zoomKey.setKeyCode(code);else if(target==2)BetterZoom.settingsKey.setKeyCode(code);else if(target==3)BetterZoom.zoomInKey.setKeyCode(code);else if(target==4)BetterZoom.zoomOutKey.setKeyCode(code);
   KeyBinding.resetKeyBindingArrayAndHash();mc.gameSettings.saveOptions();waiting=false;target=0;return;
  }
  if(code==Keyboard.KEY_ESCAPE){BetterZoom.saveConfig();mc.displayGuiScreen(null);return;}super.keyTyped(c,code);
 }
}
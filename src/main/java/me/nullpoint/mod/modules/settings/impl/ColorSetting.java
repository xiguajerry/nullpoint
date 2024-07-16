// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.modules.settings.impl;

import java.awt.Color;
import java.util.function.Predicate;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.settings.Setting;

public class ColorSetting
        extends Setting {
   public boolean isRainbow = false;
   private Color value;
   public final Timer timer = new Timer();
   public boolean injectBoolean = false;
   public boolean booleanValue = false;
   public final float effectSpeed = 4.0f;

   public ColorSetting(String name, Color defaultValue) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
      this.value = defaultValue;
   }

   public ColorSetting(String name, Color defaultValue, Predicate visibilityIn) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
      this.value = defaultValue;
   }

   public ColorSetting(String name, int defaultValue) {
      this(name, new Color(defaultValue));
   }

   public ColorSetting(String name, int defaultValue, Predicate visibilityIn) {
      this(name, new Color(defaultValue), visibilityIn);
   }

   public final Color getValue() {
      if (this.isRainbow) {
         float[] HSB = Color.RGBtoHSB(this.value.getRed(), this.value.getGreen(), this.value.getBlue(), null);
         Color preColor = Color.getHSBColor((float)this.timer.getPassedTimeMs() * 0.36f * 4.0f / 20.0f % 361.0f / 360.0f, HSB[1], HSB[2]);
         this.setValue(new Color(preColor.getRed(), preColor.getGreen(), preColor.getBlue(), this.value.getAlpha()));
      }
      return this.value;
   }

   public final void setValue(Color value) {
      this.value = value;
   }

   public final void setValue(int value) {
      this.value = new Color(value, true);
   }

   public final void setRainbow(boolean rainbow) {
      this.isRainbow = rainbow;
   }

   public ColorSetting injectBoolean(boolean value) {
      this.injectBoolean = true;
      this.booleanValue = value;
      return this;
   }

   @Override
   public void loadSetting() {
      this.value = new Color(Nullpoint.CONFIG.getInt(this.getLine(), this.value.getRGB()), true);
      this.isRainbow = Nullpoint.CONFIG.getBoolean(this.getLine() + "Rainbow");
      if (this.injectBoolean) {
         this.booleanValue = Nullpoint.CONFIG.getBoolean(this.getLine() + "Boolean", this.booleanValue);
      }
   }
}

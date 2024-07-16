// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.modules.settings.impl;

import java.util.function.Predicate;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;

public class SliderSetting
        extends Setting {
   private double value;
   private final double minValue;
   private final double maxValue;
   private final double increment;
   private String suffix = "";

   public SliderSetting(String name, double value, double min, double max, double increment) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
      this.value = value;
      this.minValue = min;
      this.maxValue = max;
      this.increment = increment;
   }

   public SliderSetting(String name, double value, double min, double max) {
      this(name, value, min, max, 0.1);
   }

   public SliderSetting(String name, int value, int min, int max) {
      this(name, value, min, max, 1.0);
   }

   public SliderSetting(String name, double value, double min, double max, double increment, Predicate visibilityIn) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
      this.value = value;
      this.minValue = min;
      this.maxValue = max;
      this.increment = increment;
   }

   public SliderSetting(String name, double value, double min, double max, Predicate visibilityIn) {
      this(name, value, min, max, 0.1, visibilityIn);
   }

   public SliderSetting(String name, int value, int min, int max, Predicate visibilityIn) {
      this(name, value, min, max, 1.0, visibilityIn);
   }

   public final double getValue() {
      return this.value;
   }

   public final float getValueFloat() {
      return (float)this.value;
   }

   public final int getValueInt() {
      return (int)this.value;
   }

   public final void setValue(double value) {
      this.value = (double)Math.round(value / this.increment) * this.increment;
   }

   public final double getMinimum() {
      return this.minValue;
   }

   public final double getMaximum() {
      return this.maxValue;
   }

   public final double getIncrement() {
      return this.increment;
   }

   public final double getRange() {
      return this.maxValue - this.minValue;
   }

   public SliderSetting setSuffix(String suffix) {
      this.suffix = suffix;
      return this;
   }

   public String getSuffix() {
      return this.suffix;
   }

   @Override
   public void loadSetting() {
      this.setValue(Nullpoint.CONFIG.getFloat(this.getLine(), (float)this.value));
   }
}

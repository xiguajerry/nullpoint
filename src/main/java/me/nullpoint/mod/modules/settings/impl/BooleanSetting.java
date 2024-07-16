// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.modules.settings.impl;

import java.util.function.Predicate;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;

public class BooleanSetting
        extends Setting {
   public boolean parent = false;
   public boolean popped = false;
   private boolean value;

   public BooleanSetting(String name, boolean defaultValue) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
      this.value = defaultValue;
   }

   public BooleanSetting(String name, boolean defaultValue, Predicate visibilityIn) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name, visibilityIn);
      this.value = defaultValue;
   }

   public final boolean getValue() {
      return this.value;
   }

   public final void setValue(boolean value) {
      this.value = value;
   }

   public final void toggleValue() {
      this.value = !this.value;
   }

   public final boolean isOpen() {
      if (this.parent) {
         return this.popped;
      }
      return true;
   }

   @Override
   public void loadSetting() {
      this.value = Nullpoint.CONFIG.getBoolean(this.getLine(), this.value);
   }

   public BooleanSetting setParent() {
      this.parent = true;
      return this;
   }
}

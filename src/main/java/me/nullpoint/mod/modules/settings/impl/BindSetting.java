// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.modules.settings.impl;

import java.lang.reflect.Field;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.modules.settings.Setting;
import org.lwjgl.glfw.GLFW;

public class BindSetting
        extends Setting {
   private boolean isListening = false;
   private int key;
   private boolean pressed = false;
   private boolean holdEnable = false;
   public boolean hold = false;

   public BindSetting(String name, int key) {
      super(name, ModuleManager.lastLoadMod.getName() + "_" + name);
      this.key = key;
   }

   @Override
   public void loadSetting() {
      this.setKey(Nullpoint.CONFIG.getInt(this.getLine(), this.key));
      this.setHoldEnable(Nullpoint.CONFIG.getBoolean(this.getLine() + "_hold"));
   }

   public int getKey() {
      return this.key;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public String getBind() {
      Object kn;
      if (this.key == -1) {
         return "None";
      }
      if (this.key >= 3 && this.key <= 4) {
         switch (this.key) {
            case 3: {
               kn = "Mouse_4";
               break;
            }
            case 4: {
               kn = "Mouse_5";
               break;
            }
            default: {
               kn = "None";
               break;
            }
         }
      } else {
         Object object = kn = this.key > 0 ? GLFW.glfwGetKeyName(this.key, GLFW.glfwGetKeyScancode(this.key)) : "None";
      }
      if (kn == null) {
         try {
            for (Field declaredField : GLFW.class.getDeclaredFields()) {
               int a;
               if (!declaredField.getName().startsWith("GLFW_KEY_") || (a = ((Integer)declaredField.get(null)).intValue()) != this.key) continue;
               String nb = declaredField.getName().substring("GLFW_KEY_".length());
               kn = nb.substring(0, 1).toUpperCase() + nb.substring(1).toLowerCase();
            }
         }
         catch (Exception ignored) {
            kn = "None";
         }
      }
      return ((String)kn).toUpperCase();
   }

   public void setListening(boolean set) {
      this.isListening = set;
   }

   public boolean isListening() {
      return this.isListening;
   }

   public void setPressed(boolean pressed) {
      this.pressed = pressed;
   }

   public boolean isPressed() {
      return this.pressed;
   }

   public void setHoldEnable(boolean holdEnable) {
      this.holdEnable = holdEnable;
   }

   public boolean isHoldEnable() {
      return this.holdEnable;
   }
}

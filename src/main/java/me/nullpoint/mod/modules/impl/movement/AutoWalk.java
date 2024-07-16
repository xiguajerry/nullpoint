package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.mod.modules.Module;

public class AutoWalk extends Module {
   public static AutoWalk INSTANCE;

   public AutoWalk() {
      super("AutoWalk", Module.Category.Movement);
      INSTANCE = this;
   }

   public void onDisable() {
      mc.options.forwardKey.setPressed(false);
   }

   public void onUpdate() {
      mc.options.forwardKey.setPressed(true);
   }
}

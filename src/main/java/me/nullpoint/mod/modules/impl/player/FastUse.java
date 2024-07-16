package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class FastUse extends Module {
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 0.0, 0.0, 4.0, 1.0));

   public FastUse() {
      super("FastUse", Module.Category.Player);
   }

   public void onUpdate() {
      if (mc.itemUseCooldown <= 4 - this.delay.getValueInt()) {
         mc.itemUseCooldown = 0;
      }

   }
}

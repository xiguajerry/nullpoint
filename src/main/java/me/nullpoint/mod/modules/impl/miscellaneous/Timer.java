package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Timer extends Module {
   public final SliderSetting multiplier = this.add(new SliderSetting("Speed", 1.0, 0.1, 5.0, 0.01));
   public static Timer INSTANCE;

   public Timer() {
      super("Timer", Module.Category.Misc);
      this.setDescription("Increases the speed of Minecraft.");
      INSTANCE = this;
   }

   public void onDisable() {
      Nullpoint.TIMER.reset();
   }

   public void onUpdate() {
      Nullpoint.TIMER.tryReset();
   }

   public void onEnable() {
      Nullpoint.TIMER.reset();
   }
}

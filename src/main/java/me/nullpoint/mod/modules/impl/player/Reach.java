package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Reach extends Module {
   public static Reach INSTANCE;
   public final SliderSetting getDistance = this.add(new SliderSetting("Distance", 5.0, 1.0, 15.0, 0.1));

   public Reach() {
      super("Reach", Module.Category.Player);
      INSTANCE = this;
   }
}

package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;

public class SwingModifer extends Module {
   public static SwingModifer instance;
   public EnumSetting mode;

   public SwingModifer() {
      super("SwingModifer", Module.Category.Render);
      this.mode = this.add(new EnumSetting("Mode", SwingModifer.Mode.Main));
      instance = this;
   }

   public enum Mode {
      Main,
      OFF,
      None;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Main, OFF, None};
      }
   }
}

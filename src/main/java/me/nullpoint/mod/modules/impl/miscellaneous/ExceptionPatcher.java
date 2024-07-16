package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;

@Beta
public class ExceptionPatcher extends Module {
   public static ExceptionPatcher INSTANCE;
   public final BooleanSetting log = this.add(new BooleanSetting("Log", true));

   public ExceptionPatcher() {
      super("ExceptionPatcher", Module.Category.Misc);
      INSTANCE = this;
   }
}

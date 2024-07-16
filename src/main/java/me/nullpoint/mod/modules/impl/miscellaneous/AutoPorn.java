package me.nullpoint.mod.modules.impl.miscellaneous;

import java.io.IOException;
import me.nullpoint.mod.modules.Module;
import net.minecraft.util.Util;

public class AutoPorn extends Module {
   public AutoPorn() {
      super("AutoPorn", Module.Category.Misc);
   }

   public void onEnable() throws IOException {
      Util.getOperatingSystem().open("https://x10liumr8kvzin.com:58008/");
   }
}

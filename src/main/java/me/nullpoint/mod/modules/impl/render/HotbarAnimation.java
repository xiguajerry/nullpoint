package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class HotbarAnimation extends Module {
   public static HotbarAnimation INSTANCE;
   public final EnumSetting animMode;
   public final SliderSetting hotbarSpeed;

   public HotbarAnimation() {
      super("HotbarAnimation", Module.Category.Render);
      this.animMode = this.add(new EnumSetting("AnimMode", AnimateUtil.AnimMode.Mio));
      this.hotbarSpeed = this.add(new SliderSetting("HotbarSpeed", 0.2, 0.01, 1.0, 0.01));
      INSTANCE = this;
   }
}

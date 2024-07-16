package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class CustomFov extends Module {
   public static CustomFov INSTANCE;
   public final SliderSetting fov = this.add(new SliderSetting("Fov", 120, 0, 160));
   public final BooleanSetting itemFov = this.add(new BooleanSetting("itemFov", true));
   public final SliderSetting itemFovModifier = this.add(new SliderSetting("ItemModifier", 120, 0, 358));

   public CustomFov() {
      super("CustomFov", Module.Category.Render);
      INSTANCE = this;
   }
}

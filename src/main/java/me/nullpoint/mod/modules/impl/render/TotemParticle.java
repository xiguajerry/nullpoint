package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.Random;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TotemParticleEvent;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class TotemParticle extends Module {
   public static TotemParticle INSTANCE;
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 255)));
   private final ColorSetting color2 = this.add(new ColorSetting("Color2", new Color(0, 0, 0, 255)));
   public final SliderSetting velocityXZ = this.add((new SliderSetting("VelocityXZ", 100.0, 0.0, 500.0, 1.0)).setSuffix("%"));
   public final SliderSetting velocityY = this.add((new SliderSetting("VelocityY", 100.0, 0.0, 500.0, 1.0)).setSuffix("%"));
   Random random = new Random();

   public TotemParticle() {
      super("TotemParticle", Module.Category.Render);
      INSTANCE = this;
   }

   @EventHandler
   public void idk(TotemParticleEvent event) {
      event.cancel();
      event.velocityZ *= this.velocityXZ.getValue() / 100.0;
      event.velocityX *= this.velocityXZ.getValue() / 100.0;
      event.velocityY *= this.velocityY.getValue() / 100.0;
      event.color = ColorUtil.fadeColor(this.color.getValue(), this.color2.getValue(), this.random.nextDouble());
   }
}

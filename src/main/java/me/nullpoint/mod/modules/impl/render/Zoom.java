package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Zoom extends Module {
   public static Zoom INSTANCE;
   public double currentFov;
   final SliderSetting animSpeed = this.add(new SliderSetting("AnimSpeed", 0.1, 0.0, 1.0, 0.01));
   final SliderSetting fov = this.add(new SliderSetting("Fov", 60.0, -130.0, 130.0, 1.0));
   public static boolean on = false;

   public Zoom() {
      super("Zoom", Module.Category.Render);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new ZoomAnim());
   }

   public void onEnable() {
      if (mc.options.getFov().getValue() == 70) {
         mc.options.getFov().setValue(71);
      }

   }

   public class ZoomAnim {
      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (Zoom.this.isOn()) {
            Zoom.this.currentFov = AnimateUtil.animate(Zoom.this.currentFov, Zoom.this.fov.getValue(), Zoom.this.animSpeed.getValue());
            Zoom.on = true;
         } else if (Zoom.on) {
            Zoom.this.currentFov = AnimateUtil.animate(Zoom.this.currentFov, 0.0, Zoom.this.animSpeed.getValue());
            if ((int)Zoom.this.currentFov == 0) {
               Zoom.on = false;
            }
         }

      }
   }
}

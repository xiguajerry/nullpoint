package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Flight extends Module {
   public static Flight INSTANCE;
   public final SliderSetting speed = this.add(new SliderSetting("Speed", 1.0, 0.10000000149011612, 10.0));
   private final SliderSetting sneakDownSpeed = this.add(new SliderSetting("DownSpeed", 1.0, 0.10000000149011612, 10.0));
   private final SliderSetting upSpeed = this.add(new SliderSetting("UpSpeed", 1.0, 0.10000000149011612, 10.0));
   public final SliderSetting downFactor = this.add(new SliderSetting("DownFactor", 0.0, 0.0, 1.0, 9.999999974752427E-7));
   private MoveEvent event;

   public Flight() {
      super("Flight", "me", Module.Category.Movement);
      INSTANCE = this;
   }

   @EventHandler
   public void onMove(MoveEvent event) {
      if (!nullCheck()) {
         this.event = event;
         if (mc.options.sneakKey.isPressed() && mc.player.input.jumping) {
            this.setY(0.0);
         } else if (mc.options.sneakKey.isPressed()) {
            this.setY(-this.sneakDownSpeed.getValue());
         } else if (mc.player.input.jumping) {
            this.setY(this.upSpeed.getValue());
         } else {
            this.setY(-this.downFactor.getValue());
         }

         double[] dir = MovementUtil.directionSpeed(this.speed.getValue());
         this.setX(dir[0]);
         this.setZ(dir[1]);
      }
   }

   private void setX(double f) {
      this.event.setX(f);
      MovementUtil.setMotionX(f);
   }

   private void setY(double f) {
      this.event.setY(f);
      MovementUtil.setMotionY(f);
   }

   private void setZ(double f) {
      this.event.setZ(f);
      MovementUtil.setMotionZ(f);
   }
}

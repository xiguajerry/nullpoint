package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;

public class Sprint extends Module {
   public static Sprint INSTANCE;
   public final EnumSetting mode;
   public static boolean shouldSprint;

   public Sprint() {
      super("Sprint", Module.Category.Movement);
      this.mode = this.add(new EnumSetting("Mode", Sprint.Mode.Normal));
      this.setDescription("Permanently keeps player in sprinting mode.");
      INSTANCE = this;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   public void onUpdate() {
      if (!nullCheck()) {
         switch ((Mode)this.mode.getValue()) {
            case Legit:
               mc.options.sprintKey.setPressed(true);
               shouldSprint = false;
               break;
            case Normal:
               mc.options.sprintKey.setPressed(true);
               shouldSprint = false;
               if (mc.player.getHungerManager().getFoodLevel() <= 6 && !mc.player.isCreative()) {
                  return;
               }

               mc.player.setSprinting(MovementUtil.isMoving() && !mc.player.isSneaking());
               break;
            case Rage:
               shouldSprint = (mc.player.getHungerManager().getFoodLevel() > 6 || mc.player.isCreative()) && MovementUtil.isMoving() && !mc.player.isSneaking();
               mc.player.setSprinting(shouldSprint);
         }

      }
   }

   public enum Mode {
      Legit,
      Normal,
      Rage;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Legit, Normal, Rage};
      }
   }
}

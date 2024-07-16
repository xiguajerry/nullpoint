package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class Step extends Module {
   private final SliderSetting stepHeight = this.add(new SliderSetting("Height", 1.0, 0.0, 5.0, 0.5));
   public final BooleanSetting onlyMoving = this.add(new BooleanSetting("OnlyMoving", true));
   public static Step INSTANCE;

   public Step() {
      super("Step", Module.Category.Movement);
      INSTANCE = this;
   }

   public void onDisable() {
      if (!nullCheck()) {
         mc.player.setStepHeight(0.6F);
      }
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (!mc.player.isSneaking() && mc.player.horizontalCollision && !mc.player.isInLava() && !mc.player.isTouchingWater() && mc.player.isOnGround() && (EntityUtil.isInsideBlock() || !HoleKick.isInWeb(mc.player))) {
            mc.player.setStepHeight(this.stepHeight.getValueFloat());
         } else {
            mc.player.setStepHeight(0.6F);
         }
      }
   }
}

package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;

public class AntiVoid extends Module {
   private final SliderSetting voidHeight = this.add(new SliderSetting("VoidHeight", -64.0, -64.0, 319.0, 1.0));
   private final SliderSetting height = this.add(new SliderSetting("Height", 100.0, -40.0, 256.0, 1.0));

   public AntiVoid() {
      super("AntiVoid", "Allows you to fly over void blocks", Module.Category.Movement);
   }

   public void onUpdate() {
      boolean isVoid = true;

      for(int i = (int)mc.player.getY(); i > this.voidHeight.getValueInt() - 1; --i) {
         if (mc.world.getBlockState(new BlockPosX(mc.player.getX(), i, mc.player.getZ())).getBlock() != Blocks.AIR) {
            isVoid = false;
            break;
         }
      }

      if (mc.player.getY() < this.height.getValue() + this.voidHeight.getValue() && isVoid) {
         MovementUtil.setMotionY(0.0);
      }

   }
}

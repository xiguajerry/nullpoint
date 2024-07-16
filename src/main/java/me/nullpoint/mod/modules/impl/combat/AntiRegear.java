package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.MathHelper;

public class AntiRegear extends Module {
   private final SliderSetting safeRange = this.add(new SliderSetting("SafeRange", 2, 0, 8));
   private final SliderSetting range = this.add(new SliderSetting("Range", 5, 0, 8));

   public AntiRegear() {
      super("AntiRegear", "Shulker nuker", Module.Category.Combat);
   }

   public void onUpdate() {
      if (SpeedMine.breakPos == null || !(mc.world.getBlockState(SpeedMine.breakPos).getBlock() instanceof ShulkerBoxBlock)) {
         if (this.getBlock() != null) {
            SpeedMine.INSTANCE.mine(this.getBlock().getPos());
         }

      }
   }

   private ShulkerBoxBlockEntity getBlock() {
      Iterator var1 = BlockUtil.getTileEntities().iterator();

      while(var1.hasNext()) {
         BlockEntity entity = (BlockEntity)var1.next();
         if (entity instanceof ShulkerBoxBlockEntity shulker) {
            if (!((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= this.safeRange.getValue()) && (double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= this.range.getValue()) {
               return shulker;
            }
         }
      }

      return null;
   }
}

package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class AutoCity extends Module {
   public static AutoCity INSTANCE;
   private final BooleanSetting burrow = this.add(new BooleanSetting("Burrow", true));
   private final BooleanSetting surround = this.add(new BooleanSetting("Surround", true));
   public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1));

   public AutoCity() {
      super("AutoCity", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onUpdate() {
      PlayerEntity player = CombatUtil.getClosestEnemy(this.targetRange.getValue());
      if (player != null) {
         this.doBreak(player);
         this.doBreak(player);
      }
   }

   private void doBreak(PlayerEntity player) {
      BlockPos pos = EntityUtil.getEntityPos(player, true);
      if (this.burrow.getValue()) {
         double[] yOffset = new double[]{-0.8, 0.5, 1.1};
         double[] xzOffset = new double[]{0.3, -0.3, 0.0};
         Iterator var5 = CombatUtil.getEnemies(this.targetRange.getValue()).iterator();

         while(var5.hasNext()) {
            PlayerEntity entity = (PlayerEntity)var5.next();
            double[] var7 = yOffset;
            int var8 = yOffset.length;

            for(int var9 = 0; var9 < var8; ++var9) {
               double y = var7[var9];
               double[] var12 = xzOffset;
               int var13 = xzOffset.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  double x = var12[var14];
                  double[] var17 = xzOffset;
                  int var18 = xzOffset.length;

                  for(int var19 = 0; var19 < var18; ++var19) {
                     double z = var17[var19];
                     BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
                     if (this.isObsidian(offsetPos) && offsetPos.equals(SpeedMine.breakPos)) {
                        return;
                     }
                  }
               }
            }
         }

         yOffset = new double[]{0.5, 1.1};
         double[] var25 = yOffset;
         int var27 = yOffset.length;

         int var11;
         int var29;
         double y;
         double[] var31;
         int var32;
         double offset;
         for(var29 = 0; var29 < var27; ++var29) {
            y = var25[var29];
            var31 = xzOffset;
            var11 = xzOffset.length;

            for(var32 = 0; var32 < var11; ++var32) {
               offset = var31[var32];
               BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
               if (this.isObsidian(offsetPos)) {
                  SpeedMine.INSTANCE.mine(offsetPos);
                  return;
               }
            }
         }

         var25 = yOffset;
         var27 = yOffset.length;

         for(var29 = 0; var29 < var27; ++var29) {
            y = var25[var29];
            var31 = xzOffset;
            var11 = xzOffset.length;

            for(var32 = 0; var32 < var11; ++var32) {
               offset = var31[var32];
               double[] var35 = xzOffset;
               int var16 = xzOffset.length;

               for(int var36 = 0; var36 < var16; ++var36) {
                  double offset2 = var35[var36];
                  BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
                  if (this.isObsidian(offsetPos)) {
                     SpeedMine.INSTANCE.mine(offsetPos);
                     return;
                  }
               }
            }
         }
      }

      if (this.surround.getValue()) {
         Direction[] var23 = Direction.values();
         int var24 = var23.length;

         int var26;
         Direction i;
         for(var26 = 0; var26 < var24; ++var26) {
            i = var23[var26];
            if (i != Direction.UP && i != Direction.DOWN && BlockUtil.isAir(pos.offset(i)) && !player.getBoundingBox().intersects(new Box(pos.offset(i)))) {
               return;
            }
         }

         var23 = Direction.values();
         var24 = var23.length;

         for(var26 = 0; var26 < var24; ++var26) {
            i = var23[var26];
            if (i != Direction.UP && i != Direction.DOWN && !SpeedMine.godBlocks.contains(BlockUtil.getBlock(pos.offset(i))) && !(BlockUtil.getBlock(pos.offset(i)) instanceof BedBlock)) {
               SpeedMine.INSTANCE.mine(pos.offset(i));
               return;
            }
         }
      }

   }

   private boolean isObsidian(BlockPos pos) {
      return (BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST) && BlockUtil.getClickSideStrict(pos) != null && (!pos.equals(SpeedMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && !SilentDouble.INSTANCE.isOn());
   }
}

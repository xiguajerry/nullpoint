package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class SelfFlatten extends Module {
   public static SelfFlatten INSTANCE;
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 100, 0, 1000));
   private final Timer timer = new Timer();

   public SelfFlatten() {
      super("SelfFlatten", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (mc.player.isOnGround()) {
         if (this.timer.passedMs(this.delay.getValueInt())) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            int block;
            if ((block = this.getBlock()) != -1) {
               if (EntityUtil.isInsideBlock()) {
                  BlockPos pos1 = (new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6)).down();
                  BlockPos pos2 = (new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6)).down();
                  BlockPos pos3 = (new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6)).down();
                  BlockPos pos4 = (new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6)).down();
                  if (this.canPlace(pos1) || this.canPlace(pos2) || this.canPlace(pos3) || this.canPlace(pos4)) {
                     this.doSwap(block);
                     if (!this.tryPlaceObsidian(pos1, this.rotate.getValue()) && !this.tryPlaceObsidian(pos2, this.rotate.getValue()) && !this.tryPlaceObsidian(pos3, this.rotate.getValue())) {
                        this.tryPlaceObsidian(pos4, this.rotate.getValue());
                     }

                     if (this.inventory.getValue()) {
                        this.doSwap(block);
                        EntityUtil.syncInventory();
                     } else {
                        this.doSwap(oldSlot);
                     }

                  }
               }
            }
         }
      }
   }

   private boolean tryPlaceObsidian(BlockPos pos, boolean rotate) {
      if (this.canPlace(pos)) {
         if (this.checkMine.getValue() && BlockUtil.isMining(pos)) {
            return false;
         } else {
            Direction side;
            if ((side = BlockUtil.getPlaceSide(pos)) == null) {
               return false;
            } else {
               BlockUtil.placedPos.add(pos);
               BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate);
               this.timer.reset();
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private boolean canPlace(BlockPos pos) {
      if (BlockUtil.getPlaceSide(pos) == null) {
         return false;
      } else if (!BlockUtil.canReplace(pos)) {
         return false;
      } else {
         return !this.hasEntity(pos);
      }
   }

   private boolean hasEntity(BlockPos pos) {
      Iterator var2 = mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var2.hasNext()) {
                                 return false;
                              }

                              entity = (Entity)var2.next();
                           } while(entity == mc.player);
                        } while(!entity.isAlive());
                     } while(entity instanceof ItemEntity);
                  } while(entity instanceof ExperienceOrbEntity);
               } while(entity instanceof ExperienceBottleEntity);
            } while(entity instanceof ArrowEntity);
         } while(entity instanceof EndCrystalEntity);
      } while(entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue());

      return true;
   }

   private int getBlock() {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
   }
}

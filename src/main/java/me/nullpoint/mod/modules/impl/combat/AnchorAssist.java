package me.nullpoint.mod.modules.impl.combat;

import java.util.ArrayList;
import java.util.Iterator;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Beta
public class AnchorAssist extends Module {
   public static AnchorAssist INSTANCE;
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
   private final BooleanSetting checkMine = this.add(new BooleanSetting("CheckMine", false));
   private final SliderSetting range = this.add((new SliderSetting("TargetRange", 5.0, 0.0, 6.0, 0.1)).setSuffix("m"));
   private final SliderSetting minDamage = this.add(new SliderSetting("MinDamage", 6.0, 0.0, 36.0, 0.1));
   private final SliderSetting delay = this.add((new SliderSetting("Delay", 0.0, 0.0, 0.5, 0.01)).setSuffix("s"));
   private final Timer timer = new Timer();
   BlockPos foundPos;

   public AnchorAssist() {
      super("AnchorAssist", Module.Category.Combat);
      INSTANCE = this;
   }

   public String getInfo() {
      return this.foundPos != null ? "Helping" : null;
   }

   public void onUpdate() {
      this.foundPos = null;
      int anchor = this.findBlock(Blocks.RESPAWN_ANCHOR);
      int glowstone = this.findBlock(Blocks.GLOWSTONE);
      int old = mc.player.getInventory().selectedSlot;
      if (anchor != -1) {
         if (glowstone != -1) {
            if (!mc.player.isSneaking()) {
               if (!this.usingPause.getValue() || !mc.player.isUsingItem()) {
                  if (this.timer.passed((long)(this.delay.getValueFloat() * 1000.0F))) {
                     this.timer.reset();
                     double bestDamage = this.minDamage.getValue();
                     ArrayList list = new ArrayList();
                     Iterator var7 = CombatUtil.getEnemies(this.range.getValue()).iterator();

                     while(var7.hasNext()) {
                        PlayerEntity player = (PlayerEntity)var7.next();
                        list.add(new AnchorAura.PlayerAndPredict(player));
                     }

                     var7 = list.iterator();

                     while(true) {
                        BlockPos pos;
                        AnchorAura.PlayerAndPredict pap;
                        do {
                           if (!var7.hasNext()) {
                              BlockPos placePos;
                              if (this.foundPos != null && BlockUtil.getPlaceSide(this.foundPos, AnchorAura.INSTANCE.range.getValue()) == null && (placePos = this.getHelper(this.foundPos)) != null) {
                                 this.doSwap(anchor);
                                 BlockUtil.placeBlock(placePos, this.rotate.getValue());
                                 if (this.inventory.getValue()) {
                                    this.doSwap(anchor);
                                    EntityUtil.syncInventory();
                                 } else {
                                    this.doSwap(old);
                                 }
                              }

                              return;
                           }

                           pap = (AnchorAura.PlayerAndPredict)var7.next();
                           pos = EntityUtil.getEntityPos(pap.player, true).up(2);
                        } while(mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR);

                        if (BlockUtil.clientCanPlace(pos, true)) {
                           double damage = AnchorAura.INSTANCE.getAnchorDamage(pos, pap.player, pap.predict);
                           if (damage >= bestDamage) {
                              bestDamage = damage;
                              this.foundPos = pos;
                           }
                        }

                        Direction[] var18 = Direction.values();
                        int var11 = var18.length;

                        for(int var12 = 0; var12 < var11; ++var12) {
                           Direction i = var18[var12];
                           if (i != Direction.UP && i != Direction.DOWN && BlockUtil.clientCanPlace(pos.offset(i), false)) {
                              double damage = AnchorAura.INSTANCE.getAnchorDamage(pos.offset(i), pap.player, pap.predict);
                              if (damage >= bestDamage) {
                                 bestDamage = damage;
                                 this.foundPos = pos.offset(i);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public BlockPos getHelper(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction i = var2[var4];
         if ((!this.checkMine.getValue() || !BlockUtil.isMining(pos.offset(i))) && BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true) && BlockUtil.canPlace(pos.offset(i))) {
            return pos.offset(i);
         }
      }

      return null;
   }

   public int findBlock(Block blockIn) {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(blockIn) : InventoryUtil.findBlock(blockIn);
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }
}

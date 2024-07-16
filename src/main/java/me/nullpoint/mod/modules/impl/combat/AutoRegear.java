package me.nullpoint.mod.modules.impl.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ArmorItem.Type;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class AutoRegear extends Module {
   private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
   private final SliderSetting disableTime = this.add(new SliderSetting("DisableTime", 500, 0, 1000));
   public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final BooleanSetting place = this.add(new BooleanSetting("Place", true));
   private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final BooleanSetting preferOpen = this.add(new BooleanSetting("PerferOpen", true));
   private final BooleanSetting open = this.add(new BooleanSetting("Open", true));
   private final BooleanSetting close = this.add(new BooleanSetting("Close", true));
   private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 0.0, 6.0));
   private final SliderSetting minRange = this.add(new SliderSetting("MinRange", 1.0, 0.0, 3.0));
   private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
   private final BooleanSetting take = this.add(new BooleanSetting("Take", true));
   private final SliderSetting empty = this.add(new SliderSetting("Empty", 1, 0, 36, (v) -> {
      return this.take.getValue();
   }));
   private final BooleanSetting smart = this.add((new BooleanSetting("Smart", true, (v) -> {
      return this.take.getValue();
   })).setParent());
   private final SliderSetting helmet = this.add(new SliderSetting("Helmet", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting chestplate = this.add(new SliderSetting("ChestPlate", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting leggings = this.add(new SliderSetting("Leggings", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting boots = this.add(new SliderSetting("Boots", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting elytra = this.add(new SliderSetting("Elytra", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting sword = this.add(new SliderSetting("Sword", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting pickaxe = this.add(new SliderSetting("Pickaxe", 1, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting crystal = this.add(new SliderSetting("Crystal", 256, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting exp = this.add(new SliderSetting("Exp", 256, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting totem = this.add(new SliderSetting("Totem", 6, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting turtleMaster = this.add(new SliderSetting("Turtle_Master", 6, 0, 36, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting gapple = this.add(new SliderSetting("Gapple", 128, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting cfruit = this.add(new SliderSetting("Cfruit", 64, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting endChest = this.add(new SliderSetting("EndChest", 64, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting web = this.add(new SliderSetting("Web", 64, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting glowstone = this.add(new SliderSetting("Glowstone", 256, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting anchor = this.add(new SliderSetting("Anchor", 256, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting piston = this.add(new SliderSetting("Piston", 64, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting redstone = this.add(new SliderSetting("RedStone", 64, 0, 512, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   private final SliderSetting pearl = this.add(new SliderSetting("Pearl", 16, 0, 64, (v) -> {
      return this.take.getValue() && this.smart.isOpen();
   }));
   final int[] stealCountList = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   private final Timer timer = new Timer();
   BlockPos placePos = null;
   private final Timer disableTimer = new Timer();
   BlockPos openPos;
   boolean opend = false;

   public AutoRegear() {
      super("AutoRegear", "Auto place shulker and replenish", Module.Category.Combat);
   }

   public int findShulker() {
      AtomicInteger atomicInteger = new AtomicInteger(-1);
      if (this.findClass(ShulkerBoxBlock.class) != -1) {
         atomicInteger.set(this.findClass(ShulkerBoxBlock.class));
      }

      return atomicInteger.get();
   }

   public int findClass(Class clazz) {
      return this.inventory.getValue() ? InventoryUtil.findClassInventorySlot(clazz) : InventoryUtil.findClass(clazz);
   }

   public void onEnable() {
      this.openPos = null;
      this.disableTimer.reset();
      this.placePos = null;
      if (!nullCheck()) {
         int oldSlot = mc.player.getInventory().selectedSlot;
         if (this.place.getValue()) {
            double distance = 100.0;
            BlockPos bestPos = null;
            Iterator var5 = BlockUtil.getSphere((float)this.range.getValue()).iterator();

            while(true) {
               BlockPos pos;
               do {
                  while(true) {
                     do {
                        do {
                           do {
                              do {
                                 do {
                                    if (!var5.hasNext()) {
                                       if (bestPos != null) {
                                          int slot = this.findShulker();
                                          if (slot == -1) {
                                             CommandManager.sendChatMessage("\u00a7c[!] No shulkerbox found");
                                             return;
                                          }

                                          this.doSwap(slot);
                                          this.placeBlock(bestPos);
                                          this.placePos = bestPos;
                                          if (this.inventory.getValue()) {
                                             this.doSwap(slot);
                                             EntityUtil.syncInventory();
                                          } else {
                                             this.doSwap(oldSlot);
                                          }

                                          this.timer.reset();
                                       } else {
                                          CommandManager.sendChatMessage("\u00a7c[!] No place pos found");
                                       }

                                       return;
                                    }

                                    pos = (BlockPos)var5.next();
                                 } while(!BlockUtil.isAir(pos.up()));

                                 if (this.preferOpen.getValue() && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                                    return;
                                 }
                              } while((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos())) < this.minRange.getValue());
                           } while(!BlockUtil.clientCanPlace(pos, false));
                        } while(!BlockUtil.isStrictDirection(pos.offset(Direction.DOWN), Direction.UP));
                     } while(!BlockUtil.canClick(pos.offset(Direction.DOWN)));

                     if (!this.detectMining.getValue() || !Nullpoint.BREAK.isMining(pos) && !pos.equals(SpeedMine.breakPos)) {
                        break;
                     }
                  }
               } while(bestPos != null && !((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos())) < distance));

               distance = MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos()));
               bestPos = pos;
            }
         }
      }
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private void update() {
      this.stealCountList[0] = (int)(this.crystal.getValue() - (double)InventoryUtil.getItemCount(Items.END_CRYSTAL));
      this.stealCountList[1] = (int)(this.exp.getValue() - (double)InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE));
      this.stealCountList[2] = (int)(this.totem.getValue() - (double)InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING));
      this.stealCountList[3] = (int)(this.gapple.getValue() - (double)InventoryUtil.getItemCount(Items.ENCHANTED_GOLDEN_APPLE));
      this.stealCountList[4] = (int)(this.endChest.getValue() - (double)InventoryUtil.getItemCount(Item.fromBlock(Blocks.ENDER_CHEST)));
      this.stealCountList[5] = (int)(this.web.getValue() - (double)InventoryUtil.getItemCount(Item.fromBlock(Blocks.COBWEB)));
      this.stealCountList[6] = (int)(this.glowstone.getValue() - (double)InventoryUtil.getItemCount(Item.fromBlock(Blocks.GLOWSTONE)));
      this.stealCountList[7] = (int)(this.anchor.getValue() - (double)InventoryUtil.getItemCount(Item.fromBlock(Blocks.RESPAWN_ANCHOR)));
      this.stealCountList[8] = (int)(this.pearl.getValue() - (double)InventoryUtil.getItemCount(Items.ENDER_PEARL));
      this.stealCountList[9] = (int)(this.turtleMaster.getValue() - (double)InventoryUtil.getPotCount(StatusEffects.RESISTANCE));
      this.stealCountList[10] = (int)(this.helmet.getValue() - (double)InventoryUtil.getArmorCount(Type.HELMET));
      this.stealCountList[11] = (int)(this.chestplate.getValue() - (double)InventoryUtil.getArmorCount(Type.CHESTPLATE));
      this.stealCountList[12] = (int)(this.leggings.getValue() - (double)InventoryUtil.getArmorCount(Type.LEGGINGS));
      this.stealCountList[13] = (int)(this.boots.getValue() - (double)InventoryUtil.getArmorCount(Type.BOOTS));
      this.stealCountList[14] = (int)(this.elytra.getValue() - (double)InventoryUtil.getItemCount(Items.ELYTRA));
      this.stealCountList[15] = (int)(this.sword.getValue() - (double)InventoryUtil.getClassCount(SwordItem.class));
      this.stealCountList[16] = (int)(this.pickaxe.getValue() - (double)InventoryUtil.getClassCount(PickaxeItem.class));
      this.stealCountList[17] = (int)(this.piston.getValue() - (double)InventoryUtil.getClassCount(PistonBlock.class));
      this.stealCountList[18] = (int)(this.redstone.getValue() - (double)InventoryUtil.getItemCount(Item.fromBlock(Blocks.REDSTONE_BLOCK)));
      this.stealCountList[19] = (int)(this.cfruit.getValue() - (double)InventoryUtil.getItemCount(Items.CHORUS_FRUIT));
   }

   public void onDisable() {
      this.opend = false;
      if (this.mine.getValue() && this.placePos != null) {
         SpeedMine.INSTANCE.mine(this.placePos);
      }

   }

   public void onUpdate() {
      if (this.smart.getValue()) {
         this.update();
      }

      boolean take;
      if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
         if (this.opend) {
            this.opend = false;
            if (this.autoDisable.getValue()) {
               this.disable2();
            }

            if (this.mine.getValue() && this.openPos != null) {
               if (mc.world.getBlockState(this.openPos).getBlock() instanceof ShulkerBoxBlock) {
                  SpeedMine.INSTANCE.mine(this.openPos);
               } else {
                  this.openPos = null;
               }
            }

         } else {
            if (this.open.getValue()) {
               if (this.placePos == null || !((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(this.placePos.toCenterPos())) <= this.range.getValue()) || !mc.world.isAir(this.placePos.up()) || this.timer.passedMs(500L) && !(mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock)) {
                  take = false;
                  Iterator var5 = BlockUtil.getSphere((float)this.range.getValue()).iterator();

                  while(var5.hasNext()) {
                     BlockPos pos = (BlockPos)var5.next();
                     if (BlockUtil.isAir(pos.up()) && mc.world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                        this.openPos = pos;
                        BlockUtil.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue());
                        take = true;
                        break;
                     }
                  }

                  if (!take && this.autoDisable.getValue()) {
                     this.disable2();
                  }
               } else if (mc.world.getBlockState(this.placePos).getBlock() instanceof ShulkerBoxBlock) {
                  this.openPos = this.placePos;
                  BlockUtil.clickBlock(this.placePos, BlockUtil.getClickSide(this.placePos), this.rotate.getValue());
               }
            } else if (!this.take.getValue() && this.autoDisable.getValue()) {
               this.disable2();
            }

         }
      } else {
         this.opend = true;
         if (!this.take.getValue()) {
            if (this.autoDisable.getValue()) {
               this.disable2();
            }

         } else {
            take = false;
            ScreenHandler var3 = mc.player.currentScreenHandler;
            if (var3 instanceof ShulkerBoxScreenHandler shulker) {
                Iterator var6 = shulker.slots.iterator();

               label115:
               while(true) {
                  Slot slot;
                  do {
                     do {
                        do {
                           if (!var6.hasNext()) {
                              break label115;
                           }

                           slot = (Slot)var6.next();
                        } while(slot.id >= 27);
                     } while(slot.getStack().isEmpty());
                  } while(this.smart.getValue() && !this.needSteal(slot.getStack()));

                  if ((double)InventoryUtil.getEmptySlotCount() > this.empty.getValue()) {
                     mc.interactionManager.clickSlot(shulker.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                     take = true;
                  }
               }
            }

            if (this.autoDisable.getValue() && !take) {
               this.disable2();
            }

         }
      }
   }

   private void disable2() {
      if (this.disableTimer.passedMs(this.disableTime.getValueInt())) {
         if (this.close.getValue()) {
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.player.closeHandledScreen();
         }

         this.disable();
      }

   }

   private boolean needSteal(ItemStack i) {
      if (i.getItem().equals(Items.END_CRYSTAL) && this.stealCountList[0] > 0) {
         this.stealCountList[0] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Items.EXPERIENCE_BOTTLE) && this.stealCountList[1] > 0) {
         this.stealCountList[1] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Items.TOTEM_OF_UNDYING) && this.stealCountList[2] > 0) {
         this.stealCountList[2] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE) && this.stealCountList[3] > 0) {
         this.stealCountList[3] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Item.fromBlock(Blocks.ENDER_CHEST)) && this.stealCountList[4] > 0) {
         this.stealCountList[4] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Item.fromBlock(Blocks.COBWEB)) && this.stealCountList[5] > 0) {
         this.stealCountList[5] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Item.fromBlock(Blocks.GLOWSTONE)) && this.stealCountList[6] > 0) {
         this.stealCountList[6] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Item.fromBlock(Blocks.RESPAWN_ANCHOR)) && this.stealCountList[7] > 0) {
         this.stealCountList[7] -= i.getCount();
         return true;
      } else if (i.getItem().equals(Items.ENDER_PEARL) && this.stealCountList[8] > 0) {
         this.stealCountList[8] -= i.getCount();
         return true;
      } else {
         if (i.getItem().equals(Items.SPLASH_POTION) && this.stealCountList[9] > 0) {
            List effects = new ArrayList(PotionUtil.getPotionEffects(i));
            Iterator var3 = effects.iterator();

            while(var3.hasNext()) {
               StatusEffectInstance potionEffect = (StatusEffectInstance)var3.next();
               if (potionEffect.getEffectType() == StatusEffects.RESISTANCE) {
                  this.stealCountList[9] -= i.getCount();
                  return true;
               }
            }
         }

         if (InventoryUtil.CheckArmorType(i.getItem(), Type.HELMET) && this.stealCountList[10] > 0) {
            this.stealCountList[10] -= i.getCount();
            return true;
         } else if (InventoryUtil.CheckArmorType(i.getItem(), Type.CHESTPLATE) && this.stealCountList[11] > 0) {
            this.stealCountList[11] -= i.getCount();
            return true;
         } else if (InventoryUtil.CheckArmorType(i.getItem(), Type.LEGGINGS) && this.stealCountList[12] > 0) {
            this.stealCountList[12] -= i.getCount();
            return true;
         } else if (InventoryUtil.CheckArmorType(i.getItem(), Type.BOOTS) && this.stealCountList[13] > 0) {
            this.stealCountList[13] -= i.getCount();
            return true;
         } else if (i.getItem().equals(Items.ELYTRA) && this.stealCountList[14] > 0) {
            this.stealCountList[14] -= i.getCount();
            return true;
         } else if (i.getItem() instanceof SwordItem && this.stealCountList[15] > 0) {
            this.stealCountList[15] -= i.getCount();
            return true;
         } else if (i.getItem() instanceof PickaxeItem && this.stealCountList[16] > 0) {
            this.stealCountList[16] -= i.getCount();
            return true;
         } else if (i.getItem() instanceof BlockItem && ((BlockItem)i.getItem()).getBlock() instanceof PistonBlock && this.stealCountList[17] > 0) {
            this.stealCountList[17] -= i.getCount();
            return true;
         } else if (i.getItem().equals(Item.fromBlock(Blocks.REDSTONE_BLOCK)) && this.stealCountList[18] > 0) {
            this.stealCountList[18] -= i.getCount();
            return true;
         } else if (i.getItem().equals(Items.CHORUS_FRUIT) && this.stealCountList[19] > 0) {
            this.stealCountList[19] -= i.getCount();
            return true;
         } else {
            return false;
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      BlockUtil.clickBlock(pos.offset(Direction.DOWN), Direction.UP, this.rotate.getValue());
   }
}

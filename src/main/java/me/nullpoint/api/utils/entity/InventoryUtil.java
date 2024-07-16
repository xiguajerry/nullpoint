package me.nullpoint.api.utils.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtil implements Wrapper {
   static int lastSlot = -1;
   static int lastSelect = -1;

   public static void inventorySwap(int slot, int selectedSlot) {
      if (slot == lastSlot) {
         switchToSlot(lastSelect);
         lastSlot = -1;
         lastSelect = -1;
      } else if (slot - 36 != selectedSlot) {
         if (CombatSetting.INSTANCE.invSwapBypass.getValue()) {
            if (slot - 36 >= 0) {
               lastSlot = slot;
               lastSelect = selectedSlot;
               switchToSlot(slot - 36);
            } else {
               mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));
            }
         } else {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, selectedSlot, SlotActionType.SWAP, mc.player);
         }
      }
   }

   public static void doSwap(int slot) {
      inventorySwap(slot, mc.player.getInventory().selectedSlot);
      switchToSlot(slot);
   }

   public static void switchToSlot(int slot) {
      mc.player.getInventory().selectedSlot = slot;
      mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
   }

   public static boolean holdingItem(Class clazz) {
      ItemStack stack = mc.player.getMainHandStack();
      boolean result = isInstanceOf(stack, clazz);
      if (!result) {
         result = isInstanceOf(stack, clazz);
      }

      return result;
   }

   public static boolean isInstanceOf(ItemStack stack, Class clazz) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (clazz.isInstance(item)) {
            return true;
         } else if (item instanceof BlockItem) {
            Block block = Block.getBlockFromItem(item);
            return clazz.isInstance(block);
         } else {
            return false;
         }
      }
   }

   public static ItemStack getStackInSlot(int i) {
      return mc.player.getInventory().getStack(i);
   }

   public static int findItem(Item input) {
      for(int i = 0; i < 9; ++i) {
         Item item = getStackInSlot(i).getItem();
         if (Item.getRawId(item) == Item.getRawId(input)) {
            return i;
         }
      }

      return -1;
   }

   public static int getItemCount(Item item) {
      int count = 0;
      Iterator var2 = getInventoryAndHotbarSlots().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (((ItemStack)entry.getValue()).getItem() == item) {
            count += ((ItemStack)entry.getValue()).getCount();
         }
      }

      return count;
   }

   public static int getEmptySlotCount() {
      int count = 0;
      Iterator var1 = getNoArmorInventoryAndHotbarSlots().entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         if (entry.getValue() == ItemStack.EMPTY) {
            ++count;
         }
      }

      return count;
   }

   public static int findClass(Class clazz) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof BlockItem && clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int getClassCount(Class clazz) {
      int count = 0;
      Iterator var2 = getInventoryAndHotbarSlots().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (entry.getValue() != ItemStack.EMPTY) {
            if (clazz.isInstance(((ItemStack)entry.getValue()).getItem())) {
               count += ((ItemStack)entry.getValue()).getCount();
            }

            if (((ItemStack)entry.getValue()).getItem() instanceof BlockItem && clazz.isInstance(((BlockItem)((ItemStack)entry.getValue()).getItem()).getBlock())) {
               count += ((ItemStack)entry.getValue()).getCount();
            }
         }
      }

      return count;
   }

   public static int findClassInventorySlot(Class clazz) {
      for(int i = 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i < 9 ? i + 36 : i;
            }

            if (stack.getItem() instanceof BlockItem && clazz.isInstance(((BlockItem)stack.getItem()).getBlock())) {
               return i < 9 ? i + 36 : i;
            }
         }
      }

      return -1;
   }

   public static int findBlock(Block blockIn) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   public static int getPotCount(StatusEffect potion) {
      int count = 0;
      Iterator var2 = getInventoryAndHotbarSlots().entrySet().iterator();

      while(true) {
         while(true) {
            Map.Entry entry;
            do {
               if (!var2.hasNext()) {
                  return count;
               }

               entry = (Map.Entry)var2.next();
            } while(!(((ItemStack)entry.getValue()).getItem() instanceof SplashPotionItem));

            List effects = new ArrayList(PotionUtil.getPotionEffects((ItemStack)entry.getValue()));
            Iterator var5 = effects.iterator();

            while(var5.hasNext()) {
               StatusEffectInstance potionEffect = (StatusEffectInstance)var5.next();
               if (potionEffect.getEffectType() == potion) {
                  count += ((ItemStack)entry.getValue()).getCount();
                  break;
               }
            }
         }
      }
   }

   public static int getArmorCount(ArmorItem.Type type) {
      int count = 0;
      Iterator var2 = getInventoryAndHotbarSlots().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (((ItemStack)entry.getValue()).getItem() instanceof ArmorItem && ((ArmorItem)((ItemStack)entry.getValue()).getItem()).getType() == type) {
            count += ((ItemStack)entry.getValue()).getCount();
         }
      }

      return count;
   }

   public static boolean CheckArmorType(Item item, ArmorItem.Type type) {
      return item instanceof ArmorItem && ((ArmorItem)item).getType() == type;
   }

   public static int findPot(StatusEffect potion) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof SplashPotionItem) {
            List effects = new ArrayList(PotionUtil.getPotionEffects(stack));
            Iterator var4 = effects.iterator();

            while(var4.hasNext()) {
               StatusEffectInstance potionEffect = (StatusEffectInstance)var4.next();
               if (potionEffect.getEffectType() == potion) {
                  return i;
               }
            }
         }
      }

      return -1;
   }

   public static int findUnBlock() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (!(stack.getItem() instanceof BlockItem)) {
            return i;
         }
      }

      return -1;
   }

   public static int findBlock() {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = getStackInSlot(i);
         if (stack.getItem() instanceof BlockItem && !BlockUtil.shiftBlocks.contains(Block.getBlockFromItem(stack.getItem())) && ((BlockItem)stack.getItem()).getBlock() != Blocks.COBWEB) {
            return i;
         }
      }

      return -1;
   }

   public static int findBlockInventorySlot(Block block) {
      return findItemInventorySlot(Item.fromBlock(block));
   }

   public static int findItemInventorySlot(Item item) {
      for(int i = 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack.getItem() == item) {
            return i < 9 ? i + 36 : i;
         }
      }

      return -1;
   }

   public static int findPotInventorySlot(StatusEffect potion) {
      for(int i = 0; i < 45; ++i) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof SplashPotionItem) {
            List effects = new ArrayList(PotionUtil.getPotionEffects(stack));
            Iterator var4 = effects.iterator();

            while(var4.hasNext()) {
               StatusEffectInstance potionEffect = (StatusEffectInstance)var4.next();
               if (potionEffect.getEffectType() == potion) {
                  return i < 9 ? i + 36 : i;
               }
            }
         }
      }

      return -1;
   }

   public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
      HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<>();

      for(int current = 0; current <= 44; ++current) {
         fullInventorySlots.put(current, mc.player.getInventory().getStack(current));
      }

      return fullInventorySlots;
   }

   public static Map getNoArmorInventoryAndHotbarSlots() {
      HashMap fullInventorySlots = new HashMap();

      for(int current = 0; current <= 35; ++current) {
         fullInventorySlots.put(current, mc.player.getInventory().getStack(current));
      }

      return fullInventorySlots;
   }
}

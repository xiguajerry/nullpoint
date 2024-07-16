package me.nullpoint.mod.modules.impl.player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.movement.ElytraFly;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {
   private final BooleanSetting noMove = this.add(new BooleanSetting("NoMove", false));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 3.0, 0.0, 10.0, 1.0));
   private final BooleanSetting autoElytra = this.add(new BooleanSetting("AutoElytra", true));
   public static AutoArmor INSTANCE;
   private int tickDelay = 0;

   public AutoArmor() {
      super("AutoArmor", Module.Category.Player);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ClickGuiScreen) {
         if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
            if (!MovementUtil.isMoving() || !this.noMove.getValue()) {
               if (this.tickDelay > 0) {
                  --this.tickDelay;
               } else {
                  this.tickDelay = this.delay.getValueInt();
                  Map armorMap = new HashMap(4);
                  armorMap.put(EquipmentSlot.FEET, new int[]{36, this.getProtection(mc.player.getInventory().getStack(36)), -1, -1});
                  armorMap.put(EquipmentSlot.LEGS, new int[]{37, this.getProtection(mc.player.getInventory().getStack(37)), -1, -1});
                  armorMap.put(EquipmentSlot.CHEST, new int[]{38, this.getProtection(mc.player.getInventory().getStack(38)), -1, -1});
                  armorMap.put(EquipmentSlot.HEAD, new int[]{39, this.getProtection(mc.player.getInventory().getStack(39)), -1, -1});

                  label141:
                  for(int s = 0; s < 36; ++s) {
                     if (mc.player.getInventory().getStack(s).getItem() instanceof ArmorItem || mc.player.getInventory().getStack(s).getItem() == Items.ELYTRA) {
                        int protection = this.getProtection(mc.player.getInventory().getStack(s));
                        EquipmentSlot slot = mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem ? EquipmentSlot.CHEST : ((ArmorItem)mc.player.getInventory().getStack(s).getItem()).getSlotType();
                        Iterator var5 = armorMap.entrySet().iterator();

                        while(true) {
                           Map.Entry e;
                           do {
                              do {
                                 while(true) {
                                    if (!var5.hasNext()) {
                                       continue label141;
                                    }

                                    e = (Map.Entry)var5.next();
                                    if (this.autoElytra.getValue() && ElytraFly.INSTANCE.isOn() && e.getKey() == EquipmentSlot.CHEST) {
                                       break;
                                    }

                                    if (protection > 0 && e.getKey() == slot && protection > ((int[])e.getValue())[1] && protection > ((int[])e.getValue())[3]) {
                                       ((int[])e.getValue())[2] = s;
                                       ((int[])e.getValue())[3] = protection;
                                    }
                                 }
                              } while(!mc.player.getInventory().getStack(38).isEmpty() && mc.player.getInventory().getStack(38).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(38)));
                           } while(((int[])e.getValue())[2] != -1 && !mc.player.getInventory().getStack(((int[])e.getValue())[2]).isEmpty() && mc.player.getInventory().getStack(((int[])e.getValue())[2]).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(((int[])e.getValue())[2])));

                           if (!mc.player.getInventory().getStack(s).isEmpty() && mc.player.getInventory().getStack(s).getItem() instanceof ElytraItem && ElytraItem.isUsable(mc.player.getInventory().getStack(s))) {
                              ((int[])e.getValue())[2] = s;
                           }
                        }
                     }
                  }

                  Iterator var7 = armorMap.entrySet().iterator();

                  Map.Entry equipmentSlotEntry;
                  do {
                     if (!var7.hasNext()) {
                        return;
                     }

                     equipmentSlotEntry = (Map.Entry)var7.next();
                  } while(((int[])equipmentSlotEntry.getValue())[2] == -1);

                  if (((int[])equipmentSlotEntry.getValue())[1] == -1 && ((int[])equipmentSlotEntry.getValue())[2] < 9) {
                     if (((int[])equipmentSlotEntry.getValue())[2] != mc.player.getInventory().selectedSlot) {
                        mc.player.getInventory().selectedSlot = ((int[])equipmentSlotEntry.getValue())[2];
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(((int[])equipmentSlotEntry.getValue())[2]));
                     }

                     mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + ((int[])equipmentSlotEntry.getValue())[2], 1, SlotActionType.QUICK_MOVE, mc.player);
                     EntityUtil.syncInventory();
                  } else if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
                     int armorSlot = ((int[])equipmentSlotEntry.getValue())[0] - 34 + (39 - ((int[])equipmentSlotEntry.getValue())[0]) * 2;
                     int newArmorSlot = ((int[])equipmentSlotEntry.getValue())[2] < 9 ? 36 + ((int[])equipmentSlotEntry.getValue())[2] : ((int[])equipmentSlotEntry.getValue())[2];
                     mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
                     mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
                     if (((int[])equipmentSlotEntry.getValue())[1] != -1) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, newArmorSlot, 0, SlotActionType.PICKUP, mc.player);
                     }

                     EntityUtil.syncInventory();
                  }

               }
            }
         }
      }
   }

   private int getProtection(ItemStack is) {
      if (!(is.getItem() instanceof ArmorItem) && is.getItem() != Items.ELYTRA) {
         return !is.isEmpty() ? 0 : -1;
      } else {
         int prot = 0;
         if (is.getItem() instanceof ElytraItem) {
            if (!ElytraItem.isUsable(is)) {
               return 0;
            }

            prot = 1;
         }

         if (is.hasEnchantments()) {
            Iterator var3 = EnchantmentHelper.get(is).entrySet().iterator();

            while(var3.hasNext()) {
               Map.Entry e = (Map.Entry)var3.next();
               if (e.getKey() instanceof ProtectionEnchantment) {
                  prot += (Integer)e.getValue();
               }
            }
         }

         return (is.getItem() instanceof ArmorItem ? ((ArmorItem)is.getItem()).getProtection() : 0) + prot;
      }
   }
}

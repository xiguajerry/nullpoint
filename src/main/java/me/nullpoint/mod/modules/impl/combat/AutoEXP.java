package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

public class AutoEXP extends Module {
   public static AutoEXP INSTANCE;
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 3, 0, 5));
   public final BooleanSetting down = this.add(new BooleanSetting("Down", true));
   public final BooleanSetting onlyBroken = this.add(new BooleanSetting("OnlyBroken", true));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
   private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final Timer delayTimer = new Timer();
   private boolean throwing = false;
   int exp = 0;

   public AutoEXP() {
      super("AutoEXP", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onDisable() {
      this.throwing = false;
   }

   public void onUpdate() {
      if (!this.getBind().isPressed()) {
         this.disable();
      } else {
         this.throwing = this.checkThrow();
         if (this.isThrow() && this.delayTimer.passedMs((long)this.delay.getValueInt() * 20L) && (!this.onlyGround.getValue() || mc.player.isOnGround())) {
            this.exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE) - 1;
            this.throwExp();
         }

      }
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         this.exp = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
      }
   }

   public String getInfo() {
      return String.valueOf(this.exp);
   }

   public void throwExp() {
      int oldSlot = mc.player.getInventory().selectedSlot;
      int newSlot;
      if (this.inventory.getValue() && (newSlot = InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE)) != -1) {
         InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
         InventoryUtil.inventorySwap(newSlot, mc.player.getInventory().selectedSlot);
         EntityUtil.syncInventory();
         this.delayTimer.reset();
      } else if ((newSlot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE)) != -1) {
         InventoryUtil.switchToSlot(newSlot);
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
         InventoryUtil.switchToSlot(oldSlot);
         this.delayTimer.reset();
      }

   }

   @EventHandler(
      priority = -200
   )
   public void RotateEvent(RotateEvent event) {
      if (this.down.getValue()) {
         if (this.isThrow()) {
            event.setPitch(88.0F);
         }

      }
   }

   public boolean isThrow() {
      return this.throwing;
   }

   public boolean checkThrow() {
      if (this.isOff()) {
         return false;
      } else if (mc.currentScreen instanceof ChatScreen) {
         return false;
      } else if (mc.currentScreen != null) {
         return false;
      } else if (this.usingPause.getValue() && mc.player.isUsingItem()) {
         return false;
      } else if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1 && (!this.inventory.getValue() || InventoryUtil.findItemInventorySlot(Items.EXPERIENCE_BOTTLE) == -1)) {
         return false;
      } else if (this.onlyBroken.getValue()) {
         DefaultedList armors = mc.player.getInventory().armor;
         Iterator var2 = armors.iterator();

         ItemStack armor;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            armor = (ItemStack)var2.next();
         } while(armor.isEmpty() || EntityUtil.getDamagePercent(armor) >= 100);

         return true;
      } else {
         return true;
      }
   }
}

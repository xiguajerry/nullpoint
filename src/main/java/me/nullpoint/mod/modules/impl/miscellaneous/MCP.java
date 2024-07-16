package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

public class MCP extends Module {
   public static MCP INSTANCE;
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   boolean click = false;

   public MCP() {
      super("MCP", Module.Category.Misc);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (mc.mouse.wasMiddleButtonClicked()) {
            if (!this.click) {
               if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
                  EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
                  mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
               } else {
                  int pearl;
                  if ((pearl = this.findItem(Items.ENDER_PEARL)) != -1) {
                     int old = mc.player.getInventory().selectedSlot;
                     this.doSwap(pearl);
                     EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
                     mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
                     if (this.inventory.getValue()) {
                        this.doSwap(pearl);
                        EntityUtil.syncInventory();
                     } else {
                        this.doSwap(old);
                     }
                  }
               }

               this.click = true;
            }
         } else {
            this.click = false;
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

   public int findItem(Item item) {
      return this.inventory.getValue() ? InventoryUtil.findItemInventorySlot(item) : InventoryUtil.findItem(item);
   }
}

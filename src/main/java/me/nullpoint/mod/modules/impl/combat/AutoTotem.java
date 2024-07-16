package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
   private final BooleanSetting mainHand = this.add(new BooleanSetting("MainHand", false));
   private final SliderSetting health = this.add(new SliderSetting("Health", 16.0, 0.0, 36.0, 0.1));
   int totems = 0;
   private final Timer timer = new Timer();

   public AutoTotem() {
      super("AutoTotem", Module.Category.Combat);
      this.setDescription("Automatically replaced totems.");
   }

   public String getInfo() {
      return String.valueOf(this.totems);
   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      this.update();
   }

   public void onUpdate() {
      this.update();
   }

   private void update() {
      if (!nullCheck()) {
         this.totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
         if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ClickGuiScreen) {
            if (this.timer.passedMs(200L)) {
               if (!((double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) > this.health.getValue())) {
                  if (mc.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                     int itemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING);
                     if (itemSlot != -1) {
                        if (this.mainHand.getValue()) {
                           InventoryUtil.switchToSlot(0);
                           if (mc.player.getInventory().getStack(0).getItem() != Items.TOTEM_OF_UNDYING) {
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 0, SlotActionType.PICKUP, mc.player);
                              mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                              EntityUtil.syncInventory();
                           }
                        } else {
                           mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                           mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                           mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
                           EntityUtil.syncInventory();
                        }

                        this.timer.reset();
                     }

                  }
               }
            }
         }
      }
   }
}

package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AntiWeakness extends Module {
   private final SliderSetting delay = this.add((new SliderSetting("Delay", 100, 0, 500)).setSuffix("ms"));
   private final EnumSetting swapMode;
   private final BooleanSetting onlyCrystal;
   private final Timer delayTimer;
   private PlayerInteractEntityC2SPacket lastPacket;
   boolean ignore;

   public AntiWeakness() {
      super("AntiWeakness", "anti weak", Module.Category.Combat);
      this.swapMode = this.add(new EnumSetting("SwapMode", AntiWeakness.SwapMode.Inventory));
      this.onlyCrystal = this.add(new BooleanSetting("OnlyCrystal", true));
      this.delayTimer = new Timer();
      this.lastPacket = null;
      this.ignore = false;
   }

   public String getInfo() {
      return this.swapMode.getValue().name();
   }

   @EventHandler(
      priority = -200
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (!nullCheck()) {
         if (!event.isCancelled()) {
            if (!this.ignore) {
               if (mc.player.getStatusEffect(StatusEffects.WEAKNESS) != null) {
                  if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
                     if (this.delayTimer.passedMs(this.delay.getValue())) {
                        Packet var3 = event.getPacket();
                        if (var3 instanceof PlayerInteractEntityC2SPacket packet) {
                            if (Criticals.getInteractType(packet) == Criticals.InteractType.ATTACK) {
                              if (this.onlyCrystal.getValue() && !(Criticals.getEntity(packet) instanceof EndCrystalEntity)) {
                                 return;
                              }

                              this.lastPacket = (PlayerInteractEntityC2SPacket)event.getPacket();
                              this.delayTimer.reset();
                              this.ignore = true;
                              this.doAnti();
                              this.ignore = false;
                              event.cancel();
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private void doAnti() {
      if (this.lastPacket != null) {
         int strong;
         if (this.swapMode.getValue() != AntiWeakness.SwapMode.Inventory) {
            strong = InventoryUtil.findClass(SwordItem.class);
         } else {
            strong = InventoryUtil.findClassInventorySlot(SwordItem.class);
         }

         if (strong != -1) {
            int old = mc.player.getInventory().selectedSlot;
            if (this.swapMode.getValue() != AntiWeakness.SwapMode.Inventory) {
               InventoryUtil.switchToSlot(strong);
            } else {
               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            }

            mc.player.networkHandler.sendPacket(this.lastPacket);
            if (this.swapMode.getValue() != AntiWeakness.SwapMode.Inventory) {
               if (this.swapMode.getValue() != AntiWeakness.SwapMode.Normal) {
                  InventoryUtil.switchToSlot(old);
               }
            } else {
               mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, strong, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
               EntityUtil.syncInventory();
            }

         }
      }
   }

   public enum SwapMode {
      Normal,
      Silent,
      Inventory;

      // $FF: synthetic method
      private static SwapMode[] $values() {
         return new SwapMode[]{Normal, Silent, Inventory};
      }
   }
}

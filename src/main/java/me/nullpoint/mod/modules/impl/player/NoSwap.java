package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.asm.accessors.IUpdateSelectedSlotS2CPacket;
import me.nullpoint.mod.modules.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class NoSwap extends Module {
   public static NoSwap INSTANCE;

   public NoSwap() {
      super("NoSwap", Module.Category.Player);
      INSTANCE = this;
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive event) {
      if (!nullCheck()) {
         Packet var3 = event.getPacket();
         if (var3 instanceof UpdateSelectedSlotS2CPacket packet) {
             int slot = mc.player.getInventory().selectedSlot;
            ((IUpdateSelectedSlotS2CPacket)packet).setslot(slot);
         }

      }
   }
}

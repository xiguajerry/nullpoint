package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.asm.accessors.IPlayerPositionLookS2CPacket;
import me.nullpoint.mod.modules.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class NoRotateSet extends Module {
   public static NoRotateSet INSTANCE;

   public NoRotateSet() {
      super("NoRotateSet", Module.Category.Player);
      INSTANCE = this;
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive event) {
      if (!nullCheck()) {
         Packet var3 = event.getPacket();
         if (var3 instanceof PlayerPositionLookS2CPacket packet) {
             float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            ((IPlayerPositionLookS2CPacket)packet).setYaw(yaw);
            ((IPlayerPositionLookS2CPacket)packet).setPitch(pitch);
         }

      }
   }
}

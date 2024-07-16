package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class AntiSpam extends Module {
   private final StringSetting name = this.add(new StringSetting("Name", "zhuan_gan_"));

   public AntiSpam() {
      super("AntiSpam", Module.Category.Misc);
   }

   @EventHandler
   private void PacketReceive(PacketEvent.Receive receive) {
      Packet var3 = receive.getPacket();
      if (var3 instanceof GameMessageS2CPacket e) {
         if (e.content().getString().contains(this.name.getValue())) {
            receive.cancel();
         }
      }

   }
}

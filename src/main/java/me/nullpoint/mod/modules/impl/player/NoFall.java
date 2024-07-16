package me.nullpoint.mod.modules.impl.player;

import java.util.Iterator;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.asm.accessors.IPlayerMoveC2SPacket;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
   private final SliderSetting getDistance = this.add(new SliderSetting("Distance", 3.0, 0.0, 8.0, 0.1));

   public NoFall() {
      super("NoFall", Module.Category.Player);
      this.setDescription("Prevents fall damage.");
   }

   public String getInfo() {
      return "SpoofGround";
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (!nullCheck()) {
         Iterator var2 = mc.player.getArmorItems().iterator();

         ItemStack is;
         do {
            if (!var2.hasNext()) {
               Packet var5 = event.getPacket();
               if (var5 instanceof PlayerMoveC2SPacket packet) {
                   if (mc.player.fallDistance >= (float)this.getDistance.getValue()) {
                     ((IPlayerMoveC2SPacket)packet).setOnGround(true);
                  }
               }

               return;
            }

            is = (ItemStack)var2.next();
         } while(is.getItem() != Items.ELYTRA);

      }
   }
}

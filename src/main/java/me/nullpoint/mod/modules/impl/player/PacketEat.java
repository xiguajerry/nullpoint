package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;

public class PacketEat extends Module {
   public static PacketEat INSTANCE;
   private final BooleanSetting sync = this.add(new BooleanSetting("Sync", false));

   public PacketEat() {
      super("PacketEat", Module.Category.Player);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (this.sync.getValue() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().isFood()) {
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, BlockUtil.getWorldActionId(mc.world)));
         }

      }
   }

   @EventHandler
   public void onPacket(PacketEvent.Send event) {
      Packet var3 = event.getPacket();
      if (var3 instanceof PlayerActionC2SPacket packet) {
         if (packet.getAction() == Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem().isFood()) {
            event.cancel();
         }
      }

   }
}

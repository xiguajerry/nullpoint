package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class Ping extends Module {
   private final EnumSetting mode;
   private long sendTime;

   public Ping() {
      super("Ping", Module.Category.Misc);
      this.mode = this.add(new EnumSetting("Mode", Ping.Mode.Command));
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         this.sendTime = System.currentTimeMillis();
         if (this.mode.getValue() == Ping.Mode.Command) {
            mc.player.networkHandler.sendCommand("chat ");
         } else if (this.mode.getValue() == Ping.Mode.Request) {
            mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(1337, "tell "));
         }

      }
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive e) {
      long var10000;
      Packet var3;
      if (this.mode.getValue() == Ping.Mode.Request) {
         var3 = e.getPacket();
         if (var3 instanceof CommandSuggestionsS2CPacket c) {
             if (c.getCompletionId() == 1337) {
               var10000 = System.currentTimeMillis() - this.sendTime;
               CommandManager.sendChatMessage("ping: " + var10000 / 2L);
               this.disable();
            }
         }
      } else if (this.mode.getValue() == Ping.Mode.Command) {
         var3 = e.getPacket();
         if (var3 instanceof GameMessageS2CPacket packet) {
             if (packet.content().getString().contains("chat.use") || packet.content().getString().contains("<--[HERE]") || packet.content().getString().contains("Unknown")) {
               var10000 = System.currentTimeMillis() - this.sendTime;
               CommandManager.sendChatMessage("ping: " + var10000 / 2L);
               this.disable();
            }
         }
      }

   }

   public enum Mode {
      Request,
      Command;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Request, Command};
      }
   }
}

package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module {
   Screen lastScreen = null;

   public XCarry() {
      super("XCarry", Module.Category.Player);
   }

   public void onUpdate() {
      if (mc.currentScreen != null && !(mc.currentScreen instanceof GameMenuScreen) && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) {
         this.lastScreen = mc.currentScreen;
      }

   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (this.lastScreen instanceof InventoryScreen && event.getPacket() instanceof CloseHandledScreenC2SPacket) {
         event.cancel();
      }

   }
}

package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.DeathEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.player.PlayerEntity;

public class PopCounter extends Module {
   public static PopCounter INSTANCE;
   public final BooleanSetting unPop = this.add(new BooleanSetting("Dead", true));

   public PopCounter() {
      super("PopCounter", "Counts players totem pops", Module.Category.Misc);
      INSTANCE = this;
   }

   @EventHandler
   public void onPlayerDeath(DeathEvent event) {
      PlayerEntity player = event.getPlayer();
      if (Nullpoint.POP.popContainer.containsKey(player.getName().getString())) {
         int l_Count = Nullpoint.POP.popContainer.get(player.getName().getString());
         if (l_Count == 1) {
            if (player.equals(mc.player)) {
               this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
            } else {
               this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
            }
         } else if (player.equals(mc.player)) {
            this.sendMessage("\u00a7fYou\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
         } else {
            this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died after popping \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
         }
      } else if (this.unPop.getValue()) {
         if (player.equals(mc.player)) {
            this.sendMessage("\u00a7fYou\u00a7r died.", player.getId());
         } else {
            this.sendMessage("\u00a7f" + player.getName().getString() + "\u00a7r died.", player.getId());
         }
      }

   }

   @EventHandler
   public void onTotem(TotemEvent event) {
      PlayerEntity player = event.getPlayer();
      int l_Count = 1;
      if (Nullpoint.POP.popContainer.containsKey(player.getName().getString())) {
         l_Count = Nullpoint.POP.popContainer.get(player.getName().getString());
      }

      if (l_Count == 1) {
         if (player.equals(mc.player)) {
            this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
         } else {
            this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rpopped \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
         }
      } else if (player.equals(mc.player)) {
         this.sendMessage("\u00a7fYou\u00a7r popped \u00a7f" + l_Count + "\u00a7r totem.", player.getId());
      } else {
         this.sendMessage("\u00a7f" + player.getName().getString() + " \u00a7rhas popped \u00a7f" + l_Count + "\u00a7r totems.", player.getId());
      }

   }

   public void sendMessage(String message, int id) {
      if (!nullCheck()) {
         CommandManager.sendChatMessageWidthId("\u00a76[!] " + message, id);
      }

   }
}

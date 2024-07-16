package me.nullpoint.mod.commands.impl;

import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

public class ReloadCommand extends Command {
   public ReloadCommand() {
      super("reload", "debug", "");
   }

   public void runCommand(String[] parameters) {
      CommandManager.sendChatMessage("\u00a7e[!] \u00a7fReloading..");
      Nullpoint.CONFIG = new ConfigManager();
      Nullpoint.PREFIX = Nullpoint.CONFIG.getString("prefix", Nullpoint.PREFIX);
      Nullpoint.CONFIG.loadSettings();
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

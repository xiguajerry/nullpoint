package me.nullpoint.mod.commands.impl;

import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;

public class ReloadAllCommand extends Command {
   public ReloadAllCommand() {
      super("reloadall", "debug", "");
   }

   public void runCommand(String[] parameters) {
      CommandManager.sendChatMessage("\u00a7e[!] \u00a7fReloading..");
      Nullpoint.unload();
      Nullpoint.load();
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

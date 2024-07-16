package me.nullpoint.mod.commands.impl;

import java.io.File;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

public class LoadCommand extends Command {
   public LoadCommand() {
      super("load", "debug", "[config]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.sendUsage();
      } else {
         CommandManager.sendChatMessage("\u00a7e[!] \u00a7fLoading..");
         ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
         Nullpoint.unload();
         Nullpoint.load();
         ConfigManager.options = new File(mc.runDirectory, "nullpoint_options.txt");
         Nullpoint.save();
      }
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

package me.nullpoint.mod.commands.impl;

import java.io.File;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.mod.commands.Command;

public class SaveCommand extends Command {
   public SaveCommand() {
      super("save", "save", "");
   }

   public void runCommand(String[] parameters) {
      CommandManager.sendChatMessage("\u00a7e[!] \u00a7fSaving..");
      if (parameters.length == 1) {
         ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
         Nullpoint.save();
         ConfigManager.options = new File(mc.runDirectory, "nullpoint_options.txt");
      }

      Nullpoint.save();
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

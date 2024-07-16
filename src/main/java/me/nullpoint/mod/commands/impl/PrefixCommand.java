package me.nullpoint.mod.commands.impl;

import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", "Set prefix", "[prefix]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.sendUsage();
      } else if (parameters[0].startsWith("/")) {
         CommandManager.sendChatMessage("\u00a76[!] \u00a7fPlease specify a valid \u00a7bprefix.");
      } else {
         Nullpoint.PREFIX = parameters[0];
         CommandManager.sendChatMessage("\u00a7a[\u221a] \u00a7bPrefix \u00a7fset to \u00a7e" + parameters[0]);
      }
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

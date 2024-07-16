package me.nullpoint.mod.commands.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.Module;
import org.apache.commons.lang3.StringUtils;

public class HelpCommand extends Command {
   final int indexesPerPage = 5;

   public HelpCommand() {
      super("help", "Shows the avaiable commands.", "[page, module]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.ShowCommands(1);
      } else if (StringUtils.isNumeric(parameters[0])) {
         int page = Integer.parseInt(parameters[0]);
         this.ShowCommands(page);
      } else {
         Module module = Nullpoint.MODULE.getModuleByName(parameters[0]);
         if (module == null) {
            CommandManager.sendChatMessage("Could not find Module '" + parameters[0] + "'.");
         } else {
            CommandManager.sendChatMessage("------------ " + module.getName() + "Help ------------");
            CommandManager.sendChatMessage("Name: " + module.getName());
            CommandManager.sendChatMessage("Description: " + module.getDescription());
            String var10000 = module.getBind().getBind();
            CommandManager.sendChatMessage("Keybind: " + var10000 + " " + module.getBind().getKey());
         }
      }

   }

   private void ShowCommands(int page) {
      CommandManager.sendChatMessage("------------ Help [Page " + page + " of 5] ------------");
      CommandManager.sendChatMessage("Use " + Nullpoint.PREFIX + "help [n] to get page n of help.");
      HashMap commands = Nullpoint.COMMAND.getCommands();
      Set keySet = commands.keySet();
      ArrayList listOfCommands = new ArrayList(keySet);

      for(int i = (page - 1) * 5; i < page * 5; ++i) {
         if (i >= 0 && i < Nullpoint.COMMAND.getNumOfCommands()) {
            String var10000 = Nullpoint.PREFIX;
            CommandManager.sendChatMessage(" " + var10000 + listOfCommands.get(i));
         }
      }

   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

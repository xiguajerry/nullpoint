// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.commands.impl;

import java.util.ArrayList;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.Module;

public class Toggle2Command
        extends Command {
   public Toggle2Command() {
      super("t", "Toggle module", "[module]");
   }

   @Override
   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.sendUsage();
         return;
      }
      String moduleName = parameters[0];
      Module module = Nullpoint.MODULE.getModuleByName(moduleName);
      if (module == null) {
         CommandManager.sendChatMessage("§4[!] §fUnknown §bmodule!");
         return;
      }
      module.toggle();
   }

   @Override
   public String[] getAutocorrect(int count, List<String> seperated) {
      if (count == 1) {
         String input = seperated.get(seperated.size() - 1).toLowerCase();
         ModuleManager cm = Nullpoint.MODULE;
         ArrayList<String> correct = new ArrayList<String>();
         for (Module x : cm.modules) {
            if (!input.equalsIgnoreCase(Nullpoint.PREFIX + "toggle") && !x.getName().toLowerCase().startsWith(input)) continue;
            correct.add(x.getName());
         }
         int numCmds = correct.size();
         String[] commands = new String[numCmds];
         int i = 0;
         for (String x : correct) {
            commands[i++] = x;
         }
         return commands;
      }
      return null;
   }
}

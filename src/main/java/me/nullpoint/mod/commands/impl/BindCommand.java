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

public class BindCommand
        extends Command {
   public BindCommand() {
      super("bind", "Bind key", "[module] [key]");
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
      if (parameters.length == 1) {
         CommandManager.sendChatMessage("§6[!] §fPlease specify a §bkey.");
         return;
      }
      String rkey = parameters[1];
      if (rkey == null) {
         CommandManager.sendChatMessage("§4Unknown Error");
         return;
      }
      if (module.setBind(rkey.toUpperCase())) {
         CommandManager.sendChatMessage("§a[√] §fBind for §a" + module.getName() + "§f set to §7" + rkey.toUpperCase());
      }
   }

   @Override
   public String[] getAutocorrect(int count, List<String> seperated) {
      if (count == 1) {
         String input = seperated.get(seperated.size() - 1).toLowerCase();
         ModuleManager cm = Nullpoint.MODULE;
         ArrayList<String> correct = new ArrayList<String>();
         for (Module x : cm.modules) {
            if (!input.equalsIgnoreCase(Nullpoint.PREFIX + "bind") && !x.getName().toLowerCase().startsWith(input)) continue;
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

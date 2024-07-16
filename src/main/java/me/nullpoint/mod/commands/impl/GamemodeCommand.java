package me.nullpoint.mod.commands.impl;

import java.util.List;
import me.nullpoint.mod.commands.Command;
import net.minecraft.world.GameMode;

public class GamemodeCommand extends Command {
   public GamemodeCommand() {
      super("gamemode", "change gamemode(client side)", "[gamemode]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.sendUsage();
      } else {
         String moduleName = parameters[0];
         if (moduleName.equalsIgnoreCase("survival")) {
            mc.interactionManager.setGameMode(GameMode.SURVIVAL);
         } else if (moduleName.equalsIgnoreCase("creative")) {
            mc.interactionManager.setGameMode(GameMode.CREATIVE);
         } else if (moduleName.equalsIgnoreCase("adventure")) {
            mc.interactionManager.setGameMode(GameMode.ADVENTURE);
         } else if (moduleName.equalsIgnoreCase("spectator")) {
            mc.interactionManager.setGameMode(GameMode.SPECTATOR);
         }

      }
   }

   public String[] getAutocorrect(int count, List seperated) {
      return count == 1 ? new String[]{"survival", "creative", "adventure", "spectator"} : null;
   }
}

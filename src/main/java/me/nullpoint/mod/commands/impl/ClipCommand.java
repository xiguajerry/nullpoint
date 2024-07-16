package me.nullpoint.mod.commands.impl;

import java.text.DecimalFormat;
import java.util.List;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.commands.Command;

public class ClipCommand extends Command {
   public ClipCommand() {
      super("clip", "Teleports the player certain blocks away (Vanilla only)", "[x] [y] [z]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length != 3) {
         this.sendUsage();
      } else if (this.isNumeric(parameters[0])) {
         double x = mc.player.getX() + Double.parseDouble(parameters[0]);
         if (this.isNumeric(parameters[1])) {
            double y = mc.player.getY() + Double.parseDouble(parameters[1]);
            if (this.isNumeric(parameters[2])) {
               double z = mc.player.getZ() + Double.parseDouble(parameters[2]);
               mc.player.setPosition(x, y, z);
               DecimalFormat df = new DecimalFormat("0.0");
               String var10000 = df.format(x);
               CommandManager.sendChatMessage("\u00a7a[\u221a] \u00a7fTeleported to \u00a7eX:" + var10000 + " Y:" + df.format(y) + " Z:" + df.format(z));
            } else {
               this.sendUsage();
            }
         } else {
            this.sendUsage();
         }
      } else {
         this.sendUsage();
      }
   }

   private boolean isNumeric(String str) {
      return str.matches("-?\\d+(\\.\\d+)?");
   }

   public String[] getAutocorrect(int count, List seperated) {
      return new String[]{"0 "};
   }
}

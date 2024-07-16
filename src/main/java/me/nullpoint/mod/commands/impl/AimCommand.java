package me.nullpoint.mod.commands.impl;

import java.text.DecimalFormat;
import java.util.List;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.mod.commands.Command;
import net.minecraft.util.math.Vec3d;

public class AimCommand extends Command {
   public AimCommand() {
      super("aim", "Aim to pos", "[x] [y] [z]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length != 3) {
         this.sendUsage();
      } else {
         double x;
         if (this.isNumeric(parameters[0])) {
            x = Double.parseDouble(parameters[0]);
         } else {
            if (!parameters[0].startsWith("~")) {
               this.sendUsage();
               return;
            }

            if (this.isNumeric(parameters[0].replace("~", ""))) {
               x = mc.player.getX() + Double.parseDouble(parameters[0].replace("~", ""));
            } else {
               if (!parameters[0].replace("~", "").equals("")) {
                  this.sendUsage();
                  return;
               }

               x = mc.player.getX();
            }
         }

         double y;
         if (this.isNumeric(parameters[1])) {
            y = Double.parseDouble(parameters[1]);
         } else {
            if (!parameters[1].startsWith("~")) {
               this.sendUsage();
               return;
            }

            if (this.isNumeric(parameters[1].replace("~", ""))) {
               y = mc.player.getY() + Double.parseDouble(parameters[1].replace("~", ""));
            } else {
               if (!parameters[1].replace("~", "").equals("")) {
                  this.sendUsage();
                  return;
               }

               y = mc.player.getY();
            }
         }

         double z;
         if (this.isNumeric(parameters[2])) {
            z = Double.parseDouble(parameters[2]);
         } else {
            if (!parameters[2].startsWith("~")) {
               this.sendUsage();
               return;
            }

            if (this.isNumeric(parameters[2].replace("~", ""))) {
               z = mc.player.getZ() + Double.parseDouble(parameters[2].replace("~", ""));
            } else {
               if (!parameters[2].replace("~", "").equals("")) {
                  this.sendUsage();
                  return;
               }

               z = mc.player.getZ();
            }
         }

         float[] angle = EntityUtil.getLegitRotations(new Vec3d(x, y, z));
         mc.player.setYaw(angle[0]);
         mc.player.setPitch(angle[1]);
         DecimalFormat df = new DecimalFormat("0.0");
         String var10000 = df.format(x);
         CommandManager.sendChatMessage("\u00a7a[\u221a] \u00a7fAim to \u00a7eX:" + var10000 + " Y:" + df.format(y) + " Z:" + df.format(z));
      }
   }

   private boolean isNumeric(String str) {
      return str.matches("-?\\d+(\\.\\d+)?");
   }

   public String[] getAutocorrect(int count, List seperated) {
      return new String[]{"~ "};
   }
}

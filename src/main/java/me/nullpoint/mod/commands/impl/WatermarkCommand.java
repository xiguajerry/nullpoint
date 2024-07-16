package me.nullpoint.mod.commands.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.impl.client.HUD;

public class WatermarkCommand extends Command {
   public WatermarkCommand() {
      super("watermark", "change watermark", "[text]");
   }

   public void runCommand(String[] parameters) {
      if (parameters.length == 0) {
         this.sendUsage();
      } else {
         StringBuilder text = new StringBuilder();
         Iterator var3 = Arrays.stream(parameters).toList().iterator();

         while(var3.hasNext()) {
            String s = (String)var3.next();
            text.append(" ").append(s);
         }

         HUD.INSTANCE.watermarkString.setValue(text.toString());
      }
   }

   public String[] getAutocorrect(int count, List seperated) {
      return null;
   }
}

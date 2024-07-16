package me.nullpoint.mod.modules.impl.miscellaneous;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import me.nullpoint.Nullpoint;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

public class AutoSpam extends Module {
   private final Random r = new Random();
   private static String[] kouzi = new String[0];
   private int lastNum;
   private final StringSetting custom = this.add(new StringSetting("Name", "campaunlas"));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 1500, 0, 10000));
   private final BooleanSetting heavy = this.add(new BooleanSetting("Heavy", false));
   private final BooleanSetting tell = this.add(new BooleanSetting("Tell", true));
   me.nullpoint.api.utils.math.Timer timer = new me.nullpoint.api.utils.math.Timer();

   public AutoSpam() {
      super("AutoSpam", Module.Category.Misc);
   }

   public String getInfo() {
      return this.custom.getValue();
   }

   public void onEnable() {
      this.timer.reset();
   }

   public void onUpdate() {
      if (!nullCheck()) {
         BufferedReader buff = null;
         buff = this.heavy.getValue() ? new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi2.txt")), StandardCharsets.UTF_8)) : new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi.txt")), StandardCharsets.UTF_8));
         List dictionary = buff.lines().toList();
         kouzi = (String[])dictionary.toArray(new String[0]);
         if (this.timer.passedMs(this.delay.getValue())) {
            this.timer.reset();
            int num = this.r.nextInt(0, kouzi.length);
            if (num == this.lastNum) {
               num = num < kouzi.length - 1 ? num + 1 : 0;
            }

            this.lastNum = num;
            String var10001 = this.tell.getValue() ? this.custom.getValue() + " " : "";
            this.send(var10001 + kouzi[num] + (this.tell.getValue() ? "" : this.custom.getValue()));
         }

      }
   }

   private void send(String s) {
      if (this.tell.getValue()) {
         mc.player.networkHandler.sendChatCommand("tell " + s);
      } else {
         mc.player.networkHandler.sendChatMessage(s);
      }

   }
}

package me.nullpoint.mod.modules.impl.miscellaneous;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.DeathEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.api.managers.FriendManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

public class AutoEZ extends Module {
   private final Random r = new Random();
   private static String[] kouzi = new String[0];
   private int lastNum;
   private final SliderSetting range = this.add(new SliderSetting("Range", 10, 1, 30));
   private final BooleanSetting kill = this.add((new BooleanSetting("kill", true)).setParent());
   private final EnumSetting killMsgMode;
   private final StringSetting custom;
   private final BooleanSetting pop;
   private final EnumSetting popMsgMode;
   private final StringSetting popcustom;
   private final String[] reb;

   public AutoEZ() {
      super("AutoEZ", Module.Category.Misc);
      this.killMsgMode = this.add(new EnumSetting("msgMode", AutoEZ.MessageMode.Rebirth, (v) -> {
         return this.kill.isOpen();
      }));
      this.custom = this.add(new StringSetting("Custom", "killed %player%", (v) -> {
         return this.killMsgMode.getValue() == AutoEZ.MessageMode.Custom && this.kill.isOpen();
      }));
      this.pop = this.add((new BooleanSetting("pop", true)).setParent());
      this.popMsgMode = this.add(new EnumSetting("msgMode", AutoEZ.MessageMode.Rebirth, (v) -> {
         return this.pop.isOpen();
      }));
      this.popcustom = this.add(new StringSetting("Custom", "%player% pop %totem%", (v) -> {
         return this.popMsgMode.getValue() == AutoEZ.MessageMode.Custom && this.pop.isOpen();
      }));
      this.reb = new String[]{"%player% Killed by Rebirth v2.1.3", "Rebirth v2.1.3 Killed You %player%"};
   }

   public String getInfo() {
      return this.killMsgMode.getValue().name();
   }

   public void onEnable() {
      this.lastNum = -1;
   }

   @EventHandler
   public void onDeath(DeathEvent event) {
      if (!(event.getPlayer().distanceTo(mc.player) > (float)this.range.getValueInt()) && event.getPlayer() != mc.player) {
         FriendManager var10000 = Nullpoint.FRIEND;
         if (!FriendManager.isFriend(event.getPlayer().getName().getString())) {
            int popCount;
            if (this.kill.getValue() && this.killMsgMode.getValue() == AutoEZ.MessageMode.Rebirth) {
               popCount = this.r.nextInt(0, this.reb.length);
               if (popCount == this.lastNum) {
                  popCount = popCount < this.reb.length - 1 ? popCount + 1 : 0;
               }

               this.lastNum = popCount;
               this.send(this.reb[popCount].replaceAll("%player%", event.getPlayer().getName().getString()));
            }

            if (this.kill.getValue() && this.killMsgMode.getValue() == AutoEZ.MessageMode.NEW) {
               popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
               this.send("\u4eba\u751f\u81ea\u53e4\u8c01\u65e0\u6b7b\uff1f\u9057\u61be\u7684\uff0c%player%\u5728pop %totem% \u4e2a\u56fe\u817e\u4ee5\u540e\u5df2\u65e0\u6cd5\u4e0e\u60a8\u4e92\u52a8\uff0c\u8ba9\u6211\u4eec\u4e00\u8d77\u60bc\u5ff5\u4ed6".replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
            }

            if (this.kill.getValue() && this.killMsgMode.getValue() == AutoEZ.MessageMode.Kouzi) {
               popCount = this.r.nextInt(0, kouzi.length);
               if (popCount == this.lastNum) {
                  popCount = popCount < kouzi.length - 1 ? popCount + 1 : 0;
               }

               this.lastNum = popCount;
               this.send(kouzi[popCount].replaceAll("%player%", event.getPlayer().getName().getString()));
            }

            if (this.kill.getValue() && this.killMsgMode.getValue() == AutoEZ.MessageMode.Custom) {
               popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
               this.send(this.custom.getValue().replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
            }

         }
      }

   }

   @EventHandler
   public void onTotem(TotemEvent event) {
      if (!(event.getPlayer().distanceTo(mc.player) > (float)this.range.getValueInt()) && event.getPlayer() != mc.player) {
         FriendManager var10000 = Nullpoint.FRIEND;
         if (!FriendManager.isFriend(event.getPlayer().getName().getString())) {
            int popCount;
            if (this.pop.getValue() && this.popMsgMode.getValue() == AutoEZ.MessageMode.Rebirth) {
               popCount = this.r.nextInt(0, this.reb.length);
               if (popCount == this.lastNum) {
                  popCount = popCount < this.reb.length - 1 ? popCount + 1 : 0;
               }

               this.lastNum = popCount;
               this.send(this.reb[popCount].replaceAll("%player%", event.getPlayer().getName().getString()));
            }

            if (this.pop.getValue() && this.popMsgMode.getValue() == AutoEZ.MessageMode.NEW) {
               popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
               this.send("%player%\u7adf\u7136\u8fd8\u6d3b\u7740\uff01\u4ed6pop\u4e86%totem%\u4e2a\u56fe\u817e\uff01".replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
            }

            if (this.pop.getValue() && this.popMsgMode.getValue() == AutoEZ.MessageMode.Kouzi) {
               popCount = this.r.nextInt(0, kouzi.length);
               if (popCount == this.lastNum) {
                  popCount = popCount < kouzi.length - 1 ? popCount + 1 : 0;
               }

               this.lastNum = popCount;
               this.send(kouzi[popCount].replaceAll("%player%", event.getPlayer().getName().getString()));
            }

            if (this.pop.getValue() && this.popMsgMode.getValue() == AutoEZ.MessageMode.Custom) {
               popCount = Nullpoint.POP.popContainer.getOrDefault(event.getPlayer().getName().getString(), 0);
               this.send(this.popcustom.getValue().replaceAll("%player%", event.getPlayer().getName().getString()).replaceAll("%totem%", String.valueOf(popCount)));
            }

         }
      }

   }

   private void send(String s) {
      mc.player.networkHandler.sendChatMessage(s);
   }

   static {
      BufferedReader buff = null;
      buff = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Nullpoint.class.getClassLoader().getResourceAsStream("kouzi.txt")), StandardCharsets.UTF_8));
      List dictionary = buff.lines().toList();
      kouzi = (String[])dictionary.toArray(new String[0]);
   }

   public enum MessageMode {
      Kouzi,
      Rebirth,
      NEW,
      Custom;

      // $FF: synthetic method
      private static MessageMode[] $values() {
         return new MessageMode[]{Kouzi, Rebirth, NEW, Custom};
      }
   }
}

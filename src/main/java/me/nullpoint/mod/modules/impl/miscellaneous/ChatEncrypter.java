package me.nullpoint.mod.modules.impl.miscellaneous;

import java.util.Base64;
import java.util.Objects;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.SendMessageEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.asm.accessors.IGameMessageS2CPacket;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

public class ChatEncrypter extends Module {
   public static ChatEncrypter INSTANCE;
   private boolean elementCodec = false;
   private final StringSetting prefix = this.add(new StringSetting("Prefix", "RebirthChat"));
   private final StringSetting key = this.add(new StringSetting("KEY", "114514"));
   public final BooleanSetting encrypt = this.add((new BooleanSetting("Encrypt", true)).setParent());
   private final BindSetting openbind = this.add(new BindSetting("OpenEncryptBind", -1));
   private final BooleanSetting decrypt = this.add(new BooleanSetting("Decrypt", true));

   public ChatEncrypter() {
      super("ChatEncrypter", Module.Category.Misc);
      INSTANCE = this;
   }

   @EventHandler
   public void onSendMessage(SendMessageEvent event) {
      if (!nullCheck() && !event.isCancel()) {
         if (this.encrypt.getValue()) {
            String var10001;
            if (ChatSuffix.INSTANCE.isOn() && ChatSuffix.INSTANCE.green.getValue()) {
               var10001 = this.prefix.getValue();
               event.message = ">_" + var10001 + "_" + Base64.getEncoder().encodeToString(encrypt(event.message, this.key.getValue()).getBytes());
            } else {
               var10001 = this.prefix.getValue();
               event.message = "_" + var10001 + "_" + Base64.getEncoder().encodeToString(encrypt(event.message, this.key.getValue()).getBytes());
            }

         }
      }
   }

   public void onUpdate() {
      if (this.openbind.getKey() != -1) {
         if (this.openbind.isPressed() && !this.elementCodec) {
            this.encrypt.setValue(!this.encrypt.getValue());
            if (this.encrypt.getValue()) {
               CommandManager.sendChatMessage("\u00a7a[#]] \u00a7f\u00a7oOpen Encrypt");
            } else {
               CommandManager.sendChatMessage("\u00a7e[#] \u00a7c\u00a7oDisable Encrypt");
            }

            this.elementCodec = true;
         }

         if (!this.openbind.isPressed()) {
            this.elementCodec = false;
         }

      }
   }

   @EventHandler
   private void PacketReceive(PacketEvent.Receive receive) {
      if (!nullCheck()) {
         if (this.decrypt.getValue()) {
            Packet var3 = receive.getPacket();
            if (var3 instanceof GameMessageS2CPacket e) {
                String[] m = e.content().getString().split("_");
               if (m.length < 2) {
                  return;
               }

               if (Objects.equals(m[1], this.prefix.getValue())) {
                  ((IGameMessageS2CPacket)receive.getPacket()).setContent(Text.of("\u00a7(" + m[0] + " " + decrypt(new String(Base64.getDecoder().decode(m[2])), this.key.getValue())));
               }
            }

         }
      }
   }

   public static String encrypt(String string, String key) {
      char[] chars = key.toCharArray();
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < string.length(); ++i) {
         char c = string.charAt(i);
         char[] var6 = chars;
         int var7 = chars.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            char ckey = var6[var8];
            c ^= ckey;
         }

         builder.append(c);
      }

      return builder.toString();
   }

   public static String decrypt(String string, String key) {
      char[] chars = key.toCharArray();
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < string.length(); ++i) {
         char c = string.charAt(i);
         char[] var6 = chars;
         int var7 = chars.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            char ckey = var6[var8];
            c ^= ckey;
         }

         builder.append(c);
      }

      return builder.toString();
   }
}

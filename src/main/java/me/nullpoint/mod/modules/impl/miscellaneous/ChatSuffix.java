package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.SendMessageEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

public class ChatSuffix extends Module {
   public static ChatSuffix INSTANCE;
   private final StringSetting message = this.add(new StringSetting("append", "\ud835\udd2b\ud835\udd32\ud835\udd29\ud835\udd29\ud835\udd2d\ud835\udd2c\ud835\udd26\ud835\udd2b\ud835\udd31"));
   public final BooleanSetting green = this.add(new BooleanSetting("Green", false));
   public static final String nullPointSuffix = "\ud835\udd2b\ud835\udd32\ud835\udd29\ud835\udd29\ud835\udd2d\ud835\udd2c\ud835\udd26\ud835\udd2b\ud835\udd31";
   public static final String mioSuffix = "\u22c6 \u1d0d\u026a\u1d0f";
   public static final String jeezSuffix = " | Jeez\u029c\u1d00ck/1.4";
   public static final String scannerSuffix = " | Scanner \u029c\u1d00\u1d04\u1d0b";
   public static final String m7thh4ckSuffix = " | \ud835\udcc27\ud835\udcc9\ud835\udcbd\ud835\udcbd4\ud835\udcb8\ud835\udcc0-$";
   public static final String moonSuffix = "\u263d\ud835\udd10\ud835\udd2c\ud835\udd2c\ud835\udd2b";
   public static final String melonSuffix = "\ud835\udd10\ud835\udd22\ud835\udd29\ud835\udd2c\ud835\udd2b\ud835\udd05\ud835\udd22\ud835\udd31\ud835\udd1e";

   public ChatSuffix() {
      super("ChatSuffix", Module.Category.Misc);
      INSTANCE = this;
   }

   @EventHandler
   public void onSendMessage(SendMessageEvent event) {
      if (!nullCheck() && !event.isCancelled()) {
         String message = event.message;
         if (!message.startsWith("/") && !message.startsWith("!") && !message.endsWith(this.message.getValue())) {
            String suffix = this.message.getValue();
            message = message + " " + suffix;
            event.message = message;
         }
      }
   }

   public String getSuffix() {
      return null;
   }
}

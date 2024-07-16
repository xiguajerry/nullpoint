package me.nullpoint.mod.modules.impl.client;

import java.awt.Color;
import java.util.HashMap;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;

public class ChatSetting extends Module {
   public static ChatSetting INSTANCE;
   public final StringSetting hackName = this.add(new StringSetting("Name", "NullPoint"));
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(140, 146, 255)));
   public final ColorSetting pulse = this.add((new ColorSetting("Pulse", new Color(0, 0, 0))).injectBoolean(true));
   public final SliderSetting pulseSpeed = this.add(new SliderSetting("Speed", 1.0, 0.0, 5.0, 0.1, (v) -> {
      return this.pulse.booleanValue;
   }));
   public final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50, (v) -> {
      return this.pulse.booleanValue;
   }));
   public final SliderSetting animateTime = this.add(new SliderSetting("AnimTime", 300, 0, 1000));
   public final SliderSetting animateOffset = this.add(new SliderSetting("AnimOffset", -40, -200, 100));
   public final EnumSetting animQuad;
   public final BooleanSetting keepHistory;
   public final BooleanSetting infiniteChat;
   public final EnumSetting messageStyle;
   public final EnumSetting messageCode;
   public final StringSetting start;
   public final StringSetting end;
   public static final HashMap chatMessage = new HashMap();

   public ChatSetting() {
      super("ChatSetting", Module.Category.Client);
      this.animQuad = this.add(new EnumSetting("Quad", FadeUtils.Quad.In));
      this.keepHistory = this.add(new BooleanSetting("KeepHistory", true));
      this.infiniteChat = this.add(new BooleanSetting("InfiniteChat", true));
      this.messageStyle = this.add(new EnumSetting("MessageStyle", ChatSetting.Style.Mio));
      this.messageCode = this.add(new EnumSetting("MessageCode", ChatSetting.code.Mio));
      this.start = this.add(new StringSetting("StartCode", "[", (v) -> {
         return this.messageCode.getValue() == ChatSetting.code.Custom;
      }));
      this.end = this.add(new StringSetting("EndCode", "]", (v) -> {
         return this.messageCode.getValue() == ChatSetting.code.Custom;
      }));
      INSTANCE = this;
   }

   public void enable() {
      this.state = true;
   }

   public void disable() {
      this.state = true;
   }

   public boolean isOn() {
      return true;
   }

   public enum Style {
      Mio,
      Basic,
      Future,
      Earth,
      None;

      // $FF: synthetic method
      private static Style[] $values() {
         return new Style[]{Mio, Basic, Future, Earth, None};
      }
   }

   public enum code {
      Mio,
      Earth,
      Custom,
      None;

      // $FF: synthetic method
      private static code[] $values() {
         return new code[]{Mio, Earth, Custom, None};
      }
   }
}

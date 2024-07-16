package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {
   public static Ambience INSTANCE;
   public final ColorSetting worldColor = this.add((new ColorSetting("WorldColor", new Color(-1, true))).injectBoolean(true));
   public final BooleanSetting customTime = this.add((new BooleanSetting("CustomTime", false)).setParent());
   private final SliderSetting time = this.add(new SliderSetting("Time", 0, 0, 24000, (v) -> {
      return this.customTime.isOpen();
   }));
   public final ColorSetting fog = this.add((new ColorSetting("FogColor", new Color(13401557))).injectBoolean(false));
   public final ColorSetting sky = this.add((new ColorSetting("SkyColor", new Color(0))).injectBoolean(false));
   public final BooleanSetting fogDistance = this.add((new BooleanSetting("FogDistance", false)).setParent());
   public final SliderSetting fogStart = this.add(new SliderSetting("FogStart", 50, 0, 1000, (v) -> {
      return this.fogDistance.isOpen();
   }));
   public final SliderSetting fogEnd = this.add(new SliderSetting("FogEnd", 100, 0, 1000, (v) -> {
      return this.fogDistance.isOpen();
   }));
   long oldTime;

   public Ambience() {
      super("Ambience", "Custom ambience", Module.Category.Render);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (this.customTime.getValue()) {
         mc.world.setTimeOfDay((long)this.time.getValue());
      }

   }

   public void onEnable() {
      if (!nullCheck()) {
         this.oldTime = mc.world.getTimeOfDay();
      }
   }

   public void onDisable() {
      mc.world.setTimeOfDay(this.oldTime);
   }

   @EventHandler
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
         this.oldTime = ((WorldTimeUpdateS2CPacket)event.getPacket()).getTime();
         event.cancel();
      }

   }
}

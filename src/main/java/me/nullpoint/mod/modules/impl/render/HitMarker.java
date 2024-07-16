package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Identifier;

public class HitMarker extends Module {
   public SliderSetting time = this.add(new SliderSetting("Show Time", 3, 0, 60));
   private final Identifier marker = new Identifier("nullpoint", "hitmarker.png");
   public Timer timer = new Timer();
   public int ticks = 114514;

   public HitMarker() {
      super("HitMarker", Module.Category.Render);
   }

   public void onEnable() {
      this.ticks = 114514;
      this.timer.reset();
   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (this.timer.passedMs(0L)) {
         this.timer.reset();
         if ((float)this.ticks <= this.time.getValueFloat()) {
            ++this.ticks;
            drawContext.drawTexture(this.marker, mc.getWindow().getScaledWidth() / 2 - 8, mc.getWindow().getScaledHeight() / 2 - 8, 0, 0.0F, 0.0F, 16, 16, 16, 16);
         }
      }

   }

   @EventHandler
   public void onpacket(PacketEvent.Send event) {
      if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
         this.ticks = 0;
      }

   }
}

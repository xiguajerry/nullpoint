package me.nullpoint.mod.modules.impl.movement;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Objects;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@Beta
public class TickShift extends Module {
   private final SliderSetting multiplier = this.add(new SliderSetting("Speed", 2.0, 1.0, 10.0, 0.1));
   private final SliderSetting accumulate = this.add((new SliderSetting("Charge", 2000.0, 1.0, 10000.0, 50.0)).setSuffix("ms"));
   private final SliderSetting minAccumulate = this.add((new SliderSetting("MinCharge", 500.0, 1.0, 10000.0, 50.0)).setSuffix("ms"));
   private final BooleanSetting smooth = this.add((new BooleanSetting("Smooth", true)).setParent());
   private final EnumSetting quad;
   private final BooleanSetting reset;
   private final BooleanSetting indicator;
   private final ColorSetting work;
   private final ColorSetting charging;
   private final SliderSetting yOffset;
   public static TickShift INSTANCE;
   private final Timer timer;
   private final Timer timer2;
   static DecimalFormat df = new DecimalFormat("0.0");
   private final FadeUtils end;
   long lastMs;
   boolean moving;
   private int normalLookPos;
   private int rotationMode;
   private int normalPos;

   public TickShift() {
      super("TickShift", Module.Category.Movement);
      this.quad = this.add(new EnumSetting("Quad", FadeUtils.Quad.In, (v) -> {
         return this.smooth.isOpen();
      }));
      this.reset = this.add(new BooleanSetting("Reset", true));
      this.indicator = this.add((new BooleanSetting("Indicator", true)).setParent());
      this.work = this.add(new ColorSetting("Completed", new Color(0, 255, 0), (v) -> {
         return this.indicator.isOpen();
      }));
      this.charging = this.add(new ColorSetting("Charging", new Color(255, 0, 0), (v) -> {
         return this.indicator.isOpen();
      }));
      this.yOffset = this.add(new SliderSetting("YOffset", 0.0, -200.0, 200.0, 1.0, (v) -> {
         return this.indicator.isOpen();
      }));
      this.timer = new Timer();
      this.timer2 = new Timer();
      this.end = new FadeUtils(500L);
      this.lastMs = 0L;
      this.moving = false;
      INSTANCE = this;
   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
      this.timer.setMs(Math.min(Math.max(0L, this.timer.getPassedTimeMs()), this.accumulate.getValueInt()));
      double timer;
      if (MovementUtil.isMoving() && !EntityUtil.isInsideBlock()) {
         if (!this.moving) {
            if (this.timer.passedMs(this.minAccumulate.getValue())) {
               this.timer2.reset();
               this.lastMs = this.timer.getPassedTimeMs();
            } else {
               this.lastMs = 0L;
            }

            this.moving = true;
         }

         this.timer.reset();
         if (this.timer2.passed(this.lastMs)) {
            Nullpoint.TIMER.reset();
         } else if (this.smooth.getValue()) {
            timer = (double)Nullpoint.TIMER.getDefault() + (1.0 - this.end.getQuad((FadeUtils.Quad)this.quad.getValue())) * (double)(this.multiplier.getValueFloat() - 1.0F) * ((double)this.lastMs / this.accumulate.getValue());
            Nullpoint.TIMER.set((float)Math.max(Nullpoint.TIMER.getDefault(), timer));
         } else {
            Nullpoint.TIMER.set(this.multiplier.getValueFloat());
         }
      } else {
         if (this.moving) {
            Nullpoint.TIMER.reset();
            if (this.reset.getValue()) {
               this.timer.reset();
            } else {
               this.timer.setMs(Math.max(this.lastMs - this.timer2.getPassedTimeMs(), 0L));
            }

            this.moving = false;
         }

         this.end.setLength(this.timer.getPassedTimeMs());
         this.end.reset();
      }

      if (this.indicator.getValue()) {
         timer = (double)(this.moving ? Math.max(this.lastMs - this.timer2.getPassedTimeMs(), 0L) : this.timer.getPassedTimeMs());
         boolean completed = this.moving && timer > 0.0 || timer >= (double)this.minAccumulate.getValueInt();
         double max = this.accumulate.getValue();
         String text = df.format(timer / max * 100.0) + "%";
         TextRenderer var10001 = mc.textRenderer;
         int var10003 = mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2;
         int var10004 = mc.getWindow().getScaledHeight() / 2;
         Objects.requireNonNull(mc.textRenderer);
         drawContext.drawText(var10001, text, var10003, var10004 + 9 - this.yOffset.getValueInt(), completed ? this.work.getValue().getRGB() : this.charging.getValue().getRGB(), true);
      }

   }

   public String getInfo() {
      double current = (double)(this.moving ? Math.max(this.lastMs - this.timer2.getPassedTimeMs(), 0L) : this.timer.getPassedTimeMs());
      double max = this.accumulate.getValue();
      double value = Math.min(current / max * 100.0, 100.0);
      return df.format(value) + "%";
   }

   @EventHandler
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.lastMs = 0L;
      }

   }

   public void onDisable() {
      Nullpoint.TIMER.reset();
   }

   public void onEnable() {
      Nullpoint.TIMER.reset();
   }

   @EventHandler
   public final void onPacketSend(PacketEvent.Send event) {
      if (!nullCheck()) {
         if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround && this.rotationMode == 1) {
            ++this.normalPos;
            if (this.normalPos > 20) {
               this.rotationMode = 2;
            }
         } else if (event.getPacket() instanceof PlayerMoveC2SPacket.Full && this.rotationMode == 2) {
            ++this.normalLookPos;
            if (this.normalLookPos > 20) {
               this.rotationMode = 1;
            }
         }

      }
   }

   public static float nextFloat(float startInclusive, float endInclusive) {
      return startInclusive != endInclusive && !(endInclusive - startInclusive <= 0.0F) ? (float)((double)startInclusive + (double)(endInclusive - startInclusive) * Math.random()) : startInclusive;
   }

   @EventHandler
   public final void RotateEvent(RotateEvent event) {
      if (this.rotationMode == 2) {
         event.setRotation(event.getYaw() + nextFloat(1.0F, 3.0F), event.getPitch() + nextFloat(1.0F, 3.0F));
      }

   }
}

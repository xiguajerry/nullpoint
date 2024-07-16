package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TimerEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class FastWeb extends Module {
   public static FastWeb INSTANCE;
   public final EnumSetting mode;
   private final SliderSetting fastSpeed;
   public final SliderSetting xZSlow;
   public final SliderSetting ySlow;
   public final BooleanSetting onlySneak;
   private boolean work;

   public FastWeb() {
      super("FastWeb", "So you don't need to keep timer on keybind", Module.Category.Movement);
      this.mode = this.add(new EnumSetting("Mode", FastWeb.Mode.Vanilla));
      this.fastSpeed = this.add(new SliderSetting("Speed", 3.0, 0.0, 8.0, (v) -> {
         return this.mode.getValue() == FastWeb.Mode.Vanilla || this.mode.getValue() == FastWeb.Mode.Strict;
      }));
      this.xZSlow = this.add((new SliderSetting("XZSpeed", 25.0, 0.0, 100.0, 0.1, (v) -> {
         return this.mode.getValue() == FastWeb.Mode.Custom;
      })).setSuffix("%"));
      this.ySlow = this.add((new SliderSetting("YSpeed", 100.0, 0.0, 100.0, 0.1, (v) -> {
         return this.mode.getValue() == FastWeb.Mode.Custom;
      })).setSuffix("%"));
      this.onlySneak = this.add(new BooleanSetting("OnlySneak", true));
      this.work = false;
      INSTANCE = this;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   public boolean isWorking() {
      return this.work;
   }

   public void onUpdate() {
      this.work = !mc.player.isOnGround() && (mc.options.sneakKey.isPressed() || !this.onlySneak.getValue()) && HoleKick.isInWeb(mc.player);
      if (this.work && this.mode.getValue() == FastWeb.Mode.Vanilla) {
         MovementUtil.setMotionY(MovementUtil.getMotionY() - this.fastSpeed.getValue());
      }

   }

   @EventHandler(
      priority = -100
   )
   public void onTimer(TimerEvent event) {
      if (this.work && this.mode.getValue() == FastWeb.Mode.Strict) {
         event.set(this.fastSpeed.getValueFloat());
      }

   }

   public enum Mode {
      Vanilla,
      Strict,
      Custom,
      Ignore;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Vanilla, Strict, Custom, Ignore};
      }
   }
}

package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class SpinBot extends Module {
   private final EnumSetting pitchMode;
   private final EnumSetting yawMode;
   public final SliderSetting yawDelta;
   public final SliderSetting pitchDelta;
   public final BooleanSetting allowInteract;
   private float rotationYaw;
   private float rotationPitch;

   public SpinBot() {
      super("SpinBot", "fun", Module.Category.Player);
      this.pitchMode = this.add(new EnumSetting("PitchMode", SpinBot.Mode.None));
      this.yawMode = this.add(new EnumSetting("YawMode", SpinBot.Mode.None));
      this.yawDelta = this.add(new SliderSetting("YawDelta", 60, -360, 360));
      this.pitchDelta = this.add(new SliderSetting("PitchDelta", 10, -90, 90));
      this.allowInteract = this.add(new BooleanSetting("AllowInteract", true));
   }

   @EventHandler
   public void onPacket(PacketEvent.Send event) {
      Packet var3 = event.getPacket();
      if (var3 instanceof PlayerActionC2SPacket packet) {
         if (packet.getAction() == Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem() instanceof BowItem) {
            EntityUtil.sendYawAndPitch(mc.player.getYaw(), mc.player.getPitch());
         }
      }

   }

   @EventHandler(
      priority = 200
   )
   public void onUpdateWalkingPlayerPre(RotateEvent event) {
      if (this.pitchMode.getValue() == SpinBot.Mode.RandomAngle) {
         this.rotationPitch = MathUtil.random(90.0F, -90.0F);
      }

      if (this.yawMode.getValue() == SpinBot.Mode.RandomAngle) {
         this.rotationYaw = MathUtil.random(0.0F, 360.0F);
      }

      if (this.yawMode.getValue() == SpinBot.Mode.Spin) {
         this.rotationYaw = (float)((double)this.rotationYaw + this.yawDelta.getValue());
      }

      if (this.rotationYaw > 360.0F) {
         this.rotationYaw = 0.0F;
      }

      if (this.rotationYaw < 0.0F) {
         this.rotationYaw = 360.0F;
      }

      if (this.pitchMode.getValue() == SpinBot.Mode.Spin) {
         this.rotationPitch = (float)((double)this.rotationPitch + this.pitchDelta.getValue());
      }

      if (this.rotationPitch > 90.0F) {
         this.rotationPitch = -90.0F;
      }

      if (this.rotationPitch < -90.0F) {
         this.rotationPitch = 90.0F;
      }

      if (this.pitchMode.getValue() == SpinBot.Mode.Static) {
         this.rotationPitch = mc.player.getPitch() + this.pitchDelta.getValueFloat();
         this.rotationPitch = MathUtil.clamp(this.rotationPitch, -90.0F, 90.0F);
      }

      if (this.yawMode.getValue() == SpinBot.Mode.Static) {
         this.rotationYaw = mc.player.getYaw() % 360.0F + this.yawDelta.getValueFloat();
      }

      if (!this.allowInteract.getValue() || (!mc.options.useKey.isPressed() || EntityUtil.isUsing()) && !mc.options.attackKey.isPressed()) {
         if (this.yawMode.getValue() != SpinBot.Mode.None) {
            event.setYaw(this.rotationYaw);
         }

         if (this.pitchMode.getValue() != SpinBot.Mode.None) {
            event.setPitch(this.rotationPitch);
         }

      }
   }

   public enum Mode {
      None,
      RandomAngle,
      Spin,
      Static;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{None, RandomAngle, Spin, Static};
      }
   }
}

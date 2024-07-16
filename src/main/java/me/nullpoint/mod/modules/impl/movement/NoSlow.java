package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class NoSlow extends Module {
   public static NoSlow INSTANCE;
   private final EnumSetting mode;

   public NoSlow() {
      super("NoSlow", Module.Category.Movement);
      this.mode = this.add(new EnumSetting("Mode", NoSlow.Mode.Vanilla));
      INSTANCE = this;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   public void onUpdate() {
      if (this.mode.getValue() == NoSlow.Mode.NCP && mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
      }

   }

   public enum Mode {
      Vanilla,
      NCP;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Vanilla, NCP};
      }
   }
}

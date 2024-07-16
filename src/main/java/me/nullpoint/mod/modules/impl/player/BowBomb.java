package me.nullpoint.mod.modules.impl.player;

import java.util.Random;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;

public class BowBomb extends Module {
   private final Timer delayTimer = new Timer();
   private final BooleanSetting rotation = this.add(new BooleanSetting("Rotation", false));
   private final SliderSetting spoofs = this.add(new SliderSetting("Spoofs", 50.0, 0.0, 200.0, 1.0));
   private final EnumSetting exploit;
   private final BooleanSetting minimize;
   private final SliderSetting delay;
   private final SliderSetting activeTime;
   private final Random random;
   private final Timer activeTimer;

   public BowBomb() {
      super("BowBomb", "exploit", Module.Category.Exploit);
      this.exploit = this.add(new EnumSetting("Exploit", BowBomb.exploitEn.Strong));
      this.minimize = this.add(new BooleanSetting("Minimize", false));
      this.delay = this.add((new SliderSetting("Delay", 5.0, 0.0, 10.0)).setSuffix("s"));
      this.activeTime = this.add((new SliderSetting("ActiveTime", 0.4000000059604645, 0.0, 3.0)).setSuffix("s"));
      this.random = new Random();
      this.activeTimer = new Timer();
   }

   public void onUpdate() {
      if (!mc.player.isUsingItem() || mc.player.getActiveItem().getItem() != Items.BOW) {
         this.activeTimer.reset();
      }

   }

   @EventHandler
   protected void onPacketSend(PacketEvent.Send event) {
      if (!nullCheck() && this.delayTimer.passedMs((long)(this.delay.getValue() * 1000.0)) && this.activeTimer.passedMs((long)(this.activeTime.getValue() * 1000.0))) {
         Packet var3 = event.getPacket();
         if (var3 instanceof PlayerActionC2SPacket packet) {
             if (packet.getAction() == Action.RELEASE_USE_ITEM) {
               mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
               int i;
               if (this.exploit.getValue() == BowBomb.exploitEn.Fast) {
                  for(i = 0; i < this.getRuns(); ++i) {
                     this.spoof(mc.player.getX(), this.minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1.0E-10, mc.player.getZ(), true);
                     this.spoof(mc.player.getX(), mc.player.getY() + 1.0E-10, mc.player.getZ(), false);
                  }
               }

               if (this.exploit.getValue() == BowBomb.exploitEn.Strong) {
                  for(i = 0; i < this.getRuns(); ++i) {
                     this.spoof(mc.player.getX(), mc.player.getY() + 1.0E-10, mc.player.getZ(), false);
                     this.spoof(mc.player.getX(), this.minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1.0E-10, mc.player.getZ(), true);
                  }
               }

               if (this.exploit.getValue() == BowBomb.exploitEn.Phobos) {
                  for(i = 0; i < this.getRuns(); ++i) {
                     this.spoof(mc.player.getX(), mc.player.getY() + 1.3E-13, mc.player.getZ(), true);
                     this.spoof(mc.player.getX(), mc.player.getY() + 2.7E-13, mc.player.getZ(), false);
                  }
               }

               if (this.exploit.getValue() == BowBomb.exploitEn.Strict) {
                  double[] strict_direction = new double[]{100.0 * -Math.sin(Math.toRadians(mc.player.getYaw())), 100.0 * Math.cos(Math.toRadians(mc.player.getYaw()))};

                  for(i = 0; i < this.getRuns(); ++i) {
                     if (this.random.nextBoolean()) {
                        this.spoof(mc.player.getX() - strict_direction[0], mc.player.getY(), mc.player.getZ() - strict_direction[1], false);
                     } else {
                        this.spoof(mc.player.getX() + strict_direction[0], mc.player.getY(), mc.player.getZ() + strict_direction[1], true);
                     }
                  }
               }

               this.delayTimer.reset();
            }
         }

      }
   }

   private void spoof(double x, double y, double z, boolean ground) {
      if (this.rotation.getValue()) {
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
      } else {
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
      }

   }

   private int getRuns() {
      return this.spoofs.getValueInt();
   }

   private enum exploitEn {
      Strong,
      Fast,
      Strict,
      Phobos;

      // $FF: synthetic method
      private static exploitEn[] $values() {
         return new exploitEn[]{Strong, Fast, Strict, Phobos};
      }
   }
}

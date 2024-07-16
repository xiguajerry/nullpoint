package me.nullpoint.mod.modules.impl.combat;

import io.netty.buffer.Unpooled;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
   public static Criticals INSTANCE;
   public final EnumSetting mode;

   public Criticals() {
      super("Criticals", Module.Category.Combat);
      this.mode = this.add(new EnumSetting("Mode", Criticals.Mode.Packet));
      INSTANCE = this;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (!Aura.INSTANCE.sweeping && !TPAura.attacking) {
         Packet var4 = event.getPacket();
         if (var4 instanceof PlayerInteractEntityC2SPacket packet) {
             Entity entity;
            if (getInteractType(packet) == Criticals.InteractType.ATTACK && !((entity = getEntity(packet)) instanceof EndCrystalEntity)) {
               mc.player.addCritParticles(entity);
               this.doCrit();
            }
         }
      }

   }

   public void doCrit() {
      if ((mc.player.isOnGround() || mc.player.getAbilities().flying) && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
         if (this.mode.getValue() == Criticals.Mode.Strict && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() != Blocks.COBWEB) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.062600301692775, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.07260029960661, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         } else if (this.mode.getValue() == Criticals.Mode.NCP) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         } else if (this.mode.getValue() == Criticals.Mode.Packet) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.058293536E-5, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 9.16580235E-6, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.0371854E-7, mc.player.getZ(), false));
         } else if (this.mode.getValue() == Criticals.Mode.LowPacket) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.71875E-7, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
         }
      }

   }

   public static Entity getEntity(PlayerInteractEntityC2SPacket packet) {
      PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      packet.write(packetBuf);
      return mc.world == null ? null : mc.world.getEntityById(packetBuf.readVarInt());
   }

   public static InteractType getInteractType(PlayerInteractEntityC2SPacket packet) {
      PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
      packet.write(packetBuf);
      packetBuf.readVarInt();
      return packetBuf.readEnumConstant(InteractType.class);
   }

   private enum Mode {
      NCP,
      Strict,
      Packet,
      LowPacket;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{NCP, Strict, Packet, LowPacket};
      }
   }

   public enum InteractType {
      INTERACT,
      ATTACK,
      INTERACT_AT;

      // $FF: synthetic method
      private static InteractType[] $values() {
         return new InteractType[]{INTERACT, ATTACK, INTERACT_AT};
      }
   }
}

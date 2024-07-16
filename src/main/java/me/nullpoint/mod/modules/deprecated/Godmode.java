package me.nullpoint.mod.modules.deprecated;

import me.nullpoint.api.events.Event;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

/** @deprecated */
@Deprecated
public class Godmode extends Module {
   public Entity entity;

   public Godmode() {
      super("Godmode", Module.Category.Exploit);
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         if (mc.world != null && mc.player.getVehicle() != null) {
            this.entity = mc.player.getVehicle();
            mc.worldRenderer.reload();
            mc.player.dismountVehicle();
            mc.world.removeEntity(this.entity.getId(), RemovalReason.KILLED);
            mc.player.setPosition(mc.player.getPos().getX(), mc.player.getPos().getY() - 1.0, mc.player.getPos().getZ());
         }

      }
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround || event.getPacket() instanceof PlayerMoveC2SPacket.Full) {
         event.cancel();
      }

   }

   @EventHandler
   public void onPlayerWalkingUpdate(UpdateWalkingEvent event) {
      if (event.getStage() == Event.Stage.Pre) {
         if (this.entity == null) {
            return;
         }

         this.entity.copyPositionAndRotation(mc.player);
         this.entity.setYaw(mc.player.getYaw());
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), true));
         mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(MovementUtil.getMoveForward(), MovementUtil.getMoveStrafe(), false, false));
         mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(this.entity));
      }

   }
}

package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.asm.accessors.IEntityVelocityUpdateS2CPacket;
import me.nullpoint.asm.accessors.IExplosionS2CPacket;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
   public static Velocity INSTANCE;
   private final SliderSetting horizontal = this.add(new SliderSetting("Horizontal", 0.0, 0.0, 100.0, 1.0));
   private final SliderSetting vertical = this.add(new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0));
   public final BooleanSetting waterPush = this.add(new BooleanSetting("WaterPush", true));
   public final BooleanSetting entityPush = this.add(new BooleanSetting("EntityPush", true));
   public final BooleanSetting blockPush = this.add(new BooleanSetting("BlockPush", true));
   public final BooleanSetting noExplosions = this.add(new BooleanSetting("NoExplosions", true));

   public Velocity() {
      super("Velocity", Module.Category.Movement);
      this.setDescription("Prevents knockback.");
      INSTANCE = this;
   }

   public String getInfo() {
      int var10000 = this.horizontal.getValueInt();
      return "H" + var10000 + ".0%,V" + this.vertical.getValueInt() + ".0%";
   }

   @EventHandler
   public void onReceivePacket(PacketEvent.Receive event) {
      if (!nullCheck()) {
         float h = this.horizontal.getValueFloat() / 100.0F;
         float v = this.vertical.getValueFloat() / 100.0F;
         Packet var6 = event.getPacket();
         if (var6 instanceof EntityStatusS2CPacket packet) {
             if ((packet = (EntityStatusS2CPacket)event.getPacket()).getStatus() == 31) {
               Entity var10 = packet.getEntity(mc.world);
               if (var10 instanceof FishingBobberEntity fishHook) {
                   if (fishHook.getHookedEntity() == mc.player) {
                     event.setCancelled(true);
                  }
               }
            }
         }

         if (event.getPacket() instanceof ExplosionS2CPacket) {
            IExplosionS2CPacket packet = (IExplosionS2CPacket)event.getPacket();
            packet.setX(packet.getX() * h);
            packet.setY(packet.getY() * v);
            packet.setZ(packet.getZ() * h);
            if (this.noExplosions.getValue()) {
               event.cancel();
            }

         } else {
            Packet var9 = event.getPacket();
            if (var9 instanceof EntityVelocityUpdateS2CPacket packet) {
                if (packet.getId() == mc.player.getId()) {
                  if (this.horizontal.getValue() == 0.0 && this.vertical.getValue() == 0.0) {
                     event.cancel();
                  } else {
                     ((IEntityVelocityUpdateS2CPacket)packet).setX((int)((float)packet.getVelocityX() * h));
                     ((IEntityVelocityUpdateS2CPacket)packet).setY((int)((float)packet.getVelocityY() * v));
                     ((IEntityVelocityUpdateS2CPacket)packet).setZ((int)((float)packet.getVelocityZ() * h));
                  }
               }
            }

         }
      }
   }
}

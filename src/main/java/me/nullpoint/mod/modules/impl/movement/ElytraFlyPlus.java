package me.nullpoint.mod.modules.impl.movement;

import java.util.Iterator;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.Vec3d;

public class ElytraFlyPlus extends Module {
   private final Vec3d vec3d = new Vec3d(0.0, 0.0, 0.0);
   public static ElytraFlyPlus INSTANCE;
   public boolean hasElytra = false;
   public SliderSetting horizontalSpeed = this.add(new SliderSetting("HorizontalSpeed", 1.3, 0.1, 40.0));
   public SliderSetting verticalSpeed = this.add(new SliderSetting("UpSpeed", 1.3, 0.1, 40.0));

   public ElytraFlyPlus() {
      super("ElytraFlyPlus", Module.Category.Movement);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         for(Iterator var1 = mc.player.getArmorItems().iterator(); var1.hasNext(); this.hasElytra = false) {
            ItemStack is = (ItemStack)var1.next();
            if (is.getItem() instanceof ElytraItem) {
               this.hasElytra = true;
               if (mc.options.forwardKey.isPressed()) {
                  this.vec3d.add(0.0, 0.0, this.horizontalSpeed.getValue());
                  this.vec3d.rotateY(-((float)Math.toRadians(mc.player.getYaw())));
               } else if (mc.options.backKey.isPressed()) {
                  this.vec3d.add(0.0, 0.0, this.horizontalSpeed.getValue());
                  this.vec3d.rotateY((float)Math.toRadians(mc.player.getYaw()));
               }

               if (mc.options.jumpKey.isPressed()) {
                  this.vec3d.add(0.0, this.verticalSpeed.getValue(), 0.0);
               } else if (!mc.options.jumpKey.isPressed()) {
                  this.vec3d.add(0.0, -this.verticalSpeed.getValue(), 0.0);
               }

               mc.player.setVelocity(this.vec3d);
               mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
               mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
               break;
            }
         }

      }
   }

   public void onDisable() {
      mc.player.getAbilities().flying = false;
      mc.player.getAbilities().allowFlying = false;
   }

   @EventHandler
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof PlayerMoveC2SPacket) {
         mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
      }

   }

   @EventHandler
   public void onMove(TravelEvent event) {
      if (!nullCheck()) {
         mc.player.getAbilities().flying = true;
         mc.player.getAbilities().setFlySpeed(this.horizontalSpeed.getValueFloat() / 20.0F);
      }
   }
}

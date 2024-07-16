package me.nullpoint.mod.modules.impl.movement;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class Speed extends Module {
   private final BooleanSetting jump = this.add(new BooleanSetting("Jump", true));
   private final BooleanSetting inWater = this.add(new BooleanSetting("InWater", false));
   private final BooleanSetting inBlock = this.add(new BooleanSetting("InBlock", false));
   private final SliderSetting strafeSpeed = this.add(new SliderSetting("Speed", 287.3, 100.0, 1000.0, 0.1));
   private final BooleanSetting explosions = this.add(new BooleanSetting("Explosions", false));
   private final BooleanSetting velocity = this.add(new BooleanSetting("Velocity", true));
   private final SliderSetting multiplier = this.add(new SliderSetting("H-Factor", 1.0, 0.0, 5.0, 0.01));
   private final SliderSetting vertical = this.add(new SliderSetting("V-Factor", 1.0, 0.0, 5.0, 0.01));
   private final SliderSetting coolDown = this.add(new SliderSetting("CoolDown", 1000.0, 0.0, 5000.0, 1.0));
   private final SliderSetting lagTime = this.add(new SliderSetting("LagTime", 500.0, 0.0, 1000.0, 1.0));
   private final BooleanSetting slow = this.add(new BooleanSetting("Slowness", false));
   private final Timer expTimer = new Timer();
   private final Timer lagTimer = new Timer();
   private boolean stop;
   private double speed;
   private double getDistance;
   private int stage;
   private double lastExp;
   private boolean boost;
   public static Speed INSTANCE;

   public Speed() {
      super("Speed", Module.Category.Movement);
      INSTANCE = this;
   }

   public String getInfo() {
      return "Strafe";
   }

   public void onEnable() {
      if (mc.player != null) {
         this.speed = MovementUtil.getSpeed(false);
         this.getDistance = MovementUtil.getDistance2D();
      }

      this.stage = 4;
   }

   @EventHandler(
      priority = 100
   )
   public void invoke(PacketEvent.Receive event) {
      Packet var4 = event.getPacket();
      double speed;
      if (var4 instanceof EntityVelocityUpdateS2CPacket packet) {
         if (mc.player != null && packet.getId() == mc.player.getId() && this.velocity.getValue()) {
            speed = Math.sqrt(packet.getVelocityX() * packet.getVelocityX() + packet.getVelocityZ() * packet.getVelocityZ()) / 8000.0;
            this.lastExp = this.expTimer.passedMs(this.coolDown.getValueInt()) ? speed : speed - this.lastExp;
            if (this.lastExp > 0.0) {
               this.expTimer.reset();
               mc.executeTask(() -> {
                  this.speed += this.lastExp * this.multiplier.getValue();
                  this.getDistance += this.lastExp * this.multiplier.getValue();
                  if (MovementUtil.getMotionY() > 0.0 && this.vertical.getValue() != 0.0) {
                     MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                  }

               });
            }
         }
      } else if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.lagTimer.reset();
         if (mc.player != null) {
            this.getDistance = 0.0;
         }

         this.speed = 0.0;
         this.stage = 4;
      } else {
         var4 = event.getPacket();
         if (var4 instanceof ExplosionS2CPacket packet) {
             if (this.explosions.getValue() && MovementUtil.isMoving() && mc.player.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 200.0) {
               speed = Math.sqrt(Math.abs(packet.getPlayerVelocityX() * packet.getPlayerVelocityX()) + Math.abs(packet.getPlayerVelocityZ() * packet.getPlayerVelocityZ()));
               this.lastExp = this.expTimer.passedMs(this.coolDown.getValueInt()) ? speed : speed - this.lastExp;
               if (this.lastExp > 0.0) {
                  this.expTimer.reset();
                  this.speed += this.lastExp * this.multiplier.getValue();
                  this.getDistance += this.lastExp * this.multiplier.getValue();
                  if (MovementUtil.getMotionY() > 0.0) {
                     MovementUtil.setMotionY(MovementUtil.getMotionY() * this.vertical.getValue());
                  }
               }
            }
         }
      }

   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (!MovementUtil.isMoving()) {
         MovementUtil.setMotionX(0.0);
         MovementUtil.setMotionZ(0.0);
      }

      this.getDistance = MovementUtil.getDistance2D();
   }

   @EventHandler
   public void invoke(MoveEvent event) {
      if (!this.inWater.getValue() && (mc.player.isSubmergedInWater() || mc.player.isTouchingWater()) || mc.player.isHoldingOntoLadder() || !this.inBlock.getValue() && EntityUtil.isInsideBlock()) {
         this.stop = true;
      } else if (this.stop) {
         this.stop = false;
      } else if (MovementUtil.isMoving() && !HoleSnap.INSTANCE.isOn()) {
         if (!mc.player.isFallFlying()) {
            if (this.lagTimer.passedMs(this.lagTime.getValueInt())) {
               double n;
               if (this.stage == 1 && MovementUtil.isMoving()) {
                  this.speed = 1.35 * MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0) - 0.01;
               } else if (this.stage != 2 || !mc.player.isOnGround() || !MovementUtil.isMoving() || !mc.options.jumpKey.isPressed() && !this.jump.getValue()) {
                  if (this.stage == 3) {
                     this.speed = this.getDistance - 0.66 * (this.getDistance - MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
                     this.boost = !this.boost;
                  } else {
                     if ((mc.world.canCollide(null, mc.player.getBoundingBox().offset(0.0, MovementUtil.getMotionY(), 0.0)) || mc.player.collidedSoftly) && this.stage > 0) {
                        this.stage = MovementUtil.isMoving() ? 1 : 0;
                     }

                     this.speed = this.getDistance - this.getDistance / 159.0;
                  }
               } else {
                  n = 0.3999 + MovementUtil.getJumpSpeed();
                  MovementUtil.setMotionY(n);
                  event.setY(n);
                  this.speed *= this.boost ? 1.6835 : 1.395;
               }

               this.speed = Math.min(this.speed, 10.0);
               this.speed = Math.max(this.speed, MovementUtil.getSpeed(this.slow.getValue(), this.strafeSpeed.getValue() / 1000.0));
               n = MovementUtil.getMoveForward();
               double n2 = MovementUtil.getMoveStrafe();
               double n3 = mc.player.getYaw();
               if (n == 0.0 && n2 == 0.0) {
                  event.setX(0.0);
                  event.setZ(0.0);
               } else if (n != 0.0 && n2 != 0.0) {
                  n *= Math.sin(0.7853981633974483);
                  n2 *= Math.cos(0.7853981633974483);
               }

               event.setX((n * this.speed * -Math.sin(Math.toRadians(n3)) + n2 * this.speed * Math.cos(Math.toRadians(n3))) * 0.99);
               event.setZ((n * this.speed * Math.cos(Math.toRadians(n3)) - n2 * this.speed * -Math.sin(Math.toRadians(n3))) * 0.99);
               if (MovementUtil.isMoving()) {
                  ++this.stage;
               }

            }
         }
      }
   }
}

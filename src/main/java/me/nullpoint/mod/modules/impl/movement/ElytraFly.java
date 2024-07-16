package me.nullpoint.mod.modules.impl.movement;

import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {
   public static ElytraFly INSTANCE;
   private final BooleanSetting instantFly = this.add(new BooleanSetting("InstantFly", true));
   public final SliderSetting upPitch = this.add(new SliderSetting("UpPitch", 0.0, 0.0, 90.0));
   public final SliderSetting upFactor = this.add(new SliderSetting("UpFactor", 1.0, 0.0, 10.0));
   public final SliderSetting downFactor = this.add(new SliderSetting("DownFactor", 1.0, 0.0, 10.0));
   public final SliderSetting speed = this.add(new SliderSetting("Speed", 1.0, 0.10000000149011612, 10.0));
   private final SliderSetting sneakDownSpeed = this.add(new SliderSetting("DownSpeed", 1.0, 0.10000000149011612, 10.0));
   private final BooleanSetting boostTimer = this.add(new BooleanSetting("Timer", true));
   public final BooleanSetting speedLimit = this.add(new BooleanSetting("SpeedLimit", true));
   public final SliderSetting maxSpeed = this.add(new SliderSetting("MaxSpeed", 2.5, 0.10000000149011612, 10.0, (v) -> {
      return this.speedLimit.getValue();
   }));
   public final BooleanSetting noDrag = this.add(new BooleanSetting("NoDrag", false));
   private final SliderSetting timeout = this.add(new SliderSetting("Timeout", 0.5, 0.10000000149011612, 1.0));
   private boolean hasElytra = false;
   private final Timer instantFlyTimer = new Timer();
   private final Timer strictTimer = new Timer();
   private boolean hasTouchedGround = false;

   public ElytraFly() {
      super("ElytraFly", Module.Category.Movement);
      INSTANCE = this;
   }

   public String getInfo() {
      return "Control";
   }

   public void onEnable() {
      if (mc.player != null) {
         if (!mc.player.isCreative()) {
            mc.player.getAbilities().allowFlying = false;
         }

         mc.player.getAbilities().flying = false;
      }

      this.hasElytra = false;
   }

   public void onDisable() {
      Nullpoint.TIMER.reset();
      this.hasElytra = false;
      if (mc.player != null) {
         if (!mc.player.isCreative()) {
            mc.player.getAbilities().allowFlying = false;
         }

         mc.player.getAbilities().flying = false;
      }

   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (mc.player.isOnGround()) {
            this.hasTouchedGround = true;
         }

         for(Iterator var1 = mc.player.getArmorItems().iterator(); var1.hasNext(); this.hasElytra = false) {
            ItemStack is = (ItemStack)var1.next();
            if (is.getItem() instanceof ElytraItem) {
               this.hasElytra = true;
               break;
            }
         }

         if (this.strictTimer.passedMs(1500L) && !this.strictTimer.passedMs(2000L) || EntityUtil.isElytraFlying() && (double)Nullpoint.TIMER.get() == 0.3) {
            Nullpoint.TIMER.reset();
         }

         if (!mc.player.isFallFlying()) {
            if (this.hasTouchedGround && this.boostTimer.getValue() && !mc.player.isOnGround()) {
               Nullpoint.TIMER.set(0.3F);
            }

            if (!mc.player.isOnGround() && this.instantFly.getValue() && mc.player.getVelocity().getY() < 0.0) {
               if (!this.instantFlyTimer.passedMs((long)(1000.0 * this.timeout.getValue()))) {
                  return;
               }

               this.instantFlyTimer.reset();
               mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
               this.hasTouchedGround = false;
               this.strictTimer.reset();
            }
         }

      }
   }

   protected final Vec3d getRotationVector(float pitch, float yaw) {
      float f = pitch * 0.017453292F;
      float g = -yaw * 0.017453292F;
      float h = MathHelper.cos(g);
      float i = MathHelper.sin(g);
      float j = MathHelper.cos(f);
      float k = MathHelper.sin(f);
      return new Vec3d(i * j, -k, h * j);
   }

   public final Vec3d getRotationVec(float tickDelta) {
      return this.getRotationVector(-this.upPitch.getValueFloat(), mc.player.getYaw(tickDelta));
   }

   @EventHandler
   public void onMove(TravelEvent event) {
      if (!nullCheck() && this.hasElytra && mc.player.isFallFlying()) {
         Vec3d lookVec = this.getRotationVec(mc.getTickDelta());
         double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
         double motionDist = Math.sqrt(this.getX() * this.getX() + this.getZ() * this.getZ());
         if (mc.options.sneakKey.isPressed()) {
            this.setY(-this.sneakDownSpeed.getValue());
         } else if (!mc.player.input.jumping) {
            this.setY(-3.0E-14 * this.downFactor.getValue());
         }

         double finalDist;
         double[] dir;
         if (mc.player.input.jumping) {
            if (motionDist > this.upFactor.getValue() / this.upFactor.getMaximum()) {
               finalDist = motionDist * 0.01325;
               this.setY(this.getY() + finalDist * 3.2);
               this.setX(this.getX() - lookVec.x * finalDist / lookDist);
               this.setZ(this.getZ() - lookVec.z * finalDist / lookDist);
            } else {
               dir = MovementUtil.directionSpeed(this.speed.getValue());
               this.setX(dir[0]);
               this.setZ(dir[1]);
            }
         }

         if (lookDist > 0.0) {
            this.setX(this.getX() + (lookVec.x / lookDist * motionDist - this.getX()) * 0.1);
            this.setZ(this.getZ() + (lookVec.z / lookDist * motionDist - this.getZ()) * 0.1);
         }

         if (!mc.player.input.jumping) {
            dir = MovementUtil.directionSpeed(this.speed.getValue());
            this.setX(dir[0]);
            this.setZ(dir[1]);
         }

         if (!this.noDrag.getValue()) {
            this.setY(this.getY() * 0.9900000095367432);
            this.setX(this.getX() * 0.9800000190734863);
            this.setZ(this.getZ() * 0.9900000095367432);
         }

         finalDist = Math.sqrt(this.getX() * this.getX() + this.getZ() * this.getZ());
         if (this.speedLimit.getValue() && finalDist > this.maxSpeed.getValue()) {
            this.setX(this.getX() * this.maxSpeed.getValue() / finalDist);
            this.setZ(this.getZ() * this.maxSpeed.getValue() / finalDist);
         }

         event.cancel();
         mc.player.move(MovementType.SELF, mc.player.getVelocity());
      }
   }

   private void setX(double f) {
      MovementUtil.setMotionX(f);
   }

   private void setY(double f) {
      MovementUtil.setMotionY(f);
   }

   private void setZ(double f) {
      MovementUtil.setMotionZ(f);
   }

   private double getX() {
      return MovementUtil.getMotionX();
   }

   private double getY() {
      return MovementUtil.getMotionY();
   }

   private double getZ() {
      return MovementUtil.getMotionZ();
   }
}

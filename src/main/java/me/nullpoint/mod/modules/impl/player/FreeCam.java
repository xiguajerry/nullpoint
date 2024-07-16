package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.KeyboardInputEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;

public class FreeCam extends Module {
   public static FreeCam INSTANCE;
   private final SliderSetting speed = this.add(new SliderSetting("HSpeed", 1.0, 0.0, 3.0));
   private final SliderSetting hspeed = this.add(new SliderSetting("VSpeed", 0.42, 0.0, 3.0));
   final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private float fakeYaw;
   private float fakePitch;
   private float prevFakeYaw;
   private float prevFakePitch;
   private double fakeX;
   private double fakeY;
   private double fakeZ;
   private double prevFakeX;
   private double prevFakeY;
   private double prevFakeZ;
   private float preYaw;
   private float prePitch;

   public FreeCam() {
      super("FreeCam", Module.Category.Player);
      INSTANCE = this;
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         mc.chunkCullingEnabled = false;
         this.preYaw = mc.player.getYaw();
         this.prePitch = mc.player.getPitch();
         this.fakePitch = mc.player.getPitch();
         this.fakeYaw = mc.player.getYaw();
         this.prevFakePitch = this.fakePitch;
         this.prevFakeYaw = this.fakeYaw;
         this.fakeX = mc.player.getX();
         this.fakeY = mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose());
         this.fakeZ = mc.player.getZ();
         this.prevFakeX = this.fakeX;
         this.prevFakeY = this.fakeY;
         this.prevFakeZ = this.fakeZ;
      }
   }

   public void onDisable() {
      mc.chunkCullingEnabled = true;
   }

   public void onUpdate() {
      if (this.rotate.getValue() && mc.crosshairTarget != null && mc.crosshairTarget.getPos() != null) {
         float[] angle = EntityUtil.getLegitRotations(mc.crosshairTarget.getPos());
         this.preYaw = angle[0];
         this.prePitch = angle[1];
      }

   }

   @EventHandler(
      priority = 200
   )
   public void onRotate(RotateEvent event) {
      event.setYawNoModify(this.preYaw);
      event.setPitchNoModify(this.prePitch);
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      this.prevFakeYaw = this.fakeYaw;
      this.prevFakePitch = this.fakePitch;
      this.fakeYaw = mc.player.getYaw();
      this.fakePitch = mc.player.getPitch();
   }

   @EventHandler
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (mc.player != null) {
         double[] motion = MovementUtil.directionSpeed(this.speed.getValue());
         this.prevFakeX = this.fakeX;
         this.prevFakeY = this.fakeY;
         this.prevFakeZ = this.fakeZ;
         this.fakeX += motion[0];
         this.fakeZ += motion[1];
         if (mc.options.jumpKey.isPressed()) {
            this.fakeY += this.hspeed.getValue();
         }

         if (mc.options.sneakKey.isPressed()) {
            this.fakeY -= this.hspeed.getValue();
         }

         mc.player.input.movementForward = 0.0F;
         mc.player.input.movementSideways = 0.0F;
         mc.player.input.jumping = false;
         mc.player.input.sneaking = false;
      }
   }

   public float getFakeYaw() {
      return (float)MathUtil.interpolate(this.prevFakeYaw, this.fakeYaw, mc.getTickDelta());
   }

   public float getFakePitch() {
      return (float)MathUtil.interpolate(this.prevFakePitch, this.fakePitch, mc.getTickDelta());
   }

   public double getFakeX() {
      return MathUtil.interpolate(this.prevFakeX, this.fakeX, mc.getTickDelta());
   }

   public double getFakeY() {
      return MathUtil.interpolate(this.prevFakeY, this.fakeY, mc.getTickDelta());
   }

   public double getFakeZ() {
      return MathUtil.interpolate(this.prevFakeZ, this.fakeZ, mc.getTickDelta());
   }
}

package me.nullpoint.mod.modules.impl.movement;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TimerEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class HoleSnap extends Module {
   public static HoleSnap INSTANCE;
   public final BooleanSetting any = this.add(new BooleanSetting("AnyHole", true));
   private final SliderSetting range = this.add(new SliderSetting("Range", 5, 1, 50));
   private final SliderSetting timeoutTicks = this.add(new SliderSetting("TimeOut", 40, 0, 100));
   public final SliderSetting timer = this.add(new SliderSetting("Timer", 1.0, 0.1, 8.0, 0.1));
   public final BooleanSetting render = this.add(new BooleanSetting("Render", true)).setParent();
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100), (v) -> {
      return this.render.isOpen();
   }));
   public final SliderSetting circleSize = this.add(new SliderSetting("CircleSize", 1.0, 0.10000000149011612, 2.5, (v) -> {
      return this.render.isOpen();
   }));
   public final BooleanSetting fade = this.add(new BooleanSetting("Fade", true, (v) -> {
      return this.render.isOpen();
   }));
   public final SliderSetting segments = this.add(new SliderSetting("Segments", 180, 0, 360, (v) -> {
      return this.render.isOpen();
   }));
   boolean resetMove = false;
   private BlockPos holePos;
   private int stuckTicks;
   private int enabledTicks;
   Vec3d targetPos;

   public HoleSnap() {
      super("HoleSnap", "HoleSnap", Module.Category.Movement);
      INSTANCE = this;
   }

   @EventHandler(
      priority = -99
   )
   public void onTimer(TimerEvent event) {
      event.set(this.timer.getValueFloat());
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         this.resetMove = false;
         this.holePos = CombatUtil.getHole((float)this.range.getValue(), true, this.any.getValue());
      }
   }

   public void onDisable() {
      this.holePos = null;
      this.stuckTicks = 0;
      this.enabledTicks = 0;
      if (!nullCheck()) {
         if (this.resetMove) {
            MovementUtil.setMotionX(0.0);
            MovementUtil.setMotionZ(0.0);
         }

      }
   }

   @EventHandler
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.disable();
      }

   }

   public void onUpdate() {
      if (this.holePos != null) {
         if (!BlockUtil.isHole(this.holePos) && !CombatUtil.isDoubleHole(this.holePos)) {
            this.holePos = CombatUtil.getHole((float)this.range.getValue(), true, this.any.getValue());
         }

      }
   }

   @EventHandler
   public void onMove(MoveEvent event) {
      ++this.enabledTicks;
      if ((double)this.enabledTicks > this.timeoutTicks.getValue() - 1.0) {
         this.disable();
      } else if (mc.player.isAlive() && !mc.player.isFallFlying()) {
         if (this.stuckTicks > 8) {
            this.disable();
         } else if (this.holePos == null) {
            CommandManager.sendChatMessageWidthId("\u00a7e[!] \u00a7fHoles?", this.hashCode());
            this.disable();
         } else {
            Vec3d playerPos = mc.player.getPos();
            this.targetPos = new Vec3d((double)this.holePos.getX() + 0.5, mc.player.getY(), (double)this.holePos.getZ() + 0.5);
            if (CombatUtil.isDoubleHole(this.holePos)) {
               Direction facing = CombatUtil.is3Block(this.holePos);
               if (facing != null) {
                  this.targetPos = this.targetPos.add(new Vec3d((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5));
               }
            }

            this.resetMove = true;
            float rotation = getRotationTo(playerPos, this.targetPos).x;
            float yawRad = rotation / 180.0F * 3.1415927F;
            double dist = playerPos.distanceTo(this.targetPos);
            double cappedSpeed = Math.min(0.2873, dist);
            double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
            double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
            event.setX(x);
            event.setZ(z);
            if (Math.abs(x) < 0.1 && Math.abs(z) < 0.1 && playerPos.y <= (double)this.holePos.getY() + 0.5) {
               this.disable();
            }

            if (mc.player.horizontalCollision) {
               ++this.stuckTicks;
            } else {
               this.stuckTicks = 0;
            }

         }
      } else {
         this.disable();
      }
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.targetPos != null && this.holePos != null) {
         if (this.render.getValue()) {
            GL11.glEnable(3042);
            Color color = this.color.getValue();
            Vec3d pos = new Vec3d(this.targetPos.x, this.holePos.getY(), this.targetPos.getZ());
            if (this.fade.getValue()) {
               double temp = 0.01;

               for(double i = 0.0; i < this.circleSize.getValue(); i += temp) {
                  doCircle(matrixStack, ColorUtil.injectAlpha(color, (int)Math.min((double)(color.getAlpha() * 2) / (this.circleSize.getValue() / temp), 255.0)), i, pos, this.segments.getValueInt());
               }
            } else {
               doCircle(matrixStack, color, this.circleSize.getValue(), pos, this.segments.getValueInt());
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
         }
      }
   }

   public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
      Vec3d vec3d = posTo.subtract(posFrom);
      return getRotationFromVec(vec3d);
   }

   public static void doCircle(MatrixStack matrixStack, Color color, double circleSize, Vec3d pos, int segments) {
      Vec3d camPos = mc.getBlockEntityRenderDispatcher().camera.getPos();
      GL11.glDisable(2929);
      Matrix4f matrix = matrixStack.peek().getPositionMatrix();
      Tessellator tessellator = RenderSystem.renderThreadTesselator();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      RenderSystem.setShaderColor((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
      RenderSystem.setShader(GameRenderer::getPositionProgram);
      bufferBuilder.begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);

      for(double i = 0.0; i < 360.0; i += 360.0 / (double)segments) {
         double x = Math.sin(Math.toRadians(i)) * circleSize;
         double z = Math.cos(Math.toRadians(i)) * circleSize;
         Vec3d tempPos = (new Vec3d(pos.x + x, pos.y, pos.z + z)).add(-camPos.x, -camPos.y, -camPos.z);
         bufferBuilder.vertex(matrix, (float)tempPos.x, (float)tempPos.y, (float)tempPos.z).next();
      }

      tessellator.draw();
      GL11.glEnable(2929);
   }

   private static Vec2f getRotationFromVec(Vec3d vec) {
      double d = vec.x;
      double d2 = vec.z;
      double xz = Math.hypot(d, d2);
      d2 = vec.z;
      double d3 = vec.x;
      double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
      double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
      return new Vec2f((float)yaw, (float)pitch);
   }

   private static double normalizeAngle(double angleIn) {
      double angle = angleIn;
      if ((angle %= 360.0) >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }
}

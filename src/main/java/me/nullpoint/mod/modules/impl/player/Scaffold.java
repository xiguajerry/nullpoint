package me.nullpoint.mod.modules.impl.player;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.movement.HoleSnap;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Scaffold extends Module {
   private final BooleanSetting tower = this.add(new BooleanSetting("Tower", true));
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
   public final SliderSetting rotateTime = this.add(new SliderSetting("KeepRotate", 1000.0, 0.0, 3000.0, 10.0));
   private final BooleanSetting render = this.add((new BooleanSetting("Render", true)).setParent());
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100), (v) -> {
      return this.render.isOpen();
   }));
   private final BooleanSetting esp = this.add(new BooleanSetting("ESP", true, (v) -> {
      return this.render.isOpen();
   }));
   private final BooleanSetting box = this.add(new BooleanSetting("Box", true, (v) -> {
      return this.render.isOpen();
   }));
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true, (v) -> {
      return this.render.isOpen();
   }));
   public final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
      return this.render.isOpen();
   }));
   private final Timer timer = new Timer();
   private float[] angle = null;
   private BlockPos pos;
   private static Vec3d lastVec3d;
   private final Timer towerTimer = new Timer();

   public Scaffold() {
      super("Scaffold", Module.Category.Player);
   }

   @EventHandler(
      priority = 100
   )
   public void onRotation(RotateEvent event) {
      if (this.rotate.getValue() && !this.timer.passedMs(this.rotateTime.getValueInt()) && this.angle != null) {
         event.setYaw(this.angle[0]);
         event.setPitch(this.angle[1]);
      }

   }

   public void onEnable() {
      lastVec3d = null;
      this.pos = null;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.render.getValue()) {
         if (this.esp.getValue()) {
            GL11.glEnable(3042);
            double temp = 0.01;

            for(double i = 0.0; i < 0.8; i += temp) {
               HoleSnap.doCircle(matrixStack, ColorUtil.injectAlpha(this.color.getValue(), (int)Math.min((double)(this.color.getValue().getAlpha() * 2) / (0.8 / temp), 255.0)), i, new Vec3d(MathUtil.interpolate(mc.player.lastRenderX, mc.player.getX(), partialTicks), MathUtil.interpolate(mc.player.lastRenderY, mc.player.getY(), partialTicks), MathUtil.interpolate(mc.player.lastRenderZ, mc.player.getZ(), partialTicks)), 5);
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(3042);
         }

         if (this.pos != null) {
            Vec3d cur = this.pos.toCenterPos();
            if (lastVec3d == null) {
               lastVec3d = cur;
            } else {
               lastVec3d = new Vec3d(AnimateUtil.animate(lastVec3d.getX(), cur.x, this.sliderSpeed.getValue()), AnimateUtil.animate(lastVec3d.getY(), cur.y, this.sliderSpeed.getValue()), AnimateUtil.animate(lastVec3d.getZ(), cur.z, this.sliderSpeed.getValue()));
            }

            Render3DUtil.draw3DBox(matrixStack, new Box(lastVec3d.add(0.5, 0.5, 0.5), lastVec3d.add(-0.5, -0.5, -0.5)), ColorUtil.injectAlpha(this.color.getValue(), this.color.getValue().getAlpha()), this.outline.getValue(), this.box.getValue());
         }
      }

   }

   public void onUpdate() {
      int block = InventoryUtil.findBlock();
      if (block != -1) {
         BlockPos placePos = EntityUtil.getPlayerPos().down();
         if (BlockUtil.clientCanPlace(placePos, false)) {
            int old = mc.player.getInventory().selectedSlot;
            if (BlockUtil.getPlaceSide(placePos) == null) {
               double distance = 1000.0;
               BlockPos bestPos = null;
               Direction[] var7 = Direction.values();
               int var8 = var7.length;
               int var9 = 0;

               while(true) {
                  if (var9 >= var8) {
                     if (bestPos == null) {
                        return;
                     }

                     placePos = bestPos;
                     break;
                  }

                  Direction i = var7[var9];
                  if (i != Direction.UP && BlockUtil.canPlace(placePos.offset(i)) && (bestPos == null || mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos()) < distance)) {
                     bestPos = placePos.offset(i);
                     distance = mc.player.squaredDistanceTo(placePos.offset(i).toCenterPos());
                  }

                  ++var9;
               }
            }

            if (this.rotate.getValue()) {
               Direction side = BlockUtil.getPlaceSide(placePos);
               this.angle = EntityUtil.getLegitRotations(placePos.offset(side).toCenterPos().add((double)side.getOpposite().getVector().getX() * 0.5, (double)side.getOpposite().getVector().getY() * 0.5, (double)side.getOpposite().getVector().getZ() * 0.5));
               this.timer.reset();
            }

            InventoryUtil.switchToSlot(block);
            BlockUtil.placeBlock(placePos, this.rotate.getValue(), false);
            InventoryUtil.switchToSlot(old);
            this.pos = placePos;
            if (this.tower.getValue() && mc.options.jumpKey.isPressed() && !MovementUtil.isMoving()) {
               MovementUtil.setMotionY(0.42);
               MovementUtil.setMotionX(0.0);
               MovementUtil.setMotionZ(0.0);
               if (this.towerTimer.passedMs(1500L)) {
                  MovementUtil.setMotionY(-0.28);
                  this.towerTimer.reset();
               }
            } else {
               this.towerTimer.reset();
            }
         }

      }
   }
}

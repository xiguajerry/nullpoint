package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BlockHighLight extends Module {
   final BooleanSetting center = this.add(new BooleanSetting("Center", true));
   final BooleanSetting shrink = this.add(new BooleanSetting("Shrink", true));
   final ColorSetting box = this.add((new ColorSetting("Box", new Color(255, 255, 255, 255))).injectBoolean(true));
   final ColorSetting fill = this.add((new ColorSetting("Fill", new Color(255, 255, 255, 100))).injectBoolean(true));
   final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01));
   final SliderSetting startFadeTime = this.add((new SliderSetting("StartFade", 0.3, 0.0, 2.0, 0.01)).setSuffix("s"));
   final SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2, 0.01, 1.0, 0.01));
   final Timer noPosTimer = new Timer();
   static Vec3d placeVec3d;
   static Vec3d curVec3d;
   double fade = 0.0;

   public BlockHighLight() {
      super("BlockHighLight", Module.Category.Render);
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (mc.crosshairTarget != null) {
         HitResult var4 = mc.crosshairTarget;
         if (var4 instanceof BlockHitResult hitResult) {
             if (mc.crosshairTarget.getType() == Type.BLOCK) {
               this.noPosTimer.reset();
               placeVec3d = this.center.getValue() ? hitResult.getBlockPos().toCenterPos() : mc.crosshairTarget.getPos();
            }

            if (placeVec3d == null) {
               return;
            }

            if (this.fadeSpeed.getValue() >= 1.0) {
               this.fade = this.noPosTimer.passedMs((long)(this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5;
            } else {
               this.fade = AnimateUtil.animate(this.fade, this.noPosTimer.passedMs((long)(this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5, this.fadeSpeed.getValue() / 10.0);
            }

            if (this.fade == 0.0) {
               curVec3d = null;
               return;
            }

            if (curVec3d != null && !(this.sliderSpeed.getValue() >= 1.0)) {
               curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(curVec3d.y, placeVec3d.y, this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(curVec3d.z, placeVec3d.z, this.sliderSpeed.getValue() / 10.0));
            } else {
               curVec3d = placeVec3d;
            }

            Box box = new Box(curVec3d, curVec3d);
            if (this.shrink.getValue()) {
               box = box.expand(this.fade);
            } else {
               box = box.expand(0.5);
            }

            if (this.fill.booleanValue) {
               Render3DUtil.drawFill(matrixStack, box, ColorUtil.injectAlpha(this.fill.getValue(), (int)((double)this.fill.getValue().getAlpha() * this.fade * 2.0)));
            }

            if (this.box.booleanValue) {
               Render3DUtil.drawBox(matrixStack, box, ColorUtil.injectAlpha(this.box.getValue(), (int)((double)this.box.getValue().getAlpha() * this.fade * 2.0)));
            }

         }
      }

   }
}

package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class TotemAnimation extends Module {
   public static TotemAnimation instance;
   private final EnumSetting mode;
   private ItemStack floatingItem;
   private int floatingItemTimeLeft;

   public TotemAnimation() {
      super("TotemAnimation", Module.Category.Render);
      this.mode = this.add(new EnumSetting("Mode", TotemAnimation.Mode.FadeOut));
      this.floatingItem = null;
      instance = this;
   }

   public void showFloatingItem(ItemStack floatingItem) {
      this.floatingItem = floatingItem;
      this.floatingItemTimeLeft = this.getTime();
   }

   public void onUpdate() {
      if (this.floatingItemTimeLeft > 0) {
         --this.floatingItemTimeLeft;
         if (this.floatingItemTimeLeft == 0) {
            this.floatingItem = null;
         }
      }

   }

   public void renderFloatingItem(int scaledWidth, int scaledHeight, float tickDelta) {
      if (this.floatingItem != null && this.floatingItemTimeLeft > 0 && !this.mode.getValue().equals(TotemAnimation.Mode.Off)) {
         int i = this.getTime() - this.floatingItemTimeLeft;
         float f = ((float)i + tickDelta) / (float)this.getTime();
         float g = f * f;
         float h = f * g;
         float j = 10.25F * h * g - 24.95F * g * g + 25.5F * h - 13.8F * g + 4.0F * f;
         float k = j * 3.1415927F;
         RenderSystem.enableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         MatrixStack matrixStack = new MatrixStack();
         matrixStack.push();
         float f2 = (float)i + tickDelta;
         float n = 50.0F + 175.0F * MathHelper.sin(k);
         float rightFactor;
         if (this.mode.getValue().equals(TotemAnimation.Mode.FadeOut)) {
            rightFactor = (float)(Math.sin(f2 * 112.0F / 180.0F) * 100.0);
            float y2 = (float)(Math.cos(f2 * 112.0F / 180.0F) * 50.0);
            matrixStack.translate((float)(scaledWidth / 2) + rightFactor, (float)(scaledHeight / 2) + y2, -50.0F);
            matrixStack.scale(n, -n, n);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Size)) {
            matrixStack.translate((float)(scaledWidth / 2), (float)(scaledHeight / 2), -50.0F);
            matrixStack.scale(n, -n, n);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Otkisuli)) {
            matrixStack.translate((float)(scaledWidth / 2), (float)(scaledHeight / 2), -50.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f2 * 2.0F));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f2 * 2.0F));
            matrixStack.scale(200.0F - f2 * 1.5F, -200.0F + f2 * 1.5F, 200.0F - f2 * 1.5F);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Insert)) {
            matrixStack.translate((float)(scaledWidth / 2), (float)(scaledHeight / 2), -50.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f2 * 3.0F));
            matrixStack.scale(200.0F - f2 * 1.5F, -200.0F + f2 * 1.5F, 200.0F - f2 * 1.5F);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Fall)) {
            rightFactor = (float)(Math.pow(f2, 3.0) * 0.20000000298023224);
            matrixStack.translate((float)(scaledWidth / 2), (float)(scaledHeight / 2) + rightFactor, -50.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f2 * 5.0F));
            matrixStack.scale(200.0F - f2 * 1.5F, -200.0F + f2 * 1.5F, 200.0F - f2 * 1.5F);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Rocket)) {
            rightFactor = (float)(Math.pow(f2, 3.0) * 0.20000000298023224) - 20.0F;
            matrixStack.translate((float)(scaledWidth / 2), (float)(scaledHeight / 2) - rightFactor, -50.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f2 * (float)this.floatingItemTimeLeft * 2.0F));
            matrixStack.scale(200.0F - f2 * 1.5F, -200.0F + f2 * 1.5F, 200.0F - f2 * 1.5F);
         } else if (this.mode.getValue().equals(TotemAnimation.Mode.Roll)) {
            rightFactor = (float)(Math.pow(f2, 2.0) * 4.5);
            matrixStack.translate((float)(scaledWidth / 2) + rightFactor, (float)(scaledHeight / 2), -50.0F);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f2 * 40.0F));
            matrixStack.scale(200.0F - f2 * 1.5F, -200.0F + f2 * 1.5F, 200.0F - f2 * 1.5F);
         }

         VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F - f);
         mc.getItemRenderer().renderItem(this.floatingItem, ModelTransformationMode.FIXED, 15728880, OverlayTexture.DEFAULT_UV, matrixStack, immediate, mc.world, 0);
         matrixStack.pop();
         immediate.draw();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         RenderSystem.disableDepthTest();
      }

   }

   private int getTime() {
      if (this.mode.getValue().equals(TotemAnimation.Mode.FadeOut)) {
         return 10;
      } else {
         return this.mode.getValue().equals(TotemAnimation.Mode.Insert) ? 20 : 40;
      }
   }

   public enum Mode {
      FadeOut,
      Size,
      Otkisuli,
      Insert,
      Fall,
      Rocket,
      Roll,
      Off;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{FadeOut, Size, Otkisuli, Insert, Fall, Rocket, Roll, Off};
      }
   }
}

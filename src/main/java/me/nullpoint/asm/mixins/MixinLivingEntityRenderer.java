package me.nullpoint.asm.mixins;

import java.awt.Color;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.render.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer {
   @Unique
   private LivingEntity lastEntity;
   @Unique
   private float originalYaw;
   @Unique
   private float originalHeadYaw;
   @Unique
   private float originalBodyYaw;
   @Unique
   private float originalPitch;
   @Unique
   private float originalPrevYaw;
   @Unique
   private float originalPrevHeadYaw;
   @Unique
   private float originalPrevBodyYaw;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   public void onRenderPre(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && CombatSetting.INSTANCE.rotations.getValue()) {
         this.originalYaw = livingEntity.getYaw();
         this.originalHeadYaw = livingEntity.headYaw;
         this.originalBodyYaw = livingEntity.bodyYaw;
         this.originalPitch = livingEntity.getPitch();
         this.originalPrevYaw = livingEntity.prevYaw;
         this.originalPrevHeadYaw = livingEntity.prevHeadYaw;
         this.originalPrevBodyYaw = livingEntity.prevBodyYaw;
         livingEntity.setYaw(RotateManager.getRenderYawOffset());
         livingEntity.headYaw = RotateManager.getRotationYawHead();
         livingEntity.bodyYaw = RotateManager.getRenderYawOffset();
         livingEntity.setPitch(RotateManager.getRenderPitch());
         livingEntity.prevYaw = RotateManager.getPrevRenderYawOffset();
         livingEntity.prevHeadYaw = RotateManager.getPrevRotationYawHead();
         livingEntity.prevBodyYaw = RotateManager.getPrevRenderYawOffset();
         livingEntity.prevPitch = RotateManager.getPrevPitch();
      }

      this.lastEntity = livingEntity;
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   public void onRenderPost(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && CombatSetting.INSTANCE.rotations.getValue()) {
         livingEntity.setYaw(this.originalYaw);
         livingEntity.headYaw = this.originalHeadYaw;
         livingEntity.bodyYaw = this.originalBodyYaw;
         livingEntity.setPitch(this.originalPitch);
         livingEntity.prevYaw = this.originalPrevYaw;
         livingEntity.prevHeadYaw = this.originalPrevHeadYaw;
         livingEntity.prevBodyYaw = this.originalPrevBodyYaw;
         livingEntity.prevPitch = this.originalPitch;
      }

   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"
)
   )
   private void onRenderModel(EntityModel entityModel, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      Color newColor = new Color(red, green, blue, alpha);
      if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.antiPlayerCollision.getValue() && this.lastEntity != Wrapper.mc.player) {
         float overrideAlpha = (float)(Wrapper.mc.player.squaredDistanceTo(this.lastEntity.getPos()) / 3.0) + 0.07F;
         newColor = ColorUtil.injectAlpha(newColor, (int)(255.0F * MathUtil.clamp(overrideAlpha, 0.0F, 1.0F)));
      }

      entityModel.render(matrices, vertices, light, overlay, (float)newColor.getRed() / 255.0F, (float)newColor.getGreen() / 255.0F, (float)newColor.getBlue() / 255.0F, (float)newColor.getAlpha() / 255.0F);
   }
}

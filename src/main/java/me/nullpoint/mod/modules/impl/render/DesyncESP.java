package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.Event;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class DesyncESP extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
   private final EnumSetting type;
   public static DesyncESP INSTANCE;
   Model model;
   boolean update;
   float lastYaw;
   float lastPitch;

   public DesyncESP() {
      super("DesyncESP", Module.Category.Render);
      this.type = this.add(new EnumSetting("Type", DesyncESP.Type.ServerSide));
      this.update = true;
      INSTANCE = this;
   }

   public void onLogin() {
      this.update = true;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (this.update) {
            this.model = new Model();
            this.update = false;
         }

      }
   }

   @EventHandler
   public void onUpdateWalkingPost(UpdateWalkingEvent event) {
      if (event.getStage() == Event.Stage.Post) {
         this.lastYaw = mc.player.getYaw();
         this.lastPitch = mc.player.getPitch();
      }

   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (!nullCheck() && this.model != null) {
         if (mc.options.getPerspective() != Perspective.FIRST_PERSON) {
            if (!(Math.abs(this.lastYaw - Nullpoint.ROTATE.lastYaw) < 1.0F) || !(Math.abs(this.lastPitch - Nullpoint.ROTATE.lastPitch) < 1.0F)) {
               RenderSystem.depthMask(false);
               RenderSystem.enableBlend();
               RenderSystem.blendFuncSeparate(770, 771, 0, 1);
               double x = mc.player.prevX + (mc.player.getX() - mc.player.prevX) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
               double y = mc.player.prevY + (mc.player.getY() - mc.player.prevY) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
               double z = mc.player.prevZ + (mc.player.getZ() - mc.player.prevZ) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
               float bodyYaw = this.type.getValue() == DesyncESP.Type.ServerSide ? RotateManager.getPrevRenderYawOffset() + (RotateManager.getRenderYawOffset() - RotateManager.getPrevRenderYawOffset()) * mc.getTickDelta() : mc.player.prevBodyYaw + (mc.player.bodyYaw - mc.player.prevBodyYaw) * mc.getTickDelta();
               float headYaw = this.type.getValue() == DesyncESP.Type.ServerSide ? RotateManager.getPrevRotationYawHead() + (RotateManager.getRotationYawHead() - RotateManager.getPrevRotationYawHead()) * mc.getTickDelta() : mc.player.prevHeadYaw + (mc.player.headYaw - mc.player.prevHeadYaw) * mc.getTickDelta();
               float pitch = this.type.getValue() == DesyncESP.Type.ServerSide ? RotateManager.getPrevPitch() + (RotateManager.getRenderPitch() - RotateManager.getPrevPitch()) * mc.getTickDelta() : mc.player.prevPitch + (mc.player.getPitch() - mc.player.prevPitch) * mc.getTickDelta();
               matrixStack.push();
               matrixStack.translate((float)x, (float)y, (float)z);
               matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0F - bodyYaw)));
               prepareScale(matrixStack);
               this.model.modelPlayer.animateModel(mc.player, mc.player.limbAnimator.getPos(mc.getTickDelta()), mc.player.limbAnimator.getSpeed(mc.getTickDelta()), mc.getTickDelta());
               this.model.modelPlayer.setAngles(mc.player, mc.player.limbAnimator.getPos(mc.getTickDelta()), mc.player.limbAnimator.getSpeed(mc.getTickDelta()), (float)mc.player.age, headYaw - bodyYaw, pitch);
               RenderSystem.enableBlend();
               GL11.glDisable(2929);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder buffer = tessellator.getBuffer();
               RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
               RenderSystem.setShader(GameRenderer::getPositionColorProgram);
               buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
               this.model.modelPlayer.render(matrixStack, buffer, 10, 0, (float)this.color.getValue().getRed() / 255.0F, (float)this.color.getValue().getGreen() / 255.0F, (float)this.color.getValue().getBlue() / 255.0F, (float)this.color.getValue().getAlpha() / 255.0F);
               tessellator.draw();
               RenderSystem.disableBlend();
               GL11.glEnable(2929);
               matrixStack.pop();
               RenderSystem.disableBlend();
               RenderSystem.depthMask(true);
            }
         }
      }
   }

   private static void prepareScale(MatrixStack matrixStack) {
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.scale(1.6F, 1.8F, 1.6F);
      matrixStack.translate(0.0F, -1.501F, 0.0F);
   }

   public enum Type {
      ClientSide,
      ServerSide;

      // $FF: synthetic method
      private static Type[] $values() {
         return new Type[]{ClientSide, ServerSide};
      }
   }

   private static class Model {
      private final PlayerEntityModel modelPlayer;

      public Model() {
         this.modelPlayer = new PlayerEntityModel((new EntityRendererFactory.Context(Wrapper.mc.getEntityRenderDispatcher(), Wrapper.mc.getItemRenderer(), Wrapper.mc.getBlockRenderManager(), Wrapper.mc.getEntityRenderDispatcher().getHeldItemRenderer(), Wrapper.mc.getResourceManager(), Wrapper.mc.getEntityModelLoader(), Wrapper.mc.textRenderer)).getPart(EntityModelLayers.PLAYER), false);
         this.modelPlayer.getHead().scale(new Vector3f(-0.3F, -0.3F, -0.3F));
      }
   }
}

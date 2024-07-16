// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.concurrent.CopyOnWriteArrayList;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.render.PopChams;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public final class PopChams extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255)));
   private final SliderSetting alphaSpeed = this.add(new SliderSetting("AlphaSpeed", 0.2D, 0.0D, 1.0D, 0.01D));
   private final CopyOnWriteArrayList<PopChams.Person> popList = new CopyOnWriteArrayList();
   public static PopChams INSTANCE;

   public PopChams() {
      super("PopChams", Module.Category.Render);
      INSTANCE = this;
   }

   public void onUpdate() {
      this.popList.forEach((person) -> person.update(this.popList));
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFuncSeparate(770, 771, 0, 1);
      this.popList.forEach((person) -> {
         person.modelPlayer.leftPants.visible = false;
         person.modelPlayer.rightPants.visible = false;
         person.modelPlayer.leftSleeve.visible = false;
         person.modelPlayer.rightSleeve.visible = false;
         person.modelPlayer.jacket.visible = false;
         person.modelPlayer.hat.visible = false;
         this.renderEntity(matrixStack, person.player, person.modelPlayer, person.getAlpha());
      });
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
   }

   @EventHandler
   private void onTotemPop(TotemEvent e) {
      if (!e.getPlayer().equals(mc.player) && mc.world != null) {
         PlayerEntity entity = new PlayerEntity(mc.world, BlockPos.ORIGIN, e.getPlayer().bodyYaw, new GameProfile(e.getPlayer().getUuid(), e.getPlayer().getName().getString())) {
            public boolean isSpectator() {
               return false;
            }

            public boolean isCreative() {
               return false;
            }
         };
         entity.copyPositionAndRotation(e.getPlayer());
         entity.bodyYaw = e.getPlayer().bodyYaw;
         entity.headYaw = e.getPlayer().headYaw;
         entity.handSwingProgress = e.getPlayer().handSwingProgress;
         entity.handSwingTicks = e.getPlayer().handSwingTicks;
         entity.setSneaking(e.getPlayer().isSneaking());
         entity.limbAnimator.setSpeed(e.getPlayer().limbAnimator.getSpeed());
         entity.limbAnimator.pos = e.getPlayer().limbAnimator.getPos();
         this.popList.add(new PopChams.Person(entity));
      }
   }

   private void renderEntity(MatrixStack matrices, LivingEntity entity, BipedEntityModel<PlayerEntity> modelBase, int alpha) {
      double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      matrices.push();
      matrices.translate((float)x, (float)y, (float)z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180.0F - entity.bodyYaw)));
      prepareScale(matrices);
      modelBase.animateModel((PlayerEntity)entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), mc.getTickDelta());
      modelBase.setAngles((PlayerEntity)entity, entity.limbAnimator.getPos(), entity.limbAnimator.getSpeed(), (float)entity.age, entity.headYaw - entity.bodyYaw, entity.getPitch());
      RenderSystem.enableBlend();
      GL11.glDisable(2929);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      modelBase.render(matrices, buffer, 10, 0, (float)this.color.getValue().getRed() / 255.0F, (float)this.color.getValue().getGreen() / 255.0F, (float)this.color.getValue().getBlue() / 255.0F, (float)alpha / 255.0F);
      tessellator.draw();
      RenderSystem.disableBlend();
      GL11.glEnable(2929);
      matrices.pop();
   }

   private static void prepareScale(MatrixStack matrixStack) {
      matrixStack.scale(-1.0F, -1.0F, 1.0F);
      matrixStack.scale(1.6F, 1.8F, 1.6F);
      matrixStack.translate(0.0F, -1.501F, 0.0F);
   }

   private class Person {
      private final PlayerEntity player;
      private final PlayerEntityModel<PlayerEntity> modelPlayer;
      private int alpha;

      public Person(PlayerEntity player) {
         this.player = player;
         this.modelPlayer = new PlayerEntityModel((new Context(Wrapper.mc.getEntityRenderDispatcher(), Wrapper.mc.getItemRenderer(), Wrapper.mc.getBlockRenderManager(), Wrapper.mc.getEntityRenderDispatcher().getHeldItemRenderer(), Wrapper.mc.getResourceManager(), Wrapper.mc.getEntityModelLoader(), Wrapper.mc.textRenderer)).getPart(EntityModelLayers.PLAYER), false);
         this.modelPlayer.getHead().scale(new Vector3f(-0.3F, -0.3F, -0.3F));
         this.alpha = PopChams.this.color.getValue().getAlpha();
      }

      public void update(CopyOnWriteArrayList<PopChams.Person> arrayList) {
         if (this.alpha <= 0) {
            arrayList.remove(this);
            this.player.kill();
            this.player.remove(RemovalReason.KILLED);
            this.player.onRemoved();
         } else {
            this.alpha = (int)(AnimateUtil.animate(this.alpha, 0.0D, PopChams.this.alphaSpeed.getValue()) - 0.2D);
         }
      }

      public int getAlpha() {
         return (int)MathUtil.clamp((float)this.alpha, 0.0F, 255.0F);
      }
   }
}
 
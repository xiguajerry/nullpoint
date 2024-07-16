package me.nullpoint.api.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.font.FontRenderers;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class Render3DUtil implements Wrapper {
   public static MatrixStack matrixFrom(double x, double y, double z) {
      MatrixStack matrices = new MatrixStack();
      Camera camera = mc.gameRenderer.getCamera();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
      return matrices;
   }

   public static void setupRender() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   public static void endRender() {
      RenderSystem.disableBlend();
   }

   public static void drawText3D(String text, Vec3d vec3d, Color color) {
      drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color.getRGB());
   }

   public static void drawText3D(String text, Vec3d vec3d, int color) {
      drawText3D(Text.of(text), vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 1.0, color);
   }

   public static void drawText3D(Text text, Vec3d vec3d, double offX, double offY, double scale, Color color) {
      drawText3D(text, vec3d.x, vec3d.y, vec3d.z, offX, offY, scale, color.getRGB());
   }

   public static void drawText3D(Text text, double x, double y, double z, double offX, double offY, double scale, int color) {
      GL11.glDisable(2929);
      MatrixStack matrices = matrixFrom(x, y, z);
      Camera camera = mc.gameRenderer.getCamera();
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.translate(offX, offY, 0.0);
      matrices.scale(-0.025F * (float)scale, -0.025F * (float)scale, 1.0F);
      int halfWidth = mc.textRenderer.getWidth(text) / 2;
      VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
      matrices.push();
      matrices.translate(1.0F, 1.0F, 0.0F);
      mc.textRenderer.draw(Text.of(text.getString().replaceAll("\u00a7[a-zA-Z0-9]", "")), (float)(-halfWidth), 0.0F, 2105376, false, matrices.peek().getPositionMatrix(), immediate, TextLayerType.SEE_THROUGH, 0, 15728880);
      immediate.draw();
      matrices.pop();
      mc.textRenderer.draw(text.copy(), (float)(-halfWidth), 0.0F, color, false, matrices.peek().getPositionMatrix(), immediate, TextLayerType.SEE_THROUGH, 0, 15728880);
      immediate.draw();
      RenderSystem.disableBlend();
      GL11.glEnable(2929);
   }

   public static void drawTextIn3D(String text, Vec3d pos, double offX, double offY, double textOffset, Color color) {
      MatrixStack matrices = new MatrixStack();
      Camera camera = mc.gameRenderer.getCamera();
      RenderSystem.disableCull();
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
      matrices.translate(pos.getX() - camera.getPos().x, pos.getY() - camera.getPos().y, pos.getZ() - camera.getPos().z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      matrices.translate(offX, offY - 0.1, -0.01);
      matrices.scale(-0.025F, -0.025F, 0.0F);
      VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
      FontRenderers.Arial.drawCenteredString(matrices, text, textOffset, 0.0, color.getRGB());
      immediate.draw();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public static void drawFill(MatrixStack matrixStack, Box bb, Color color) {
      draw3DBox(matrixStack, bb, color, false, true);
   }

   public static void drawBox(MatrixStack matrixStack, Box bb, Color color) {
      draw3DBox(matrixStack, bb, color, true, false);
   }

   public static void drawLine(Box b, Color color, float lineWidth) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      MatrixStack matrices = matrixFrom(b.minX, b.minY, b.minZ);
      Tessellator tessellator = RenderSystem.renderThreadTesselator();
      BufferBuilder buffer = tessellator.getBuffer();
      RenderSystem.disableCull();
      RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
      RenderSystem.lineWidth(lineWidth);
      buffer.begin(DrawMode.LINES, VertexFormats.LINES);
      Box box = b.offset((new Vec3d(b.minX, b.minY, b.minZ)).negate());
      float x1 = (float)box.minX;
      float y1 = (float)box.minY;
      float z1 = (float)box.minZ;
      float x2 = (float)box.maxX;
      float y2 = (float)box.maxY;
      float z2 = (float)box.maxZ;
      vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
      vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
      vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
      vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
      vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
      vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
      vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
      vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
      vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
      vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
      vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
      vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
      tessellator.draw();
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   public static void drawSphere(MatrixStack matrix, EndCrystalEntity entity, Float radius, Float height, Float lineWidth, Color color) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)mc.getTickDelta();
      double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)mc.getTickDelta();
      double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)mc.getTickDelta();
      double pix2 = 6.283185307179586;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      RenderSystem.lineWidth(lineWidth);
      bufferBuilder.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

      for(int i = 0; i <= 180; ++i) {
         bufferBuilder.vertex(matrix.peek().getPositionMatrix(), (float)(x + (double)radius * Math.cos((double)i * pix2 / 45.0)), (float)(y + (double)height), (float)(z + (double)radius * Math.sin((double)i * pix2 / 45.0))).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
      }

      tessellator.draw();
      RenderSystem.disableBlend();
   }

   public static void draw3DBox(MatrixStack matrixStack, Box box, Color color) {
      draw3DBox(matrixStack, box, color, true, true);
   }

   public static void draw3DBox(MatrixStack matrixStack, Box box, Color color, boolean outline, boolean fill) {
      box = box.offset(mc.gameRenderer.getCamera().getPos().negate());
      RenderSystem.enableBlend();
      GL11.glDisable(2929);
      Matrix4f matrix = matrixStack.peek().getPositionMatrix();
      Tessellator tessellator = RenderSystem.renderThreadTesselator();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      if (outline) {
         RenderSystem.setShaderColor((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         bufferBuilder.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION);
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         tessellator.draw();
      }

      if (fill) {
         RenderSystem.setShaderColor((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
         RenderSystem.setShader(GameRenderer::getPositionProgram);
         bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION);
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.minZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.minY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ).next();
         bufferBuilder.vertex(matrix, (float)box.minX, (float)box.maxY, (float)box.minZ).next();
         tessellator.draw();
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glEnable(2929);
      RenderSystem.disableBlend();
   }

   public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, Color color, float width) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      MatrixStack matrices = matrixFrom(x1, y1, z1);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      RenderSystem.disableCull();
      RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
      RenderSystem.lineWidth(width);
      buffer.begin(DrawMode.LINES, VertexFormats.LINES);
      vertexLine(matrices, buffer, 0.0, 0.0, 0.0, (float)(x2 - x1), (float)(y2 - y1), (float)(z2 - z1), color);
      tessellator.draw();
      RenderSystem.enableCull();
      RenderSystem.lineWidth(1.0F);
      RenderSystem.disableBlend();
   }

   public static void vertexLine(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, Color lineColor) {
      Matrix4f model = matrices.peek().getPositionMatrix();
      Matrix3f normal = matrices.peek().getNormalMatrix();
      Vector3f normalVec = getNormal((float)x1, (float)y1, (float)z1, (float)x2, (float)y2, (float)z2);
      buffer.vertex(model, (float)x1, (float)y1, (float)z1).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
      buffer.vertex(model, (float)x2, (float)y2, (float)z2).color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), lineColor.getAlpha()).normal(normal, normalVec.x(), normalVec.y(), normalVec.z()).next();
   }

   public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
      float xNormal = x2 - x1;
      float yNormal = y2 - y1;
      float zNormal = z2 - z1;
      float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);
      return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
   }
}

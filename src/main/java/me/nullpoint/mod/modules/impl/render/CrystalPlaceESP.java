// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.render.CrystalPlaceESP;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class CrystalPlaceESP extends Module {
   BooleanSetting range = this.add(new BooleanSetting("Check Range", true)).setParent();
   SliderSetting rangeValue = this.add(new SliderSetting("Range", 12, 0, 256, (v) -> this.range.getValue()));
   ColorSetting color = this.add(new ColorSetting("Color ", new Color(255, 255, 255, 150)));
   SliderSetting animationTime = this.add(new SliderSetting("AnimationTime ", 500, 0, 1500));
   SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 500.0D, 0.0D, 1500.0D, 0.1D));
   EnumSetting mode = this.add(new EnumSetting("Mode", CrystalPlaceESP.Mode.Normal));
   SliderSetting pointsNew = this.add(new SliderSetting("Points", 3, 1, 10, (v) -> this.mode.getValue() == CrystalPlaceESP.Mode.Normal));
   SliderSetting interval = this.add(new SliderSetting("Interval ", 2, 1, 100, (v) -> this.mode.getValue() == CrystalPlaceESP.Mode.New));
   private final ConcurrentHashMap<EndCrystalEntity, CrystalPlaceESP.RenderInfo> cryList = new ConcurrentHashMap();
   private final Timer timer = new Timer();

   public CrystalPlaceESP() {
      super("CrystalPlaceESP", Module.Category.Render);
   }

   @EventHandler
   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      for(Entity e : new Iterable<Entity>() {
         public Iterator<Entity> iterator() {
            return Wrapper.mc.world.getEntities().iterator();
         }
      }) {
         if (e instanceof EndCrystalEntity && (!this.range.getValue() || !((double)mc.player.distanceTo(e) > this.rangeValue.getValue())) && !this.cryList.containsKey(e)) {
            this.cryList.put((EndCrystalEntity)e, new CrystalPlaceESP.RenderInfo((EndCrystalEntity)e, System.currentTimeMillis()));
         }
      }

      if (this.mode.getValue().equals(CrystalPlaceESP.Mode.Normal)) {
         this.cryList.forEach((e, renderInfo) -> this.draw(matrixStack, renderInfo.entity, renderInfo.time, renderInfo.time));
      } else if (this.mode.getValue().equals(CrystalPlaceESP.Mode.New)) {
         int time = 0;

         for(int i = 0; (double)i < this.pointsNew.getValue(); ++i) {
            if (this.timer.passedMs(500L)) {
               int finalTime = time;
               this.cryList.forEach((e, renderInfo) -> this.draw(matrixStack, renderInfo.entity, renderInfo.time - (long)finalTime, renderInfo.time - (long)finalTime));
            }

            time = (int)((double)time + this.interval.getValue());
         }
      }

      this.cryList.forEach((e, renderInfo) -> {
         if ((double)(System.currentTimeMillis() - renderInfo.time) > this.animationTime.getValue() && !e.isAlive()) {
            this.cryList.remove(e);
         }

         if ((double)(System.currentTimeMillis() - renderInfo.time) > this.animationTime.getValue() && (double)mc.player.distanceTo(e) > this.rangeValue.getValue()) {
            this.cryList.remove(e);
         }

      });
   }

   private void draw(MatrixStack matrixStack, EndCrystalEntity entity, long radTime, long heightTime) {
      long rad = System.currentTimeMillis() - radTime;
      long height = System.currentTimeMillis() - heightTime;
      if ((double)rad <= this.animationTime.getValue()) {
         drawCircle3D(matrixStack, entity, (float)rad / this.fadeSpeed.getValueFloat(), (float)height / 1000.0F, this.color.getValue());
      }

   }

   public static void drawCircle3D(MatrixStack stack, Entity ent, float radius, float height, Color color) {
      Render3DUtil.setupRender();
      GL11.glDisable(2929);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      bufferBuilder.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      GL11.glLineWidth(2.0F);
      double x = ent.prevX + (ent.getX() - ent.prevX) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
      double y = ent.prevY + (ent.getY() - ent.prevY) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY();
      double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)mc.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
      stack.push();
      stack.translate(x, y, z);
      Matrix4f matrix = stack.peek().getPositionMatrix();

      for(int i = 0; i <= 180; ++i) {
         bufferBuilder.vertex(matrix, (float)((double)radius * Math.cos((double)i * 6.28D / 45.0D)), 0.0F, (float)((double)radius * Math.sin((double)i * 6.28D / 45.0D))).color(color.getRGB()).next();
      }

      tessellator.draw();
      Render3DUtil.endRender();
      stack.translate(-x, -y + (double)height, -z);
      GL11.glEnable(2929);
      stack.pop();
   }

   public void onDisable() {
      this.cryList.clear();
   }

   public enum Mode {
      Normal,
      New;

      // $FF: synthetic method
      private static CrystalPlaceESP.Mode[] $values() {
         return new CrystalPlaceESP.Mode[]{Normal, New};
      }
   }

   record RenderInfo(EndCrystalEntity entity, long time) {
   }
}
 
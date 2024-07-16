package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class PlaceRender extends Module {
   public static PlaceRender INSTANCE;
   private final ColorSetting box = this.add((new ColorSetting("Box", new Color(255, 255, 255, 255))).injectBoolean(true));
   private final ColorSetting fill = this.add((new ColorSetting("Fill", new Color(255, 255, 255, 100))).injectBoolean(true));
   public final SliderSetting fadeTime = this.add(new SliderSetting("FadeTime", 500, 0, 3000));
   private final ColorSetting tryPlaceBox = this.add((new ColorSetting("TryPlaceBox", new Color(178, 178, 178, 255))).injectBoolean(true));
   private final ColorSetting tryPlaceFill = this.add((new ColorSetting("TryPlaceFill", new Color(255, 119, 119, 157))).injectBoolean(true));
   public final SliderSetting timeout = this.add(new SliderSetting("TimeOut", 500, 0, 3000));
   private final EnumSetting quad;
   private final EnumSetting mode;
   public static final HashMap renderMap = new HashMap();

   public PlaceRender() {
      super("PlaceRender", Module.Category.Render);
      this.quad = this.add(new EnumSetting("Quad", FadeUtils.Quad.In));
      this.mode = this.add(new EnumSetting("Mode", PlaceRender.Mode.All));
      this.enable();
      INSTANCE = this;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      BlockUtil.placedPos.forEach((pos) -> {
         renderMap.put(pos, new PlacePos(pos));
      });
      BlockUtil.placedPos.clear();
      if (!renderMap.isEmpty()) {
         boolean shouldClear = true;
         Iterator var4 = renderMap.values().iterator();

         while(true) {
            while(var4.hasNext()) {
               PlacePos placePosition = (PlacePos)var4.next();
               if (placePosition.isAir) {
                  if (mc.world.isAir(placePosition.pos)) {
                     if (!placePosition.timer.passed(this.timeout.getValue())) {
                        placePosition.fade.reset();
                        Box aBox = new Box(placePosition.pos);
                        if (this.tryPlaceFill.booleanValue) {
                           Render3DUtil.drawFill(matrixStack, aBox, this.tryPlaceFill.getValue());
                        }

                        if (this.tryPlaceBox.booleanValue) {
                           Render3DUtil.drawBox(matrixStack, aBox, this.tryPlaceBox.getValue());
                        }

                        shouldClear = false;
                     }
                     continue;
                  }

                  placePosition.isAir = false;
               }

               double quads = placePosition.fade.getQuad((FadeUtils.Quad)this.quad.getValue());
               if (quads != 1.0) {
                  shouldClear = false;
                  double alpha = this.mode.getValue() != PlaceRender.Mode.Fade && this.mode.getValue() != PlaceRender.Mode.All ? 1.0 : 1.0 - quads;
                  double size = this.mode.getValue() != PlaceRender.Mode.Shrink && this.mode.getValue() != PlaceRender.Mode.All ? 0.0 : quads;
                  Box aBox = (new Box(placePosition.pos)).expand(-size * 0.5, -size * 0.5, -size * 0.5);
                  if (this.fill.booleanValue) {
                     Render3DUtil.drawFill(matrixStack, aBox, ColorUtil.injectAlpha(this.fill.getValue(), (int)((double)this.fill.getValue().getAlpha() * alpha)));
                  }

                  if (this.box.booleanValue) {
                     Render3DUtil.drawBox(matrixStack, aBox, ColorUtil.injectAlpha(this.box.getValue(), (int)((double)this.box.getValue().getAlpha() * alpha)));
                  }
               }
            }

            if (shouldClear) {
               renderMap.clear();
            }

            return;
         }
      }
   }

   private enum Mode {
      Fade,
      Shrink,
      All;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Fade, Shrink, All};
      }
   }

   public class PlacePos {
      public final FadeUtils fade;
      public final BlockPos pos;
      public final Timer timer;
      public boolean isAir;

      public PlacePos(BlockPos placePos) {
         this.fade = new FadeUtils((long)PlaceRender.this.fadeTime.getValue());
         this.pos = placePos;
         this.timer = new Timer();
         this.isAir = true;
      }
   }
}

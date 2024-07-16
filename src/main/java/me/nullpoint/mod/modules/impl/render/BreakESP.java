package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public class BreakESP extends Module {
   public static BreakESP INSTANCE;
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
   public final BooleanSetting outline = this.add(new BooleanSetting("Outline", false));
   public final BooleanSetting box = this.add(new BooleanSetting("Box", true));
   public final SliderSetting animationTime = this.add(new SliderSetting("AnimationTime", 500, 0, 2000));
   private final EnumSetting quad;

   public BreakESP() {
      super("BreakESP", Module.Category.Render);
      this.quad = this.add(new EnumSetting("Quad", FadeUtils.Quad.In));
      INSTANCE = this;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      Iterator var3 = (new HashMap(Nullpoint.BREAK.breakMap)).values().iterator();

      while(var3.hasNext()) {
         MineManager.BreakData breakData = (MineManager.BreakData)var3.next();
         if (breakData != null && breakData.getEntity() != null) {
            double size = 0.5 * (1.0 - breakData.fade.getQuad((FadeUtils.Quad)this.quad.getValue()));
            Render3DUtil.draw3DBox(matrixStack, (new Box(breakData.pos)).shrink(size, size, size).shrink(-size, -size, -size), this.color.getValue(), this.outline.getValue(), this.box.getValue());
            Render3DUtil.drawText3D(breakData.getEntity().getName().getString(), breakData.pos.toCenterPos().add(0.0, 0.1, 0.0), -1);
            Render3DUtil.drawText3D(Text.of(mc.world.isAir(breakData.pos) ? "Broken" : "Breaking"), breakData.pos.toCenterPos().add(0.0, -0.1, 0.0), 0.0, 0.0, 1.0, new Color(0, 255, 51));
         }
      }

   }
}

package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Crosshair extends Module {
   public static Crosshair INSTANCE;
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 255)));
   public final SliderSetting length = this.add(new SliderSetting("Length", 5.0, 0.0, 20.0, 0.1));
   public final SliderSetting thickness = this.add(new SliderSetting("Thickness", 2.0, 0.0, 20.0, 0.1));
   public final SliderSetting interval = this.add(new SliderSetting("Interval", 2.0, 0.0, 20.0, 0.1));

   public Crosshair() {
      super("Crosshair", Module.Category.Render);
      INSTANCE = this;
   }

   public void draw(DrawContext context) {
      MatrixStack matrixStack = context.getMatrices();
      float centerX = (float)mc.getWindow().getScaledWidth() / 2.0F;
      float centerY = (float)mc.getWindow().getScaledHeight() / 2.0F;
      Render2DUtil.drawRect(matrixStack, centerX - this.thickness.getValueFloat() / 2.0F, centerY - this.length.getValueFloat() - this.interval.getValueFloat(), this.thickness.getValueFloat(), this.length.getValueFloat(), this.color.getValue());
      Render2DUtil.drawRect(matrixStack, centerX - this.thickness.getValueFloat() / 2.0F, centerY + this.interval.getValueFloat(), this.thickness.getValueFloat(), this.length.getValueFloat(), this.color.getValue());
      Render2DUtil.drawRect(matrixStack, centerX + this.interval.getValueFloat(), centerY - this.thickness.getValueFloat() / 2.0F, this.length.getValueFloat(), this.thickness.getValueFloat(), this.color.getValue());
      Render2DUtil.drawRect(matrixStack, centerX - this.interval.getValueFloat() - this.length.getValueFloat(), centerY - this.thickness.getValueFloat() / 2.0F, this.length.getValueFloat(), this.thickness.getValueFloat(), this.color.getValue());
   }
}

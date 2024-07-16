package me.nullpoint.mod.modules.impl.client;

import java.awt.Color;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.AnchorAura;
import me.nullpoint.mod.modules.impl.combat.AutoCity;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.impl.combat.AutoTrap;
import me.nullpoint.mod.modules.impl.combat.Burrow;
import me.nullpoint.mod.modules.impl.combat.FeetTrap;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.impl.combat.WebAura;
import me.nullpoint.mod.modules.impl.movement.Speed;
import me.nullpoint.mod.modules.impl.movement.Step;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Indicator extends Module {
   public static Indicator INSTANCE;
   MatrixStack matrixStack;
   float offset;
   float height;

   public Indicator() {
      super("Indicator", Module.Category.Client);
      INSTANCE = this;
   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (!nullCheck()) {
         this.matrixStack = drawContext.getMatrices();
         this.height = FontRenderers.Calibri.getFontHeight();
         this.offset = 0.0F;
         if (Burrow.INSTANCE.isOn()) {
            this.draw("BURROW", HoleKick.isInWeb(mc.player) ? Indicator.ColorType.Red : Indicator.ColorType.Green);
         }

         if (BlockUtil.isHole(EntityUtil.getPlayerPos(true))) {
            this.draw("SAFE", Indicator.ColorType.Green);
         } else {
            this.draw("UNSAFE", Indicator.ColorType.Red);
         }

         if (Speed.INSTANCE.isOn()) {
            this.draw("BHOP", Indicator.ColorType.White);
         }

         if (HoleKick.INSTANCE.isOn()) {
            this.draw("PUSH", Indicator.ColorType.White);
         }

         if (AutoTrap.INSTANCE.isOn()) {
            this.draw("TRAP", Indicator.ColorType.White);
         }

         if (AutoCity.INSTANCE.isOn()) {
            this.draw("CITY", Indicator.ColorType.White);
         }

         if (FeetTrap.INSTANCE.isOn()) {
            this.draw("DEF", Indicator.ColorType.White);
         }

         if (Step.INSTANCE.isOn()) {
            this.draw("ST", Indicator.ColorType.White);
         }

         if (WebAura.INSTANCE.isOn()) {
            this.draw("AW", Indicator.ColorType.White);
         }

         if (AutoCrystal.INSTANCE.isOn()) {
            this.draw("AC", AutoCrystal.INSTANCE.displayTarget != null && AutoCrystal.INSTANCE.lastDamage > 0.0F ? Indicator.ColorType.Green : Indicator.ColorType.Red);
         }

         if (AnchorAura.INSTANCE.isOn()) {
            this.draw("AN", AnchorAura.INSTANCE.displayTarget != null && AnchorAura.INSTANCE.currentPos != null ? Indicator.ColorType.Green : Indicator.ColorType.Red);
         }

      }
   }

   private void draw(String s, ColorType type) {
      int color = -1;
      if (type == Indicator.ColorType.Red) {
         color = (new Color(255, 0, 0)).getRGB();
      }

      if (type == Indicator.ColorType.Green) {
         color = (new Color(47, 173, 26)).getRGB();
      }

      double width = FontRenderers.Calibri.getWidth(s) + 8.0F;
      Render2DUtil.horizontalGradient(this.matrixStack, 10.0F, (float)(mc.getWindow().getScaledHeight() - 200) + this.offset, (float)(10.0 + width / 2.0), (float)(mc.getWindow().getScaledHeight() - 200) + this.offset + this.height, new Color(0, 0, 0, 0), new Color(0, 0, 0, 100));
      Render2DUtil.horizontalGradient(this.matrixStack, (float)(10.0 + width / 2.0), (float)(mc.getWindow().getScaledHeight() - 200) + this.offset, (float)(10.0 + width), (float)(mc.getWindow().getScaledHeight() - 200) + this.offset + this.height, new Color(0, 0, 0, 100), new Color(0, 0, 0, 0));
      FontRenderers.Calibri.drawString(this.matrixStack, s, 14.0F, (float)(mc.getWindow().getScaledHeight() - 195) + this.offset, color);
      this.offset -= this.height + 3.0F;
   }

   private enum ColorType {
      Red,
      Green,
      White;

      // $FF: synthetic method
      private static ColorType[] $values() {
         return new ColorType[]{Red, Green, White};
      }
   }
}

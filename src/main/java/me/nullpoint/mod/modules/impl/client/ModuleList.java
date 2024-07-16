// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.ModuleList;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.opengl.GL11;

public class ModuleList extends Module {
   public static ModuleList INSTANCE;
   private final BooleanSetting font = this.add(new BooleanSetting("Font", false));
   private final SliderSetting height = this.add(new SliderSetting("Height", 0, -2, 10));
   private final SliderSetting textOffset = this.add(new SliderSetting("TextOffset", 0, 0, 10));
   private final SliderSetting xOffset = this.add(new SliderSetting("XOffset", 0, 0, 500));
   private final SliderSetting yOffset = this.add(new SliderSetting("YOffset", 10, 0, 300));
   public final EnumSetting<AnimateUtil.AnimMode> animMode = this.add(new EnumSetting("AnimMode", AnimateUtil.AnimMode.Mio));
   public final SliderSetting disableSpeed = this.add(new SliderSetting("DisableSpeed", 0.2D, -0.2D, 1.0D, 0.01D));
   public final SliderSetting enableSpeed = this.add(new SliderSetting("EnableSpeed", 0.2D, 0.0D, 1.0D, 0.01D));
   public final SliderSetting ySpeed = this.add(new SliderSetting("YSpeed", 0.2D, 0.01D, 1.0D, 0.01D));
   private final BooleanSetting forgeHax = this.add(new BooleanSetting("ForgeHax", true));
   private final BooleanSetting space = this.add(new BooleanSetting("Space", true));
   private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
   private final BooleanSetting animY = this.add(new BooleanSetting("AnimY", true));
   private final BooleanSetting scissor = this.add(new BooleanSetting("Scissor", false));
   private final BooleanSetting onlyBind = this.add(new BooleanSetting("OnlyBind", true));
   private final EnumSetting<ModuleList.ColorMode> colorMode = this.add(new EnumSetting("ColorMode", ModuleList.ColorMode.Pulse));
   private final SliderSetting rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 200, 1, 400, (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Rainbow));
   private final SliderSetting saturation = this.add(new SliderSetting("Saturation", 130.0D, 1.0D, 255.0D, (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Rainbow));
   private final SliderSetting pulseSpeed = this.add(new SliderSetting("PulseSpeed", 1.0D, 0.0D, 5.0D, 0.1D, (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Pulse));
   private final SliderSetting pulseCounter = this.add(new SliderSetting("Counter", 10, 1, 50, (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Pulse));
   private final SliderSetting rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 600, (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Rainbow));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 255), (v) -> this.colorMode.getValue() != ModuleList.ColorMode.Rainbow));
   private final ColorSetting endColor = this.add(new ColorSetting("EndColor", new Color(255, 0, 0, 255), (v) -> this.colorMode.getValue() == ModuleList.ColorMode.Pulse));
   private final BooleanSetting rect = this.add(new BooleanSetting("Rect", true));
   private final BooleanSetting backGround = this.add((new BooleanSetting("BackGround", true)).setParent());
   private final BooleanSetting bgSync = this.add(new BooleanSetting("Sync", false, (v) -> this.backGround.isOpen()));
   private final ColorSetting bgColor = this.add(new ColorSetting("BGColor", new Color(0, 0, 0, 100), (v) -> this.backGround.isOpen()));
   private final BooleanSetting preY = this.add(new BooleanSetting("PreY", true));
   private final BooleanSetting fold = this.add((new BooleanSetting("Fold", true)).setParent());
   private final SliderSetting foldSpeed = this.add(new SliderSetting("FoldSpeed", 0.1D, 0.01D, 1.0D, 0.01D, (v) -> this.fold.isOpen()));
   private final BooleanSetting fade = this.add((new BooleanSetting("Fade", true)).setParent());
   private final SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.1D, 0.01D, 1.0D, 0.01D, (v) -> this.fade.isOpen()));
   private List<ModuleList.Modules> modulesList = new ArrayList();
   boolean update;
   private boolean aBoolean;
   private final Timer timer = new Timer();
   int progress = 0;

   public ModuleList() {
      super("ModuleList", Module.Category.Client);
      INSTANCE = this;
   }

   public void onEnable() {
      this.modulesList.clear();

      for(Module module : Nullpoint.MODULE.modules) {
         this.modulesList.add(new ModuleList.Modules(module));
      }

   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (this.space.getValue() != this.aBoolean) {
         for(ModuleList.Modules modules : this.modulesList) {
            modules.updateName();
         }

         this.aBoolean = this.space.getValue();
      }

      for(ModuleList.Modules modules : this.modulesList) {
         modules.update();
      }

      if (this.update) {
         this.modulesList = this.modulesList.stream().sorted(Comparator.comparing((module) -> this.getStringWidth(module.name) * -1)).collect(Collectors.toList());
         this.update = false;
      }

      if (this.timer.passed(25L)) {
         this.progress -= this.rainbowSpeed.getValueInt();
         this.timer.reset();
      }

      int startY = this.down.getValue() ? mc.getWindow().getScaledHeight() - this.yOffset.getValueInt() - this.getFontHeight() : this.yOffset.getValueInt();
      int lastY = startY;
      int counter = 20;
      Iterator var6 = this.modulesList.iterator();

      while(true) {
         ModuleList.Modules modules;
         while(true) {
            if (!var6.hasNext()) {
               return;
            }

            modules = (ModuleList.Modules)var6.next();
            if (!modules.module.isOn() || !modules.module.drawnSetting.getValue() || this.onlyBind.getValue() && modules.module.getBind().getKey() == -1) {
               modules.disable();
            } else {
               modules.enable();
            }

            if (modules.isEnabled) {
               if (this.fade.getValue()) {
                  modules.fade = this.animate(modules.fade, 1.0D, this.fadeSpeed.getValue());
               } else {
                  modules.fade = 1.0D;
               }

               modules.fold = 1.0D;
               modules.x = this.animate(modules.x, this.getStringWidth(this.getSuffix(modules.name)), this.enableSpeed.getValue());
               break;
            }

            if (this.fade.getValue()) {
               modules.fade = this.animate(modules.fade, 0.08D, this.fadeSpeed.getValue());
            } else {
               modules.fade = 1.0D;
            }

            modules.fold = this.animate(modules.fold, -0.1D, this.foldSpeed.getValue());
            modules.x = this.animate(modules.x, -1.0D, this.disableSpeed.getValue());
            if (!(modules.x <= 0.0D) && !(modules.fade <= 0.084D) && (!this.fold.getValue() || !(modules.fold <= 0.0D))) {
               break;
            }

            modules.hide = true;
         }

         if (modules.hide) {
            modules.updateName();
            modules.x = 0.0D;
            modules.y = this.animY.getValue() ? (double)startY : (double)lastY;
            modules.nameUpdated = false;
            modules.hide = false;
         }

         if (modules.nameUpdated) {
            modules.nameUpdated = false;
            modules.y = this.animY.getValue() && !modules.isEnabled ? (double)startY : (double)lastY;
         } else {
            modules.y = this.animate(modules.y, this.animY.getValue() && !modules.isEnabled ? (double)startY : (double)lastY, this.ySpeed.getValue());
         }

         ++counter;
         int textX = (int)((double)mc.getWindow().getScaledWidth() - modules.x - this.xOffset.getValue() - (double)(this.rect.getValue() ? 2 : 0));
         if (this.fold.getValue()) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0.0D, modules.y * (1.0D - modules.fold), 0.0D);
            drawContext.getMatrices().scale(1.0F, (float)modules.fold, 1.0F);
         }

         if (this.scissor.getValue()) {
            GL11.glEnable(3089);
            GL11.glScissor(0, 0, (mc.getWindow().getWidth() / 2 - this.xOffset.getValueInt() - (this.rect.getValue() ? 2 : 0)) * 2, mc.getWindow().getHeight());
         }

         if (this.backGround.getValue()) {
            Render2DUtil.drawRect(drawContext.getMatrices(), (float)(textX - 1), (float)((int)modules.y), (float)mc.getWindow().getScaledWidth() - (float)this.xOffset.getValueInt() + 1.0F - (float)textX + 1.0F, (float)(this.getFontHeight() + this.height.getValueInt()), this.bgSync.getValue() ? ColorUtil.injectAlpha(this.getColor(counter), (int)((double)this.bgColor.getValue().getAlpha() * modules.fade)) : ColorUtil.injectAlpha(this.bgColor.getValue().getRGB(), (int)((double)this.bgColor.getValue().getAlpha() * modules.fade)));
         }

         if (this.font.getValue()) {
            FontRenderers.Arial.drawString(drawContext.getMatrices(), this.getSuffix(modules.name), (float)textX, (float)((int)(modules.y + 1.0D + (double)this.textOffset.getValueInt())), ColorUtil.injectAlpha(this.getColor(counter), (int)(255.0D * modules.fade)));
         } else {
            drawContext.drawTextWithShadow(mc.textRenderer, this.getSuffix(modules.name), textX, (int)(modules.y + 1.0D + (double)this.textOffset.getValueInt()), ColorUtil.injectAlpha(this.getColor(counter), (int)(255.0D * modules.fade)));
         }

         if (this.scissor.getValue()) {
            GL11.glDisable(3089);
         }

         if (this.fold.getValue()) {
            drawContext.getMatrices().pop();
         }

         if (this.rect.getValue()) {
            Render2DUtil.drawRect(drawContext.getMatrices(), (float)mc.getWindow().getScaledWidth() - (float)this.xOffset.getValueInt() - 1.0F, (float)((int)modules.y), 1.0F, (float)(this.getFontHeight() + this.height.getValueInt()), ColorUtil.injectAlpha(this.getColor(counter), (int)(255.0D * modules.fade)));
         }

         if (modules.isEnabled || !this.preY.getValue()) {
            if (this.down.getValue()) {
               lastY -= this.getFontHeight() + this.height.getValueInt();
            } else {
               lastY += this.getFontHeight() + this.height.getValueInt();
            }
         }
      }
   }

   public double animate(double current, double endPoint, double speed) {
      if (speed >= 1.0D) {
         return endPoint;
      } else {
         return speed == 0.0D ? current : AnimateUtil.animate(current, endPoint, speed, this.animMode.getValue());
      }
   }

   private String getSuffix(String s) {
      return this.forgeHax.getValue() ? s + "§r<" : s;
   }

   private int getColor(int counter) {
      return this.colorMode.getValue() != ModuleList.ColorMode.Custom ? this.rainbow(counter).getRGB() : this.color.getValue().getRGB();
   }

   private Color rainbow(int delay) {
      if (this.colorMode.getValue() == ModuleList.ColorMode.Pulse) {
         return ColorUtil.pulseColor(this.color.getValue(), this.endColor.getValue(), delay, this.pulseCounter.getValueInt(), this.pulseSpeed.getValue());
      } else if (this.colorMode.getValue() == ModuleList.ColorMode.Rainbow) {
         double rainbowState = Math.ceil(((double)this.progress + (double)delay * this.rainbowDelay.getValue()) / 20.0D);
         return Color.getHSBColor((float)(rainbowState % 360.0D / 360.0D), this.saturation.getValueFloat() / 255.0F, 1.0F);
      } else {
         return this.color.getValue();
      }
   }

   private int getStringWidth(String text) {
      return this.font.getValue() ? (int)FontRenderers.Arial.getWidth(text) : mc.textRenderer.getWidth(text);
   }

   private int getFontHeight() {
      return this.font.getValue() ? (int)FontRenderers.Arial.getFontHeight() : 9;
   }

   private enum ColorMode {
      Custom,
      Pulse,
      Rainbow;

      // $FF: synthetic method
      private static ModuleList.ColorMode[] $values() {
         return new ModuleList.ColorMode[]{Custom, Pulse, Rainbow};
      }
   }

   public class Modules {
      public boolean isEnabled = false;
      public final Module module;
      public double x = 0.0D;
      public double y = 0.0D;
      public double fade = 0.0D;
      public boolean hide = true;
      public double fold = 0.0D;
      public String lastName = "";
      public String name = "";
      public boolean nameUpdated = false;

      public Modules(Module module) {
         this.module = module;
      }

      public void enable() {
         if (!this.isEnabled) {
            this.isEnabled = true;
         }
      }

      public void disable() {
         if (this.isEnabled) {
            this.isEnabled = false;
         }
      }

      public void updateName() {
         String name = this.module.getArrayName();
         this.lastName = name;
         if (ModuleList.this.space.getValue()) {
            name = this.module.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
            if (name.startsWith(" ")) {
               name = name.replaceFirst(" ", "");
            }

            name = name + this.module.getArrayInfo();
         }

         this.name = name;
         ModuleList.this.update = true;
      }

      public void update() {
         String name = this.module.getArrayName();
         if (!this.lastName.equals(name)) {
            this.lastName = name;
            if (ModuleList.this.space.getValue()) {
               name = this.module.getName().replaceAll("([a-z])([A-Z])", "$1 $2");
               if (name.startsWith(" ")) {
                  name = name.replaceFirst(" ", "");
               }

               name = name + this.module.getArrayInfo();
            }

            this.name = name;
            ModuleList.this.update = true;
            this.nameUpdated = true;
         }

      }
   }
}
 
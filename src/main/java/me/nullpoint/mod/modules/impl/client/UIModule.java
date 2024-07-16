package me.nullpoint.mod.modules.impl.client;

import java.awt.Color;
import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.components.impl.BooleanComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.ColorComponents;
import me.nullpoint.mod.gui.clickgui.components.impl.ModuleComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.SliderComponent;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.Screen;

public class UIModule extends Module {
   public static UIModule INSTANCE;
   private final EnumSetting<Pages> page;
   public final EnumSetting<AnimateUtil.AnimMode> animMode;
   public final SliderSetting height;
   public final ColorSetting bindC;
   public final ColorSetting gearColor;
   public final EnumSetting<Mode> mode;
   public final BooleanSetting scissor;
   public final BooleanSetting snow;
   public final SliderSetting animationSpeed;
   public final SliderSetting sliderSpeed;
   public final SliderSetting booleanSpeed;
   public final BooleanSetting customFont;
   public final ColorSetting color;
   public final ColorSetting mainHover;
   public final ColorSetting categoryEnd;
   public final ColorSetting disableText;
   public final ColorSetting enableText;
   public final ColorSetting mbgColor;
   public final ColorSetting moduleEnd;
   public final ColorSetting moduleEnable;
   public final ColorSetting mhColor;
   public final ColorSetting sbgColor;
   public final ColorSetting shColor;
   public final ColorSetting bgColor;
   public static final FadeUtils fade = new FadeUtils(300L);
   int lastHeight;

   public UIModule() {
      super("UI", Module.Category.Client);
      this.page = this.add(new EnumSetting("Page", UIModule.Pages.General));
      this.animMode = this.add(new EnumSetting("AnimMode", AnimateUtil.AnimMode.Mio, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.height = this.add(new SliderSetting("Height", 16.0, 10.0, 20.0, 1.0, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.bindC = this.add((new ColorSetting("BindText", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      })).injectBoolean(true));
      this.gearColor = this.add((new ColorSetting("Gear", new Color(150, 150, 150), (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      })).injectBoolean(true));
      this.mode = this.add(new EnumSetting("EnableAnim", UIModule.Mode.Reset, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.scissor = this.add(new BooleanSetting("Scissor", true, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.snow = this.add(new BooleanSetting("Snow", false, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.animationSpeed = this.add(new SliderSetting("AnimationSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.booleanSpeed = this.add(new SliderSetting("BooleanSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.customFont = this.add(new BooleanSetting("CustomFont", false, (v) -> {
         return this.page.getValue() == UIModule.Pages.General;
      }));
      this.color = this.add(new ColorSetting("Main", new Color(140, 146, 255), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.mainHover = this.add(new ColorSetting("MainHover", new Color(186, 188, 252), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.categoryEnd = this.add((new ColorSetting("CategoryEnd", -2113929216, (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      })).injectBoolean(true));
      this.disableText = this.add(new ColorSetting("DisableText", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.enableText = this.add(new ColorSetting("EnableText", new Color(130, 135, 255), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.mbgColor = this.add(new ColorSetting("Module", new Color(63, 63, 63, 42), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.moduleEnd = this.add((new ColorSetting("ModuleEnd", -2113929216, (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      })).injectBoolean(true));
      this.moduleEnable = this.add(new ColorSetting("ModuleEnable", new Color(170, 182, 255), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.mhColor = this.add(new ColorSetting("ModuleHover", new Color(152, 152, 152, 123), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.sbgColor = this.add(new ColorSetting("Setting", new Color(24, 24, 24, 0), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.shColor = this.add(new ColorSetting("SettingHover", new Color(152, 152, 152, 123), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      this.bgColor = this.add(new ColorSetting("Background", new Color(24, 24, 24, 42), (v) -> {
         return this.page.getValue() == UIModule.Pages.Color;
      }));
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!(mc.currentScreen instanceof ClickGuiScreen)) {
         this.disable();
      }

   }

   public void onEnable() {
      Iterator var1;
      ClickGuiTab tab;
      Iterator var3;
      Component component;
      ModuleComponent moduleComponent;
      Iterator var6;
      Component settingComponent;
      if (this.lastHeight != this.height.getValueInt()) {
         var1 = Nullpoint.GUI.tabs.iterator();

         while(var1.hasNext()) {
            tab = (ClickGuiTab)var1.next();

            for(var3 = tab.getChildren().iterator(); var3.hasNext(); component.defaultHeight = this.height.getValueInt()) {
               component = (Component)var3.next();
               if (component instanceof ModuleComponent) {
                  moduleComponent = (ModuleComponent)component;

                  for(var6 = moduleComponent.getSettingsList().iterator(); var6.hasNext(); settingComponent.defaultHeight = this.height.getValueInt()) {
                     settingComponent = (Component)var6.next();
                     settingComponent.setHeight(this.height.getValueInt());
                  }
               }

               component.setHeight(this.height.getValueInt());
            }
         }

         this.lastHeight = this.height.getValueInt();
      }

      if (this.mode.getValue() == UIModule.Mode.Reset) {
         label59:
         for(var1 = Nullpoint.GUI.tabs.iterator(); var1.hasNext(); tab.currentHeight = 0.0) {
            tab = (ClickGuiTab)var1.next();
            var3 = tab.getChildren().iterator();

            while(true) {
               do {
                  if (!var3.hasNext()) {
                     continue label59;
                  }

                  component = (Component)var3.next();
                  component.currentOffset = 0.0;
               } while(!(component instanceof ModuleComponent));

               moduleComponent = (ModuleComponent)component;
               moduleComponent.isPopped = false;
               var6 = moduleComponent.getSettingsList().iterator();

               while(var6.hasNext()) {
                  settingComponent = (Component)var6.next();
                  settingComponent.currentOffset = 0.0;
                  if (settingComponent instanceof SliderComponent sliderComponent) {
                      sliderComponent.renderSliderPosition = 0.0;
                  } else if (settingComponent instanceof BooleanComponent booleanComponent) {
                      booleanComponent.currentWidth = 0.0;
                  } else if (settingComponent instanceof ColorComponents colorComponents) {
                      colorComponents.currentWidth = 0.0;
                  }
               }
            }
         }
      }

      fade.reset();
      if (nullCheck()) {
         this.disable();
      } else {
         mc.setScreen(GuiManager.clickGui);
      }
   }

   public void onDisable() {
      if (mc.currentScreen instanceof ClickGuiScreen) {
         mc.setScreen(null);
      }

   }

   private enum Pages {
      General,
      Color;

      // $FF: synthetic method
      private static Pages[] $values() {
         return new Pages[]{General, Color};
      }
   }

   public enum Mode {
      Scale,
      Pull,
      Scissor,
      Reset,
      None;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Scale, Pull, Scissor, Reset, None};
      }
   }
}

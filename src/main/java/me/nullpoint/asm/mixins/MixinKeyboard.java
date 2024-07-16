package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Keyboard.class})
public class MixinKeyboard implements Wrapper {
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(
      method = {"onKey"},
      at = {@At("HEAD")}
   )
   private void onKey(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      if (!(mc.currentScreen instanceof ClickGuiScreen) || action != 1 || !Nullpoint.MODULE.setBind(key)) {
         if (action == 1) {
            Nullpoint.MODULE.onKeyPressed(key);
         }

         if (action == 0) {
            Nullpoint.MODULE.onKeyReleased(key);
         }

      }
   }

   @Inject(
      method = {"onChar"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChar(long window, int codePoint, int modifiers, CallbackInfo ci) {
      if (window == this.client.getWindow().getHandle()) {
         Element element = this.client.currentScreen;
         if (element != null && this.client.getOverlay() == null) {
            if (Character.charCount(codePoint) == 1) {
               if (!Module.nullCheck() && Nullpoint.GUI != null && Nullpoint.GUI.isClickGuiOpen()) {
                  Nullpoint.MODULE.modules.forEach((module) -> {
                     module.getSettings().stream().filter((setting) -> {
                        return setting instanceof StringSetting;
                     }).map((setting) -> {
                        return (StringSetting)setting;
                     }).filter(StringSetting::isListening).forEach((setting) -> {
                        setting.charType((char)codePoint);
                     });
                  });
               }

               Screen.wrapScreenError(() -> {
                  element.charTyped((char)codePoint, modifiers);
               }, "charTyped event handler", element.getClass().getCanonicalName());
            } else {
               char[] var6 = Character.toChars(codePoint);
               char[] var8 = var6;
               int var9 = var6.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  char c = var8[var10];
                  if (!Module.nullCheck() && Nullpoint.GUI != null && Nullpoint.GUI.isClickGuiOpen()) {
                     Nullpoint.MODULE.modules.forEach((module) -> {
                        module.getSettings().stream().filter((setting) -> {
                           return setting instanceof StringSetting;
                        }).map((setting) -> {
                           return (StringSetting)setting;
                        }).filter(StringSetting::isListening).forEach((setting) -> {
                           setting.charType(c);
                        });
                     });
                  }

                  Screen.wrapScreenError(() -> {
                     element.charTyped(c, modifiers);
                  }, "charTyped event handler", element.getClass().getCanonicalName());
               }
            }
         }
      }

      ci.cancel();
   }
}

// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.asm.mixins;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={Window.class})
public class MixinWindow {
   @Redirect(method={"setIcon"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/util/Icons;getIcons(Lnet/minecraft/resource/ResourcePack;)Ljava/util/List;"))
   private List<InputSupplier<InputStream>> onSetIcon(Icons instance, ResourcePack resourcePack) throws IOException {
      InputStream stream16 = MixinWindow.class.getResourceAsStream("/assets/minecraft/icon.png");
      InputStream stream32 = MixinWindow.class.getResourceAsStream("/assets/minecraft/icon.png");
      if (stream16 == null || stream32 == null) {
         return instance.getIcons(resourcePack);
      }
      return List.of(() -> stream16, () -> stream32);
   }
}

package me.nullpoint.asm.mixins;

import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.modules.impl.miscellaneous.SilentDisconnect;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientCommonNetworkHandler.class})
public class MixinClientCommonNetworkHandler {
   @Inject(
      method = {"onDisconnected"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDisconnected(Text reason, CallbackInfo ci) {
      if (Wrapper.mc.player != null && Wrapper.mc.world != null && SilentDisconnect.INSTANCE.isOn()) {
         CommandManager.sendChatMessage("\u00a74[!] \u00a7cDisconnect! reason: \u00a77" + reason.getString());
         ci.cancel();
      }

   }
}

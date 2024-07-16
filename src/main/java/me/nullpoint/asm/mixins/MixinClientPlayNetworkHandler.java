package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.SendMessageEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class MixinClientPlayNetworkHandler {
   @Unique
   private boolean nullpoint_ignoreChatMessage;

   @Shadow
   public abstract void sendChatMessage(String var1);

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendChatMessage(String message, CallbackInfo ci) {
      if (!this.nullpoint_ignoreChatMessage) {
         if (message.startsWith(Nullpoint.PREFIX)) {
            Nullpoint.COMMAND.command(message.split(" "));
            ci.cancel();
         } else {
            SendMessageEvent event = new SendMessageEvent(message);
            Nullpoint.EVENT_BUS.post(event);
            if (event.isCancelled()) {
               ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
               this.nullpoint_ignoreChatMessage = true;
               this.sendChatMessage(event.message);
               this.nullpoint_ignoreChatMessage = false;
               ci.cancel();
            }
         }

      }
   }
}

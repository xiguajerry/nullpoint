package me.nullpoint.asm.mixins;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.mod.modules.impl.miscellaneous.ExceptionPatcher;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientConnection.class})
public class MixinClientConnection {
   @Inject(
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
   ordinal = 0
)},
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      cancellable = true
   )
   protected void onChannelRead(ChannelHandlerContext channelHandlerContext, Packet packet, CallbackInfo ci) {
      PacketEvent event = new PacketEvent.Receive(packet);
      Nullpoint.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
   shift = Shift.BEFORE
)},
      cancellable = true
   )
   private void onHandlePacket(ChannelHandlerContext channelHandlerContext, Packet packet, CallbackInfo ci) {
      PacketEvent event = new PacketEvent.Receive(packet);
      if (event.isCancelled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"send(Lnet/minecraft/network/packet/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacketPre(Packet packet, CallbackInfo info) {
      PacketEvent.Send event = new PacketEvent.Send(packet);
      Nullpoint.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         info.cancel();
      }

   }

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
      if (!(throwable instanceof TimeoutException) && !(throwable instanceof PacketEncoderException) && ExceptionPatcher.INSTANCE.isOn()) {
         if (ExceptionPatcher.INSTANCE.log.getValue()) {
            CommandManager.sendChatMessage("\u00a74[!] Caught exception: \u00a77" + throwable.getMessage());
         }

         ci.cancel();
      }

   }
}

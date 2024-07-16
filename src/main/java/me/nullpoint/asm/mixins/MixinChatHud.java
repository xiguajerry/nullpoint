package me.nullpoint.asm.mixins;

import java.util.HashMap;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.ReceiveMessageEvent;
import me.nullpoint.api.interfaces.IChatHud;
import me.nullpoint.api.interfaces.IChatHudLine;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatHud.class})
public abstract class MixinChatHud implements IChatHud {
   @Shadow
   @Final
   private List visibleMessages;
   @Shadow
   @Final
   private List messages;
   @Unique
   private int nullpoint_nextId = 0;
   @Unique
   private final HashMap map = new HashMap();
   @Unique
   private ChatHudLine.Visible last;

   @Shadow
   public abstract void addMessage(Text var1);

   public void nullpoint_nextgen_master$add(Text message, int id) {
      this.nullpoint_nextId = id;
      this.addMessage(message);
      this.nullpoint_nextId = 0;
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/List;add(ILjava/lang/Object;)V",
   ordinal = 0,
   shift = Shift.AFTER
)}
   )
   private void onAddMessageAfterNewChatHudLineVisible(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      ((IChatHudLine)this.visibleMessages.get(0)).nullpoint_nextgen_master$setId(this.nullpoint_nextId);
   }

   @Inject(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/util/List;add(ILjava/lang/Object;)V",
   ordinal = 1,
   shift = Shift.AFTER
)}
   )
   private void onAddMessageAfterNewChatHudLine(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      ((IChatHudLine)this.messages.get(0)).nullpoint_nextgen_master$setId(this.nullpoint_nextId);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"}
   )
   private void onAddMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh, CallbackInfo info) {
      if (this.nullpoint_nextId != 0) {
         this.visibleMessages.removeIf((msg) -> {
            return msg == null || ((IChatHudLine)msg).nullpoint_nextgen_master$getId() == this.nullpoint_nextId;
         });
         this.messages.removeIf((msg) -> {
            return msg == null || ((IChatHudLine)msg).nullpoint_nextgen_master$getId() == this.nullpoint_nextId;
         });
      }

   }

   @Redirect(
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;size()I",
   ordinal = 2,
   remap = false
)
   )
   public int chatLinesSize(List list) {
      return ChatSetting.INSTANCE.isOn() && ChatSetting.INSTANCE.infiniteChat.getValue() ? -2147483647 : list.size();
   }

   @Redirect(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"
)
   )
   private int drawStringWithShadow(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
      if (ChatSetting.chatMessage.containsKey(text) && ((StringVisitable)ChatSetting.chatMessage.get(text)).getString().startsWith("\u00a7(")) {
         return ChatSetting.INSTANCE.pulse.booleanValue ? TextUtil.drawStringPulse(drawContext, text, x, y, ColorUtil.injectAlpha(ChatSetting.INSTANCE.color.getValue(), color >> 24 & 255), ColorUtil.injectAlpha(ChatSetting.INSTANCE.pulse.getValue(), color >> 24 & 255), ChatSetting.INSTANCE.pulseSpeed.getValue(), ChatSetting.INSTANCE.pulseCounter.getValueInt()) : drawContext.drawTextWithShadow(textRenderer, text, x, y, ColorUtil.injectAlpha(ChatSetting.INSTANCE.color.getValue(), color >> 24 & 255).getRGB());
      } else {
         return drawContext.drawTextWithShadow(textRenderer, text, x, y, color);
      }
   }

   @ModifyArg(
      method = {"render"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;get(I)Ljava/lang/Object;",
   ordinal = 0,
   remap = false
)
   )
   private int get(int i) {
      this.last = (ChatHudLine.Visible)this.visibleMessages.get(i);
      if (this.last != null && !this.map.containsKey(this.last)) {
         this.map.put(this.last, (new FadeUtils(ChatSetting.INSTANCE.animateTime.getValueInt())).reset());
      }

      return i;
   }

   @Inject(
      method = {"render"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I",
   ordinal = 0,
   shift = Shift.BEFORE
)}
   )
   private void translate(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
      if (this.map.containsKey(this.last)) {
         context.getMatrices().translate(ChatSetting.INSTANCE.animateOffset.getValue() * (1.0 - ((FadeUtils)this.map.get(this.last)).getQuad((FadeUtils.Quad)ChatSetting.INSTANCE.animQuad.getValue())), 0.0, 0.0);
      }

   }

   @Inject(
      at = {@At("HEAD")},
      method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V"},
      cancellable = true
   )
   public void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
      ReceiveMessageEvent event = new ReceiveMessageEvent(message.getString());
      Nullpoint.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         ci.cancel();
      }

   }
}

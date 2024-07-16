package me.nullpoint.asm.mixins;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatInputSuggestor.class})
public abstract class MixinChatInputSuggestor {
   @Final
   @Shadow
   TextFieldWidget textField;
   @Shadow
   private CompletableFuture pendingSuggestions;
   @Final
   @Shadow
   private List messages;
   @Unique
   private boolean showOutline = false;

   @Shadow
   public abstract void show(boolean var1);

   @Inject(
      at = {@At("HEAD")},
      method = {"render"}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
      if (this.showOutline) {
         int x = this.textField.getX() - 3;
         int y = this.textField.getY() - 3;
         Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)y, (float)(this.textField.getWidth() + 1), 1.0F, ChatSetting.INSTANCE.color.getValue().getRGB());
         Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)(y + this.textField.getHeight() + 1), (float)(this.textField.getWidth() + 1), 1.0F, ChatSetting.INSTANCE.color.getValue().getRGB());
         Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)y, 1.0F, (float)(this.textField.getHeight() + 1), ChatSetting.INSTANCE.color.getValue().getRGB());
         Render2DUtil.drawRect(context.getMatrices(), (float)(x + this.textField.getWidth() + 1), (float)y, 1.0F, (float)(this.textField.getHeight() + 2), ChatSetting.INSTANCE.color.getValue().getRGB());
      }

   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I",
   ordinal = 0
)},
      method = {"refresh()V"}
   )
   private void onRefresh(CallbackInfo ci) {
      String prefix = Nullpoint.PREFIX;
      String string = this.textField.getText();
      this.showOutline = string.startsWith(prefix);
      if (string.length() > 0) {
         int cursorPos = this.textField.getCursor();
         String string2 = string.substring(0, cursorPos);
         if (prefix.startsWith(string2) || string2.startsWith(prefix)) {
            int j = 0;

            for(Matcher matcher = Pattern.compile("(\\s+)").matcher(string2); matcher.find(); j = matcher.end()) {
            }

            SuggestionsBuilder builder = new SuggestionsBuilder(string2, j);
            if (string2.length() < prefix.length()) {
               if (!prefix.startsWith(string2)) {
                  return;
               }

               builder.suggest(prefix);
            } else {
               if (!string2.startsWith(prefix)) {
                  return;
               }

               int count = StringUtils.countMatches(string2, " ");
               List seperated = Arrays.asList(string2.split(" "));
               if (count == 0) {
                  Object[] var11 = Nullpoint.COMMAND.getCommands().keySet().toArray();
                  int var12 = var11.length;

                  for(int var13 = 0; var13 < var12; ++var13) {
                     Object strObj = var11[var13];
                     String str = (String)strObj;
                     builder.suggest(Nullpoint.PREFIX + str + " ");
                  }
               } else {
                  if (seperated.size() < 1) {
                     return;
                  }

                  Command c = Nullpoint.COMMAND.getCommandBySyntax(((String)seperated.get(0)).substring(prefix.length()));
                  if (c == null) {
                     this.messages.add(Text.of("\u00a7cno commands found: \u00a7e" + ((String)seperated.get(0)).substring(prefix.length())).asOrderedText());
                     return;
                  }

                  String[] suggestions = c.getAutocorrect(count, seperated);
                  if (suggestions == null || suggestions.length == 0) {
                     return;
                  }

                  String[] var19 = suggestions;
                  int var20 = suggestions.length;

                  for(int var21 = 0; var21 < var20; ++var21) {
                     String str = var19[var21];
                     builder.suggest(str + " ");
                  }
               }
            }

            this.pendingSuggestions = builder.buildFuture();
            this.show(false);
         }
      }

   }
}

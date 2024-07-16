package me.nullpoint.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Iterator;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.mod.modules.impl.miscellaneous.ShulkerViewer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HandledScreen.class})
public abstract class MixinHandledScreen extends Screen implements ScreenHandlerProvider {
   @Shadow
   protected @Nullable Slot focusedSlot;
   @Shadow
   protected int x;
   @Shadow
   protected int y;

   protected MixinHandledScreen(Text title) {
      super(title);
   }

   @Inject(
      method = {"render"},
      at = {@At("TAIL")}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty() && this.client.player.playerScreenHandler.getCursorStack().isEmpty() && this.hasItems(this.focusedSlot.getStack()) && ShulkerViewer.INSTANCE.isOn()) {
         this.renderShulkerToolTip(context, mouseX, mouseY, this.focusedSlot.getStack());
      }

   }

   public void renderShulkerToolTip(DrawContext context, int mouseX, int mouseY, ItemStack stack) {
      try {
         NbtCompound compoundTag = stack.getSubNbt("BlockEntityTag");
         DefaultedList itemStacks = DefaultedList.ofSize(27, ItemStack.EMPTY);
         Inventories.readNbt(compoundTag, itemStacks);
         this.draw(context, itemStacks, mouseX, mouseY);
      } catch (Exception var7) {
      }

   }

   private void draw(DrawContext context, DefaultedList itemStacks, int mouseX, int mouseY) {
      RenderSystem.disableDepthTest();
      GL11.glClear(256);
      mouseX += 8;
      mouseY -= 82;
      this.drawBackground(context, mouseX, mouseY);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      DiffuseLighting.enableGuiDepthLighting();
      int row = 0;
      int i = 0;
      Iterator var7 = itemStacks.iterator();

      while(var7.hasNext()) {
         ItemStack itemStack = (ItemStack)var7.next();
         context.drawItem(itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
         context.drawItemInSlot(Wrapper.mc.textRenderer, itemStack, mouseX + 8 + i * 18, mouseY + 7 + row * 18);
         ++i;
         if (i >= 9) {
            i = 0;
            ++row;
         }
      }

      DiffuseLighting.disableGuiDepthLighting();
      RenderSystem.enableDepthTest();
   }

   private void drawBackground(DrawContext context, int x, int y) {
      Render2DUtil.drawRect(context.getMatrices(), (float)x, (float)y, 176.0F, 67.0F, new Color(0, 0, 0, 120));
   }

   private boolean hasItems(ItemStack itemStack) {
      NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
      return compoundTag != null && compoundTag.contains("Items", 9);
   }
}

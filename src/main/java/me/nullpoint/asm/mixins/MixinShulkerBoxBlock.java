package me.nullpoint.asm.mixins;

import java.util.List;
import me.nullpoint.mod.modules.impl.miscellaneous.ShulkerViewer;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ShulkerBoxBlock.class})
public class MixinShulkerBoxBlock {
   @Inject(
      method = {"appendTooltip"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onAppendTooltip(ItemStack stack, BlockView view, List tooltip, TooltipContext options, CallbackInfo info) {
      if (ShulkerViewer.INSTANCE.isOn()) {
         info.cancel();
      }

   }
}

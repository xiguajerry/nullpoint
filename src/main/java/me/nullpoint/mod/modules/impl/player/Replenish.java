package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Replenish extends Module {
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 0.5, 0.0, 4.0, 0.01));
   private final SliderSetting min = this.add(new SliderSetting("Min", 16, 1, 64));
   private final Timer timer = new Timer();

   public Replenish() {
      super("Replenish", Module.Category.Player);
   }

   public void onUpdate() {
      if (mc.currentScreen == null || mc.currentScreen instanceof ClickGuiScreen) {
         if (this.timer.passedMs((long)(this.delay.getValue() * 1000.0))) {
            for(int i = 0; i < 9; ++i) {
               if (this.replenish(i)) {
                  this.timer.reset();
                  return;
               }
            }

         }
      }
   }

   private boolean replenish(int slot) {
      ItemStack stack = mc.player.getInventory().getStack(slot);
      if (stack.isEmpty()) {
         return false;
      } else if (!stack.isStackable()) {
         return false;
      } else if ((double)stack.getCount() >= this.min.getValue()) {
         return false;
      } else if (stack.getCount() == stack.getMaxCount()) {
         return false;
      } else {
         for(int i = 9; i < 36; ++i) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (!item.isEmpty() && this.canMerge(stack, item)) {
               mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
               return true;
            }
         }

         return false;
      }
   }

   private boolean canMerge(ItemStack source, ItemStack stack) {
      return source.getItem() == stack.getItem() && source.getName().equals(stack.getName());
   }
}

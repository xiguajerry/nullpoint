package me.nullpoint.mod.modules.impl.miscellaneous;

import java.util.Iterator;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Beta
public class BedCrafter extends Module {
   public static BedCrafter INSTANCE;
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
   private final SliderSetting range = this.add(new SliderSetting("Range", 5, 0, 8));
   private final SliderSetting beds = this.add(new SliderSetting("Beds", 5, 1, 30));
   private final BooleanSetting disable = this.add(new BooleanSetting("Disable", true));
   boolean open = false;

   public BedCrafter() {
      super("BedCrafter", Module.Category.Misc);
      INSTANCE = this;
   }

   public void onDisable() {
      this.open = false;
   }

   public void onUpdate() {
      if (getEmptySlots() == 0) {
         if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
            mc.player.closeHandledScreen();
         }

         if (this.disable.getValue()) {
            this.disable();
         }

      } else {
         if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
            this.open = true;
            boolean craft = false;
            Iterator var2 = mc.player.getRecipeBook().getOrderedResults().iterator();

            while(true) {
               label56:
               while(var2.hasNext()) {
                  RecipeResultCollection recipeResult = (RecipeResultCollection)var2.next();
                  Iterator var4 = recipeResult.getRecipes(true).iterator();

                  while(var4.hasNext()) {
                     RecipeEntry recipe = (RecipeEntry)var4.next();
                     if (recipe.value().getResult(mc.world.getRegistryManager()).getItem() instanceof BedItem) {
                        int bed = 0;

                        for(int i = 0; i < getEmptySlots(); ++i) {
                           craft = true;
                           if (bed >= this.beds.getValueInt()) {
                              continue label56;
                           }

                           ++bed;
                           mc.interactionManager.clickRecipe(mc.player.currentScreenHandler.syncId, recipe, false);
                           mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 0, 1, SlotActionType.QUICK_MOVE, mc.player);
                        }
                        break;
                     }
                  }
               }

               if (!craft) {
                  if (mc.player.currentScreenHandler instanceof CraftingScreenHandler) {
                     mc.player.closeHandledScreen();
                  }

                  if (this.disable.getValue()) {
                     this.disable();
                  }
               }
               break;
            }
         } else {
            if (this.disable.getValue() && this.open) {
               this.disable();
               return;
            }

            this.doPlace();
         }

      }
   }

   private void doPlace() {
      BlockPos bestPos = null;
      double distance = 100.0;
      boolean place = true;
      Iterator var5 = BlockUtil.getSphere(this.range.getValueFloat()).iterator();

      while(var5.hasNext()) {
         BlockPos pos = (BlockPos)var5.next();
         if (mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE && BlockUtil.getClickSideStrict(pos) != null) {
            place = false;
            bestPos = pos;
            break;
         }

         if (BlockUtil.canPlace(pos) && (bestPos == null || (double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos())) < distance)) {
            bestPos = pos;
            distance = MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos()));
         }
      }

      if (bestPos != null) {
         if (!place) {
            BlockUtil.clickBlock(bestPos, BlockUtil.getClickSide(bestPos), this.rotate.getValue());
         } else {
            if (InventoryUtil.findItem(Item.fromBlock(Blocks.CRAFTING_TABLE)) == -1) {
               return;
            }

            int old = mc.player.getInventory().selectedSlot;
            InventoryUtil.switchToSlot(InventoryUtil.findItem(Item.fromBlock(Blocks.CRAFTING_TABLE)));
            BlockUtil.placeBlock(bestPos, this.rotate.getValue());
            InventoryUtil.switchToSlot(old);
         }
      }

   }

   public static int getEmptySlots() {
      int emptySlots = 0;

      for(int i = 0; i < 36; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack == null || itemStack.getItem() instanceof AirBlockItem) {
            ++emptySlots;
         }
      }

      return emptySlots;
   }
}

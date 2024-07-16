package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import java.util.List;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;

public class Quiver extends Module {
   public static Quiver INSTANCE;
   private final BooleanSetting smart = this.add(new BooleanSetting("Smart", true));

   public Quiver() {
      super("Quiver", Module.Category.Combat);
      INSTANCE = this;
   }

   @EventHandler(
      priority = -101
   )
   public void onRotate(RotateEvent event) {
      if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() instanceof BowItem) {
         if (!this.smart.getValue()) {
            event.setPitch(-90.0F);
         } else {
            boolean rotate = false;

            label48:
            for(int i = 9; i < 45; ++i) {
               ItemStack stack = mc.player.getInventory().getStack(i);
               if (stack.getItem() == Items.ARROW) {
                  rotate = false;
               } else if (stack.getItem() == Items.SPECTRAL_ARROW) {
                  rotate = false;
               } else if (stack.getItem() == Items.TIPPED_ARROW) {
                  boolean good = false;
                  List effects = PotionUtil.getPotionEffects(stack);
                  Iterator var7 = effects.iterator();

                  while(true) {
                     StatusEffectInstance effect;
                     do {
                        if (!var7.hasNext()) {
                           rotate = good;
                           continue label48;
                        }

                        effect = (StatusEffectInstance)var7.next();
                     } while(effect.getEffectType() != StatusEffects.SPEED && effect.getEffectType() != StatusEffects.STRENGTH);

                     good = true;
                  }
               }
            }

            if (rotate) {
               event.setPitch(-90.0F);
            }
         }
      }

   }
}

package me.nullpoint.mod.modules.impl.movement;

import java.util.Iterator;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.TravelEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ElytraFlyBypass extends Module {
   public static ElytraFlyBypass INSTANCE;
   private boolean hasElytra = false;
   public SliderSetting factor = this.add(new SliderSetting("UpPitch", 1.2999999523162842, 0.10000000149011612, 4.0));

   public ElytraFlyBypass() {
      super("ElytraFlyBypass", Module.Category.Movement);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         for(Iterator var1 = mc.player.getArmorItems().iterator(); var1.hasNext(); this.hasElytra = false) {
            ItemStack is = (ItemStack)var1.next();
            if (is.getItem() instanceof ElytraItem) {
               this.hasElytra = true;
               break;
            }
         }

      }
   }

   @EventHandler
   public void onMove(TravelEvent event) {
      if (!nullCheck() && this.hasElytra && mc.player.isFallFlying()) {
         float yaw = (float)Math.toRadians(mc.player.getYaw());
         mc.player.addVelocity((double)(-MathHelper.sin(yaw)) * this.factor.getValue() / 10.0, 0.0, (double)MathHelper.cos(yaw) * this.factor.getValue() / 10.0);
      }
   }
}

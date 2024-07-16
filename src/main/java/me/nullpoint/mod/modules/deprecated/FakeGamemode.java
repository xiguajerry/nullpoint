package me.nullpoint.mod.modules.deprecated;

import me.nullpoint.mod.modules.Module;
import net.minecraft.world.GameMode;

/** @deprecated */
@Deprecated
public class FakeGamemode extends Module {
   boolean set = false;

   public FakeGamemode() {
      super("FakeGamemode", Module.Category.Player);
   }

   public void onEnable() {
      if (!nullCheck()) {
         if (mc.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) {
            this.set = true;
            mc.interactionManager.setGameMode(GameMode.CREATIVE);
         }
      }
   }

   public void onDisable() {
      if (this.set) {
         this.set = false;
         if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
            mc.interactionManager.setGameMode(mc.interactionManager.getPreviousGameMode());
         }
      }

   }
}

package me.nullpoint.asm.mixins;

import me.nullpoint.api.interfaces.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ChatHudLine.class})
public abstract class MixinChatHudLine implements IChatHudLine {
   @Unique
   private int id = 0;

   public int nullpoint_nextgen_master$getId() {
      return this.id;
   }

   public void nullpoint_nextgen_master$setId(int id) {
      this.id = id;
   }
}

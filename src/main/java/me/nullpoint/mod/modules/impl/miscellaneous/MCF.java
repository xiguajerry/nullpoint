package me.nullpoint.mod.modules.impl.miscellaneous;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.FriendManager;
import me.nullpoint.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

public class MCF extends Module {
   public static MCF INSTANCE;
   boolean click = false;

   public MCF() {
      super("MCF", Module.Category.Misc);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (mc.mouse.wasMiddleButtonClicked()) {
            if (!this.click) {
               this.onClick();
            }

            this.click = true;
         } else {
            this.click = false;
         }

      }
   }

   private void onClick() {
      HitResult result = mc.crosshairTarget;
      if (result != null && result.getType() == Type.ENTITY && result instanceof EntityHitResult entityHitResult) {
         Entity var4 = entityHitResult.getEntity();
         if (var4 instanceof PlayerEntity entity) {
            FriendManager var10000 = Nullpoint.FRIEND;
            Formatting var5;
            if (FriendManager.isFriend(entity.getName().getString())) {
               var10000 = Nullpoint.FRIEND;
               FriendManager.removeFriend(entity.getName().getString());
               var5 = Formatting.RED;
               CommandManager.sendChatMessage(var5 + entity.getName().getString() + Formatting.RED + " has been unfriended.");
            } else {
               Nullpoint.FRIEND.addFriend(entity.getName().getString());
               var5 = Formatting.AQUA;
               CommandManager.sendChatMessage(var5 + entity.getName().getString() + Formatting.GREEN + " has been friended.");
            }
         }
      }

      this.click = true;
   }
}

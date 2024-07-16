package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class SilentDouble extends Module {
   public static int slotMain;
   public static int swithc2;
   public static SilentDouble INSTANCE;

   public SilentDouble() {
      super("SilentDouble", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onUpdate() {
      this.update();
   }

   public void update() {
      if (!SpeedMine.INSTANCE.isOn()) {
         CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oAutoMine?");
         this.disable();
      } else {
         if (SpeedMine.secondPos != null && !SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos) == -1 ? mc.player.getInventory().selectedSlot : SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 0.89))) {
            slotMain = mc.player.getInventory().selectedSlot;
         }

         if (SpeedMine.secondPos != null && SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 0.9))) {
            if (mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
               if (!mc.options.useKey.isPressed()) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(SpeedMine.INSTANCE.getTool(SpeedMine.secondPos)));
                  swithc2 = 1;
               } else if (swithc2 == 1) {
                  mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotMain));
                  EntityUtil.syncInventory();
               }
            } else {
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(SpeedMine.INSTANCE.getTool(SpeedMine.secondPos)));
               swithc2 = 1;
            }
         }

         if (SpeedMine.secondPos != null && SpeedMine.INSTANCE.secondTimer.passed(SpeedMine.INSTANCE.getBreakTime(SpeedMine.secondPos, SpeedMine.INSTANCE.getTool(SpeedMine.secondPos), 1.2)) && swithc2 == 1) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotMain));
            EntityUtil.syncInventory();
         }

      }
   }
}

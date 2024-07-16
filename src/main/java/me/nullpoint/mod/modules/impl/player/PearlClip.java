package me.nullpoint.mod.modules.impl.player;

import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PearlClip extends Module {
   public static PearlClip INSTANCE;
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   public final BooleanSetting autoYaw = this.add(new BooleanSetting("AutoYaw", true));

   public PearlClip() {
      super("PearlClip", Module.Category.Misc);
      INSTANCE = this;
   }

   public void onEnable() {
      if (nullCheck()) {
         this.disable();
      } else {
         Vec3d targetPos = new Vec3d(mc.player.getX() + MathHelper.clamp(this.roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.241, Math.floor(mc.player.getX()) + 0.759) - mc.player.getX(), -0.03, 0.03), mc.player.getY(), mc.player.getZ() + MathHelper.clamp(this.roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.241, Math.floor(mc.player.getZ()) + 0.759) - mc.player.getZ(), -0.03, 0.03));
         this.doPearl(this.autoYaw.getValue() ? EntityUtil.getLegitRotations(targetPos)[0] : mc.player.getYaw(), 80.0F);
         this.disable();
      }
   }

   public void doPearl(float yaw, float pitch) {
      if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
         EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
      } else {
         int pearl;
         if ((pearl = this.findItem(Items.ENDER_PEARL)) != -1) {
            int old = mc.player.getInventory().selectedSlot;
            this.doSwap(pearl);
            EntityUtil.sendLook(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround()));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
            if (this.inventory.getValue()) {
               this.doSwap(pearl);
               EntityUtil.syncInventory();
            } else {
               this.doSwap(old);
            }
         }
      }

   }

   private double roundToClosest(double num, double low, double high) {
      double d1 = num - low;
      double d2 = high - num;
      return d2 > d1 ? low : high;
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   public int findItem(Item item) {
      return this.inventory.getValue() ? InventoryUtil.findItemInventorySlot(item) : InventoryUtil.findItem(item);
   }
}

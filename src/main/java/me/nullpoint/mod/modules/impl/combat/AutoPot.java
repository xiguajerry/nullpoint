package me.nullpoint.mod.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AutoPot extends Module {
   private final BooleanSetting rotate = this.add((new BooleanSetting("Rotate", true)).setParent());
   private final SliderSetting pitch = this.add(new SliderSetting("Pitch", 86, 80, 90, (v) -> {
      return this.rotate.isOpen();
   }));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   public final SliderSetting delay = this.add(new SliderSetting("Delay", 1050, 0, 2000));
   private final BooleanSetting hcehck = this.add((new BooleanSetting("HealthCheck", false)).setParent());
   public final SliderSetting health = this.add((new SliderSetting("Health", 20, 0, 36, (v) -> {
      return this.hcehck.isOpen();
   })).setSuffix("HP"));
   public final SliderSetting effectRange = this.add(new SliderSetting("EffectRange", 3.0, 0.0, 6.0, 0.1));
   private final SliderSetting predictTicks = this.add((new SliderSetting("Predict", 2, 0, 10)).setSuffix("ticks"));
   public final BooleanSetting debug = this.add(new BooleanSetting("debug", false));
   private final Timer timer = new Timer();

   public AutoPot() {
      super("AutoPot", Module.Category.Combat);
   }

   public String getInfo() {
      return String.valueOf(InventoryUtil.getPotCount(StatusEffects.RESISTANCE));
   }

   public void onUpdate() {
      if (this.timer.passedMs(this.delay.getValueInt())) {
         if (!this.hcehck.getValue() || !((double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) >= this.health.getValue())) {
            if (!(mc.player.getPos().add(CombatUtil.getMotionVec(mc.player, (float)this.predictTicks.getValueInt(), true)).squaredDistanceTo(this.calcTrajectory(Items.SPLASH_POTION, Nullpoint.ROTATE.rotateYaw, this.pitch.getValueFloat())) > this.effectRange.getValue())) {
               List effects = new ArrayList(mc.player.getStatusEffects());
               Iterator var2 = effects.iterator();

               StatusEffectInstance potionEffect;
               do {
                  if (!var2.hasNext()) {
                     this.doPot();
                     return;
                  }

                  potionEffect = (StatusEffectInstance)var2.next();
               } while(potionEffect.getEffectType() != StatusEffects.RESISTANCE || potionEffect.getAmplifier() + 1 <= 1);

            }
         }
      }
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.debug.getValue()) {
         Vec3d pos = this.calcTrajectory(Items.SPLASH_POTION, Nullpoint.ROTATE.rotateYaw, this.pitch.getValueFloat());
         Render3DUtil.draw3DBox(matrixStack, new Box(new BlockPosX(pos.x, pos.y, pos.z)), new Color(255, 255, 255, 80));
         Render3DUtil.draw3DBox(matrixStack, ((IEntity)mc.player).getDimensions().getBoxAt(mc.player.getPos().add(CombatUtil.getMotionVec(mc.player, (float)this.predictTicks.getValueInt(), true))).expand(0.0, 0.1, 0.0), new Color(0, 255, 255, 80), false, true);
      }

   }

   private void doPot() {
      int oldSlot = mc.player.getInventory().selectedSlot;
      int slot = this.findPot(StatusEffects.RESISTANCE);
      if (slot == -1) {
         CommandManager.sendChatMessage("\u00a7c[!] No Potion found");
         this.disable();
      } else {
         this.timer.reset();
         this.doSwap(slot);
         if (this.rotate.getValue()) {
            EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, this.pitch.getValueFloat());
         }

         mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, EntityUtil.getWorldActionId(mc.world)));
         if (this.inventory.getValue()) {
            this.doSwap(slot);
            EntityUtil.syncInventory();
         } else {
            this.doSwap(oldSlot);
         }

      }
   }

   public int findPot(StatusEffect statusEffect) {
      return this.inventory.getValue() ? InventoryUtil.findPotInventorySlot(statusEffect) : InventoryUtil.findPot(statusEffect);
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private Vec3d calcTrajectory(Item item, float yaw, float pitch) {
      double x = MathUtil.interpolate(mc.player.prevX, mc.player.getX(), mc.getTickDelta());
      double y = MathUtil.interpolate(mc.player.prevY, mc.player.getY(), mc.getTickDelta());
      double z = MathUtil.interpolate(mc.player.prevZ, mc.player.getZ(), mc.getTickDelta());
      y = y + (double)mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
      x -= MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F;
      z -= MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F;
      float maxDist = this.getDistance(item);
      double motionX = -MathHelper.sin(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F) * maxDist;
      double motionY = -MathHelper.sin((pitch - (float)this.getThrowPitch(item)) / 180.0F * 3.141593F) * maxDist;
      double motionZ = MathHelper.cos(yaw / 180.0F * 3.1415927F) * MathHelper.cos(pitch / 180.0F * 3.1415927F) * maxDist;
      float power = (float)mc.player.getItemUseTime() / 20.0F;
      power = (power * power + power * 2.0F) / 3.0F;
      if (power > 1.0F) {
         power = 1.0F;
      }

      float distance = MathHelper.sqrt((float)(motionX * motionX + motionY * motionY + motionZ * motionZ));
      motionX /= distance;
      motionY /= distance;
      motionZ /= distance;
      float pow = (item instanceof BowItem ? power * 2.0F : (item instanceof CrossbowItem ? 2.2F : 1.0F)) * this.getThrowVelocity(item);
      motionX *= pow;
      motionY *= pow;
      motionZ *= pow;
      if (!mc.player.isOnGround()) {
         motionY += mc.player.getVelocity().getY();
      }

      for(int i = 0; i < 300; ++i) {
         Vec3d lastPos = new Vec3d(x, y, z);
         x += motionX;
         y += motionY;
         z += motionZ;
         if (mc.world.getBlockState(new BlockPos((int)x, (int)y, (int)z)).getBlock() == Blocks.WATER) {
            motionX *= 0.8;
            motionY *= 0.8;
            motionZ *= 0.8;
         } else {
            motionX *= 0.99;
            motionY *= 0.99;
            motionZ *= 0.99;
         }

         if (item instanceof BowItem) {
            motionY -= 0.05000000074505806;
         } else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) {
            motionY -= 0.05000000074505806;
         } else {
            motionY -= 0.029999999329447746;
         }

         Vec3d pos = new Vec3d(x, y, z);
         BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
         if (bhr != null && bhr.getType() == Type.BLOCK) {
            return bhr.getPos();
         }
      }

      return null;
   }

   private float getDistance(Item item) {
      return item instanceof BowItem ? 1.0F : 0.4F;
   }

   private float getThrowVelocity(Item item) {
      if (!(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
         if (item instanceof ExperienceBottleItem) {
            return 0.59F;
         } else {
            return item instanceof TridentItem ? 2.0F : 1.5F;
         }
      } else {
         return 0.5F;
      }
   }

   private int getThrowPitch(Item item) {
      return !(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem) && !(item instanceof ExperienceBottleItem) ? 0 : 20;
   }
}

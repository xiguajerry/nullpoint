package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.Iterator;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
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

public class Trajectories extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(575714484)));
   private final ColorSetting lcolor = this.add(new ColorSetting("LandedColor", new Color(575714484)));

   public Trajectories() {
      super("Trajectories", Module.Category.Render);
   }

   private boolean isThrowable(Item item) {
      return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
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

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (!nullCheck()) {
         ItemStack mainHand = mc.player.getMainHandStack();
         ItemStack offHand = mc.player.getOffHandStack();
         Hand hand;
         if (!(mainHand.getItem() instanceof BowItem) && !(mainHand.getItem() instanceof CrossbowItem) && !this.isThrowable(mainHand.getItem())) {
            if (!(offHand.getItem() instanceof BowItem) && !(offHand.getItem() instanceof CrossbowItem) && !this.isThrowable(offHand.getItem())) {
               return;
            }

            hand = Hand.OFF_HAND;
         } else {
            hand = Hand.MAIN_HAND;
         }

         boolean prev_bob = mc.options.getBobView().getValue();
         mc.options.getBobView().setValue(false);
         if (mainHand.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(Enchantments.MULTISHOT, mainHand) != 0) {
            this.calcTrajectory(matrixStack, hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() - 10.0F);
            this.calcTrajectory(matrixStack, hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw());
            this.calcTrajectory(matrixStack, hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw() + 10.0F);
         } else {
            this.calcTrajectory(matrixStack, hand == Hand.OFF_HAND ? offHand.getItem() : mainHand.getItem(), mc.player.getYaw());
         }

         mc.options.getBobView().setValue(prev_bob);
      }
   }

   private void calcTrajectory(MatrixStack matrixStack, Item item, float yaw) {
      double x = MathUtil.interpolate(mc.player.prevX, mc.player.getX(), mc.getTickDelta());
      double y = MathUtil.interpolate(mc.player.prevY, mc.player.getY(), mc.getTickDelta());
      double z = MathUtil.interpolate(mc.player.prevZ, mc.player.getZ(), mc.getTickDelta());
      y = y + (double)mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161;
      if (item == mc.player.getMainHandStack().getItem()) {
         x -= MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F;
         z -= MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F;
      } else {
         x += MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F;
         z += MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F;
      }

      float maxDist = this.getDistance(item);
      double motionX = -MathHelper.sin(yaw / 180.0F * 3.1415927F) * MathHelper.cos(mc.player.getPitch() / 180.0F * 3.1415927F) * maxDist;
      double motionY = -MathHelper.sin((mc.player.getPitch() - (float)this.getThrowPitch(item)) / 180.0F * 3.141593F) * maxDist;
      double motionZ = MathHelper.cos(yaw / 180.0F * 3.1415927F) * MathHelper.cos(mc.player.getPitch() / 180.0F * 3.1415927F) * maxDist;
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
         Iterator var23 = mc.world.getEntities().iterator();

         while(var23.hasNext()) {
            Entity ent = (Entity)var23.next();
            if (!(ent instanceof ArrowEntity) && !ent.equals(mc.player) && ent.getBoundingBox().intersects(new Box(x - 0.4, y - 0.4, z - 0.4, x + 0.4, y + 0.4, z + 0.4))) {
               Render3DUtil.draw3DBox(matrixStack, ent.getBoundingBox(), this.lcolor.getValue());
               break;
            }
         }

         BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
         if (bhr != null && bhr.getType() == Type.BLOCK) {
            Render3DUtil.draw3DBox(matrixStack, new Box(bhr.getBlockPos()), this.lcolor.getValue());
            break;
         }

         if (y <= -65.0) {
            break;
         }

         if (motionX != 0.0 || motionY != 0.0 || motionZ != 0.0) {
            Render3DUtil.drawLine((float)lastPos.x, (float)lastPos.y, (float)lastPos.z, (float)x, (float)y, (float)z, this.color.getValue(), 2.0F);
         }
      }

   }
}

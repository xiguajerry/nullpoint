package me.nullpoint.api.utils.world;

import me.nullpoint.api.utils.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class InteractUtil implements Wrapper {
   public static HitResult getRtxTarget(float yaw, float pitch, double x, double y, double z) {
      HitResult result = rayTrace(5.0, yaw, pitch, x, y, z);
      Vec3d vec3d = (new Vec3d(x, y, z)).add(0.0, mc.player.getEyeHeight(mc.player.getPose()), 0.0);
      double distancePow2 = 25.0;
      if (result != null) {
         distancePow2 = result.getPos().squaredDistanceTo(vec3d);
      }

      Vec3d vec3d2 = getRotationVector(yaw, pitch);
      Vec3d vec3d3 = vec3d.add(vec3d2.x * 5.0, vec3d2.y * 5.0, vec3d2.z * 5.0);
      Box box = (new Box(x - 0.3, y, z - 0.3, x + 0.3, y + (double)mc.player.getEyeHeight(mc.player.getPose()), z + 0.3)).stretch(vec3d2.multiply(5.0)).expand(1.0, 1.0, 1.0);
      EntityHitResult entityHitResult = ProjectileUtil.raycast(mc.player, vec3d, vec3d3, box, (entity) -> {
         return !entity.isSpectator() && entity.canHit();
      }, distancePow2);
      if (entityHitResult != null) {
         Entity entity2 = entityHitResult.getEntity();
         Vec3d vec3d4 = entityHitResult.getPos();
         double g = vec3d.squaredDistanceTo(vec3d4);
         if ((g < distancePow2 || result == null) && entity2 instanceof LivingEntity) {
            return entityHitResult;
         }
      }

      return result;
   }

   public static HitResult rayTrace(double dst, float yaw, float pitch, double x, double y, double z) {
      Vec3d vec3d = new Vec3d(x, y, z);
      Vec3d vec3d2 = getRotationVector(pitch, yaw);
      Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
      return mc.world.raycast(new RaycastContext(vec3d, vec3d3, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
   }

   private static Vec3d getRotationVector(float yaw, float pitch) {
      return new Vec3d(MathHelper.sin(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F), -MathHelper.sin(yaw * 0.017453292F), MathHelper.cos(-pitch * 0.017453292F) * MathHelper.cos(yaw * 0.017453292F));
   }
}

package me.nullpoint.api.utils.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.FriendManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.SwingSide;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CombatUtil implements Wrapper {
   public static boolean terrainIgnore = false;
   public static BlockPos modifyPos;
   public static BlockState modifyBlockState;
   public static final Timer breakTimer;

   public static List<PlayerEntity> getEnemies(double range) {
      List list = new ArrayList();
      Iterator var3 = mc.world.getPlayers().iterator();

      while(var3.hasNext()) {
         PlayerEntity player = (PlayerEntity)var3.next();
         if (isValid(player, range)) {
            list.add(player);
         }
      }

      return list;
   }

   public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
      Iterator var3 = mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos)).iterator();
      if (var3.hasNext()) {
         EndCrystalEntity entity = (EndCrystalEntity)var3.next();
         attackCrystal(entity, rotate, eatingPause);
      }

   }

   public static void attackCrystal(Box box, boolean rotate, boolean eatingPause) {
      Iterator var3 = mc.world.getNonSpectatingEntities(EndCrystalEntity.class, box).iterator();
      if (var3.hasNext()) {
         EndCrystalEntity entity = (EndCrystalEntity)var3.next();
         attackCrystal(entity, rotate, eatingPause);
      }

   }

   public static void attackCrystal(Entity crystal, boolean rotate, boolean usingPause) {
      if (breakTimer.passedMs((long)(CombatSetting.INSTANCE.attackDelay.getValue() * 1000.0))) {
         if (!usingPause || !EntityUtil.isUsing()) {
            if (crystal != null) {
               breakTimer.reset();
               mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
               mc.player.resetLastAttackedTicks();
               EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
               if (rotate && CombatSetting.INSTANCE.attackRotate.getValue()) {
                  EntityUtil.faceVector(new Vec3d(crystal.getX(), crystal.getY() + 0.25, crystal.getZ()));
               }
            }

         }
      }
   }

   public static boolean isValid(Entity entity, double range) {
      boolean var4;
      label27: {
         if (entity != null && entity.isAlive() && !entity.equals(mc.player)) {
            label23: {
               if (entity instanceof PlayerEntity) {
                  FriendManager var10000 = Nullpoint.FRIEND;
                  if (FriendManager.isFriend(entity.getName().getString())) {
                     break label23;
                  }
               }

               if (!(mc.player.squaredDistanceTo(entity) > MathUtil.square(range))) {
                  var4 = false;
                  break label27;
               }
            }
         }

         var4 = true;
      }

      boolean invalid = var4;
      return !invalid;
   }

   public static BlockPos getHole(float range, boolean doubleHole, boolean any) {
      BlockPos bestPos = null;
      double bestDistance = range + 1.0F;
      Iterator var6 = BlockUtil.getSphere(range).iterator();

      while(true) {
         BlockPos pos;
         double distance;
         do {
            do {
               do {
                  if (!var6.hasNext()) {
                     return bestPos;
                  }

                  pos = (BlockPos)var6.next();
               } while(!BlockUtil.isHole(pos, true, true, any) && (!doubleHole || !isDoubleHole(pos)));
            } while(pos.getY() - mc.player.getBlockY() > 1);

            distance = MathHelper.sqrt((float)mc.player.squaredDistanceTo((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5));
         } while(bestPos != null && !(distance < bestDistance));

         bestPos = pos;
         bestDistance = distance;
      }
   }

   public static boolean isDoubleHole(BlockPos pos) {
      Direction unHardFacing = is3Block(pos);
      if (unHardFacing != null) {
         pos = pos.offset(unHardFacing);
         unHardFacing = is3Block(pos);
         return unHardFacing != null;
      } else {
         return false;
      }
   }

   public static Direction is3Block(BlockPos pos) {
      if (!isHard(pos.down())) {
         return null;
      } else if (BlockUtil.isAir(pos) && BlockUtil.isAir(pos.up()) && BlockUtil.isAir(pos.up(2))) {
         int progress = 0;
         Direction unHardFacing = null;
         Direction[] var3 = Direction.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Direction facing = var3[var5];
            if (facing != Direction.UP && facing != Direction.DOWN) {
               if (isHard(pos.offset(facing))) {
                  ++progress;
               } else {
                  int progress2 = 0;
                  Direction[] var8 = Direction.values();
                  int var9 = var8.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Direction facing2 = var8[var10];
                     if (facing2 != Direction.DOWN && facing2 != facing.getOpposite() && isHard(pos.offset(facing).offset(facing2))) {
                        ++progress2;
                     }
                  }

                  if (progress2 == 4) {
                     ++progress;
                  } else {
                     unHardFacing = facing;
                  }
               }
            }
         }

         if (progress == 3) {
            return unHardFacing;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static PlayerEntity getClosestEnemy(double distance) {
      PlayerEntity closest = null;
      Iterator var3 = getEnemies(distance).iterator();

      while(var3.hasNext()) {
         PlayerEntity player = (PlayerEntity)var3.next();
         if (closest == null) {
            closest = player;
         } else if (mc.player.getEyePos().squaredDistanceTo(player.getPos()) < mc.player.squaredDistanceTo(closest)) {
            closest = player;
         }
      }

      return closest;
   }

   public static Vec3d getEntityPosVec(PlayerEntity entity, int ticks) {
      return ticks <= 0 ? entity.getPos() : entity.getPos().add(getMotionVec(entity, (float)ticks, true));
   }

   public static Vec3d getMotionVec(Entity entity, float ticks, boolean collision) {
      double dX = entity.getX() - entity.prevX;
      double dZ = entity.getZ() - entity.prevZ;
      double entityMotionPosX = 0.0;
      double entityMotionPosZ = 0.0;
      if (collision) {
         for(double i = 1.0; i <= (double)ticks && !mc.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, 0.0, dZ * i))); i += 0.5) {
            entityMotionPosX = dX * i;
            entityMotionPosZ = dZ * i;
         }
      } else {
         entityMotionPosX = dX * (double)ticks;
         entityMotionPosZ = dZ * (double)ticks;
      }

      return new Vec3d(entityMotionPosX, 0.0, entityMotionPosZ);
   }

   public static Vec3d getMotionVecWithY(Entity entity, int ticks, boolean collision) {
      double dX = entity.getX() - entity.prevX;
      double dY = entity.getY() - entity.prevY;
      double dZ = entity.getZ() - entity.prevZ;
      double entityMotionPosX = 0.0;
      double entityMotionPosY = 0.0;
      double entityMotionPosZ = 0.0;
      if (collision) {
         for(double i = 1.0; i <= (double)ticks && !mc.world.canCollide(entity, entity.getBoundingBox().offset(new Vec3d(dX * i, dY * i, dZ * i))); i += 0.5) {
            entityMotionPosX = dX * i;
            entityMotionPosY = dY * i;
            entityMotionPosZ = dZ * i;
         }
      } else {
         entityMotionPosX = dX * (double)ticks;
         entityMotionPosY = dY * (double)ticks;
         entityMotionPosZ = dZ * (double)ticks;
      }

      return new Vec3d(entityMotionPosX, entityMotionPosY, entityMotionPosZ);
   }

   public static boolean isHard(BlockPos pos) {
      Block block = BlockUtil.getState(pos).getBlock();
      return block == Blocks.OBSIDIAN || block == Blocks.NETHERITE_BLOCK || block == Blocks.ENDER_CHEST || block == Blocks.BEDROCK || block == Blocks.ANVIL;
   }

   static {
      modifyBlockState = Blocks.AIR.getDefaultState();
      breakTimer = new Timer();
   }
}

package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class HoleKick extends Module {
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final BooleanSetting yawDeceive = this.add(new BooleanSetting("YawDeceive", true));
   private final BooleanSetting pistonPacket = this.add(new BooleanSetting("PistonPacket", false));
   private final BooleanSetting redStonePacket = this.add(new BooleanSetting("RedStonePacket", true));
   private final BooleanSetting noEating = this.add(new BooleanSetting("NoEating", true));
   private final BooleanSetting attackCrystal = this.add(new BooleanSetting("BreakCrystal", true));
   private final BooleanSetting mine = this.add(new BooleanSetting("Mine", true));
   private final BooleanSetting allowWeb = this.add(new BooleanSetting("AllowWeb", true));
   private final SliderSetting updateDelay = this.add(new SliderSetting("UpdateDelay", 100, 0, 500));
   private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
   private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false));
   private final BooleanSetting checkPiston = this.add(new BooleanSetting("CheckPiston", false));
   private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
   private final BooleanSetting pullBack = this.add((new BooleanSetting("PullBack", true)).setParent());
   private final BooleanSetting onlyBurrow = this.add(new BooleanSetting("OnlyBurrow", true, (v) -> {
      return this.pullBack.isOpen();
   }));
   private final SliderSetting range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0));
   private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0));
   private final SliderSetting surroundCheck = this.add(new SliderSetting("SurroundCheck", 2, 0, 4));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final Timer timer = new Timer();
   private PlayerEntity displayTarget = null;
   public static HoleKick INSTANCE;

   public HoleKick() {
      super("HoleKick", "use piston push hole fag", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onEnable() {
      AutoCrystal.INSTANCE.lastBreakTimer.reset();
   }

   public static void pistonFacing(Direction i) {
      if (i == Direction.EAST) {
         EntityUtil.sendYawAndPitch(-90.0F, 5.0F);
      } else if (i == Direction.WEST) {
         EntityUtil.sendYawAndPitch(90.0F, 5.0F);
      } else if (i == Direction.NORTH) {
         EntityUtil.sendYawAndPitch(180.0F, 5.0F);
      } else if (i == Direction.SOUTH) {
         EntityUtil.sendYawAndPitch(0.0F, 5.0F);
      }

   }

   static boolean isTargetHere(BlockPos pos, Entity target) {
      return (new Box(pos)).intersects(target.getBoundingBox());
   }

   public static boolean isInWeb(PlayerEntity player) {
      Vec3d playerPos = player.getPos();
      float[] var2 = new float[]{0.0F, 0.3F, -0.3F};
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         float x = var2[var4];
         float[] var6 = new float[]{0.0F, 0.3F, -0.3F};
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            float z = var6[var8];
            int[] var10 = new int[]{0, 1, 2};
            int var11 = var10.length;

            for(int var12 = 0; var12 < var11; ++var12) {
               int y = var10[var12];
               BlockPos pos = (new BlockPosX(playerPos.getX() + (double)x, playerPos.getY(), playerPos.getZ() + (double)z)).up(y);
               if (isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public void onUpdate() {
      if (this.timer.passedMs(this.updateDelay.getValue())) {
         if (this.selfGround.getValue() && !mc.player.isOnGround()) {
            if (this.autoDisable.getValue()) {
               this.disable();
            }

         } else if (this.findBlock(Blocks.REDSTONE_BLOCK) != -1 && this.findClass(PistonBlock.class) != -1) {
            if (!this.noEating.getValue() || !EntityUtil.isUsing()) {
               this.timer.reset();
               Iterator var1 = CombatUtil.getEnemies(this.range.getValue()).iterator();

               PlayerEntity target;
               do {
                  do {
                     do {
                        do {
                           if (!var1.hasNext()) {
                              if (this.autoDisable.getValue()) {
                                 this.disable();
                              }

                              this.displayTarget = null;
                              return;
                           }

                           target = (PlayerEntity)var1.next();
                        } while(!this.canPush(target));
                     } while(!target.isOnGround() && this.onlyGround.getValue());
                  } while(isInWeb(target) && !this.allowWeb.getValue());

                  this.displayTarget = target;
               } while(!this.doPush(EntityUtil.getEntityPos(target), target));

            }
         } else {
            if (this.autoDisable.getValue()) {
               this.disable();
            }

         }
      }
   }

   private boolean checkPiston(BlockPos targetPos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction i = var2[var4];
         if (i != Direction.DOWN && i != Direction.UP) {
            BlockPos pos = targetPos.up();
            if (this.getBlock(pos.offset(i)) instanceof PistonBlock && this.getBlockState(pos.offset(i)).get(FacingBlock.FACING).getOpposite() == i) {
               Direction[] var7 = Direction.values();
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Direction i2 = var7[var9];
                  if (this.getBlock(pos.offset(i).offset(i2)) == Blocks.REDSTONE_BLOCK && this.mine.getValue()) {
                     this.mine(pos.offset(i).offset(i2));
                     if (this.autoDisable.getValue()) {
                        this.disable();
                     }

                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean doPush(BlockPos targetPos, PlayerEntity target) {
      if (this.checkPiston.getValue() && this.checkPiston(targetPos)) {
         return true;
      } else {
         Direction[] var3;
         int var4;
         int var5;
         Direction i;
         BlockPos pos;
         Direction[] var8;
         int var9;
         int var10;
         Direction i2;
         if (!mc.world.getBlockState(targetPos.up(2)).blocksMovement()) {
            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof PistonBlock && !this.getBlockState(pos.offset(i, -2)).blocksMovement() && (this.getBlock(pos.offset(i, -2).up()) == Blocks.AIR || this.getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK) && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i) {
                     var8 = Direction.values();
                     var9 = var8.length;

                     for(var10 = 0; var10 < var9; ++var10) {
                        i2 = var8[var10];
                        if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                           if (this.mine.getValue()) {
                              this.mine(pos.offset(i2));
                           }

                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }
            }

            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof PistonBlock && !this.getBlockState(pos.offset(i, -2)).blocksMovement() && (this.getBlock(pos.offset(i, -2).up()) == Blocks.AIR || this.getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK) && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i && this.doPower(pos)) {
                     return true;
                  }
               }
            }

            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (!(mc.player.getY() - target.getY() <= -1.0) && !(mc.player.getY() - target.getY() >= 2.0) || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)) {
                     this.attackCrystal(pos);
                     if (this.isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false) && !this.getBlockState(pos.offset(i, -2)).blocksMovement() && !this.getBlockState(pos.offset(i, -2).up()).blocksMovement()) {
                        if (BlockUtil.getPlaceSide(pos) != null || !this.downPower(pos)) {
                           this.doPiston(i, pos, this.mine.getValue());
                           return true;
                        }
                        break;
                     }
                  }
               }
            }

            if (this.getBlock(targetPos) == Blocks.AIR && this.onlyBurrow.getValue() || !this.pullBack.getValue()) {
               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               return true;
            } else {
               var3 = Direction.values();
               var4 = var3.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  i = var3[var5];
                  if (i != Direction.DOWN && i != Direction.UP) {
                     pos = targetPos.offset(i).up();
                     var8 = Direction.values();
                     var9 = var8.length;

                     for(var10 = 0; var10 < var9; ++var10) {
                        i2 = var8[var10];
                        if (this.getBlock(pos) instanceof PistonBlock && this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i) {
                           this.mine(pos.offset(i2));
                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }

               var3 = Direction.values();
               var4 = var3.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  i = var3[var5];
                  if (i != Direction.DOWN && i != Direction.UP) {
                     pos = targetPos.offset(i).up();
                     var8 = Direction.values();
                     var9 = var8.length;

                     for(var10 = 0; var10 < var9; ++var10) {
                        i2 = var8[var10];
                        if (this.getBlock(pos) instanceof PistonBlock && this.getBlock(pos.offset(i2)) == Blocks.AIR && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i) {
                           this.attackCrystal(pos.offset(i2));
                           if (!this.doPower(pos, i2)) {
                              this.mine(pos.offset(i2));
                              return true;
                           }
                        }
                     }
                  }
               }

               var3 = Direction.values();
               var4 = var3.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  i = var3[var5];
                  if (i != Direction.DOWN && i != Direction.UP) {
                     pos = targetPos.offset(i).up();
                     if (!(mc.player.getY() - target.getY() <= -1.0) && !(mc.player.getY() - target.getY() >= 2.0) || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)) {
                        this.attackCrystal(pos);
                        if (this.isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false) && !this.downPower(pos)) {
                           this.doPiston(i, pos, true);
                           return true;
                        }
                     }
                  }
               }

               return false;
            }
         } else {
            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof PistonBlock && (mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down()) || isTargetHere(pos.offset(i, 2), target)) && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i) {
                     var8 = Direction.values();
                     var9 = var8.length;

                     for(var10 = 0; var10 < var9; ++var10) {
                        i2 = var8[var10];
                        if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                           if (this.mine.getValue()) {
                              this.mine(pos.offset(i2));
                           }

                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }
            }

            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof PistonBlock && (mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down()) || isTargetHere(pos.offset(i, 2), target)) && this.getBlockState(pos).get(FacingBlock.FACING).getOpposite() == i && this.doPower(pos)) {
                     return true;
                  }
               }
            }

            var3 = Direction.values();
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               i = var3[var5];
               if (i != Direction.DOWN && i != Direction.UP) {
                  pos = targetPos.offset(i).up();
                  if (!(mc.player.getY() - target.getY() <= -1.0) && !(mc.player.getY() - target.getY() >= 2.0) || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)) {
                     this.attackCrystal(pos);
                     if (this.isTrueFacing(pos, i) && BlockUtil.clientCanPlace(pos, false) && (mc.world.isAir(pos.offset(i, -2)) && mc.world.isAir(pos.offset(i, -2).down()) || isTargetHere(pos.offset(i, 2), target)) && !this.getBlockState(pos.offset(i, -2).up()).blocksMovement()) {
                        if (BlockUtil.getPlaceSide(pos) != null || !this.downPower(pos)) {
                           this.doPiston(i, pos, this.mine.getValue());
                           return true;
                        }

                        return false;
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   private boolean isTrueFacing(BlockPos pos, Direction facing) {
      if (this.yawDeceive.getValue()) {
         return true;
      } else {
         Direction side = BlockUtil.getPlaceSide(pos);
         if (side == null) {
            side = Direction.UP;
         }

         side = side.getOpposite();
         Vec3d hitVec = pos.offset(side.getOpposite()).toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5));
         return Direction.fromRotation(EntityUtil.getLegitRotations(hitVec)[0]) == facing;
      }
   }

   private boolean doPower(BlockPos pos, Direction i2) {
      if (!BlockUtil.canPlace(pos.offset(i2), this.placeRange.getValue())) {
         return true;
      } else {
         int old = mc.player.getInventory().selectedSlot;
         int power = this.findBlock(Blocks.REDSTONE_BLOCK);
         this.doSwap(power);
         BlockUtil.placeBlock(pos.offset(i2), this.rotate.getValue(), this.redStonePacket.getValue(), true);
         if (this.inventory.getValue()) {
            this.doSwap(power);
            EntityUtil.syncInventory();
         } else {
            this.doSwap(old);
         }

         return false;
      }
   }

   private boolean doPower(BlockPos pos) {
      Direction facing = BlockUtil.getBestNeighboring(pos, null);
      if (facing != null) {
         this.attackCrystal(pos.offset(facing));
         if (!this.doPower(pos, facing)) {
            return true;
         }
      }

      Direction[] var3 = Direction.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Direction i2 = var3[var5];
         this.attackCrystal(pos.offset(i2));
         if (!this.doPower(pos, i2)) {
            return true;
         }
      }

      return false;
   }

   private boolean downPower(BlockPos pos) {
      if (BlockUtil.getPlaceSide(pos) == null) {
         boolean noPower = true;
         Direction[] var3 = Direction.values();
         int power = var3.length;

         for(int var5 = 0; var5 < power; ++var5) {
            Direction i2 = var3[var5];
            if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
               noPower = false;
               break;
            }
         }

         if (noPower) {
            if (!BlockUtil.canPlace(pos.add(0, -1, 0), this.placeRange.getValue())) {
               return true;
            }

            int old = mc.player.getInventory().selectedSlot;
            power = this.findBlock(Blocks.REDSTONE_BLOCK);
            this.doSwap(power);
            BlockUtil.placeBlock(pos.add(0, -1, 0), this.rotate.getValue(), this.redStonePacket.getValue(), true);
            if (this.inventory.getValue()) {
               this.doSwap(power);
               EntityUtil.syncInventory();
            } else {
               this.doSwap(old);
            }
         }
      }

      return false;
   }

   private void doPiston(Direction i, BlockPos pos, boolean mine) {
      if (BlockUtil.canPlace(pos, this.placeRange.getValue())) {
         int piston = this.findClass(PistonBlock.class);
         Direction side = BlockUtil.getPlaceSide(pos);
         if (this.rotate.getValue()) {
            EntityUtil.facePosSide(pos.offset(side), side.getOpposite());
         }

         if (this.yawDeceive.getValue()) {
            pistonFacing(i);
         }

         int old = mc.player.getInventory().selectedSlot;
         this.doSwap(piston);
         BlockUtil.placeBlock(pos, false, this.pistonPacket.getValue(), true);
         if (this.inventory.getValue()) {
            this.doSwap(piston);
            EntityUtil.syncInventory();
         } else {
            this.doSwap(old);
         }

         if (this.rotate.getValue()) {
            EntityUtil.facePosSide(pos.offset(side), side.getOpposite());
         }

         Direction[] var7 = Direction.values();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Direction i2 = var7[var9];
            if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
               if (mine) {
                  this.mine(pos.offset(i2));
               }

               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               return;
            }
         }

         this.doPower(pos);
      }

   }

   public String getInfo() {
      return this.displayTarget != null ? this.displayTarget.getName().getString() : null;
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   public int findBlock(Block blockIn) {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(blockIn) : InventoryUtil.findBlock(blockIn);
   }

   public int findClass(Class clazz) {
      return this.inventory.getValue() ? InventoryUtil.findClassInventorySlot(clazz) : InventoryUtil.findClass(clazz);
   }

   private void attackCrystal(BlockPos pos) {
      if (this.attackCrystal.getValue()) {
         Iterator var2 = mc.world.getEntities().iterator();

         Entity crystal;
         do {
            if (!var2.hasNext()) {
               return;
            }

            crystal = (Entity)var2.next();
         } while(!(crystal instanceof EndCrystalEntity) || (double)MathHelper.sqrt((float)crystal.squaredDistanceTo((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5)) > 2.0);

         CombatUtil.attackCrystal(crystal, this.rotate.getValue(), false);
      }
   }

   private void mine(BlockPos pos) {
      SpeedMine.INSTANCE.mine(pos);
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   private BlockState getBlockState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   private Boolean canPush(PlayerEntity player) {
      int progress = 0;
      if (!mc.world.isAir(new BlockPosX(player.getX() + 1.0, player.getY() + 0.5, player.getZ()))) {
         ++progress;
      }

      if (!mc.world.isAir(new BlockPosX(player.getX() - 1.0, player.getY() + 0.5, player.getZ()))) {
         ++progress;
      }

      if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() + 1.0))) {
         ++progress;
      }

      if (!mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ() - 1.0))) {
         ++progress;
      }

      if (mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 2.5, player.getZ()))) {
         return !mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ())) || (double) progress > this.surroundCheck.getValue() - 1.0;
      } else {
         Direction[] var3 = Direction.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Direction i = var3[var5];
            if (i != Direction.UP && i != Direction.DOWN) {
               BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
               if (mc.world.isAir(pos) && mc.world.isAir(pos.up()) || isTargetHere(pos, player)) {
                  return !mc.world.isAir(new BlockPosX(player.getX(), player.getY() + 0.5, player.getZ())) || (double) progress > this.surroundCheck.getValue() - 1.0;
               }
            }
         }

         return false;
      }
   }
}

package me.nullpoint.mod.modules.impl.combat;

import java.util.Iterator;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.Placement;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class PistonCrystal extends Module {
   public static PistonCrystal INSTANCE;
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", false));
   private final BooleanSetting pistonPacket = this.add(new BooleanSetting("PistonPacket", false));
   private final BooleanSetting noEating = this.add(new BooleanSetting("NoEating", true));
   private final BooleanSetting eatingBreak = this.add(new BooleanSetting("EatingBreak", false));
   private final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 1.0, 8.0));
   private final SliderSetting range = this.add(new SliderSetting("Range", 4.0, 1.0, 8.0));
   private final BooleanSetting fire = this.add(new BooleanSetting("Fire", true));
   private final BooleanSetting switchPos = this.add(new BooleanSetting("Switch", false));
   private final BooleanSetting onlyGround = this.add(new BooleanSetting("SelfGround", true));
   private final BooleanSetting onlyStatic = this.add(new BooleanSetting("MovingPause", true));
   private final SliderSetting updateDelay = this.add(new SliderSetting("PlaceDelay", 100, 0, 500));
   private final SliderSetting posUpdateDelay = this.add(new SliderSetting("PosUpdateDelay", 500, 0, 1000));
   private final SliderSetting stageSetting = this.add(new SliderSetting("Stage", 4, 1, 10));
   private final SliderSetting pistonStage = this.add(new SliderSetting("PistonStage", 1, 1, 10));
   private final SliderSetting pistonMaxStage = this.add(new SliderSetting("PistonMaxStage", 1, 1, 10));
   private final SliderSetting powerStage = this.add(new SliderSetting("PowerStage", 3, 1, 10));
   private final SliderSetting powerMaxStage = this.add(new SliderSetting("PowerMaxStage", 3, 1, 10));
   private final SliderSetting crystalStage = this.add(new SliderSetting("CrystalStage", 4, 1, 10));
   private final SliderSetting crystalMaxStage = this.add(new SliderSetting("CrystalMaxStage", 4, 1, 10));
   private final SliderSetting fireStage = this.add(new SliderSetting("FireStage", 2, 1, 10));
   private final SliderSetting fireMaxStage = this.add(new SliderSetting("FireMaxStage", 2, 1, 10));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final BooleanSetting debug = this.add(new BooleanSetting("Debug", false));
   private PlayerEntity target = null;
   private final Timer timer = new Timer();
   private final Timer crystalTimer = new Timer();
   public BlockPos bestPos = null;
   public BlockPos bestOPos = null;
   public Direction bestFacing = null;
   public double getDistance = 100.0;
   public boolean getPos = false;
   private boolean isPiston = false;
   public int stage = 1;

   public PistonCrystal() {
      super("PistonCrystal", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onTick() {
      if (this.pistonStage.getValue() > this.stageSetting.getValue()) {
         this.pistonStage.setValue(this.stageSetting.getValue());
      }

      if (this.fireStage.getValue() > this.stageSetting.getValue()) {
         this.fireStage.setValue(this.stageSetting.getValue());
      }

      if (this.powerStage.getValue() > this.stageSetting.getValue()) {
         this.powerStage.setValue(this.stageSetting.getValue());
      }

      if (this.crystalStage.getValue() > this.stageSetting.getValue()) {
         this.crystalStage.setValue(this.stageSetting.getValue());
      }

      if (this.pistonMaxStage.getValue() > this.stageSetting.getValue()) {
         this.pistonMaxStage.setValue(this.stageSetting.getValue());
      }

      if (this.fireMaxStage.getValue() > this.stageSetting.getValue()) {
         this.fireMaxStage.setValue(this.stageSetting.getValue());
      }

      if (this.powerMaxStage.getValue() > this.stageSetting.getValue()) {
         this.powerMaxStage.setValue(this.stageSetting.getValue());
      }

      if (this.crystalMaxStage.getValue() > this.stageSetting.getValue()) {
         this.crystalMaxStage.setValue(this.stageSetting.getValue());
      }

      if (this.crystalMaxStage.getValue() < this.crystalStage.getValue()) {
         this.crystalStage.setValue(this.crystalMaxStage.getValue());
      }

      if (this.powerMaxStage.getValue() < this.powerStage.getValue()) {
         this.powerStage.setValue(this.powerMaxStage.getValue());
      }

      if (this.pistonMaxStage.getValue() < this.pistonStage.getValue()) {
         this.pistonStage.setValue(this.pistonMaxStage.getValue());
      }

      if (this.fireMaxStage.getValue() < this.fireStage.getValue()) {
         this.fireStage.setValue(this.fireMaxStage.getValue());
      }

   }

   public void onUpdate() {
      this.onTick();
      this.target = CombatUtil.getClosestEnemy(this.range.getValue());
      if (this.target != null) {
         if (!this.noEating.getValue() || !EntityUtil.isUsing()) {
            if (!this.check(this.onlyStatic.getValue(), !mc.player.isOnGround(), this.onlyGround.getValue())) {
               BlockPos pos = EntityUtil.getEntityPos(this.target, true);
               if (!EntityUtil.isUsing() || this.eatingBreak.getValue()) {
                  if (this.checkCrystal(pos.up(0))) {
                     CombatUtil.attackCrystal(pos.up(0), this.rotate.getValue(), true);
                  }

                  if (this.checkCrystal(pos.up(1))) {
                     CombatUtil.attackCrystal(pos.up(1), this.rotate.getValue(), true);
                  }

                  if (this.checkCrystal(pos.up(2))) {
                     CombatUtil.attackCrystal(pos.up(2), this.rotate.getValue(), true);
                  }
               }

               if (this.bestPos != null && mc.world.getBlockState(this.bestPos).getBlock() instanceof PistonBlock) {
                  this.isPiston = true;
               } else if (this.isPiston) {
                  this.isPiston = false;
                  this.crystalTimer.reset();
                  this.bestPos = null;
               }

               if (this.crystalTimer.passedMs(this.posUpdateDelay.getValueInt())) {
                  this.stage = 0;
                  this.getDistance = 100.0;
                  this.getPos = false;
                  this.getBestPos(pos.up(2));
                  this.getBestPos(pos.up());
               }

               if (this.timer.passedMs(this.updateDelay.getValueInt())) {
                  if (this.getPos && this.bestPos != null) {
                     this.timer.reset();
                     if (this.debug.getValue()) {
                        BlockPos var10000 = this.bestPos;
                        CommandManager.sendChatMessage("[Debug] PistonPos:" + var10000 + " Facing:" + this.bestFacing + " CrystalPos:" + this.bestOPos.offset(this.bestFacing));
                     }

                     this.doPistonAura(this.bestPos, this.bestFacing, this.bestOPos);
                  }

               }
            }
         }
      }
   }

   public boolean check(boolean onlyStatic, boolean onGround, boolean onlyGround) {
      if (MovementUtil.isMoving() && onlyStatic) {
         return true;
      } else if (onGround && onlyGround) {
         return true;
      } else if (this.findBlock(Blocks.REDSTONE_BLOCK) == -1) {
         return true;
      } else if (this.findClass(PistonBlock.class) == -1) {
         return true;
      } else {
         return this.findItem(Items.END_CRYSTAL) == -1;
      }
   }

   private boolean checkCrystal(BlockPos pos) {
       for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
           if (entity instanceof EndCrystalEntity) {
               float damage = AutoCrystal.INSTANCE.calculateDamage(entity.getPos(), this.target, this.target);
               if (damage > 7.0F) {
                   return true;
               }
           }
       }

      return false;
   }

   private boolean checkCrystal2(BlockPos pos) {
      Iterator<Entity> var2 = null;
      if (mc.world != null) {
         var2 = mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();
      }

      Entity entity;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         entity = var2.next();
      } while (!(entity instanceof EndCrystalEntity) || !EntityUtil.getEntityPos(entity).equals(pos));

      return true;
   }

   public String getInfo() {
      return this.target != null ? this.target.getName().getString() : null;
   }

   private void getBestPos(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction i = var2[var4];
         if (i != Direction.DOWN && i != Direction.UP) {
            this.getPos(pos, i);
         }
      }

   }

   private void getPos(BlockPos pos, Direction i) {
      if (BlockUtil.canPlaceCrystal(pos.offset(i)) || this.checkCrystal2(pos.offset(i))) {
         this.getPos(pos.offset(i, 3), i, pos);
         this.getPos(pos.offset(i, 3).up(), i, pos);
         int offsetX = pos.offset(i).getX() - pos.getX();
         int offsetZ = pos.offset(i).getZ() - pos.getZ();
         this.getPos(pos.offset(i, 3).add(offsetZ, 0, offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(-offsetZ, 0, -offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(offsetZ, 1, offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(-offsetZ, 1, -offsetX), i, pos);
         this.getPos(pos.offset(i, 2), i, pos);
         this.getPos(pos.offset(i, 2).up(), i, pos);
         this.getPos(pos.offset(i, 2).add(offsetZ, 0, offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(-offsetZ, 0, -offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(offsetZ, 1, offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(-offsetZ, 1, -offsetX), i, pos);
      }
   }

   private void getPos(BlockPos pos, Direction facing, BlockPos oPos) {
      if (!this.switchPos.getValue() || this.bestPos == null || !this.bestPos.equals(pos) || !mc.world.isAir(this.bestPos)) {
         if (BlockUtil.canPlace(pos, this.placeRange.getValue()) || this.getBlock(pos) instanceof PistonBlock) {
            if (this.findClass(PistonBlock.class) != -1) {
               if (this.getBlock(pos) instanceof PistonBlock || !(mc.player.getY() - (double)pos.getY() <= -2.0) && !(mc.player.getY() - (double)pos.getY() >= 3.0) || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)) {
                  if (mc.world.isAir(pos.offset(facing, -1)) && mc.world.getBlockState(pos.offset(facing, -1)).getBlock() != Blocks.FIRE && (this.getBlock(pos.offset(facing.getOpposite())) != Blocks.MOVING_PISTON || this.checkCrystal2(pos.offset(facing.getOpposite())))) {
                     if (BlockUtil.canPlace(pos, this.placeRange.getValue()) || this.isPiston(pos, facing)) {
                        if ((double)MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) < this.getDistance || this.bestPos == null) {
                           this.bestPos = pos;
                           this.bestOPos = oPos;
                           this.bestFacing = facing;
                           this.getDistance = MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos()));
                           this.getPos = true;
                           this.crystalTimer.reset();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void doPistonAura(BlockPos pos, Direction facing, BlockPos oPos) {
      if ((double)this.stage >= this.stageSetting.getValue()) {
         this.stage = 0;
      }

      ++this.stage;
      if (mc.world.isAir(pos)) {
         if (!BlockUtil.canPlace(pos)) {
            return;
         }

         if ((double)this.stage >= this.pistonStage.getValue() && (double)this.stage <= this.pistonMaxStage.getValue()) {
            Direction side = BlockUtil.getPlaceSide(pos);
            if (side == null) {
               return;
            }

            int old = mc.player.getInventory().selectedSlot;
            HoleKick.pistonFacing(facing);
            int piston = this.findClass(PistonBlock.class);
            this.doSwap(piston);
            BlockUtil.placeBlock(pos, false, this.pistonPacket.getValue());
            if (this.inventory.getValue()) {
               this.doSwap(piston);
               EntityUtil.syncInventory();
            } else {
               this.doSwap(old);
            }

            BlockPos neighbour = pos.offset(side);
            Direction opposite = side.getOpposite();
            if (this.rotate.getValue()) {
               EntityUtil.facePosSide(neighbour, opposite);
            }
         }
      }

      if ((double)this.stage >= this.powerStage.getValue() && (double)this.stage <= this.powerMaxStage.getValue()) {
         this.doRedStone(pos, facing, oPos.offset(facing));
      }

      if ((double)this.stage >= this.crystalStage.getValue() && (double)this.stage <= this.crystalMaxStage.getValue()) {
         this.placeCrystal(oPos, facing);
      }

      if ((double)this.stage >= this.fireStage.getValue() && (double)this.stage <= this.fireMaxStage.getValue()) {
         this.doFire(oPos, facing);
      }

   }

   private void placeCrystal(BlockPos pos, Direction facing) {
      if (BlockUtil.canPlaceCrystal(pos.offset(facing))) {
         int crystal = this.findItem(Items.END_CRYSTAL);
         if (crystal != -1) {
            int old = mc.player.getInventory().selectedSlot;
            this.doSwap(crystal);
            BlockUtil.placeCrystal(pos.offset(facing), true);
            if (this.inventory.getValue()) {
               this.doSwap(crystal);
               EntityUtil.syncInventory();
            } else {
               this.doSwap(old);
            }

         }
      }
   }

   private boolean isPiston(BlockPos pos, Direction facing) {
      if (!(mc.world.getBlockState(pos).getBlock() instanceof PistonBlock)) {
         return false;
      } else if (mc.world.getBlockState(pos).get(FacingBlock.FACING).getOpposite() != facing) {
         return false;
      } else {
         return mc.world.isAir(pos.offset(facing, -1)) || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE || this.getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON;
      }
   }

   private void doFire(BlockPos pos, Direction facing) {
      if (this.fire.getValue()) {
         int fire = this.findItem(Items.FLINT_AND_STEEL);
         if (fire != -1) {
            int old = mc.player.getInventory().selectedSlot;
            int[] xOffset = new int[]{0, facing.getOffsetZ(), -facing.getOffsetZ()};
            int[] yOffset = new int[]{0, 1};
            int[] zOffset = new int[]{0, facing.getOffsetX(), -facing.getOffsetX()};
            int[] var8 = xOffset;
            int var9 = xOffset.length;

            int var10;
            int x;
            int[] var12;
            int var13;
            int var14;
            int y;
            int[] var16;
            int var17;
            int var18;
            int z;
            for(var10 = 0; var10 < var9; ++var10) {
               x = var8[var10];
               var12 = yOffset;
               var13 = yOffset.length;

               for(var14 = 0; var14 < var13; ++var14) {
                  y = var12[var14];
                  var16 = zOffset;
                  var17 = zOffset.length;

                  for(var18 = 0; var18 < var17; ++var18) {
                     z = var16[var18];
                     if (this.getBlock(pos.add(x, y, z)) == Blocks.FIRE) {
                        return;
                     }
                  }
               }
            }

            var8 = xOffset;
            var9 = xOffset.length;

            for(var10 = 0; var10 < var9; ++var10) {
               x = var8[var10];
               var12 = yOffset;
               var13 = yOffset.length;

               for(var14 = 0; var14 < var13; ++var14) {
                  y = var12[var14];
                  var16 = zOffset;
                  var17 = zOffset.length;

                  for(var18 = 0; var18 < var17; ++var18) {
                     z = var16[var18];
                     if (canFire(pos.add(x, y, z))) {
                        this.doSwap(fire);
                        this.placeFire(pos.add(x, y, z));
                        if (this.inventory.getValue()) {
                           this.doSwap(fire);
                           EntityUtil.syncInventory();
                        } else {
                           this.doSwap(old);
                        }

                        return;
                     }
                  }
               }
            }

         }
      }
   }

   public void placeFire(BlockPos pos) {
      BlockPos neighbour = pos.offset(Direction.DOWN);
      BlockUtil.clickBlock(neighbour, Direction.UP, this.rotate.getValue());
   }

   private static boolean canFire(BlockPos pos) {
      if (BlockUtil.canReplace(pos.down())) {
         return false;
      } else if (!mc.world.isAir(pos)) {
         return false;
      } else if (!BlockUtil.canClick(pos.offset(Direction.DOWN))) {
         return false;
      } else {
         return CombatSetting.INSTANCE.placement.getValue() != Placement.Strict || BlockUtil.isStrictDirection(pos.down(), Direction.UP);
      }
   }

   private void doRedStone(BlockPos pos, Direction facing, BlockPos crystalPos) {
      if (mc.world.isAir(pos.offset(facing, -1)) || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE || this.getBlock(pos.offset(facing.getOpposite())) == Blocks.MOVING_PISTON) {
         Direction[] var4 = Direction.values();
         int old = var4.length;

         for(int var6 = 0; var6 < old; ++var6) {
            Direction i = var4[var6];
            if (this.getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) {
               return;
            }
         }

         int power = this.findBlock(Blocks.REDSTONE_BLOCK);
         if (power != -1) {
            old = mc.player.getInventory().selectedSlot;
            Direction bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
            if (bestNeighboring != null && bestNeighboring != facing.getOpposite() && BlockUtil.canPlace(pos.offset(bestNeighboring), this.placeRange.getValue()) && !pos.offset(bestNeighboring).equals(crystalPos)) {
               this.doSwap(power);
               BlockUtil.placeBlock(pos.offset(bestNeighboring), this.rotate.getValue());
               if (this.inventory.getValue()) {
                  this.doSwap(power);
                  EntityUtil.syncInventory();
               } else {
                  this.doSwap(old);
               }

            } else {
               Direction[] var13 = Direction.values();
               int var8 = var13.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Direction i = var13[var9];
                  if (BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue()) && !pos.offset(i).equals(crystalPos) && i != facing.getOpposite()) {
                     this.doSwap(power);
                     BlockUtil.placeBlock(pos.offset(i), this.rotate.getValue());
                     if (this.inventory.getValue()) {
                        this.doSwap(power);
                        EntityUtil.syncInventory();
                     } else {
                        this.doSwap(old);
                     }

                     return;
                  }
               }

            }
         }
      }
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   public int findItem(Item itemIn) {
      return this.inventory.getValue() ? InventoryUtil.findItemInventorySlot(itemIn) : InventoryUtil.findItem(itemIn);
   }

   public int findBlock(Block blockIn) {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(blockIn) : InventoryUtil.findBlock(blockIn);
   }

   public int findClass(Class clazz) {
      return this.inventory.getValue() ? InventoryUtil.findClassInventorySlot(clazz) : InventoryUtil.findClass(clazz);
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }
}

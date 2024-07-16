package me.nullpoint.mod.modules.impl.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class Burrow extends Module {
   public static Burrow INSTANCE;
   private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true));
   private final BooleanSetting antiLag = this.add(new BooleanSetting("AntiLag", true));
   public final BooleanSetting helper = this.add(new BooleanSetting("Helper", true));
   private final BooleanSetting headFill = this.add(new BooleanSetting("HeadFill", true));
   private final BooleanSetting noSelfPos = this.add(new BooleanSetting("NoSelfPos", false));
   private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
   private final BooleanSetting sound = this.add(new BooleanSetting("Sound", true));
   private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 4.0, 1.0, 4.0, 1.0));
   private final EnumSetting rotate;
   private final BooleanSetting breakCrystal;
   private final BooleanSetting wait;
   private final BooleanSetting fakeMove;
   private final BooleanSetting center;
   private final BooleanSetting inventory;
   private final EnumSetting lagMode;
   private final EnumSetting aboveLagMode;
   private final SliderSetting smartX;
   private final SliderSetting smartUp;
   private final SliderSetting smartDown;
   private final SliderSetting smartDistance;
   private int progress;
   private final List placePos;
   BlockPos movedPos;

   public Burrow() {
      super("Burrow", Module.Category.Combat);
      this.rotate = this.add(new EnumSetting("RotateMode", Burrow.RotateMode.Bypass));
      this.breakCrystal = this.add(new BooleanSetting("Break", true));
      this.wait = this.add(new BooleanSetting("Wait", true));
      this.fakeMove = this.add((new BooleanSetting("FakeMove", true)).setParent());
      this.center = this.add(new BooleanSetting("AllowCenter", false, (v) -> {
         return this.fakeMove.isOpen();
      }));
      this.inventory = this.add(new BooleanSetting("InventorySwap", true));
      this.lagMode = this.add(new EnumSetting("LagMode", Burrow.LagBackMode.TrollHack));
      this.aboveLagMode = this.add(new EnumSetting("MoveLagMode", Burrow.LagBackMode.Smart));
      this.smartX = this.add(new SliderSetting("SmartXZ", 3.0, 0.0, 10.0, 0.1, (v) -> {
         return this.lagMode.getValue() == Burrow.LagBackMode.Smart || this.aboveLagMode.getValue() == Burrow.LagBackMode.Smart;
      }));
      this.smartUp = this.add(new SliderSetting("SmartUp", 3.0, 0.0, 10.0, 0.1, (v) -> {
         return this.lagMode.getValue() == Burrow.LagBackMode.Smart || this.aboveLagMode.getValue() == Burrow.LagBackMode.Smart;
      }));
      this.smartDown = this.add(new SliderSetting("SmartDown", 3.0, 0.0, 10.0, 0.1, (v) -> {
         return this.lagMode.getValue() == Burrow.LagBackMode.Smart || this.aboveLagMode.getValue() == Burrow.LagBackMode.Smart;
      }));
      this.smartDistance = this.add(new SliderSetting("SmartDistance", 2.0, 0.0, 10.0, 0.1, (v) -> {
         return this.lagMode.getValue() == Burrow.LagBackMode.Smart || this.aboveLagMode.getValue() == Burrow.LagBackMode.Smart;
      }));
      this.progress = 0;
      this.placePos = new ArrayList();
      this.movedPos = null;
      INSTANCE = this;
   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      this.movedPos = null;
      if (mc.player.isOnGround()) {
         if (!HoleKick.isInWeb(mc.player)) {
            if (!this.antiLag.getValue() || BlockUtil.getState(EntityUtil.getPlayerPos(true).down()).blocksMovement()) {
               int oldSlot = mc.player.getInventory().selectedSlot;
               int block;
               if ((block = this.getBlock()) == -1) {
                  CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oObsidian" + (this.enderChest.getValue() ? "/EnderChest" : "") + "?");
                  this.disable();
               } else {
                  this.progress = 0;
                  this.placePos.clear();
                  double offset = CombatSetting.getOffset();
                  BlockPos pos1 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
                  BlockPos pos2 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() + offset);
                  BlockPos pos3 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
                  BlockPos pos4 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 0.5, mc.player.getZ() - offset);
                  BlockPos pos5 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
                  BlockPos pos6 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() + offset);
                  BlockPos pos7 = new BlockPosX(mc.player.getX() + offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
                  BlockPos pos8 = new BlockPosX(mc.player.getX() - offset, mc.player.getY() + 1.5, mc.player.getZ() - offset);
                  BlockPos playerPos = EntityUtil.getPlayerPos(true);
                  boolean headFill = false;
                  if (!this.canPlace(pos1) && !this.canPlace(pos2) && !this.canPlace(pos3) && !this.canPlace(pos4)) {
                     headFill = true;
                     if (!this.headFill.getValue() || !this.canPlace(pos5) && !this.canPlace(pos6) && !this.canPlace(pos7) && !this.canPlace(pos8)) {
                        if (!this.wait.getValue()) {
                           this.disable();
                        }

                        return;
                     }
                  }

                  boolean above = false;
                  BlockPos headPos = EntityUtil.getPlayerPos(true).up(2);
                  boolean rotate = this.rotate.getValue() == Burrow.RotateMode.Normal;
                  CombatUtil.attackCrystal(pos1, rotate, false);
                  CombatUtil.attackCrystal(pos2, rotate, false);
                  CombatUtil.attackCrystal(pos3, rotate, false);
                  CombatUtil.attackCrystal(pos4, rotate, false);
                  if (mc.player.isOnGround()) {
                     if (!headFill && !mc.player.isInSneakingPose() && !this.Trapped(headPos) && !this.Trapped(headPos.add(1, 0, 0)) && !this.Trapped(headPos.add(-1, 0, 0)) && !this.Trapped(headPos.add(0, 0, 1)) && !this.Trapped(headPos.add(0, 0, -1)) && !this.Trapped(headPos.add(1, 0, -1)) && !this.Trapped(headPos.add(-1, 0, -1)) && !this.Trapped(headPos.add(1, 0, 1)) && !this.Trapped(headPos.add(-1, 0, 1))) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.4199999868869781, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.7531999805212017, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.9999957640154541, mc.player.getZ(), false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.1661092609382138, mc.player.getZ(), false));
                     } else {
                        above = true;
                        if (!this.fakeMove.getValue()) {
                           if (!this.wait.getValue()) {
                              this.disable();
                           }

                           return;
                        }

                        boolean moved = false;
                        BlockPos offPos = playerPos;
                        if (!this.checkSelf(offPos) || BlockUtil.canReplace(offPos) || this.headFill.getValue() && BlockUtil.canReplace(offPos.up())) {
                           Direction[] var21 = Direction.values();
                           int var22 = var21.length;

                           int var23;
                           Direction facing;
                           for(var23 = 0; var23 < var22; ++var23) {
                              facing = var21[var23];
                              if (facing != Direction.UP && facing != Direction.DOWN) {
                                 offPos = playerPos.offset(facing);
                                 if (this.checkSelf(offPos) && !BlockUtil.canReplace(offPos) && (!this.headFill.getValue() || !BlockUtil.canReplace(offPos.up()))) {
                                    this.gotoPos(offPos);
                                    moved = true;
                                    break;
                                 }
                              }
                           }

                           if (!moved) {
                              var21 = Direction.values();
                              var22 = var21.length;

                              for(var23 = 0; var23 < var22; ++var23) {
                                 facing = var21[var23];
                                 if (facing != Direction.UP && facing != Direction.DOWN) {
                                    offPos = playerPos.offset(facing);
                                    if (this.checkSelf(offPos)) {
                                       this.gotoPos(offPos);
                                       moved = true;
                                       break;
                                    }
                                 }
                              }

                              if (!moved) {
                                 if (!this.center.getValue()) {
                                    return;
                                 }

                                 var21 = Direction.values();
                                 var22 = var21.length;

                                 for(var23 = 0; var23 < var22; ++var23) {
                                    facing = var21[var23];
                                    if (facing != Direction.UP && facing != Direction.DOWN) {
                                       offPos = playerPos.offset(facing);
                                       if (this.canGoto(offPos)) {
                                          this.gotoPos(offPos);
                                          moved = true;
                                          break;
                                       }
                                    }
                                 }

                                 if (!moved) {
                                    if (!this.wait.getValue()) {
                                       this.disable();
                                    }

                                    return;
                                 }
                              }
                           }
                        } else {
                           this.gotoPos(offPos);
                        }
                     }

                     this.doSwap(block);
                     if (this.rotate.getValue() == Burrow.RotateMode.Bypass) {
                        event.cancelRotate();
                        EntityUtil.sendYawAndPitch(Nullpoint.ROTATE.rotateYaw, 90.0F);
                     }

                     if (mc.player.isOnGround()) {
                        if (this.helper.getValue()) {
                           this.placeBlock(playerPos.down(), rotate);
                        }

                        this.placeBlock(playerPos, rotate);
                        if (this.helper.getValue()) {
                           this.placeBlock(pos1.down(), rotate);
                        }

                        this.placeBlock(pos1, rotate);
                        if (this.helper.getValue()) {
                           this.placeBlock(pos2.down(), rotate);
                        }

                        this.placeBlock(pos2, rotate);
                        if (this.helper.getValue()) {
                           this.placeBlock(pos3.down(), rotate);
                        }

                        this.placeBlock(pos3, rotate);
                        if (this.helper.getValue()) {
                           this.placeBlock(pos4.down(), rotate);
                        }

                        this.placeBlock(pos4, rotate);
                        if (this.headFill.getValue() && above) {
                           this.placeBlock(pos5, rotate);
                           this.placeBlock(pos6, rotate);
                           this.placeBlock(pos7, rotate);
                           this.placeBlock(pos8, rotate);
                        }

                        if (this.inventory.getValue()) {
                           this.doSwap(block);
                           EntityUtil.syncInventory();
                        } else {
                           this.doSwap(oldSlot);
                        }

                        label208:
                        switch (above ? (LagBackMode)this.aboveLagMode.getValue() : (LagBackMode)this.lagMode.getValue()) {
                           case Smart:
                              ArrayList list = new ArrayList();

                              double distance;
                              for(distance = mc.player.getPos().getX() - this.smartX.getValue(); distance < mc.player.getPos().getX() + this.smartX.getValue(); ++distance) {
                                 for(double z = mc.player.getPos().getZ() - this.smartX.getValue(); z < mc.player.getPos().getZ() + this.smartX.getValue(); ++z) {
                                    for(double y = mc.player.getPos().getY() - this.smartDown.getValue(); y < mc.player.getPos().getY() + this.smartUp.getValue(); ++y) {
                                       list.add(new BlockPosX(distance, y, z));
                                    }
                                 }
                              }

                              distance = 0.0;
                              BlockPos bestPos = null;
                              Iterator var30 = list.iterator();

                              while(true) {
                                 BlockPos pos;
                                 do {
                                    do {
                                       do {
                                          if (!var30.hasNext()) {
                                             if (bestPos != null) {
                                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
                                             }
                                             break label208;
                                          }

                                          pos = (BlockPos)var30.next();
                                       } while(!this.canGoto(pos));
                                    } while((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(pos.toCenterPos().add(0.0, -0.5, 0.0))) < this.smartDistance.getValue());
                                 } while(bestPos != null && !(mc.player.squaredDistanceTo(pos.toCenterPos()) < distance));

                                 bestPos = pos;
                                 distance = mc.player.squaredDistanceTo(pos.toCenterPos());
                              }
                           case Invalid:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1337.0, mc.player.getZ(), false));
                              break;
                           case Fly:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.16610926093821, mc.player.getZ(), false));
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.170005801788139, mc.player.getZ(), false));
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.2426308013947485, mc.player.getZ(), false));
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.640088003576279, mc.player.getZ(), false));
                              break;
                           case TrollHack:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 2.3400880035762786, mc.player.getZ(), false));
                              break;
                           case Normal:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.9, mc.player.getZ(), false));
                              break;
                           case ToVoid:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), -70.0, mc.player.getZ(), false));
                              break;
                           case Rotation:
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(-180.0F, -90.0F, false));
                              mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(180.0F, 90.0F, false));
                        }

                        this.disable();
                     }
                  }
               }
            }
         }
      }
   }

   private void placeBlock(BlockPos pos, boolean rotate) {
      if (this.canPlace(pos) && !this.placePos.contains(pos) && this.progress < this.blocksPer.getValueInt()) {
         this.placePos.add(pos);
         ++this.progress;
         Direction side;
         if ((side = BlockUtil.getPlaceSide(pos)) == null) {
            return;
         }

         BlockUtil.placedPos.add(pos);
         if (this.sound.getValue()) {
            mc.world.playSound(mc.player, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
         }

         BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate, this.packetPlace.getValue());
      }

   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private void gotoPos(BlockPos offPos) {
      if (this.rotate.getValue() == Burrow.RotateMode.None) {
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround((double)offPos.getX() + 0.5, mc.player.getY() + 0.2, (double)offPos.getZ() + 0.5, false));
      } else {
         mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full((double)offPos.getX() + 0.5, mc.player.getY() + 0.2, (double)offPos.getZ() + 0.5, Nullpoint.ROTATE.rotateYaw, 90.0F, false));
      }

   }

   private boolean canGoto(BlockPos pos) {
      return mc.world.isAir(pos) && mc.world.isAir(pos.up());
   }

   public boolean canPlace(BlockPos pos) {
      if (this.noSelfPos.getValue() && EntityUtil.getPlayerPos().equals(pos)) {
         return false;
      } else if (BlockUtil.getPlaceSide(pos) == null) {
         return false;
      } else if (!BlockUtil.canReplace(pos)) {
         return false;
      } else {
         if (BurrowAssist.INSTANCE.isOn() && BurrowAssist.INSTANCE.mcheck.getValue()) {
            Iterator var2 = (new HashMap(Nullpoint.BREAK.breakMap)).values().iterator();

            while(var2.hasNext()) {
               MineManager.BreakData breakData = (MineManager.BreakData)var2.next();
               if (breakData != null && breakData.getEntity() != null && pos.equals(breakData.pos) && breakData.getEntity() != mc.player) {
                  return false;
               }
            }
         }

         return !this.hasEntity(pos);
      }
   }

   private boolean hasEntity(BlockPos pos) {
      Iterator var2 = mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var2.hasNext()) {
                                 return false;
                              }

                              entity = (Entity)var2.next();
                           } while(entity == mc.player);
                        } while(!entity.isAlive());
                     } while(entity instanceof ItemEntity);
                  } while(entity instanceof ExperienceOrbEntity);
               } while(entity instanceof ExperienceBottleEntity);
            } while(entity instanceof ArrowEntity);
         } while(entity instanceof EndCrystalEntity && this.breakCrystal.getValue());
      } while(entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue());

      return true;
   }

   private boolean checkSelf(BlockPos pos) {
      return mc.player.getBoundingBox().intersects(new Box(pos));
   }

   private boolean Trapped(BlockPos pos) {
      return mc.world.canCollide(mc.player, new Box(pos)) && this.checkSelf(pos.down(2));
   }

   private int getBlock() {
      if (this.inventory.getValue()) {
         return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) == -1 && this.enderChest.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST) : InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
      } else {
         return InventoryUtil.findBlock(Blocks.OBSIDIAN) == -1 && this.enderChest.getValue() ? InventoryUtil.findBlock(Blocks.ENDER_CHEST) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
      }
   }

   private enum RotateMode {
      Bypass,
      Normal,
      None;

      // $FF: synthetic method
      private static RotateMode[] $values() {
         return new RotateMode[]{Bypass, Normal, None};
      }
   }

   private enum LagBackMode {
      Smart,
      Invalid,
      TrollHack,
      ToVoid,
      Normal,
      Rotation,
      Fly;

      // $FF: synthetic method
      private static LagBackMode[] $values() {
         return new LagBackMode[]{Smart, Invalid, TrollHack, ToVoid, Normal, Rotation, Fly};
      }
   }
}

// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.combat.AutoTrap;
import me.nullpoint.mod.modules.settings.Placement;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class AutoTrap extends Module {
   final Timer timer = new Timer();
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
   private final BooleanSetting autoDisable = this.add(new BooleanSetting("AutoDisable", true));
   private final SliderSetting range = this.add((new SliderSetting("Range", 5.0D, 1.0D, 8.0D)).setSuffix("m"));
   private final EnumSetting<AutoTrap.TargetMode> targetMod = this.add(new EnumSetting("TargetMode", AutoTrap.TargetMode.Single));
   private final BooleanSetting checkMine = this.add(new BooleanSetting("DetectMining", false));
   private final BooleanSetting helper = this.add(new BooleanSetting("Helper", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   private final BooleanSetting extend = this.add(new BooleanSetting("Extend", true));
   private final BooleanSetting antiStep = this.add(new BooleanSetting("AntiStep", false));
   private final BooleanSetting onlyBreak = this.add(new BooleanSetting("OnlyBreak", false, (v) -> this.antiStep.getValue()));
   private final BooleanSetting head = this.add(new BooleanSetting("Head", true));
   private final BooleanSetting headExtend = this.add(new BooleanSetting("HeadExtend", true));
   private final BooleanSetting headAnchor = this.add(new BooleanSetting("HeadAnchor", true));
   private final BooleanSetting chestUp = this.add(new BooleanSetting("ChestUp", true));
   private final BooleanSetting onlyBreaking = this.add(new BooleanSetting("OnlyBreaking", false, (v) -> this.chestUp.getValue()));
   private final BooleanSetting chest = this.add(new BooleanSetting("Chest", true));
   private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", false, (v) -> this.chest.getValue()));
   private final BooleanSetting legs = this.add(new BooleanSetting("Legs", false));
   private final BooleanSetting legAnchor = this.add(new BooleanSetting("LegAnchor", true));
   private final BooleanSetting down = this.add(new BooleanSetting("Down", false));
   private final BooleanSetting onlyHole = this.add(new BooleanSetting("OnlyHole", false));
   private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true));
   public final SliderSetting delay = this.add((new SliderSetting("Delay", 100, 0, 500)).setSuffix("ms"));
   private final SliderSetting placeRange = this.add((new SliderSetting("PlaceRange", 4.0D, 1.0D, 6.0D)).setSuffix("m"));
   private final BooleanSetting selfGround = this.add(new BooleanSetting("SelfGround", true));
   public final BooleanSetting render = this.add(new BooleanSetting("Render", true));
   public final BooleanSetting box = this.add(new BooleanSetting("Box", true, (v) -> this.render.getValue()));
   public final BooleanSetting outline = this.add(new BooleanSetting("Outline", false, (v) -> this.render.getValue()));
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100), (v) -> this.render.getValue()));
   public final SliderSetting fadeTime = this.add((new SliderSetting("FadeTime", 500, 0, 5000, (v) -> this.render.getValue())).setSuffix("ms"));
   public final BooleanSetting pre = this.add(new BooleanSetting("Pre", false, (v) -> this.render.getValue()));
   public final BooleanSetting sync = this.add(new BooleanSetting("Sync", true, (v) -> this.render.getValue()));
   public PlayerEntity target;
   public static AutoTrap INSTANCE;
   int progress = 0;
   private final ArrayList<BlockPos> trapList = new ArrayList();
   private final ArrayList<BlockPos> placeList = new ArrayList();

   public AutoTrap() {
      super("AutoTrap", "Automatically trap the enemy", Module.Category.Combat);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new AutoTrap.AutoTrapRender());
   }

   public void onUpdate() {
      if (!nullCheck()) {
         this.trapList.clear();
         this.placeList.clear();
         this.progress = 0;
         if (this.selfGround.getValue() && !mc.player.isOnGround()) {
            this.target = null;
         } else if (this.usingPause.getValue() && EntityUtil.isUsing()) {
            this.target = null;
         } else if (this.timer.passedMs((long)this.delay.getValue())) {
            if (this.targetMod.getValue() == AutoTrap.TargetMode.Single) {
               this.target = CombatUtil.getClosestEnemy(this.range.getValue());
               if (this.target == null) {
                  if (this.autoDisable.getValue()) {
                     this.disable();
                  }

                  return;
               }

               this.trapTarget(this.target);
            } else if (this.targetMod.getValue() == AutoTrap.TargetMode.Multi) {
               boolean found = false;

               for(PlayerEntity player : CombatUtil.getEnemies(this.range.getValue())) {
                  found = true;
                  this.target = player;
                  this.trapTarget(this.target);
               }

               if (!found) {
                  if (this.autoDisable.getValue()) {
                     this.disable();
                  }

                  this.target = null;
               }
            }

         }
      }
   }

   private void trapTarget(PlayerEntity target) {
      if (!this.onlyHole.getValue() || BlockUtil.isHole(EntityUtil.getEntityPos(target))) {
         this.doTrap(EntityUtil.getEntityPos(target, true));
      }
   }

   private void doTrap(BlockPos pos) {
      if (!this.trapList.contains(pos)) {
         this.trapList.add(pos);
         if (this.legs.getValue()) {
            for(Direction i : Direction.values()) {
               if (i != Direction.DOWN && i != Direction.UP) {
                  BlockPos offsetPos = pos.offset(i);
                  this.tryPlaceBlock(offsetPos, this.legAnchor.getValue());
                  if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue()) && this.getHelper(offsetPos) != null) {
                     this.tryPlaceObsidian(this.getHelper(offsetPos));
                  }
               }
            }
         }

         if (this.headExtend.getValue()) {
            for(int x : new int[]{1, 0, -1}) {
               for(int z : new int[]{1, 0, -1}) {
                  BlockPos offsetPos = pos.add(z, 0, x);
                  if (this.checkEntity(new BlockPos(offsetPos))) {
                     this.tryPlaceBlock(offsetPos.up(2), this.headAnchor.getValue());
                  }
               }
            }
         }

         if (this.head.getValue() && BlockUtil.clientCanPlace(pos.up(2), this.breakCrystal.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(2)) == null) {
               boolean trapChest = this.helper.getValue();
               if (this.getHelper(pos.up(2)) != null) {
                  this.tryPlaceObsidian(this.getHelper(pos.up(2)));
                  trapChest = false;
               }

               if (trapChest) {
                  for(Direction i : Direction.values()) {
                     if (i != Direction.DOWN && i != Direction.UP) {
                        BlockPos offsetPos = pos.offset(i).up();
                        if (BlockUtil.clientCanPlace(offsetPos.up(), this.breakCrystal.getValue()) && BlockUtil.canPlace(offsetPos, this.placeRange.getValue(), this.breakCrystal.getValue())) {
                           this.tryPlaceObsidian(offsetPos);
                           trapChest = false;
                           break;
                        }
                     }
                  }

                  if (trapChest) {
                     for(Direction i : Direction.values()) {
                        if (i != Direction.DOWN && i != Direction.UP) {
                           BlockPos offsetPos = pos.offset(i).up();
                           if (BlockUtil.clientCanPlace(offsetPos.up(), this.breakCrystal.getValue()) && BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue()) && this.getHelper(offsetPos) != null) {
                              this.tryPlaceObsidian(this.getHelper(offsetPos));
                              trapChest = false;
                              break;
                           }
                        }
                     }

                     if (trapChest) {
                        for(Direction i : Direction.values()) {
                           if (i != Direction.DOWN && i != Direction.UP) {
                              BlockPos offsetPos = pos.offset(i).up();
                              if (BlockUtil.clientCanPlace(offsetPos.up(), this.breakCrystal.getValue()) && BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue()) && this.getHelper(offsetPos) != null && BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), this.breakCrystal.getValue()) && this.getHelper(offsetPos.down()) != null) {
                                 this.tryPlaceObsidian(this.getHelper(offsetPos.down()));
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }

            this.tryPlaceBlock(pos.up(2), this.headAnchor.getValue());
         }

         if (this.antiStep.getValue() && (BlockUtil.isMining(pos.up(2)) || !this.onlyBreak.getValue())) {
            if (BlockUtil.getPlaceSide(pos.up(3)) == null && BlockUtil.clientCanPlace(pos.up(3), this.breakCrystal.getValue()) && this.getHelper(pos.up(3), Direction.DOWN) != null) {
               this.tryPlaceObsidian(this.getHelper(pos.up(3)));
            }

            this.tryPlaceObsidian(pos.up(3));
         }

         if (this.down.getValue()) {
            BlockPos offsetPos = pos.down();
            this.tryPlaceObsidian(offsetPos);
            if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue()) && this.getHelper(offsetPos) != null) {
               this.tryPlaceObsidian(this.getHelper(offsetPos));
            }
         }

         if (this.chestUp.getValue()) {
            for(Direction i : Direction.values()) {
               if (i != Direction.DOWN && i != Direction.UP) {
                  BlockPos offsetPos = pos.offset(i).up(2);
                  if (!this.onlyBreaking.getValue() || BlockUtil.isMining(pos.up(2))) {
                     this.tryPlaceObsidian(offsetPos);
                     if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue())) {
                        if (this.getHelper(offsetPos) != null) {
                           this.tryPlaceObsidian(this.getHelper(offsetPos));
                        } else if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), this.breakCrystal.getValue()) && this.getHelper(offsetPos.down()) != null) {
                           this.tryPlaceObsidian(this.getHelper(offsetPos.down()));
                        }
                     }
                  }
               }
            }
         }

         if (this.chest.getValue() && (!this.onlyGround.getValue() || this.target.isOnGround())) {
            for(Direction i : Direction.values()) {
               if (i != Direction.DOWN && i != Direction.UP) {
                  BlockPos offsetPos = pos.offset(i).up();
                  this.tryPlaceObsidian(offsetPos);
                  if (BlockUtil.getPlaceSide(offsetPos) == null && BlockUtil.clientCanPlace(offsetPos, this.breakCrystal.getValue())) {
                     if (this.getHelper(offsetPos) != null) {
                        this.tryPlaceObsidian(this.getHelper(offsetPos));
                     } else if (BlockUtil.getPlaceSide(offsetPos.down()) == null && BlockUtil.clientCanPlace(offsetPos.down(), this.breakCrystal.getValue()) && this.getHelper(offsetPos.down()) != null) {
                        this.tryPlaceObsidian(this.getHelper(offsetPos.down()));
                     }
                  }
               }
            }
         }

         if (this.extend.getValue()) {
            for(int x : new int[]{1, 0, -1}) {
               for(int z : new int[]{1, 0, -1}) {
                  BlockPos offsetPos = pos.add(x, 0, z);
                  if (this.checkEntity(new BlockPos(offsetPos))) {
                     this.doTrap(offsetPos);
                  }
               }
            }
         }

      }
   }

   public String getInfo() {
      return this.target != null ? this.target.getName().getString() : null;
   }

   public BlockPos getHelper(BlockPos pos) {
      if (!this.helper.getValue()) {
         return null;
      } else {
         for(Direction i : Direction.values()) {
            if ((!this.checkMine.getValue() || !BlockUtil.isMining(pos.offset(i))) && (CombatSetting.INSTANCE.placement.getValue() != Placement.Strict || BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) && BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue(), this.breakCrystal.getValue())) {
               return pos.offset(i);
            }
         }

         return null;
      }
   }

   public BlockPos getHelper(BlockPos pos, Direction ignore) {
      if (!this.helper.getValue()) {
         return null;
      } else {
         for(Direction i : Direction.values()) {
            if (i != ignore && (!this.checkMine.getValue() || !BlockUtil.isMining(pos.offset(i))) && BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true) && BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue(), this.breakCrystal.getValue())) {
               return pos.offset(i);
            }
         }

         return null;
      }
   }

   private boolean checkEntity(BlockPos pos) {
      if (mc.player.getBoundingBox().intersects(new Box(pos))) {
         return false;
      } else {
         for(Entity entity : mc.world.getNonSpectatingEntities(PlayerEntity.class, new Box(pos))) {
            if (entity.isAlive()) {
               return true;
            }
         }

         return false;
      }
   }

   private void tryPlaceBlock(BlockPos pos, boolean anchor) {
      if (this.pre.getValue()) {
         AutoTrap.AutoTrapRender.addBlock(pos);
      }

      if (!this.placeList.contains(pos)) {
         if (!BlockUtil.isMining(pos)) {
            if (BlockUtil.canPlace(pos, 6.0D, this.breakCrystal.getValue())) {
               if ((double)this.progress < this.blocksPer.getValue()) {
                  if (!((double)MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > this.placeRange.getValue())) {
                     int old = mc.player.getInventory().selectedSlot;
                     int block = anchor && this.getAnchor() != -1 ? this.getAnchor() : this.getBlock();
                     if (block != -1) {
                        if (!this.pre.getValue()) {
                           AutoTrap.AutoTrapRender.addBlock(pos);
                        }

                        this.placeList.add(pos);
                        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
                        this.doSwap(block);
                        BlockUtil.placeBlock(pos, this.rotate.getValue());
                        if (this.inventory.getValue()) {
                           this.doSwap(block);
                           EntityUtil.syncInventory();
                        } else {
                           this.doSwap(old);
                        }

                        this.timer.reset();
                        ++this.progress;
                     }
                  }
               }
            }
         }
      }
   }

   private void tryPlaceObsidian(BlockPos pos) {
      if (this.pre.getValue()) {
         AutoTrap.AutoTrapRender.addBlock(pos);
      }

      if (!this.placeList.contains(pos)) {
         if (!BlockUtil.isMining(pos)) {
            if (BlockUtil.canPlace(pos, 6.0D, this.breakCrystal.getValue())) {
               if ((double)this.progress < this.blocksPer.getValue()) {
                  if (!((double)MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(pos.toCenterPos())) > this.placeRange.getValue())) {
                     int old = mc.player.getInventory().selectedSlot;
                     int block = this.getBlock();
                     if (block != -1) {
                        if (!this.pre.getValue()) {
                           AutoTrap.AutoTrapRender.addBlock(pos);
                        }

                        this.placeList.add(pos);
                        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
                        this.doSwap(block);
                        BlockUtil.placeBlock(pos, this.rotate.getValue());
                        if (this.inventory.getValue()) {
                           this.doSwap(block);
                           EntityUtil.syncInventory();
                        } else {
                           this.doSwap(old);
                        }

                        this.timer.reset();
                        ++this.progress;
                     }
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

   private int getBlock() {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
   }

   private int getAnchor() {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
   }

   public class AutoTrapRender {
      public static final HashMap<BlockPos, AutoTrap.AutoTrapRender.placePosition> PlaceMap = new HashMap();

      public static void addBlock(BlockPos pos) {
         if (BlockUtil.clientCanPlace(pos, true) && !PlaceMap.containsKey(pos)) {
            PlaceMap.put(pos, new AutoTrap.AutoTrapRender.placePosition(pos));
         }

      }

      private void drawBlock(BlockPos pos, double alpha, Color color, MatrixStack matrixStack) {
         if (AutoTrap.this.sync.getValue()) {
            color = AutoTrap.INSTANCE.color.getValue();
         }

         Render3DUtil.draw3DBox(matrixStack, new Box(pos), ColorUtil.injectAlpha(color, (int)alpha), AutoTrap.this.outline.getValue(), AutoTrap.this.box.getValue());
      }

      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (AutoTrap.this.render.getValue()) {
            if (!PlaceMap.isEmpty()) {
               boolean shouldClear = true;

               for(AutoTrap.AutoTrapRender.placePosition placePosition : PlaceMap.values()) {
                  if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                     placePosition.isAir = false;
                  }

                  if (!placePosition.timer.passedMs((long)(AutoTrap.this.delay.getValue() + 100.0D)) && placePosition.isAir) {
                     placePosition.firstFade.reset();
                  }

                  if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) != 1.0D) {
                     shouldClear = false;
                     this.drawBlock(placePosition.pos, (double)AutoTrap.this.color.getValue().getAlpha() * (1.0D - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)), placePosition.posColor, event.getMatrixStack());
                  }
               }

               if (shouldClear) {
                  PlaceMap.clear();
               }

            }
         }
      }

      public static class placePosition {
         public final FadeUtils firstFade = new FadeUtils((long)AutoTrap.INSTANCE.fadeTime.getValue());
         public final BlockPos pos;
         public final Color posColor;
         public final Timer timer;
         public boolean isAir;

         public placePosition(BlockPos placePos) {
            this.pos = placePos;
            this.posColor = AutoTrap.INSTANCE.color.getValue();
            this.timer = new Timer();
            this.isAir = true;
            this.timer.reset();
         }
      }
   }

   public enum TargetMode {
      Single,
      Multi;

      // $FF: synthetic method
      private static AutoTrap.TargetMode[] $values() {
         return new AutoTrap.TargetMode[]{Single, Multi};
      }
   }
}
 
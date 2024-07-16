package me.nullpoint.mod.modules.impl.combat;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.MoveEvent;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FeetTrap extends Module {
   public static FeetTrap INSTANCE = new FeetTrap();
   private final Timer timer = new Timer();
   public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500));
   private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8));
   private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", false));
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
   private final BooleanSetting breakCrystal = this.add((new BooleanSetting("Break", true)).setParent());
   private final BooleanSetting usingPause = this.add(new BooleanSetting("EatingPause", true, (v) -> {
      return this.breakCrystal.isOpen();
   }));
   private final BooleanSetting center = this.add(new BooleanSetting("Center", true));
   public final BooleanSetting extend = this.add(new BooleanSetting("Extend", true));
   private final BooleanSetting inventory = this.add(new BooleanSetting("InventorySwap", true));
   public final BooleanSetting inAir = this.add(new BooleanSetting("InAir", true));
   private final BooleanSetting moveDisable = this.add(new BooleanSetting("AutoDisable", true));
   private final BooleanSetting jumpDisable = this.add(new BooleanSetting("JumpDisable", true));
   private final BooleanSetting enderChest = this.add(new BooleanSetting("EnderChest", true));
   public final BooleanSetting render = this.add((new BooleanSetting("Render", true)).setParent());
   final ColorSetting box = this.add((new ColorSetting("Box", new Color(255, 255, 255, 255), (v) -> {
      return this.render.isOpen();
   })).injectBoolean(true));
   final ColorSetting fill = this.add((new ColorSetting("Fill", new Color(255, 255, 255, 100), (v) -> {
      return this.render.isOpen();
   })).injectBoolean(true));
   public final SliderSetting fadeTime = this.add(new SliderSetting("FadeTime", 500, 0, 5000, (v) -> {
      return this.render.getValue();
   }));
   public final BooleanSetting pre = this.add(new BooleanSetting("Pre", false, (v) -> {
      return this.render.getValue();
   }));
   public final BooleanSetting moveReset = this.add(new BooleanSetting("Reset", true, (v) -> {
      return this.render.getValue();
   }));
   double startX = 0.0;
   double startY = 0.0;
   double startZ = 0.0;
   int progress = 0;
   private boolean shouldCenter = true;

   public FeetTrap() {
      super("FeetTrap", "Surrounds you with Obsidian", Module.Category.Combat);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new FeetTrapRenderer());
   }

   public void onEnable() {
      if (!nullCheck()) {
         this.startX = mc.player.getX();
         this.startY = mc.player.getY();
         this.startZ = mc.player.getZ();
         this.shouldCenter = true;
      } else {
         if (this.moveDisable.getValue() || this.jumpDisable.getValue()) {
            this.disable();
         }

      }
   }

   @EventHandler(
      priority = -1
   )
   public void onMove(MoveEvent event) {
      if (!nullCheck() && this.center.getValue() && !EntityUtil.isElytraFlying()) {
         BlockPos blockPos = EntityUtil.getPlayerPos(true);
         if (mc.player.getX() - (double)blockPos.getX() - 0.5 <= 0.2 && mc.player.getX() - (double)blockPos.getX() - 0.5 >= -0.2 && mc.player.getZ() - (double)blockPos.getZ() - 0.5 <= 0.2 && mc.player.getZ() - 0.5 - (double)blockPos.getZ() >= -0.2) {
            if (this.shouldCenter && (mc.player.isOnGround() || MovementUtil.isMoving())) {
               event.setX(0.0);
               event.setZ(0.0);
               this.shouldCenter = false;
            }
         } else if (this.shouldCenter) {
            Vec3d centerPos = EntityUtil.getPlayerPos(true).toCenterPos();
            float rotation = getRotationTo(mc.player.getPos(), centerPos).x;
            float yawRad = rotation / 180.0F * 3.1415927F;
            double dist = mc.player.getPos().distanceTo(new Vec3d(centerPos.x, mc.player.getY(), centerPos.z));
            double cappedSpeed = Math.min(0.2873, dist);
            double x = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
            double z = (double)((float)Math.cos(yawRad)) * cappedSpeed;
            event.setX(x);
            event.setZ(z);
         }

      }
   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (!event.isPost() && this.timer.passedMs((long)this.placeDelay.getValue())) {
         this.progress = 0;
         if (!MovementUtil.isMoving() && !mc.options.jumpKey.isPressed()) {
            this.startX = mc.player.getX();
            this.startY = mc.player.getY();
            this.startZ = mc.player.getZ();
         }

         BlockPos pos = EntityUtil.getPlayerPos(true);
         double distanceToStart = MathHelper.sqrt((float)mc.player.squaredDistanceTo(this.startX, this.startY, this.startZ));
         if (this.getBlock() == -1) {
            CommandManager.sendChatMessage("\u00a7e[?] \u00a7c\u00a7oObsidian" + (this.enderChest.getValue() ? "/EnderChest" : "") + "?");
            this.disable();
         } else if ((!this.moveDisable.getValue() || !(distanceToStart > 1.0)) && (!this.jumpDisable.getValue() || !(Math.abs(this.startY - mc.player.getY()) > 0.5))) {
            if (this.inAir.getValue() || mc.player.isOnGround()) {
               Direction[] var5 = Direction.values();
               int var6 = var5.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  Direction i = var5[var7];
                  if (i != Direction.UP) {
                     BlockPos offsetPos = pos.offset(i);
                     if (BlockUtil.getPlaceSide(offsetPos) != null) {
                        this.tryPlaceBlock(offsetPos);
                     } else if (BlockUtil.canReplace(offsetPos)) {
                        this.tryPlaceBlock(this.getHelperPos(offsetPos));
                     }

                     if (selfIntersectPos(offsetPos) && this.extend.getValue()) {
                        Direction[] var10 = Direction.values();
                        int var11 = var10.length;

                        for(int var12 = 0; var12 < var11; ++var12) {
                           Direction i2 = var10[var12];
                           if (i2 != Direction.UP) {
                              BlockPos offsetPos2 = offsetPos.offset(i2);
                              if (selfIntersectPos(offsetPos2)) {
                                 Direction[] var15 = Direction.values();
                                 int var16 = var15.length;

                                 for(int var17 = 0; var17 < var16; ++var17) {
                                    Direction i3 = var15[var17];
                                    if (i3 != Direction.UP) {
                                       this.tryPlaceBlock(offsetPos2);
                                       BlockPos offsetPos3 = offsetPos2.offset(i3);
                                       this.tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos3) == null && BlockUtil.canReplace(offsetPos3) ? this.getHelperPos(offsetPos3) : offsetPos3);
                                    }
                                 }
                              }

                              this.tryPlaceBlock(BlockUtil.getPlaceSide(offsetPos2) == null && BlockUtil.canReplace(offsetPos2) ? this.getHelperPos(offsetPos2) : offsetPos2);
                           }
                        }
                     }
                  }
               }

            }
         } else {
            this.disable();
         }
      }
   }

   private void tryPlaceBlock(BlockPos pos) {
      if (pos != null) {
         if (!this.detectMining.getValue() || !BlockUtil.isMining(pos)) {
            if (this.pre.getValue() && BlockUtil.clientCanPlace(pos, true)) {
               FeetTrap.FeetTrapRenderer.addBlock(pos);
            }

            if ((double)this.progress < this.blocksPer.getValue()) {
               int block = this.getBlock();
               if (block != -1) {
                  if (BlockUtil.canPlace(pos, 6.0, true)) {
                     if (this.breakCrystal.getValue()) {
                        CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.usingPause.getValue());
                     } else if (BlockUtil.hasEntity(pos, false)) {
                        return;
                     }

                     if (this.rotate.getValue()) {
                        RotateManager.lastEvent.cancelRotate();
                     }

                     int old = mc.player.getInventory().selectedSlot;
                     this.doSwap(block);
                     BlockUtil.placeBlock(pos, this.rotate.getValue(), this.packetPlace.getValue());
                     if (this.inventory.getValue()) {
                        this.doSwap(block);
                        EntityUtil.syncInventory();
                     } else {
                        this.doSwap(old);
                     }

                     ++this.progress;
                     this.timer.reset();
                     FeetTrap.FeetTrapRenderer.addBlock(pos);
                  }
               }
            }
         }
      }
   }

   public static boolean selfIntersectPos(BlockPos pos) {
      return mc.player.getBoundingBox().intersects(new Box(pos));
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private int getBlock() {
      if (this.inventory.getValue()) {
         return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN) == -1 && this.enderChest.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.ENDER_CHEST) : InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
      } else {
         return InventoryUtil.findBlock(Blocks.OBSIDIAN) == -1 && this.enderChest.getValue() ? InventoryUtil.findBlock(Blocks.ENDER_CHEST) : InventoryUtil.findBlock(Blocks.OBSIDIAN);
      }
   }

   public BlockPos getHelperPos(BlockPos pos) {
      Direction[] var2 = Direction.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Direction i = var2[var4];
         if ((!this.detectMining.getValue() || !BlockUtil.isMining(pos.offset(i))) && BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true) && BlockUtil.canPlace(pos.offset(i))) {
            return pos.offset(i);
         }
      }

      return null;
   }

   public static Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
      Vec3d vec3d = posTo.subtract(posFrom);
      return getRotationFromVec(vec3d);
   }

   private static Vec2f getRotationFromVec(Vec3d vec) {
      double d = vec.x;
      double d2 = vec.z;
      double xz = Math.hypot(d, d2);
      d2 = vec.z;
      double d3 = vec.x;
      double yaw = normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
      double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
      return new Vec2f((float)yaw, (float)pitch);
   }

   private static double normalizeAngle(double angleIn) {
      double angle = angleIn;
      if ((angle %= 360.0) >= 180.0) {
         angle -= 360.0;
      }

      if (angle < -180.0) {
         angle += 360.0;
      }

      return angle;
   }

   public class FeetTrapRenderer {
      public static final HashMap renderMap = new HashMap();
      private BlockPos lastPos = null;

      public static void addBlock(BlockPos pos) {
         renderMap.put(pos, new placePosition(pos));
      }

      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (FeetTrap.INSTANCE.render.getValue()) {
            if (FeetTrap.INSTANCE.moveReset.getValue() && !EntityUtil.getPlayerPos(true).equals(this.lastPos)) {
               this.lastPos = EntityUtil.getPlayerPos(true);
               renderMap.clear();
            }

            if (!renderMap.isEmpty()) {
               boolean shouldClear = true;
               Iterator var3 = renderMap.values().iterator();

               while(var3.hasNext()) {
                  placePosition placePosition = (placePosition)var3.next();
                  if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                     placePosition.isAir = false;
                  }

                  if (!placePosition.timer.passedMs((long)(FeetTrap.this.placeDelay.getValue() + 100.0)) && placePosition.isAir) {
                     placePosition.firstFade.reset();
                  }

                  if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) != 1.0) {
                     shouldClear = false;
                     MatrixStack matrixStack = event.getMatrixStack();
                     if (FeetTrap.INSTANCE.fill.booleanValue) {
                        Render3DUtil.drawFill(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(FeetTrap.INSTANCE.fill.getValue(), (int)((double)FeetTrap.this.fill.getValue().getAlpha() * (1.0 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                     }

                     if (FeetTrap.INSTANCE.box.booleanValue) {
                        Render3DUtil.drawBox(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(FeetTrap.INSTANCE.box.getValue(), (int)((double)FeetTrap.this.box.getValue().getAlpha() * (1.0 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                     }
                  }
               }

               if (shouldClear) {
                  renderMap.clear();
               }

            }
         }
      }

      public static class placePosition {
         public final FadeUtils firstFade;
         public final BlockPos pos;
         public final Timer timer;
         public boolean isAir;

         public placePosition(BlockPos placePos) {
            this.firstFade = new FadeUtils((long)FeetTrap.INSTANCE.fadeTime.getValue());
            this.pos = placePos;
            this.timer = new Timer();
            this.isAir = true;
         }
      }
   }
}

// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import java.util.ArrayList;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.combat.WebAura;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WebAura extends Module {
   public static WebAura INSTANCE;
   public final EnumSetting<WebAura.Page> page = this.add(new EnumSetting("Page", WebAura.Page.General));
   public final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, (v) -> this.page.getValue() == WebAura.Page.General));
   public final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 2, 1, 10, (v) -> this.page.getValue() == WebAura.Page.General));
   public final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 2.0D, 0.0D, 50.0D, 1.0D, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting extend = this.add(new BooleanSetting("Extend", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting extendFace = this.add(new BooleanSetting("ExtendFace", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting leg = this.add(new BooleanSetting("Leg", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting down = this.add(new BooleanSetting("Down", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting noHole = this.add(new BooleanSetting("NoHole", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, (v) -> this.page.getValue() == WebAura.Page.General));
   public final SliderSetting placeRange = this.add(new SliderSetting("PlaceRange", 5.0D, 0.0D, 6.0D, 0.1D, (v) -> this.page.getValue() == WebAura.Page.General));
   public final SliderSetting targetRange = this.add(new SliderSetting("TargetRange", 8.0D, 0.0D, 8.0D, 0.1D, (v) -> this.page.getValue() == WebAura.Page.General));
   private final BooleanSetting rotate = this.add((new BooleanSetting("Rotate", true, (v) -> this.page.getValue() == WebAura.Page.Rotate)).setParent());
   private final BooleanSetting newRotate = this.add(new BooleanSetting("NewRotate", false, (v) -> this.rotate.isOpen() && this.page.getValue() == WebAura.Page.Rotate));
   private final SliderSetting yawStep = this.add(new SliderSetting("YawStep", 0.3F, 0.1F, 1.0D, 0.01F, (v) -> this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == WebAura.Page.Rotate));
   private final BooleanSetting checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == WebAura.Page.Rotate));
   private final SliderSetting fov = this.add(new SliderSetting("Fov", 5.0D, 0.0D, 30.0D, (v) -> this.rotate.isOpen() && this.newRotate.getValue() && this.checkLook.getValue() && this.page.getValue() == WebAura.Page.Rotate));
   private final Timer timer = new Timer();
   public Vec3d directionVec = null;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;
   int progress = 0;
   private final ArrayList<BlockPos> pos = new ArrayList();

   public WebAura() {
      super("WebAura", Module.Category.Combat);
      INSTANCE = this;
   }

   public String getInfo() {
      return this.pos.isEmpty() ? null : "Working";
   }

   @EventHandler(
           priority = 98
   )
   public void onRotate(RotateEvent event) {
      if (this.newRotate.getValue() && this.directionVec != null) {
         float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValueFloat());
         this.lastYaw = newAngle[0];
         this.lastPitch = newAngle[1];
         event.setYaw(this.lastYaw);
         event.setPitch(this.lastPitch);
      } else {
         this.lastYaw = Nullpoint.ROTATE.lastYaw;
         this.lastPitch = Nullpoint.ROTATE.lastPitch;
      }

   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (!event.isPost() && this.timer.passedMs(this.placeDelay.getValueInt())) {
         this.pos.clear();
         this.progress = 0;
         this.directionVec = null;
         if (this.getWebSlot() != -1) {
            if (!this.usingPause.getValue() || !mc.player.isUsingItem()) {
               label159:
               for(PlayerEntity player : CombatUtil.getEnemies(this.targetRange.getValue())) {
                  Vec3d playerPos = this.predictTicks.getValue() > 0.0D ? CombatUtil.getEntityPosVec(player, this.predictTicks.getValueInt()) : player.getPos();
                  if (!this.leg.getValue() || this.noHole.getValue() && BlockUtil.isHole(EntityUtil.getEntityPos(player, true)) || !this.placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
                     if (this.down.getValue()) {
                        this.placeWeb(new BlockPosX(playerPos.getX(), playerPos.getY() - 0.8D, playerPos.getZ()));
                     }

                     boolean skip = false;
                     if (this.extend.getValue() || this.extendFace.getValue()) {
                        for(float x : new float[]{0.0F, 0.3F, -0.3F}) {
                           for(float z : new float[]{0.0F, 0.3F, -0.3F}) {
                              for(float y : new float[]{0.0F, 1.0F, -1.0F}) {
                                 BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY() + (double)y, playerPos.getZ() + (double)z);
                                 if (this.isTargetHere(pos, player) && mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB && !BlockUtil.isMining(pos)) {
                                    skip = true;
                                 }
                              }
                           }
                        }

                        if (!skip) {
                           if (this.extend.getValue()) {
                              label134:
                              for(float x : new float[]{0.0F, 0.3F, -0.3F}) {
                                 for(float z : new float[]{0.0F, 0.3F, -0.3F}) {
                                    BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY(), playerPos.getZ() + (double)z);
                                    if (!pos.equals(new BlockPosX(playerPos.getX(), playerPos.getY(), playerPos.getZ())) && this.isTargetHere(pos, player) && this.placeWeb(pos)) {
                                       skip = true;
                                       break label134;
                                    }
                                 }
                              }
                           }

                           if (!skip && this.extendFace.getValue()) {
                              for(float x : new float[]{0.0F, 0.3F, -0.3F}) {
                                 for(float z : new float[]{0.0F, 0.3F, -0.3F}) {
                                    BlockPosX pos = new BlockPosX(playerPos.getX() + (double)x, playerPos.getY() + 1.1D, playerPos.getZ() + (double)z);
                                    if (this.isTargetHere(pos, player) && this.placeWeb(pos)) {
                                       continue label159;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

            }
         }
      }
   }

   private boolean isTargetHere(BlockPos pos, PlayerEntity target) {
      return (new Box(pos)).intersects(target.getBoundingBox());
   }

   private boolean placeWeb(BlockPos pos) {
      if (this.pos.contains(pos)) {
         return false;
      } else {
         this.pos.add(pos);
         if (this.progress >= this.blocksPer.getValueInt()) {
            return false;
         } else if (this.getWebSlot() == -1) {
            return false;
         } else if (!this.detectMining.getValue() || !Nullpoint.BREAK.isMining(pos) && !pos.equals(SpeedMine.breakPos)) {
            if (BlockUtil.getPlaceSide(pos, this.placeRange.getValue()) != null && mc.world.isAir(pos)) {
               int oldSlot = mc.player.getInventory().selectedSlot;
               int webSlot = this.getWebSlot();
               if (!this.placeBlock(pos, this.rotate.getValue(), webSlot)) {
                  return false;
               } else {
                  ++this.progress;
                  if (this.inventorySwap.getValue()) {
                     this.doSwap(webSlot);
                     EntityUtil.syncInventory();
                  } else {
                     this.doSwap(oldSlot);
                  }

                  this.timer.reset();
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {
      if (BlockUtil.airPlace()) {
         for(Direction i : Direction.values()) {
            if (mc.world.isAir(pos.offset(i))) {
               return this.clickBlock(pos, i, rotate, slot);
            }
         }
      }

      Direction side = BlockUtil.getPlaceSide(pos);
      if (side == null) {
         return false;
      } else {
         Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5D + (double)side.getVector().getX() * 0.5D, (double)pos.getY() + 0.5D + (double)side.getVector().getY() * 0.5D, (double)pos.getZ() + 0.5D + (double)side.getVector().getZ() * 0.5D);
         BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
         BlockUtil.placedPos.add(pos);
         boolean sprint = false;
         if (mc.player != null) {
            sprint = mc.player.isSprinting();
         }

         boolean sneak = false;
         if (mc.world != null) {
            sneak = needSneak(mc.world.getBlockState(result.getBlockPos()).getBlock()) && !mc.player.isSneaking();
         }

         if (sprint) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
         }

         if (sneak) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
         }

         this.clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
         if (sneak) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
         }

         if (sprint) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
         }

         EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
         return true;
      }
   }

   public static boolean needSneak(Block in) {
      return BlockUtil.shiftBlocks.contains(in);
   }

   public boolean clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
      Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5D + (double)side.getVector().getX() * 0.5D, (double)pos.getY() + 0.5D + (double)side.getVector().getY() * 0.5D, (double)pos.getZ() + 0.5D + (double)side.getVector().getZ() * 0.5D);
      if (rotate && !this.faceVector(directionVec)) {
         return false;
      } else {
         this.doSwap(slot);
         EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
         BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
         mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(mc.world)));
         return true;
      }
   }

   private boolean faceVector(Vec3d directionVec) {
      if (!this.newRotate.getValue()) {
         RotateManager.lastEvent.cancelRotate();
         EntityUtil.faceVector(directionVec);
         return true;
      } else {
         this.directionVec = directionVec;
         float[] angle = EntityUtil.getLegitRotations(directionVec);
         if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValueFloat()) {
            EntityUtil.sendYawAndPitch(angle[0], angle[1]);
            return true;
         } else {
            return !this.checkLook.getValue();
         }
      }
   }

   private float[] injectStep(float[] angle, float steps) {
      if (steps < 0.01F) {
         steps = 0.01F;
      }

      if (steps > 1.0F) {
         steps = 1.0F;
      }

      if (steps < 1.0F && angle != null) {
         float packetYaw = this.lastYaw;
         float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);
         if (Math.abs(diff) > 90.0F * steps) {
            angle[0] = packetYaw + diff * (90.0F * steps / Math.abs(diff));
         }

         float packetPitch = this.lastPitch;
         diff = angle[1] - packetPitch;
         if (Math.abs(diff) > 90.0F * steps) {
            angle[1] = packetPitch + diff * (90.0F * steps / Math.abs(diff));
         }
      }

      return new float[]{angle[0], angle[1]};
   }

   private void doSwap(int slot) {
      if (this.inventorySwap.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private int getWebSlot() {
      return this.inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.COBWEB) : InventoryUtil.findBlock(Blocks.COBWEB);
   }

   public enum Page {
      General,
      Rotate;

      // $FF: synthetic method
      private static WebAura.Page[] $values() {
         return new WebAura.Page[]{General, Rotate};
      }
   }
}

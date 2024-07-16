package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class HoleFiller extends Module {
   public static HoleFiller INSTANCE;
   public final EnumSetting page;
   public final SliderSetting delay;
   public final SliderSetting blocksPer;
   private final BooleanSetting detectMining;
   private final BooleanSetting usingPause;
   private final BooleanSetting inventory;
   private final BooleanSetting webs;
   private final SliderSetting range;
   private final SliderSetting saferange;
   public final SliderSetting placeRange;
   private final SliderSetting targetRange;
   public final BooleanSetting any;
   public final BooleanSetting doubleHole;
   private final SliderSetting predictTicks;
   private final BooleanSetting rotate;
   private final BooleanSetting newRotate;
   private final SliderSetting yawStep;
   private final BooleanSetting checkLook;
   private final SliderSetting fov;
   private PlayerEntity closestTarget;
   private final Timer timer;
   public Vec3d directionVec;
   private float lastYaw;
   private float lastPitch;
   int progress;

   public HoleFiller() {
      super("HoleFiller", "Fills all safe spots in radius", Module.Category.Combat);
      this.page = this.add(new EnumSetting("Page", HoleFiller.Page.General));
      this.delay = this.add(new SliderSetting("Delay", 50, 0, 500, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.blocksPer = this.add(new SliderSetting("BlocksPer", 2, 1, 10, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.detectMining = this.add(new BooleanSetting("DetectMining", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.usingPause = this.add(new BooleanSetting("UsingPause", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.inventory = this.add(new BooleanSetting("InventorySwap", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.webs = this.add(new BooleanSetting("Webs", false, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.range = this.add(new SliderSetting("Radius", 1.9, 0.0, 6.0, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.saferange = this.add(new SliderSetting("SafeRange", 1.4, 0.0, 6.0, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.placeRange = this.add(new SliderSetting("PlaceRange", 5.0, 0.0, 6.0, 0.1, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.targetRange = this.add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.any = this.add(new BooleanSetting("AnyHole", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.doubleHole = this.add(new BooleanSetting("DoubleHole", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.predictTicks = this.add(new SliderSetting("PredictTicks", 4, 0, 10, (v) -> {
         return this.page.getValue() == HoleFiller.Page.General;
      }));
      this.rotate = this.add((new BooleanSetting("Rotate", true, (v) -> {
         return this.page.getValue() == HoleFiller.Page.Rotate;
      })).setParent());
      this.newRotate = this.add(new BooleanSetting("NewRotate", false, (v) -> {
         return this.rotate.isOpen() && this.page.getValue() == HoleFiller.Page.Rotate;
      }));
      this.yawStep = this.add(new SliderSetting("YawStep", 0.30000001192092896, 0.10000000149011612, 1.0, 0.009999999776482582, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == HoleFiller.Page.Rotate;
      }));
      this.checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == HoleFiller.Page.Rotate;
      }));
      this.fov = this.add(new SliderSetting("Fov", 5.0, 0.0, 30.0, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.checkLook.getValue() && this.page.getValue() == HoleFiller.Page.Rotate;
      }));
      this.timer = new Timer();
      this.directionVec = null;
      this.lastYaw = 0.0F;
      this.lastPitch = 0.0F;
      this.progress = 0;
      INSTANCE = this;
   }

   public void onDisable() {
      this.closestTarget = null;
   }

   public String getInfo() {
      return "";
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

   public void onUpdate() {
      if (!nullCheck()) {
         if (this.timer.passedMs(this.delay.getValueInt())) {
            if (!this.usingPause.getValue() || !mc.player.isUsingItem()) {
               this.progress = 0;
               this.directionVec = null;
               this.timer.reset();
               int obbySlot = this.findBlock(Blocks.OBSIDIAN);
               int eChestSlot = this.findBlock(Blocks.ENDER_CHEST);
               int webSlot = this.findBlock(Blocks.COBWEB);
               int block = this.webs.getValue() ? (webSlot == -1 ? (obbySlot == -1 ? eChestSlot : obbySlot) : webSlot) : (obbySlot == -1 ? eChestSlot : obbySlot);
               if (this.webs.getValue() || obbySlot != -1 || eChestSlot != -1) {
                  if (!this.webs.getValue() || webSlot != -1 || obbySlot != -1 || eChestSlot != -1) {
                     ArrayList list = new ArrayList();
                     Iterator var6 = CombatUtil.getEnemies(this.targetRange.getRange()).iterator();

                     while(var6.hasNext()) {
                        PlayerEntity target = (PlayerEntity)var6.next();
                        list.add(new PlayerAndPredict(target));
                     }

                     new PlayerAndPredict(mc.player);
                     if (!list.isEmpty()) {
                        Iterator var12 = list.iterator();

                        label117:
                        while(var12.hasNext()) {
                           PlayerAndPredict pap = (PlayerAndPredict)var12.next();
                           Iterator var9 = BlockUtil.getSphere(this.range.getValueFloat(), pap.player.getPos()).iterator();

                           while(true) {
                              BlockPos pos;
                              do {
                                 do {
                                    do {
                                       if (!var9.hasNext()) {
                                          continue label117;
                                       }

                                       pos = (BlockPos)var9.next();
                                    } while(!BlockUtil.isHole(pos, true, true, this.any.getValue()) && (!this.doubleHole.getValue() || !CombatUtil.isDoubleHole(pos)));
                                 } while(mc.player.squaredDistanceTo(pos.toCenterPos()) < this.saferange.getValue());
                              } while(this.detectMining.getValue() && (Nullpoint.BREAK.isMining(pos) || pos.equals(SpeedMine.breakPos)));

                              if (this.progress < this.blocksPer.getValueInt() && BlockUtil.canPlace(pos, this.placeRange.getValue()) && BlockUtil.getPlaceSide(pos, this.placeRange.getValue()) != null && mc.world.isAir(pos)) {
                                 int oldSlot = mc.player.getInventory().selectedSlot;
                                 if (this.placeBlock(pos, this.rotate.getValue(), block)) {
                                    ++this.progress;
                                    if (this.inventory.getValue()) {
                                       this.doSwap(block);
                                       EntityUtil.syncInventory();
                                    } else {
                                       this.doSwap(oldSlot);
                                    }

                                    this.timer.reset();
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

   public boolean placeBlock(BlockPos pos, boolean rotate, int slot) {
      if (BlockUtil.airPlace()) {
         Direction[] var4 = Direction.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Direction i = var4[var6];
            if (mc.world.isAir(pos.offset(i))) {
               return this.clickBlock(pos, i, rotate, slot);
            }
         }
      }

      Direction side = BlockUtil.getPlaceSide(pos);
      if (side == null) {
         return false;
      } else {
         Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
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
      Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
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

   public int findBlock(Block blockIn) {
      return this.inventory.getValue() ? InventoryUtil.findBlockInventorySlot(blockIn) : InventoryUtil.findBlock(blockIn);
   }

   private void doSwap(int slot) {
      if (this.inventory.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   public enum Page {
      General,
      Rotate;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{General, Rotate};
      }
   }

   public class PlayerAndPredict {
      PlayerEntity player;
      PlayerEntity predict;

      public PlayerAndPredict(PlayerEntity player) {
         this.player = player;
         if (HoleFiller.this.predictTicks.getValueFloat() > 0.0F) {
            this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            };
            this.predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)HoleFiller.INSTANCE.predictTicks.getValueInt(), true)));
            this.predict.setHealth(player.getHealth());
            this.predict.prevX = player.prevX;
            this.predict.prevZ = player.prevZ;
            this.predict.prevY = player.prevY;
            this.predict.setOnGround(player.isOnGround());
            this.predict.getInventory().clone(player.getInventory());
            this.predict.setPose(player.getPose());
            Iterator var3 = player.getStatusEffects().iterator();

            while(var3.hasNext()) {
               StatusEffectInstance se = (StatusEffectInstance)var3.next();
               this.predict.addStatusEffect(se);
            }
         } else {
            this.predict = player;
         }

      }
   }
}

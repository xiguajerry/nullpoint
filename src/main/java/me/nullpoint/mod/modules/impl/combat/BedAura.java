package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.combat.ExplosionUtil;
import me.nullpoint.api.utils.combat.MeteorExplosionUtil;
import me.nullpoint.api.utils.combat.OyveyExplosionUtil;
import me.nullpoint.api.utils.combat.ThunderExplosionUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Beta
public class BedAura extends Module {
   public static BedAura INSTANCE;
   public final EnumSetting page;
   private final BooleanSetting yawDeceive;
   private final BooleanSetting checkMine;
   private final BooleanSetting noUsing;
   private final EnumSetting calcMode;
   private final EnumSetting swingMode;
   private final SliderSetting antiSuicide;
   private final SliderSetting targetRange;
   private final SliderSetting updateDelay;
   private final SliderSetting calcDelay;
   private final BooleanSetting inventorySwap;
   private final BooleanSetting rotate;
   private final BooleanSetting newRotate;
   private final SliderSetting yawStep;
   private final BooleanSetting random;
   private final BooleanSetting sync;
   private final BooleanSetting checkLook;
   private final SliderSetting fov;
   private final BooleanSetting place;
   private final SliderSetting placeDelay;
   private final BooleanSetting Break;
   private final SliderSetting breakDelay;
   private final SliderSetting range;
   private final SliderSetting placeMinDamage;
   private final SliderSetting placeMaxSelf;
   private final BooleanSetting smart;
   private final BooleanSetting breakOnlyHasCrystal;
   private final BooleanSetting render;
   private final BooleanSetting shrink;
   private final BooleanSetting outline;
   private final SliderSetting outlineAlpha;
   private final BooleanSetting box;
   private final SliderSetting boxAlpha;
   private final BooleanSetting reset;
   private final ColorSetting color;
   private final SliderSetting animationTime;
   private final SliderSetting startFadeTime;
   private final SliderSetting fadeTime;
   private final SliderSetting predictTicks;
   private final BooleanSetting terrainIgnore;
   public static BlockPos placePos;
   private final Timer delayTimer;
   private final Timer calcTimer;
   private final Timer breakTimer;
   private final Timer placeTimer;
   private final Timer noPosTimer;
   private final FadeUtils fadeUtils;
   private final FadeUtils animation;
   double lastSize;
   private PlayerEntity displayTarget;
   private float lastYaw;
   private float lastPitch;
   public float lastDamage;
   public Vec3d directionVec;
   private BlockPos renderPos;
   private Box lastBB;
   private Box nowBB;

   public BedAura() {
      super("BedAura", Module.Category.Combat);
      this.page = this.add(new EnumSetting("Page", BedAura.Page.General));
      this.yawDeceive = this.add(new BooleanSetting("YawDeceive", true, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.checkMine = this.add(new BooleanSetting("DetectMining", true, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.noUsing = this.add(new BooleanSetting("EatingPause", true, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.calcMode = this.add(new EnumSetting("CalcMode", AnchorAura.CalcMode.OyVey, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.swingMode = this.add(new EnumSetting("Swing", SwingSide.Server, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.antiSuicide = this.add(new SliderSetting("AntiSuicide", 3.0, 0.0, 10.0, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.targetRange = this.add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.updateDelay = this.add(new SliderSetting("UpdateDelay", 50, 0, 1000, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.calcDelay = this.add(new SliderSetting("CalcDelay", 200, 0, 1000, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.inventorySwap = this.add(new BooleanSetting("InventorySwap", true, (v) -> {
         return this.page.getValue() == BedAura.Page.General;
      }));
      this.rotate = this.add((new BooleanSetting("Rotate", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Rotate;
      })).setParent());
      this.newRotate = this.add(new BooleanSetting("NewRotate", false, (v) -> {
         return this.rotate.isOpen() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.yawStep = this.add(new SliderSetting("YawStep", 0.30000001192092896, 0.10000000149011612, 1.0, 0.009999999776482582, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.random = this.add(new BooleanSetting("Random", true, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.sync = this.add(new BooleanSetting("Sync", false, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.fov = this.add(new SliderSetting("Fov", 5.0, 0.0, 30.0, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.checkLook.getValue() && this.page.getValue() == BedAura.Page.Rotate;
      }));
      this.place = this.add(new BooleanSetting("Place", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.placeDelay = this.add(new SliderSetting("PlaceDelay", 300, 0, 1000, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc && this.place.getValue();
      }));
      this.Break = this.add(new BooleanSetting("Break", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.breakDelay = this.add(new SliderSetting("BreakDelay", 300, 0, 1000, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc && this.Break.getValue();
      }));
      this.range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.placeMinDamage = this.add(new SliderSetting("MinDamage", 5.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.placeMaxSelf = this.add(new SliderSetting("MaxSelfDamage", 12.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.smart = this.add(new BooleanSetting("Smart", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc;
      }));
      this.breakOnlyHasCrystal = this.add(new BooleanSetting("OnlyHasBed", false, (v) -> {
         return this.page.getValue() == BedAura.Page.Calc && this.Break.getValue();
      }));
      this.render = this.add(new BooleanSetting("Render", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Render;
      }));
      this.shrink = this.add(new BooleanSetting("Shrink", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.outline = this.add((new BooleanSetting("Outline", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      })).setParent());
      this.outlineAlpha = this.add(new SliderSetting("OutlineAlpha", 150, 0, 255, (v) -> {
         return this.outline.isOpen() && this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.box = this.add((new BooleanSetting("Box", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      })).setParent());
      this.boxAlpha = this.add(new SliderSetting("BoxAlpha", 70, 0, 255, (v) -> {
         return this.box.isOpen() && this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.reset = this.add(new BooleanSetting("Reset", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.color = this.add(new ColorSetting("Color", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.animationTime = this.add(new SliderSetting("AnimationTime", 2.0, 0.0, 8.0, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.startFadeTime = this.add(new SliderSetting("StartFadeTime", 0.3, 0.0, 2.0, 0.01, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.fadeTime = this.add(new SliderSetting("FadeTime", 0.3, 0.0, 2.0, 0.01, (v) -> {
         return this.page.getValue() == BedAura.Page.Render && this.render.getValue();
      }));
      this.predictTicks = this.add(new SliderSetting("PredictTicks", 4, 0, 10, (v) -> {
         return this.page.getValue() == BedAura.Page.Predict;
      }));
      this.terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true, (v) -> {
         return this.page.getValue() == BedAura.Page.Predict;
      }));
      this.delayTimer = new Timer();
      this.calcTimer = new Timer();
      this.breakTimer = new Timer();
      this.placeTimer = new Timer();
      this.noPosTimer = new Timer();
      this.fadeUtils = new FadeUtils(500L);
      this.animation = new FadeUtils(500L);
      this.lastSize = 0.0;
      this.lastYaw = 0.0F;
      this.lastPitch = 0.0F;
      this.directionVec = null;
      this.renderPos = null;
      this.lastBB = null;
      this.nowBB = null;
      INSTANCE = this;
   }

   public String getInfo() {
      return this.displayTarget != null && placePos != null ? this.displayTarget.getName().getString() : super.getInfo();
   }

   public void onEnable() {
      this.lastYaw = Nullpoint.ROTATE.lastYaw;
      this.lastPitch = Nullpoint.ROTATE.lastPitch;
   }

   @EventHandler
   public void onRotate(RotateEvent event) {
      if (placePos != null && this.newRotate.getValue() && this.directionVec != null) {
         float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.yawStep.getValueFloat());
         this.lastYaw = newAngle[0];
         this.lastPitch = newAngle[1];
         if (this.random.getValue() && (new Random()).nextBoolean()) {
            this.lastPitch = Math.min((new Random()).nextFloat() * 2.0F + this.lastPitch, 90.0F);
         }

         event.setYaw(this.lastYaw);
         event.setPitch(this.lastPitch);
      } else {
         this.lastYaw = Nullpoint.ROTATE.lastYaw;
         this.lastPitch = Nullpoint.ROTATE.lastPitch;
      }

   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      this.update();
   }

   public void onUpdate() {
      this.update();
   }

   private void update() {
      if (!nullCheck()) {
         this.animUpdate();
         if (this.delayTimer.passedMs((long)this.updateDelay.getValue())) {
            if (this.noUsing.getValue() && EntityUtil.isUsing()) {
               placePos = null;
            } else if (mc.player.isSneaking()) {
               placePos = null;
            } else if (mc.world.getRegistryKey().equals(World.OVERWORLD)) {
               placePos = null;
            } else if (this.breakOnlyHasCrystal.getValue() && this.getBed() == -1) {
               placePos = null;
            } else {
               this.delayTimer.reset();
               if (this.calcTimer.passedMs(this.calcDelay.getValueInt())) {
                  this.calcTimer.reset();
                  placePos = null;
                  this.lastDamage = 0.0F;
                  ArrayList list = new ArrayList();
                  Iterator var2 = CombatUtil.getEnemies(this.targetRange.getRange()).iterator();

                  while(var2.hasNext()) {
                     PlayerEntity target = (PlayerEntity)var2.next();
                     list.add(new PlayerAndPredict(target));
                  }

                  PlayerAndPredict self = new PlayerAndPredict(mc.player);
                  Iterator var10 = BlockUtil.getSphere((float)this.range.getValue()).iterator();

                  label95:
                  while(true) {
                     BlockPos pos;
                     do {
                        if (!var10.hasNext()) {
                           break label95;
                        }

                        pos = (BlockPos)var10.next();
                     } while(!this.canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock));

                     Iterator var5 = list.iterator();

                     while(true) {
                        PlayerAndPredict pap;
                        float damage;
                        float selfDamage;
                        do {
                           do {
                              do {
                                 do {
                                    if (!var5.hasNext()) {
                                       continue label95;
                                    }

                                    pap = (PlayerAndPredict)var5.next();
                                    damage = this.calculateDamage(pos, pap.player, pap.predict);
                                    selfDamage = this.calculateDamage(pos, self.player, self.predict);
                                 } while((double)selfDamage > this.placeMaxSelf.getValue());
                              } while(this.antiSuicide.getValue() > 0.0 && (double)selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.antiSuicide.getValue());
                           } while(damage < EntityUtil.getHealth(pap.player) && (damage < this.placeMinDamage.getValueFloat() || this.smart.getValue() && damage < selfDamage));
                        } while(placePos != null && !(damage > this.lastDamage));

                        this.displayTarget = pap.player;
                        placePos = pos;
                        this.lastDamage = damage;
                     }
                  }
               }

               if (placePos != null) {
                  this.doBed(placePos);
               }

            }
         }
      }
   }

   public void doBed(BlockPos pos) {
      if (this.canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) {
         if (this.getBed() != -1) {
            this.doPlace(pos);
         }
      } else {
         this.doBreak(pos);
      }

   }

   private void doBreak(BlockPos pos) {
      if (this.Break.getValue()) {
         if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
            Direction side = BlockUtil.getClickSide(pos);
            Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)side.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)side.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)side.getVector().getZ() * 0.5);
            if (this.rotate.getValue() && !this.faceVector(directionVec)) {
               return;
            }

            if (!this.breakTimer.passedMs((long)this.breakDelay.getValue())) {
               return;
            }

            this.breakTimer.reset();
            EntityUtil.swingHand(Hand.MAIN_HAND, (SwingSide)this.swingMode.getValue());
            BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(mc.world)));
         }

      }
   }

   private void doPlace(BlockPos pos) {
      if (this.place.getValue()) {
         int bedSlot;
         if ((bedSlot = this.getBed()) == -1) {
            placePos = null;
         } else {
            int oldSlot = mc.player.getInventory().selectedSlot;
            Direction facing = null;
            Direction[] var5 = Direction.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Direction i = var5[var7];
               if (i != Direction.UP && i != Direction.DOWN && BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!this.checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
                  facing = i;
                  break;
               }
            }

            if (facing != null) {
               Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5 + (double)Direction.UP.getVector().getX() * 0.5, (double)pos.getY() + 0.5 + (double)Direction.UP.getVector().getY() * 0.5, (double)pos.getZ() + 0.5 + (double)Direction.UP.getVector().getZ() * 0.5);
               if (this.rotate.getValue() && !this.faceVector(directionVec)) {
                  return;
               }

               if (!this.placeTimer.passedMs((long)this.placeDelay.getValue())) {
                  return;
               }

               this.placeTimer.reset();
               this.doSwap(bedSlot);
               if (this.yawDeceive.getValue()) {
                  HoleKick.pistonFacing(facing.getOpposite());
               }

               BlockUtil.clickBlock(pos.offset(facing).down(), Direction.UP, false);
               if (this.rotate.getValue() && this.sync.getValue()) {
                  EntityUtil.faceVector(directionVec);
               }

               if (this.inventorySwap.getValue()) {
                  this.doSwap(bedSlot);
                  EntityUtil.syncInventory();
               } else {
                  this.doSwap(oldSlot);
               }
            }

         }
      }
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      this.update();
      double quad = this.noPosTimer.passedMs(this.startFadeTime.getValue() * 1000.0) ? this.fadeUtils.easeOutQuad() : 0.0;
      if (this.nowBB != null && this.render.getValue() && quad < 1.0) {
         Box bb = this.nowBB;
         if (this.shrink.getValue()) {
            bb = this.nowBB.shrink(quad * 0.5, quad * 0.5, quad * 0.5);
            bb = bb.shrink(-quad * 0.5, -quad * 0.5, -quad * 0.5);
         }

         if (this.box.getValue()) {
            Render3DUtil.drawFill(matrixStack, bb, ColorUtil.injectAlpha(this.color.getValue(), (int)(this.boxAlpha.getValue() * Math.abs(quad - 1.0))));
         }

         if (this.outline.getValue()) {
            Render3DUtil.drawBox(matrixStack, bb, ColorUtil.injectAlpha(this.color.getValue(), (int)(this.outlineAlpha.getValue() * Math.abs(quad - 1.0))));
         }
      } else if (this.reset.getValue()) {
         this.nowBB = null;
      }

   }

   private void animUpdate() {
      this.fadeUtils.setLength((long)(this.fadeTime.getValue() * 1000.0));
      if (placePos != null) {
         this.lastBB = new Box(placePos);
         this.noPosTimer.reset();
         if (this.nowBB == null) {
            this.nowBB = this.lastBB;
         }

         if (this.renderPos == null || !this.renderPos.equals(placePos)) {
            this.animation.setLength(this.animationTime.getValue() * 1000.0 <= 0.0 ? 0L : (long)(Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ) <= 5.0 ? (double)((long)((Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ)) * this.animationTime.getValue() * 1000.0)) : this.animationTime.getValue() * 5000.0));
            this.animation.reset();
            this.renderPos = placePos;
         }
      }

      if (!this.noPosTimer.passedMs((long)(this.startFadeTime.getValue() * 1000.0))) {
         this.fadeUtils.reset();
      }

      double size = this.animation.easeOutQuad();
      if (this.nowBB != null && this.lastBB != null) {
         if (Math.abs(this.nowBB.minX - this.lastBB.minX) + Math.abs(this.nowBB.minY - this.lastBB.minY) + Math.abs(this.nowBB.minZ - this.lastBB.minZ) > 16.0) {
            this.nowBB = this.lastBB;
         }

         if (this.lastSize != size) {
            this.nowBB = new Box(this.nowBB.minX + (this.lastBB.minX - this.nowBB.minX) * size, this.nowBB.minY + (this.lastBB.minY - this.nowBB.minY) * size, this.nowBB.minZ + (this.lastBB.minZ - this.nowBB.minZ) * size, this.nowBB.maxX + (this.lastBB.maxX - this.nowBB.maxX) * size, this.nowBB.maxY + (this.lastBB.maxY - this.nowBB.maxY) * size, this.nowBB.maxZ + (this.lastBB.maxZ - this.nowBB.maxZ) * size);
            this.lastSize = size;
         }
      }

   }

   public int getBed() {
      return this.inventorySwap.getValue() ? InventoryUtil.findClassInventorySlot(BedItem.class) : InventoryUtil.findClass(BedItem.class);
   }

   private void doSwap(int slot) {
      if (this.inventorySwap.getValue()) {
         InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
      CombatUtil.modifyPos = pos;
      CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
      float damage = this.calculateDamage(pos.toCenterPos(), player, predict);
      CombatUtil.modifyPos = null;
      return damage;
   }

   public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
      if (this.terrainIgnore.getValue()) {
         CombatUtil.terrainIgnore = true;
      }

      float damage = 0.0F;
      switch ((AnchorAura.CalcMode)this.calcMode.getValue()) {
         case Meteor -> damage = (float)MeteorExplosionUtil.crystalDamage(player, pos, predict);
         case Thunder -> damage = ThunderExplosionUtil.calculateDamage(pos, player, predict, 6.0F);
         case OyVey -> damage = OyveyExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6.0F);
         case Edit -> damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6.0F);
      }

      CombatUtil.terrainIgnore = false;
      return damage;
   }

   private boolean canPlaceBed(BlockPos pos) {
      if (BlockUtil.canReplace(pos) && (!this.checkMine.getValue() || !BlockUtil.isMining(pos))) {
         Direction[] var2 = Direction.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Direction i = var2[var4];
            if (i != Direction.UP && i != Direction.DOWN && BlockUtil.isStrictDirection(pos.offset(i).down(), Direction.UP) && this.isTrueFacing(pos.offset(i), i.getOpposite()) && BlockUtil.clientCanPlace(pos.offset(i), false) && BlockUtil.canClick(pos.offset(i).down()) && (!this.checkMine.getValue() || !BlockUtil.isMining(pos.offset(i)))) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isTrueFacing(BlockPos pos, Direction facing) {
      if (this.yawDeceive.getValue()) {
         return true;
      } else {
         Vec3d hitVec = pos.toCenterPos().add(new Vec3d(0.0, -0.5, 0.0));
         return Direction.fromRotation(EntityUtil.getLegitRotations(hitVec)[0]) == facing;
      }
   }

   public boolean faceVector(Vec3d directionVec) {
      if (!this.newRotate.getValue()) {
         EntityUtil.faceVector(directionVec);
         return true;
      } else {
         this.directionVec = directionVec;
         float[] angle = EntityUtil.getLegitRotations(directionVec);
         if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValueFloat()) {
            if (this.sync.getValue()) {
               EntityUtil.sendYawAndPitch(angle[0], angle[1]);
            }

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
         float diff = MathHelper.wrapDegrees(angle[0] - this.lastYaw);
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

   public enum Page {
      General,
      Rotate,
      Calc,
      Predict,
      Render;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{General, Rotate, Calc, Predict, Render};
      }
   }

   public class PlayerAndPredict {
      final PlayerEntity player;
      final PlayerEntity predict;

      public PlayerAndPredict(PlayerEntity player) {
         this.player = player;
         if (BedAura.this.predictTicks.getValueFloat() > 0.0F) {
            this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            };
            this.predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)BedAura.INSTANCE.predictTicks.getValueInt(), true)));
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

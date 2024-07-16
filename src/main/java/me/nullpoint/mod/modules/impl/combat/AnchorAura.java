// Decompiled with: Procyon 0.6.0
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import net.minecraft.util.math.Box;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.events.impl.Render3DEvent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import me.nullpoint.api.utils.combat.MioExplosionUtil;
import me.nullpoint.api.utils.combat.ExplosionUtil;
import me.nullpoint.api.utils.combat.OyveyExplosionUtil;
import me.nullpoint.api.utils.combat.ThunderExplosionUtil;
import me.nullpoint.api.utils.combat.MeteorExplosionUtil;
import net.minecraft.item.Items;
import java.util.Iterator;
import java.util.List;
import net.minecraft.util.math.Vec3i;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import net.minecraft.block.Blocks;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.events.eventbus.EventHandler;
import java.util.Random;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.events.impl.RotateEvent;
import net.minecraft.entity.Entity;
import net.minecraft.client.util.math.MatrixStack;
import me.nullpoint.Nullpoint;
import java.awt.Color;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.PlayerEntity;
import me.nullpoint.api.utils.math.Timer;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.Module;

public class AnchorAura extends Module
{
   public final EnumSetting<Page> page;
   private final BooleanSetting light;
   private final BooleanSetting inventory;
   public final SliderSetting range;
   public final SliderSetting targetRange;
   private final SliderSetting tickDelay;
   private final EnumSetting<PlaceMode> placeMode;
   private final BooleanSetting breakCrystal;
   private final BooleanSetting spam;
   private final BooleanSetting mineSpam;
   private final BooleanSetting spamPlace;
   private final BooleanSetting inSpam;
   private final BooleanSetting usingPause;
   private final EnumSetting<SwingSide> swingMode;
   private final SliderSetting placeDelay;
   private final SliderSetting spamDelay;
   private final SliderSetting calcDelay;
   private final SliderSetting updateDelay;
   private final BooleanSetting rotate;
   private final BooleanSetting yawStep;
   private final SliderSetting steps;
   private final BooleanSetting packet;
   private final BooleanSetting random;
   private final BooleanSetting checkLook;
   private final SliderSetting fov;
   private final EnumSetting<CalcMode> calcMode;
   private final BooleanSetting noSuicide;
   private final BooleanSetting terrainIgnore;
   public final SliderSetting minDamage;
   public final SliderSetting breakMin;
   public final SliderSetting headDamage;
   private final SliderSetting minPrefer;
   private final SliderSetting maxSelfDamage;
   public final SliderSetting predictTicks;
   private final EnumSetting<Aura.TargetESP> mode;
   private final ColorSetting color;
   final BooleanSetting render;
   final BooleanSetting shrink;
   final ColorSetting box;
   private final BooleanSetting bold;
   private final SliderSetting lineWidth;
   final ColorSetting fill;
   final SliderSetting sliderSpeed;
   final SliderSetting startFadeTime;
   final SliderSetting fadeSpeed;
   private final ArrayList<BlockPos> chargeList;
   private final Timer updateTimer;
   private final Timer delayTimer;
   private final Timer calcTimer;
   final Timer noPosTimer;
   public PlayerEntity displayTarget;
   public static AnchorAura INSTANCE;
   public Vec3d directionVec;
   private float lastPitch;
   public BlockPos currentPos;
   private float lastYaw;
   public double lastDamage;
   public BlockPos tempPos;
   static Vec3d placeVec3d;
   static Vec3d curVec3d;
   double fade;
   private final Timer timer;

   public AnchorAura() {
      super("AnchorAura", Category.Combat);
      this.page = this.add(new EnumSetting<Page>("Page", Page.General));
      this.light = this.add(new BooleanSetting("Light", true, v -> this.page.getValue() == Page.General));
      this.inventory = this.add(new BooleanSetting("InventorySwap", true, v -> this.page.getValue() == Page.General));
      this.range = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, 0.1, v -> this.page.getValue() == Page.General).setSuffix("m"));
      this.targetRange = this.add(new SliderSetting("TargetRange", 8.0, 0.0, 16.0, 0.1, v -> this.page.getValue() == Page.General).setSuffix("m"));
      this.tickDelay = this.add(new SliderSetting("TickDelay", 100.0, 165.0, 500.0, 1.0, v -> this.page.getValue() == Page.General).setSuffix("ms"));
      this.placeMode = this.add(new EnumSetting<PlaceMode>("PlaceMode", PlaceMode.tick, v -> this.page.getValue() == Page.General));
      this.breakCrystal = this.add(new BooleanSetting("BreakCrystal", true, v -> this.page.getValue() == Page.General));
      this.spam = this.add(new BooleanSetting("Spam", true, v -> this.page.getValue() == Page.General).setParent());
      this.mineSpam = this.add(new BooleanSetting("OnlyMining", true, v -> this.page.getValue() == Page.General && this.spam.isOpen()));
      this.spamPlace = this.add(new BooleanSetting("Fast", true, v -> this.page.getValue() == Page.General).setParent());
      this.inSpam = this.add(new BooleanSetting("WhenSpamming", true, v -> this.page.getValue() == Page.General && this.spamPlace.isOpen()));
      this.usingPause = this.add(new BooleanSetting("UsingPause", true, v -> this.page.getValue() == Page.General));
      this.swingMode = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.Server, v -> this.page.getValue() == Page.General));
      this.placeDelay = this.add(new SliderSetting("Delay", 100.0, 0.0, 500.0, 1.0, v -> this.page.getValue() == Page.General).setSuffix("ms"));
      this.spamDelay = this.add(new SliderSetting("SpamDelay", 200.0, 0.0, 1000.0, 1.0, v -> this.page.getValue() == Page.General).setSuffix("ms"));
      this.calcDelay = this.add(new SliderSetting("CalcDelay", 200.0, 0.0, 1000.0, 1.0, v -> this.page.getValue() == Page.General).setSuffix("ms"));
      this.updateDelay = this.add(new SliderSetting("UpdateDelay", 50, 0, 1000, v -> this.page.getValue() == Page.General).setSuffix("ms"));
      this.rotate = this.add(new BooleanSetting("Rotate", true, v -> this.page.getValue() == Page.Rotate).setParent());
      this.yawStep = this.add(new BooleanSetting("YawStep", true, v -> this.rotate.isOpen() && this.page.getValue() == Page.Rotate));
      this.steps = this.add(new SliderSetting("Steps", 0.30000001192092896, 0.10000000149011612, 1.0, 0.01, v -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == Page.Rotate));
      this.packet = this.add(new BooleanSetting("Packet", false, v -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == Page.Rotate));
      this.random = this.add(new BooleanSetting("Random", true, v -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == Page.Rotate));
      this.checkLook = this.add(new BooleanSetting("CheckLook", true, v -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == Page.Rotate));
      this.fov = this.add(new SliderSetting("Fov", 5.0, 0.0, 30.0, v -> this.rotate.isOpen() && this.yawStep.getValue() && this.checkLook.getValue() && this.page.getValue() == Page.Rotate));
      this.calcMode = this.add(new EnumSetting<CalcMode>("CalcMode", CalcMode.Meteor, v -> this.page.getValue() == Page.Calc));
      this.noSuicide = this.add(new BooleanSetting("NoSuicide", true, v -> this.page.getValue() == Page.Calc));
      this.terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true, v -> this.page.getValue() == Page.Calc));
      this.minDamage = this.add(new SliderSetting("Min", 4.0, 0.0, 36.0, 0.1, v -> this.page.getValue() == Page.Calc).setSuffix("dmg"));
      this.breakMin = this.add(new SliderSetting("ExplosionMin", 4.0, 0.0, 36.0, 0.1, v -> this.page.getValue() == Page.Calc).setSuffix("dmg"));
      this.headDamage = this.add(new SliderSetting("ForceHead", 7.0, 0.0, 36.0, 0.1, v -> this.page.getValue() == Page.Calc).setSuffix("dmg"));
      this.minPrefer = this.add(new SliderSetting("Prefer", 7.0, 0.0, 36.0, 0.1, v -> this.page.getValue() == Page.Calc).setSuffix("dmg"));
      this.maxSelfDamage = this.add(new SliderSetting("MaxSelf", 8.0, 0.0, 36.0, 0.1, v -> this.page.getValue() == Page.Calc).setSuffix("dmg"));
      this.predictTicks = this.add(new SliderSetting("Predict", 2.0, 0.0, 50.0, 1.0, v -> this.page.getValue() == Page.Calc).setSuffix("ticks"));
      this.mode = this.add(new EnumSetting<Aura.TargetESP>("TargetESP", Aura.TargetESP.Jello, v -> this.page.getValue() == Page.Render));
      this.color = this.add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), v -> this.page.getValue() == Page.Render));
      this.render = this.add(new BooleanSetting("Render", true, v -> this.page.getValue() == Page.Render));
      this.shrink = this.add(new BooleanSetting("Shrink", true, v -> this.page.getValue() == Page.Render && this.render.getValue()));
      this.box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> this.page.getValue() == Page.Render && this.render.getValue()).injectBoolean(true));
      this.bold = this.add(new BooleanSetting("Bold", false, v -> this.page.getValue() == Page.Render && this.render.getValue() && this.box.booleanValue)).setParent();
      this.lineWidth = this.add(new SliderSetting("LineWidth", 4, 1, 5, v -> this.page.getValue() == Page.Render && this.render.getValue() && this.bold.isOpen() && this.box.booleanValue));
      this.fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> this.page.getValue() == Page.Render && this.render.getValue()).injectBoolean(true));
      this.sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.0, 1.0, 0.01, v -> this.page.getValue() == Page.Render && this.render.getValue()));
      this.startFadeTime = this.add(new SliderSetting("StartFade", 0.3, 0.0, 2.0, 0.01, v -> this.page.getValue() == Page.Render && this.render.getValue()).setSuffix("s"));
      this.fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2, 0.01, 1.0, 0.01, v -> this.page.getValue() == Page.Render && this.render.getValue()));
      this.chargeList = new ArrayList<BlockPos>();
      this.updateTimer = new Timer();
      this.delayTimer = new Timer();
      this.calcTimer = new Timer();
      this.noPosTimer = new Timer();
      this.directionVec = null;
      this.lastPitch = 0.0f;
      this.lastYaw = 0.0f;
      this.fade = 0.0;
      this.timer = new Timer();
      AnchorAura.INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new AnchorRender());
   }

   @Override
   public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
      this.update();
      if (AnchorAura.INSTANCE.displayTarget != null && AnchorAura.INSTANCE.currentPos != null) {
         Aura.doRender(matrixStack, partialTicks, AnchorAura.INSTANCE.displayTarget, this.color.getValue(), this.mode.getValue());
      }
   }

   @Override
   public String getInfo() {
      if (this.displayTarget != null && this.currentPos != null) {
         return this.displayTarget.getName().getString();
      }
      return null;
   }

   @EventHandler
   public void onRotate(final RotateEvent event) {
      if (this.currentPos != null && this.yawStep.getValue() && this.directionVec != null) {
         final float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.steps.getValueFloat());
         if (newAngle != null) {
            this.lastYaw = newAngle[0];
         }
         if (newAngle != null) {
            this.lastPitch = newAngle[1];
         }
         if (this.random.getValue() && new Random().nextBoolean()) {
            this.lastPitch = Math.min(new Random().nextFloat() * 2.0f + this.lastPitch, 90.0f);
         }
         event.setYaw(this.lastYaw);
         event.setPitch(this.lastPitch);
      }
      else {
         this.lastYaw = Nullpoint.ROTATE.lastYaw;
         this.lastPitch = Nullpoint.ROTATE.lastPitch;
      }
   }

   @Override
   public void onDisable() {
      this.currentPos = null;
      this.tempPos = null;
   }

   @Override
   public void onEnable() {
      this.lastYaw = Nullpoint.ROTATE.lastYaw;
      this.lastPitch = Nullpoint.ROTATE.lastPitch;
   }

   @EventHandler
   public void onUpdateWalking(final UpdateWalkingEvent event) {
      this.update();
   }

   @Override
   public void onUpdate() {
      this.update();
   }

   public void update() {
      if (nullCheck()) {
         return;
      }
      this.anchor();
      this.currentPos = this.tempPos;
   }

   public void anchor() {
      if (!this.timer.passedMs(this.tickDelay.getValueInt())) {
         return;
      }
      if (!this.updateTimer.passedMs((long)this.updateDelay.getValue())) {
         return;
      }
      final int anchor = this.findBlock(Blocks.RESPAWN_ANCHOR);
      final int glowstone = this.findBlock(Blocks.GLOWSTONE);
      int old = 0;
      if (AnchorAura.mc.player != null) {
         old = AnchorAura.mc.player.getInventory().selectedSlot;
      }
      if (anchor == -1) {
         this.tempPos = null;
         return;
      }
      if (glowstone == -1) {
         this.tempPos = null;
         return;
      }
      final int unBlock;
      if ((unBlock = InventoryUtil.findUnBlock()) == -1) {
         this.tempPos = null;
         return;
      }
      if (AnchorAura.mc.player.isSneaking()) {
         this.tempPos = null;
         return;
      }
      if (this.usingPause.getValue() && AnchorAura.mc.player.isUsingItem()) {
         this.tempPos = null;
         return;
      }
      this.updateTimer.reset();
      final PlayerAndPredict selfPredict = new PlayerAndPredict(AnchorAura.mc.player);
      if (this.calcTimer.passed((long)this.calcDelay.getValueFloat())) {
         this.calcTimer.reset();
         this.tempPos = null;
         double placeDamage = this.minDamage.getValue();
         double breakDamage = this.breakMin.getValue();
         boolean anchorFound = false;
         final List<PlayerEntity> enemies = CombatUtil.getEnemies(this.targetRange.getValue());
         final ArrayList<PlayerAndPredict> list = new ArrayList<PlayerAndPredict>();
         for (final PlayerEntity player : enemies) {
            list.add(new PlayerAndPredict(player));
         }
         for (final PlayerAndPredict pap : list) {
            final BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
            if (BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue()) || (BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR && BlockUtil.getClickSideStrict(pos) != null)) {
               final double selfDamage;
               if ((selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) {
                  continue;
               }
               if (this.noSuicide.getValue() && selfDamage > AnchorAura.mc.player.getHealth() + AnchorAura.mc.player.getAbsorptionAmount()) {
                  continue;
               }
               final double damage;
               if ((damage = this.getAnchorDamage(pos, pap.player, pap.predict)) > this.headDamage.getValueFloat()) {
                  this.lastDamage = damage;
                  this.displayTarget = pap.player;
                  this.tempPos = pos;
                  break;
               }
               continue;
            }
         }
         if (this.tempPos == null) {
            for (final BlockPos pos2 : BlockUtil.getSphere(this.range.getValueFloat())) {
               for (final PlayerAndPredict pap2 : list) {
                  if (this.light.getValue()) {
                     CombatUtil.modifyPos = pos2;
                     CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                     final boolean skip = !AutoCrystal.canSee(pos2.toCenterPos(), pap2.predict.getPos());
                     CombatUtil.modifyPos = null;
                     if (skip) {
                        continue;
                     }
                  }
                  if (BlockUtil.getBlock(pos2) != Blocks.RESPAWN_ANCHOR) {
                     if (anchorFound) {
                        continue;
                     }
                     if (!BlockUtil.canPlace(pos2, this.range.getValue(), this.breakCrystal.getValue())) {
                        continue;
                     }
                     CombatUtil.modifyPos = pos2;
                     CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
                     final boolean skip = BlockUtil.getClickSideStrict(pos2) == null;
                     CombatUtil.modifyPos = null;
                     if (skip) {
                        continue;
                     }
                     final double damage = this.getAnchorDamage(pos2, pap2.player, pap2.predict);
                     if (damage < placeDamage || (AutoCrystal.crystalPos != null && !AutoCrystal.INSTANCE.isOff() && AutoCrystal.INSTANCE.lastDamage >= damage)) {
                        continue;
                     }
                     final double selfDamage2;
                     if ((selfDamage2 = this.getAnchorDamage(pos2, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) {
                        continue;
                     }
                     if (this.noSuicide.getValue() && selfDamage2 > AnchorAura.mc.player.getHealth() + AnchorAura.mc.player.getAbsorptionAmount()) {
                        continue;
                     }
                     this.lastDamage = damage;
                     this.displayTarget = pap2.player;
                     placeDamage = damage;
                     this.tempPos = pos2;
                  }
                  else {
                     final double damage2 = this.getAnchorDamage(pos2, pap2.player, pap2.predict);
                     if (BlockUtil.getClickSideStrict(pos2) == null) {
                        continue;
                     }
                     if (damage2 < breakDamage) {
                        continue;
                     }
                     if (damage2 >= this.minPrefer.getValue()) {
                        anchorFound = true;
                     }
                     if (!anchorFound && damage2 < placeDamage) {
                        continue;
                     }
                     if (AutoCrystal.crystalPos != null && !AutoCrystal.INSTANCE.isOff() && AutoCrystal.INSTANCE.lastDamage >= damage2) {
                        continue;
                     }
                     final double selfDamage3;
                     if ((selfDamage3 = this.getAnchorDamage(pos2, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) {
                        continue;
                     }
                     if (this.noSuicide.getValue() && selfDamage3 > AnchorAura.mc.player.getHealth() + AnchorAura.mc.player.getAbsorptionAmount()) {
                        continue;
                     }
                     this.lastDamage = damage2;
                     this.displayTarget = pap2.player;
                     breakDamage = damage2;
                     this.tempPos = pos2;
                  }
               }
            }
         }
      }
      if (this.tempPos != null) {
         if (AnchorAura.mc.player != null && this.usingPause.getValue() && AnchorAura.mc.player.isUsingItem()) {
            return;
         }
         if (this.breakCrystal.getValue()) {
            CombatUtil.attackCrystal(new BlockPos(this.tempPos), this.rotate.getValue(), false);
         }
         switch (this.placeMode.getValue()) {
            case tick: {
               if (BlockUtil.canPlace(this.tempPos, this.range.getValue(), this.breakCrystal.getValue())) {
                  this.globalPlace(this.tempPos);
               }
               this.GlowPlaceable(this.tempPos);
               this.TickBreak(this.tempPos);
               this.timer.reset();
               break;
            }
            case NullPoint: {
               if (this.breakCrystal.getValue()) {
                  CombatUtil.attackCrystal(new BlockPos(this.tempPos), this.rotate.getValue(), false);
               }
               final boolean shouldSpam = this.spam.getValue() && (!this.mineSpam.getValue() || Nullpoint.BREAK.isMining(this.tempPos));
               if (shouldSpam) {
                  if (!this.delayTimer.passed((long)this.spamDelay.getValueFloat())) {
                     return;
                  }
                  this.delayTimer.reset();
                  if (BlockUtil.canPlace(this.tempPos, this.range.getValue(), this.breakCrystal.getValue())) {
                     this.placeBlock(this.tempPos, this.rotate.getValue(), anchor);
                  }
                  if (!this.chargeList.contains(this.tempPos)) {
                     this.delayTimer.reset();
                     this.clickBlock(this.tempPos, BlockUtil.getClickSide(this.tempPos), this.rotate.getValue(), glowstone);
                     this.chargeList.add(this.tempPos);
                  }
                  this.chargeList.remove(this.tempPos);
                  this.clickBlock(this.tempPos, BlockUtil.getClickSide(this.tempPos), this.rotate.getValue(), unBlock);
                  if (this.spamPlace.getValue() && this.inSpam.getValue()) {
                     CombatUtil.modifyPos = this.tempPos;
                     CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                     this.placeBlock(this.tempPos, this.rotate.getValue(), anchor);
                     CombatUtil.modifyPos = null;
                  }
               }
               else if (BlockUtil.canPlace(this.tempPos, this.range.getValue(), this.breakCrystal.getValue())) {
                  if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                     return;
                  }
                  this.delayTimer.reset();
                  this.placeBlock(this.tempPos, this.rotate.getValue(), anchor);
               }
               else if (BlockUtil.getBlock(this.tempPos) == Blocks.RESPAWN_ANCHOR) {
                  if (!this.chargeList.contains(this.tempPos)) {
                     if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                        return;
                     }
                     this.delayTimer.reset();
                     this.clickBlock(this.tempPos, BlockUtil.getClickSide(this.tempPos), this.rotate.getValue(), glowstone);
                     this.chargeList.add(this.tempPos);
                  }
                  else {
                     if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                        return;
                     }
                     this.delayTimer.reset();
                     this.chargeList.remove(this.tempPos);
                     this.clickBlock(this.tempPos, BlockUtil.getClickSide(this.tempPos), this.rotate.getValue(), unBlock);
                     if (this.spamPlace.getValue()) {
                        CombatUtil.modifyPos = this.tempPos;
                        CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                        this.placeBlock(this.tempPos, this.rotate.getValue(), anchor);
                        CombatUtil.modifyPos = null;
                     }
                  }
               }
               if (!this.inventory.getValue()) {
                  InventoryUtil.switchToSlot(old);
                  break;
               }
               break;
            }
         }
      }
   }

   public void TickBreak(final BlockPos pos) {
      final int anchor = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
      int old = 0;
      if (AnchorAura.mc.player != null) {
         old = AnchorAura.mc.player.getInventory().selectedSlot;
      }
      this.doSwap(anchor);
      this.clickBlocks(pos, BlockUtil.getClickSide(pos), this.rotate.getValue());
      if (this.inventory.getValue()) {
         this.doSwap(anchor);
         EntityUtil.syncInventory();
      }
      else {
         this.doSwap(old);
      }
   }

   public void globalPlace(final BlockPos pos) {
      final int anchor = this.findBlock(Blocks.RESPAWN_ANCHOR);
      int old = 0;
      if (AnchorAura.mc.player != null) {
         old = AnchorAura.mc.player.getInventory().selectedSlot;
      }
      this.doSwap(anchor);
      this.placeBlock(pos, this.rotate.getValue());
      if (this.inventory.getValue()) {
         this.doSwap(anchor);
         EntityUtil.syncInventory();
      }
      else {
         this.doSwap(old);
      }
   }

   public void GlowPlaceable(final BlockPos pos) {
      final int glowStone = this.findBlock(Blocks.GLOWSTONE);
      int old = 0;
      if (AnchorAura.mc.player != null) {
         old = AnchorAura.mc.player.getInventory().selectedSlot;
      }
      this.doSwap(glowStone);
      this.clickBlock(pos, BlockUtil.getClickSide(pos), this.rotate.getValue());
      if (this.inventory.getValue()) {
         this.doSwap(glowStone);
         EntityUtil.syncInventory();
      }
      else {
         this.doSwap(old);
      }
   }

   public double getAnchorDamage(final BlockPos anchorPos, final PlayerEntity target, final PlayerEntity predict) {
      if (this.terrainIgnore.getValue()) {
         CombatUtil.terrainIgnore = true;
      }
      double damage = 0.0;
      switch (this.calcMode.getValue()) {
         case Meteor: {
            damage = MeteorExplosionUtil.anchorDamage(target, anchorPos, predict);
            break;
         }
         case Thunder: {
            damage = ThunderExplosionUtil.anchorDamage(anchorPos, target, predict);
            break;
         }
         case OyVey: {
            damage = OyveyExplosionUtil.anchorDamage(anchorPos, target, predict);
            break;
         }
         case Edit: {
            damage = ExplosionUtil.anchorDamage(anchorPos, target, predict);
            break;
         }
         case Mio: {
            damage = MioExplosionUtil.anchorDamage(target, anchorPos, predict);
            break;
         }
      }
      CombatUtil.terrainIgnore = false;
      return damage;
   }

   private void doSwap(final int slot) {
      if (this.inventory.getValue()) {
         if (AnchorAura.mc.player != null) {
            InventoryUtil.inventorySwap(slot, AnchorAura.mc.player.getInventory().selectedSlot);
         }
      }
      else {
         InventoryUtil.switchToSlot(slot);
      }
   }

   private float[] injectStep(final float[] angle, float steps) {
      if (steps < 0.01f) {
         steps = 0.01f;
      }
      if (steps > 1.0f) {
         steps = 1.0f;
      }
      if (steps < 1.0f && angle != null) {
         final float packetYaw = this.lastYaw;
         float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);
         if (Math.abs(diff) > 90.0f * steps) {
            angle[0] = packetYaw + diff * (90.0f * steps / Math.abs(diff));
         }
         final float packetPitch = this.lastPitch;
         diff = angle[1] - packetPitch;
         if (Math.abs(diff) > 90.0f * steps) {
            angle[1] = packetPitch + diff * (90.0f * steps / Math.abs(diff));
         }
      }
      if (angle != null) {
         return new float[] { angle[0], angle[1] };
      }
      return null;
   }

   public void placeBlock(final BlockPos pos, final boolean rotate) {
      if (BlockUtil.airPlace()) {
         for (final Direction i : Direction.values()) {
            if (AnchorAura.mc.world != null && AnchorAura.mc.world.isAir(pos.offset(i))) {
               this.clickBlock(pos, i, rotate);
               return;
            }
         }
      }
      final Direction side = BlockUtil.getPlaceSide(pos);
      if (side == null) {
         return;
      }
      BlockUtil.placedPos.add(pos);
      final Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
      final BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
      boolean sprint = false;
      if (AnchorAura.mc.player != null) {
         sprint = AnchorAura.mc.player.isSprinting();
      }
      boolean sneak = false;
      if (AnchorAura.mc.world != null) {
         sneak = (BlockUtil.needSneak(AnchorAura.mc.world.getBlockState(result.getBlockPos()).getBlock()) && !AnchorAura.mc.player.isSneaking());
      }
      if (sprint) {
         AnchorAura.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(AnchorAura.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
      }
      if (sneak) {
         AnchorAura.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(AnchorAura.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
      }
      this.clickBlock(pos.offset(side), side.getOpposite(), rotate);
      if (sneak) {
         AnchorAura.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(AnchorAura.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
      }
      if (sprint) {
         AnchorAura.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(AnchorAura.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
      }
      EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
   }

   public void clickBlocks(final BlockPos pos, final Direction side, final boolean rotate) {
      final Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
      if (rotate && !this.faceVector(directionVec)) {
         return;
      }
      EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
      final BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
      if (AnchorAura.mc.interactionManager != null) {
         AnchorAura.mc.interactionManager.interactBlock(AnchorAura.mc.player, Hand.MAIN_HAND, result);
      }
      if (AnchorAura.mc.player != null) {
         AnchorAura.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(AnchorAura.mc.world)));
      }
   }

   public void clickBlock(final BlockPos pos, final Direction side, final boolean rotate) {
      final Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
      if (rotate && !this.faceVector(directionVec)) {
         return;
      }
      EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
      final BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
      if (AnchorAura.mc.player != null) {
         AnchorAura.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(AnchorAura.mc.world)));
      }
   }

   public boolean faceVector(final Vec3d directionVec) {
      if (!this.yawStep.getValue()) {
         EntityUtil.faceVector(directionVec);
         return true;
      }
      this.directionVec = directionVec;
      final float[] angle = EntityUtil.getLegitRotations(directionVec);
      if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValueFloat()) {
         if (this.packet.getValue()) {
            EntityUtil.sendYawAndPitch(angle[0], angle[1]);
         }
         return true;
      }
      return !this.checkLook.getValue();
   }

   public void placeBlock(final BlockPos pos, final boolean rotate, final int slot) {
      if (BlockUtil.airPlace()) {
         for (final Direction i : Direction.values()) {
            if (AnchorAura.mc.world != null && AnchorAura.mc.world.isAir(pos.offset(i))) {
               this.clickBlock(pos, i, rotate, slot);
               return;
            }
         }
      }
      final Direction side = BlockUtil.getPlaceSide(pos);
      if (side == null) {
         return;
      }
      BlockUtil.placedPos.add(pos);
      this.clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
   }

   public void clickBlock(final BlockPos pos, final Direction side, final boolean rotate, final int slot) {
      final Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
      if (rotate && !this.faceVector(directionVec)) {
         return;
      }
      this.doSwap(slot);
      EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
      final BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
      if (AnchorAura.mc.player != null) {
         AnchorAura.mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(AnchorAura.mc.world)));
      }
      if (this.inventory.getValue()) {
         this.doSwap(slot);
         EntityUtil.syncInventory();
      }
   }

   public int findBlock(final Block blockIn) {
      if (this.inventory.getValue()) {
         return InventoryUtil.findBlockInventorySlot(blockIn);
      }
      return InventoryUtil.findBlock(blockIn);
   }

   public static class PlayerAndPredict
   {
      public final PlayerEntity player;
      public final PlayerEntity predict;

      public PlayerAndPredict(final PlayerEntity player) {
         this.player = player;
         if (AnchorAura.INSTANCE.predictTicks.getValueFloat() > 0.0f) {
            (this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               @Override
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            }).setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)AnchorAura.INSTANCE.predictTicks.getValueInt(), true)));
            this.predict.setHealth(player.getHealth());
            this.predict.prevX = player.prevX;
            this.predict.prevZ = player.prevZ;
            this.predict.prevY = player.prevY;
            this.predict.setOnGround(player.isOnGround());
            this.predict.getInventory().clone(player.getInventory());
            this.predict.setPose(player.getPose());
            for (final StatusEffectInstance se : player.getStatusEffects()) {
               this.predict.addStatusEffect(se);
            }
         }
         else {
            this.predict = player;
         }
      }
   }

   public class AnchorRender
   {
      @EventHandler
      public void onRender3D(final Render3DEvent event) {
         if (AnchorAura.this.currentPos != null) {
            AnchorAura.this.noPosTimer.reset();
            AnchorAura.placeVec3d = AnchorAura.this.currentPos.toCenterPos();
         }
         if (AnchorAura.placeVec3d == null) {
            return;
         }
         if (AnchorAura.this.fadeSpeed.getValue() >= 1.0) {
            AnchorAura.this.fade = (AnchorAura.this.noPosTimer.passedMs((long)(AnchorAura.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5);
         }
         else {
            AnchorAura.this.fade = AnimateUtil.animate(AnchorAura.this.fade, AnchorAura.this.noPosTimer.passedMs((long)(AnchorAura.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5, AnchorAura.this.fadeSpeed.getValue() / 10.0);
         }
         if (AnchorAura.this.fade == 0.0) {
            AnchorAura.curVec3d = null;
            return;
         }
         if (AnchorAura.curVec3d == null || AnchorAura.this.sliderSpeed.getValue() >= 1.0) {
            AnchorAura.curVec3d = AnchorAura.placeVec3d;
         }
         else {
            AnchorAura.curVec3d = new Vec3d(AnimateUtil.animate(AnchorAura.curVec3d.x, AnchorAura.placeVec3d.x, AnchorAura.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AnchorAura.curVec3d.y, AnchorAura.placeVec3d.y, AnchorAura.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AnchorAura.curVec3d.z, AnchorAura.placeVec3d.z, AnchorAura.this.sliderSpeed.getValue() / 10.0));
         }
         if (AnchorAura.this.render.getValue()) {
            Box cbox = new Box(AnchorAura.curVec3d, AnchorAura.curVec3d);
            if (AnchorAura.this.shrink.getValue()) {
               cbox = cbox.expand(AnchorAura.this.fade);
            }
            else {
               cbox = cbox.expand(0.5);
            }
            final MatrixStack matrixStack = event.getMatrixStack();
            if (AnchorAura.this.fill.booleanValue) {
               Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(AnchorAura.this.fill.getValue(), (int)(AnchorAura.this.fill.getValue().getAlpha() * AnchorAura.this.fade * 2.0)));
            }
            if (AnchorAura.this.box.booleanValue) {
               if (!AnchorAura.this.bold.getValue()) {
                  Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(AnchorAura.this.box.getValue(), (int)(AnchorAura.this.box.getValue().getAlpha() * AnchorAura.this.fade * 2.0)));
               }
               else {
                  Render3DUtil.drawLine(cbox, ColorUtil.injectAlpha(AnchorAura.this.box.getValue(), (int)(AnchorAura.this.box.getValue().getAlpha() * AnchorAura.this.fade * 2.0)), (float)AnchorAura.this.lineWidth.getValueInt());
               }
            }
         }
      }
   }

   public enum Page
   {
      General("General", 0),
      Calc("Calc", 1),
      Rotate("Rotate", 2),
      Render("Render", 3);

      Page(final String string, final int i) {
      }
   }

   public enum PlaceMode
   {
      tick("tick", 0),
      NullPoint("NullPoint", 1);

      PlaceMode(final String string, final int i) {
      }
   }

   public enum CalcMode
   {
      OyVey("OyVey", 0),
      Meteor("Meteor", 1),
      Thunder("Thunder", 2),
      Edit("Edit", 3),
      Mio("Mio", 4);

      CalcMode(final String string, final int i) {
      }
   }
}

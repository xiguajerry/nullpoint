// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
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
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.Aura;
import me.nullpoint.mod.modules.impl.combat.AutoAnchor;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AutoAnchor extends Module {
   public static AutoAnchor INSTANCE;
   public final EnumSetting<AutoAnchor.Page> page = this.add(new EnumSetting("Page", AutoAnchor.Page.General));
   private final BooleanSetting light = this.add(new BooleanSetting("Light", true, (v) -> this.page.getValue() == AutoAnchor.Page.General));
   public final SliderSetting range = this.add((new SliderSetting("Range", 5.0D, 0.0D, 6.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("m"));
   public final SliderSetting targetRange = this.add((new SliderSetting("TargetRange", 8.0D, 0.0D, 16.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("m"));
   private final BooleanSetting breakCrystal = this.add(new BooleanSetting("BreakCrystal", true, (v) -> this.page.getValue() == AutoAnchor.Page.General));
   private final BooleanSetting spam = this.add((new BooleanSetting("Spam", true, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setParent());
   private final BooleanSetting mineSpam = this.add(new BooleanSetting("OnlyMining", true, (v) -> this.page.getValue() == AutoAnchor.Page.General && this.spam.isOpen()));
   private final BooleanSetting spamPlace = this.add((new BooleanSetting("Fast", true, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setParent());
   private final BooleanSetting inSpam = this.add(new BooleanSetting("WhenSpamming", true, (v) -> this.page.getValue() == AutoAnchor.Page.General && this.spamPlace.isOpen()));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", true, (v) -> this.page.getValue() == AutoAnchor.Page.General));
   private final EnumSetting<SwingSide> swingMode = this.add(new EnumSetting("Swing", SwingSide.Server, (v) -> this.page.getValue() == AutoAnchor.Page.General));
   private final SliderSetting placeDelay = this.add((new SliderSetting("Delay", 100.0D, 0.0D, 500.0D, 1.0D, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("ms"));
   private final SliderSetting spamDelay = this.add((new SliderSetting("SpamDelay", 200.0D, 0.0D, 1000.0D, 1.0D, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("ms"));
   private final SliderSetting calcDelay = this.add((new SliderSetting("CalcDelay", 200.0D, 0.0D, 1000.0D, 1.0D, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("ms"));
   private final SliderSetting updateDelay = this.add((new SliderSetting("UpdateDelay", 50, 0, 1000, (v) -> this.page.getValue() == AutoAnchor.Page.General)).setSuffix("ms"));
   private final BooleanSetting rotate = this.add((new BooleanSetting("Rotate", true, (v) -> this.page.getValue() == AutoAnchor.Page.Rotate)).setParent());
   private final BooleanSetting yawStep = this.add(new BooleanSetting("YawStep", true, (v) -> this.rotate.isOpen() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final SliderSetting steps = this.add(new SliderSetting("Steps", 0.3F, 0.1F, 1.0D, 0.01D, (v) -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final BooleanSetting packet = this.add(new BooleanSetting("Packet", false, (v) -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final BooleanSetting random = this.add(new BooleanSetting("Random", true, (v) -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final BooleanSetting checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final SliderSetting fov = this.add(new SliderSetting("Fov", 5.0D, 0.0D, 30.0D, (v) -> this.rotate.isOpen() && this.yawStep.getValue() && this.checkLook.getValue() && this.page.getValue() == AutoAnchor.Page.Rotate));
   private final EnumSetting<AutoAnchor.CalcMode> calcMode = this.add(new EnumSetting("CalcMode", AutoAnchor.CalcMode.Meteor, (v) -> this.page.getValue() == AutoAnchor.Page.Calc));
   private final BooleanSetting noSuicide = this.add(new BooleanSetting("NoSuicide", true, (v) -> this.page.getValue() == AutoAnchor.Page.Calc));
   private final BooleanSetting terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true, (v) -> this.page.getValue() == AutoAnchor.Page.Calc));
   public final SliderSetting minDamage = this.add((new SliderSetting("Min", 4.0D, 0.0D, 36.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("dmg"));
   public final SliderSetting breakMin = this.add((new SliderSetting("ExplosionMin", 4.0D, 0.0D, 36.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("dmg"));
   public final SliderSetting headDamage = this.add((new SliderSetting("ForceHead", 7.0D, 0.0D, 36.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("dmg"));
   private final SliderSetting minPrefer = this.add((new SliderSetting("Prefer", 7.0D, 0.0D, 36.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("dmg"));
   private final SliderSetting maxSelfDamage = this.add((new SliderSetting("MaxSelf", 8.0D, 0.0D, 36.0D, 0.1D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("dmg"));
   public final SliderSetting predictTicks = this.add((new SliderSetting("Predict", 2.0D, 0.0D, 50.0D, 1.0D, (v) -> this.page.getValue() == AutoAnchor.Page.Calc)).setSuffix("ticks"));
   private final EnumSetting<Aura.TargetESP> mode = this.add(new EnumSetting("TargetESP", Aura.TargetESP.Jello, (v) -> this.page.getValue() == AutoAnchor.Page.Render));
   private final ColorSetting color = this.add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), (v) -> this.page.getValue() == AutoAnchor.Page.Render));
   final BooleanSetting render = this.add(new BooleanSetting("Render", true, (v) -> this.page.getValue() == AutoAnchor.Page.Render));
   final BooleanSetting shrink = this.add(new BooleanSetting("Shrink", true, (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue()));
   final ColorSetting box = this.add((new ColorSetting("Box", new Color(255, 255, 255, 255), (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue())).injectBoolean(true));
   final ColorSetting fill = this.add((new ColorSetting("Fill", new Color(255, 255, 255, 100), (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue())).injectBoolean(true));
   final SliderSetting sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2D, 0.0D, 1.0D, 0.01D, (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue()));
   final SliderSetting startFadeTime = this.add((new SliderSetting("StartFade", 0.3D, 0.0D, 2.0D, 0.01D, (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue())).setSuffix("s"));
   final SliderSetting fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2D, 0.01D, 1.0D, 0.01D, (v) -> this.page.getValue() == AutoAnchor.Page.Render && this.render.getValue()));
   private final Timer updateTimer = new Timer();
   private final Timer delayTimer = new Timer();
   private final Timer calcTimer = new Timer();
   public Vec3d directionVec = null;
   private float lastYaw = 0.0F;
   private float lastPitch = 0.0F;
   public PlayerEntity displayTarget;
   private final ArrayList<BlockPos> chargeList = new ArrayList();
   public BlockPos currentPos;
   public BlockPos tempPos;
   public double lastDamage;
   final Timer noPosTimer = new Timer();
   static Vec3d placeVec3d;
   static Vec3d curVec3d;
   double fade = 0.0D;

   public AutoAnchor() {
      super("AutoAnchor", Module.Category.Combat);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new AutoAnchor.AnchorRender());
   }

   public String getInfo() {
      return this.displayTarget != null && this.currentPos != null ? this.displayTarget.getName().getString() : null;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      this.update();
      if (INSTANCE.displayTarget != null && INSTANCE.currentPos != null) {
         Aura.doRender(matrixStack, partialTicks, INSTANCE.displayTarget, this.color.getValue(), this.mode.getValue());
      }

   }

   @EventHandler
   public void onRotate(RotateEvent event) {
      if (this.currentPos != null && this.yawStep.getValue() && this.directionVec != null) {
         float[] newAngle = this.injectStep(EntityUtil.getLegitRotations(this.directionVec), this.steps.getValueFloat());
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

   public void onDisable() {
      this.currentPos = null;
      this.tempPos = null;
   }

   public void onEnable() {
      this.lastYaw = Nullpoint.ROTATE.lastYaw;
      this.lastPitch = Nullpoint.ROTATE.lastPitch;
   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      this.update();
   }

   public void onUpdate() {
      this.update();
   }

   public void update() {
      if (!nullCheck()) {
         this.anchor();
         this.currentPos = this.tempPos;
      }
   }

   public void anchor() {
      if (this.updateTimer.passedMs((long)this.updateDelay.getValue())) {
         int anchor = InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
         int glowstone = InventoryUtil.findBlock(Blocks.GLOWSTONE);
         int old = mc.player.getInventory().selectedSlot;
         if (anchor == -1) {
            this.tempPos = null;
         } else if (glowstone == -1) {
            this.tempPos = null;
         } else {
            int unBlock;
            if ((unBlock = InventoryUtil.findUnBlock()) == -1) {
               this.tempPos = null;
            } else if (mc.player.isSneaking()) {
               this.tempPos = null;
            } else if (this.usingPause.getValue() && mc.player.isUsingItem()) {
               this.tempPos = null;
            } else {
               this.updateTimer.reset();
               AutoAnchor.PlayerAndPredict selfPredict = new AutoAnchor.PlayerAndPredict(mc.player);
               if (this.calcTimer.passed((long)this.calcDelay.getValueFloat())) {
                  this.calcTimer.reset();
                  this.tempPos = null;
                  double placeDamage = this.minDamage.getValue();
                  double breakDamage = this.breakMin.getValue();
                  boolean anchorFound = false;
                  List<PlayerEntity> enemies = CombatUtil.getEnemies(this.targetRange.getValue());
                  ArrayList<AutoAnchor.PlayerAndPredict> list = new ArrayList();

                  for(PlayerEntity player : enemies) {
                     list.add(new AutoAnchor.PlayerAndPredict(player));
                  }

                  for(AutoAnchor.PlayerAndPredict pap : list) {
                     BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
                     double selfDamage;
                     double damage;
                     if ((BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue()) || BlockUtil.getBlock(pos) == Blocks.RESPAWN_ANCHOR && BlockUtil.getClickSideStrict(pos) != null) && !((selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) && (!this.noSuicide.getValue() || !(selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()))) && (damage = this.getAnchorDamage(pos, pap.player, pap.predict)) > (double)this.headDamage.getValueFloat()) {
                        this.lastDamage = damage;
                        this.displayTarget = pap.player;
                        this.tempPos = pos;
                        break;
                     }
                  }

                  if (this.tempPos == null) {
                     label227:
                     for(BlockPos pos : BlockUtil.getSphere(this.range.getValueFloat())) {
                        Iterator var27 = list.iterator();

                        while(true) {
                           boolean skip;
                           AutoAnchor.PlayerAndPredict pap;
                           do {
                              if (!var27.hasNext()) {
                                 continue label227;
                              }

                              pap = (AutoAnchor.PlayerAndPredict)var27.next();
                              if (!this.light.getValue()) {
                                 break;
                              }

                              CombatUtil.modifyPos = pos;
                              CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
                              skip = !AutoCrystal.canSee(pos.toCenterPos(), pap.predict.getPos());
                              CombatUtil.modifyPos = null;
                           } while(skip);

                           if (BlockUtil.getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
                              if (!anchorFound && BlockUtil.canPlace(pos, this.range.getValue(), this.breakCrystal.getValue())) {
                                 CombatUtil.modifyPos = pos;
                                 CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
                                 skip = BlockUtil.getClickSideStrict(pos) == null;
                                 CombatUtil.modifyPos = null;
                                 if (!skip) {
                                    double damage = this.getAnchorDamage(pos, pap.player, pap.predict);
                                    double selfDamage;
                                    if (damage >= placeDamage && (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || (double)AutoCrystal.INSTANCE.lastDamage < damage) && !((selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) && (!this.noSuicide.getValue() || !(selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount())))) {
                                       this.lastDamage = damage;
                                       this.displayTarget = pap.player;
                                       placeDamage = damage;
                                       this.tempPos = pos;
                                    }
                                 }
                              }
                           } else {
                              double damage = this.getAnchorDamage(pos, pap.player, pap.predict);
                              if (BlockUtil.getClickSideStrict(pos) != null && damage >= breakDamage) {
                                 if (damage >= this.minPrefer.getValue()) {
                                    anchorFound = true;
                                 }

                                 double selfDamage;
                                 if ((anchorFound || !(damage < placeDamage)) && (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || (double)AutoCrystal.INSTANCE.lastDamage < damage) && !((selfDamage = this.getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > this.maxSelfDamage.getValue()) && (!this.noSuicide.getValue() || !(selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount())))) {
                                    this.lastDamage = damage;
                                    this.displayTarget = pap.player;
                                    breakDamage = damage;
                                    this.tempPos = pos;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (this.tempPos != null) {
                  if (this.breakCrystal.getValue()) {
                     CombatUtil.attackCrystal(new BlockPos(this.tempPos), this.rotate.getValue(), false);
                  }

                  boolean shouldSpam = this.spam.getValue() && (!this.mineSpam.getValue() || Nullpoint.BREAK.isMining(this.tempPos));
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
                  } else if (BlockUtil.canPlace(this.tempPos, this.range.getValue(), this.breakCrystal.getValue())) {
                     if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                        return;
                     }

                     this.delayTimer.reset();
                     this.placeBlock(this.tempPos, this.rotate.getValue(), anchor);
                  } else if (BlockUtil.getBlock(this.tempPos) == Blocks.RESPAWN_ANCHOR) {
                     if (!this.chargeList.contains(this.tempPos)) {
                        if (!this.delayTimer.passed((long)this.placeDelay.getValueFloat())) {
                           return;
                        }

                        this.delayTimer.reset();
                        this.clickBlock(this.tempPos, BlockUtil.getClickSide(this.tempPos), this.rotate.getValue(), glowstone);
                        this.chargeList.add(this.tempPos);
                     } else {
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

                  InventoryUtil.switchToSlot(old);
               }

            }
         }
      }
   }

   public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target, PlayerEntity predict) {
      if (this.terrainIgnore.getValue()) {
         CombatUtil.terrainIgnore = true;
      }

      double damage = 0.0D;
      switch(this.calcMode.getValue()) {
         case Meteor:
            damage = MeteorExplosionUtil.anchorDamage(target, anchorPos, predict);
            break;
         case Thunder:
            damage = ThunderExplosionUtil.anchorDamage(anchorPos, target, predict);
            break;
         case Oyvey:
            damage = OyveyExplosionUtil.anchorDamage(anchorPos, target, predict);
            break;
         case Edit:
            damage = ExplosionUtil.anchorDamage(anchorPos, target, predict);
      }

      CombatUtil.terrainIgnore = false;
      return damage;
   }

   public void placeBlock(BlockPos pos, boolean rotate, int slot) {
      if (BlockUtil.airPlace()) {
         for(Direction i : Direction.values()) {
            if (mc.world.isAir(pos.offset(i))) {
               this.clickBlock(pos, i, rotate, slot);
               return;
            }
         }
      }

      Direction side = BlockUtil.getPlaceSide(pos);
      if (side != null) {
         BlockUtil.placedPos.add(pos);
         this.clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
      }
   }

   public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
      Vec3d directionVec = new Vec3d((double)pos.getX() + 0.5D + (double)side.getVector().getX() * 0.5D, (double)pos.getY() + 0.5D + (double)side.getVector().getY() * 0.5D, (double)pos.getZ() + 0.5D + (double)side.getVector().getZ() * 0.5D);
      if (!rotate || this.faceVector(directionVec)) {
         InventoryUtil.switchToSlot(slot);
         EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
         BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
         mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, BlockUtil.getWorldActionId(mc.world)));
      }
   }

   public boolean faceVector(Vec3d directionVec) {
      if (!this.yawStep.getValue()) {
         EntityUtil.faceVector(directionVec);
         return true;
      } else {
         this.directionVec = directionVec;
         float[] angle = EntityUtil.getLegitRotations(directionVec);
         if (Math.abs(MathHelper.wrapDegrees(angle[0] - this.lastYaw)) < this.fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - this.lastPitch)) < this.fov.getValueFloat()) {
            if (this.packet.getValue()) {
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

   public class AnchorRender {
      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (AutoAnchor.this.currentPos != null) {
            AutoAnchor.this.noPosTimer.reset();
            AutoAnchor.placeVec3d = AutoAnchor.this.currentPos.toCenterPos();
         }

         if (AutoAnchor.placeVec3d != null) {
            if (AutoAnchor.this.fadeSpeed.getValue() >= 1.0D) {
               AutoAnchor.this.fade = AutoAnchor.this.noPosTimer.passedMs((long)(AutoAnchor.this.startFadeTime.getValue() * 1000.0D)) ? 0.0D : 0.5D;
            } else {
               AutoAnchor.this.fade = AnimateUtil.animate(AutoAnchor.this.fade, AutoAnchor.this.noPosTimer.passedMs((long)(AutoAnchor.this.startFadeTime.getValue() * 1000.0D)) ? 0.0D : 0.5D, AutoAnchor.this.fadeSpeed.getValue() / 10.0D);
            }

            if (AutoAnchor.this.fade == 0.0D) {
               AutoAnchor.curVec3d = null;
            } else {
               if (AutoAnchor.curVec3d != null && !(AutoAnchor.this.sliderSpeed.getValue() >= 1.0D)) {
                  AutoAnchor.curVec3d = new Vec3d(AnimateUtil.animate(AutoAnchor.curVec3d.x, AutoAnchor.placeVec3d.x, AutoAnchor.this.sliderSpeed.getValue() / 10.0D), AnimateUtil.animate(AutoAnchor.curVec3d.y, AutoAnchor.placeVec3d.y, AutoAnchor.this.sliderSpeed.getValue() / 10.0D), AnimateUtil.animate(AutoAnchor.curVec3d.z, AutoAnchor.placeVec3d.z, AutoAnchor.this.sliderSpeed.getValue() / 10.0D));
               } else {
                  AutoAnchor.curVec3d = AutoAnchor.placeVec3d;
               }

               if (AutoAnchor.this.render.getValue()) {
                  Box cbox = new Box(AutoAnchor.curVec3d, AutoAnchor.curVec3d);
                  if (AutoAnchor.this.shrink.getValue()) {
                     cbox = cbox.expand(AutoAnchor.this.fade);
                  } else {
                     cbox = cbox.expand(0.5D);
                  }

                  MatrixStack matrixStack = event.getMatrixStack();
                  if (AutoAnchor.this.fill.booleanValue) {
                     Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(AutoAnchor.this.fill.getValue(), (int)((double)AutoAnchor.this.fill.getValue().getAlpha() * AutoAnchor.this.fade * 2.0D)));
                  }

                  if (AutoAnchor.this.box.booleanValue) {
                     Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(AutoAnchor.this.box.getValue(), (int)((double)AutoAnchor.this.box.getValue().getAlpha() * AutoAnchor.this.fade * 2.0D)));
                  }
               }

            }
         }
      }
   }

   public enum CalcMode {
      Oyvey,
      Meteor,
      Thunder,
      Edit;

      // $FF: synthetic method
      private static AutoAnchor.CalcMode[] $values() {
         return new AutoAnchor.CalcMode[]{Oyvey, Meteor, Thunder, Edit};
      }
   }

   public enum Page {
      General,
      Calc,
      Rotate,
      Render;

      // $FF: synthetic method
      private static AutoAnchor.Page[] $values() {
         return new AutoAnchor.Page[]{General, Calc, Rotate, Render};
      }
   }

   public static class PlayerAndPredict {
      public final PlayerEntity player;
      public final PlayerEntity predict;

      public PlayerAndPredict(PlayerEntity player) {
         this.player = player;
         if (AutoAnchor.INSTANCE.predictTicks.getValueFloat() > 0.0F) {
            this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            };
            this.predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)AutoAnchor.INSTANCE.predictTicks.getValueInt(), true)));
            this.predict.setHealth(player.getHealth());
            this.predict.prevX = player.prevX;
            this.predict.prevZ = player.prevZ;
            this.predict.prevY = player.prevY;
            this.predict.setOnGround(player.isOnGround());
            this.predict.getInventory().clone(player.getInventory());
            this.predict.setPose(player.getPose());

            for(StatusEffectInstance se : player.getStatusEffects()) {
               this.predict.addStatusEffect(se);
            }
         } else {
            this.predict = player;
         }

      }
   }
}
 
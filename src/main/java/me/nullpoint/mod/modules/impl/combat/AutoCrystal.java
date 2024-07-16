package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.combat.ExplosionUtil;
import me.nullpoint.api.utils.combat.MeteorExplosionUtil;
import me.nullpoint.api.utils.combat.MioExplosionUtil;
import me.nullpoint.api.utils.combat.OyveyExplosionUtil;
import me.nullpoint.api.utils.combat.ThunderExplosionUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class AutoCrystal extends Module {
   public static AutoCrystal INSTANCE;
   public static BlockPos tempPos;
   public static BlockPos crystalPos;
   private final EnumSetting page;
   private final BooleanSetting preferAnchor;
   private final EnumSetting swingMode;
   private final BooleanSetting eatingPause;
   private final SliderSetting switchCooldown;
   private final SliderSetting targetRange;
   private final SliderSetting updateDelay;
   private final SliderSetting wallRange;
   private final BooleanSetting rotate;
   private final BooleanSetting onBreak;
   private final BooleanSetting yawStep;
   private final SliderSetting steps;
   private final BooleanSetting random;
   private final BooleanSetting packet;
   private final BooleanSetting checkLook;
   private final SliderSetting fov;
   private final SliderSetting minDamage;
   private final SliderSetting maxSelf;
   private final SliderSetting range;
   private final SliderSetting noSuicide;
   private final BooleanSetting place;
   private final SliderSetting placeDelay;
   private final EnumSetting autoSwap;
   private final BooleanSetting spam;
   private final BooleanSetting Break;
   private final SliderSetting breakDelay;
   private final BooleanSetting breakOnlyHasCrystal;
   private final BooleanSetting breakRemove;
   private final EnumSetting mode;
   private final ColorSetting color;
   final ColorSetting text;
   final BooleanSetting render;
   final BooleanSetting shrink;
   final ColorSetting box;
   private final BooleanSetting bold;
   private final SliderSetting lineWidth;
   final ColorSetting fill;
   final SliderSetting sliderSpeed;
   final SliderSetting startFadeTime;
   final SliderSetting fadeSpeed;
   private final BooleanSetting smart;
   private final BooleanSetting useThread;
   private final BooleanSetting doCrystal;
   private final BooleanSetting lite;
   private final EnumSetting calcMode;
   private final SliderSetting predictTicks;
   private final BooleanSetting terrainIgnore;
   private final BooleanSetting antiSurround;
   private final SliderSetting antiSurroundMax;
   private final BooleanSetting slowPlace;
   private final SliderSetting slowDelay;
   private final SliderSetting slowMinDamage;
   private final BooleanSetting forcePlace;
   private final SliderSetting forceMaxHealth;
   private final SliderSetting forceMin;
   private final BooleanSetting armorBreaker;
   private final SliderSetting maxDurable;
   private final SliderSetting armorBreakerDamage;
   private final SliderSetting hurtTime;
   private final Timer switchTimer;
   private final Timer delayTimer;
   public static final Timer placeTimer = new Timer();
   public final Timer lastBreakTimer;
   final Timer noPosTimer;
   public PlayerEntity displayTarget;
   private float lastYaw;
   private float lastPitch;
   private int lastHotbar;
   public float tempDamage;
   public float lastDamage;
   public Vec3d directionVec;
   public static Thread thread;
   static Vec3d placeVec3d;
   static Vec3d curVec3d;
   double fade;

   public AutoCrystal() {
      super("AutoCrystal", "Recode", Module.Category.Combat);
      this.page = this.add(new EnumSetting("Page", AutoCrystal.Page.General));
      this.preferAnchor = this.add(new BooleanSetting("PreferAnchor", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      }));
      this.swingMode = this.add(new EnumSetting("Swing", SwingSide.Server, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      }));
      this.eatingPause = this.add(new BooleanSetting("EatingPause", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      }));
      this.switchCooldown = this.add((new SliderSetting("SwitchPause", 100, 0, 1000, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      })).setSuffix("ms"));
      this.targetRange = this.add((new SliderSetting("TargetRange", 12.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      })).setSuffix("m"));
      this.updateDelay = this.add((new SliderSetting("UpdateDelay", 50, 0, 1000, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      })).setSuffix("ms"));
      this.wallRange = this.add((new SliderSetting("WallRange", 6.0, 0.0, 6.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.General;
      })).setSuffix("m"));
      this.rotate = this.add((new BooleanSetting("Rotate", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Rotate;
      })).setParent());
      this.onBreak = this.add(new BooleanSetting("OnBreak", false, (v) -> {
         return this.rotate.isOpen() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.yawStep = this.add(new BooleanSetting("YawStep", false, (v) -> {
         return this.rotate.isOpen() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.steps = this.add(new SliderSetting("Steps", 0.30000001192092896, 0.10000000149011612, 1.0, 0.009999999776482582, (v) -> {
         return this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.random = this.add(new BooleanSetting("Random", true, (v) -> {
         return this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.packet = this.add(new BooleanSetting("Packet", false, (v) -> {
         return this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> {
         return this.rotate.isOpen() && this.yawStep.getValue() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.fov = this.add(new SliderSetting("Fov", 30.0, 0.0, 90.0, (v) -> {
         return this.rotate.isOpen() && this.yawStep.getValue() && this.checkLook.getValue() && this.page.getValue() == AutoCrystal.Page.Rotate;
      }));
      this.minDamage = this.add((new SliderSetting("Min", 5.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setSuffix("dmg"));
      this.maxSelf = this.add((new SliderSetting("Self", 12.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setSuffix("dmg"));
      this.range = this.add((new SliderSetting("Range", 5.0, 0.0, 6.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setSuffix("m"));
      this.noSuicide = this.add((new SliderSetting("NoSuicide", 3.0, 0.0, 10.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setSuffix("dmg"));
      this.place = this.add((new BooleanSetting("Place", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setParent());
      this.placeDelay = this.add((new SliderSetting("PlaceDelay", 300, 0, 1000, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.place.isOpen();
      })).setSuffix("ms"));
      this.autoSwap = this.add(new EnumSetting("AutoSwap", AutoCrystal.SwapMode.Off, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.place.isOpen();
      }));
      this.spam = this.add(new BooleanSetting("Spam", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.place.isOpen();
      }));
      this.Break = this.add((new BooleanSetting("Break", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact;
      })).setParent());
      this.breakDelay = this.add((new SliderSetting("BreakDelay", 300, 0, 1000, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.Break.isOpen();
      })).setSuffix("ms"));
      this.breakOnlyHasCrystal = this.add(new BooleanSetting("OnlyHasCrystal", false, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.Break.isOpen();
      }));
      this.breakRemove = this.add(new BooleanSetting("Remove", false, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Interact && this.Break.isOpen();
      }));
      this.mode = this.add(new EnumSetting("TargetESP", Aura.TargetESP.Jello, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render;
      }));
      this.color = this.add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render;
      }));
      this.text = this.add((new ColorSetting("Text", new Color(-1), (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render;
      })).injectBoolean(true));
      this.render = this.add(new BooleanSetting("Render", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render;
      }));
      this.shrink = this.add(new BooleanSetting("Shrink", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      }));
      this.box = this.add((new ColorSetting("Box", new Color(255, 255, 255, 255), (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      })).injectBoolean(true));
      this.bold = this.add(new BooleanSetting("Bold", false, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.box.booleanValue;
      })).setParent();
      this.lineWidth = this.add(new SliderSetting("LineWidth", 4, 1, 5, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.bold.isOpen() && this.box.booleanValue;
      }));
      this.fill = this.add((new ColorSetting("Fill", new Color(255, 255, 255, 100), (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      })).injectBoolean(true));
      this.sliderSpeed = this.add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      }));
      this.startFadeTime = this.add((new SliderSetting("StartFade", 0.3, 0.0, 2.0, 0.01, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      })).setSuffix("s"));
      this.fadeSpeed = this.add(new SliderSetting("FadeSpeed", 0.2, 0.01, 1.0, 0.01, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Render && this.render.getValue();
      }));
      this.smart = this.add(new BooleanSetting("Smart", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.useThread = this.add(new BooleanSetting("UseThread", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.doCrystal = this.add(new BooleanSetting("CalcDoCrystal", false, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.lite = this.add(new BooleanSetting("Lite", false, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.calcMode = this.add(new EnumSetting("CalcMode", AnchorAura.CalcMode.OyVey, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.predictTicks = this.add((new SliderSetting("Predict", 4, 0, 10, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      })).setSuffix("ticks"));
      this.terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Calc;
      }));
      this.antiSurround = this.add((new BooleanSetting("AntiSurround", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc;
      })).setParent());
      this.antiSurroundMax = this.add((new SliderSetting("WhenLower", 5.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.antiSurround.isOpen();
      })).setSuffix("dmg"));
      this.slowPlace = this.add((new BooleanSetting("Timeout", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc;
      })).setParent());
      this.slowDelay = this.add((new SliderSetting("TimeoutDelay", 600, 0, 2000, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.slowPlace.isOpen();
      })).setSuffix("ms"));
      this.slowMinDamage = this.add((new SliderSetting("TimeoutMin", 1.5, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.slowPlace.isOpen();
      })).setSuffix("dmg"));
      this.forcePlace = this.add((new BooleanSetting("ForcePlace", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc;
      })).setParent());
      this.forceMaxHealth = this.add((new SliderSetting("LowerThan", 7, 0, 36, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.forcePlace.isOpen();
      })).setSuffix("health"));
      this.forceMin = this.add((new SliderSetting("ForceMin", 1.5, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.forcePlace.isOpen();
      })).setSuffix("dmg"));
      this.armorBreaker = this.add((new BooleanSetting("ArmorBreaker", true, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc;
      })).setParent());
      this.maxDurable = this.add((new SliderSetting("MaxDurable", 8, 0, 100, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.armorBreaker.isOpen();
      })).setSuffix("%"));
      this.armorBreakerDamage = this.add((new SliderSetting("BreakerMin", 3.0, 0.0, 36.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc && this.armorBreaker.isOpen();
      })).setSuffix("dmg"));
      this.hurtTime = this.add(new SliderSetting("HurtTime", 10.0, 0.0, 10.0, 1.0, (v) -> {
         return this.page.getValue() == AutoCrystal.Page.Misc;
      }));
      this.switchTimer = new Timer();
      this.delayTimer = new Timer();
      this.lastBreakTimer = new Timer();
      this.noPosTimer = new Timer();
      this.lastYaw = 0.0F;
      this.lastPitch = 0.0F;
      this.lastHotbar = -1;
      this.directionVec = null;
      this.fade = 0.0;
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new CrystalRender());
   }

   public String getInfo() {
      if (this.displayTarget != null && this.lastDamage > 0.0F) {
         String var10000 = this.displayTarget.getName().getString();
         return var10000 + ", " + (new DecimalFormat("0.0")).format(this.lastDamage);
      } else {
         return null;
      }
   }

   public void onDisable() {
      crystalPos = null;
      tempPos = null;
   }

   public void onEnable() {
      this.lastYaw = Nullpoint.ROTATE.lastYaw;
      this.lastPitch = Nullpoint.ROTATE.lastPitch;
      this.lastBreakTimer.reset();
   }

   public void onUpdate() {
      if (this.useThread.getValue()) {
         if (thread == null || !thread.isAlive()) {
            thread = new Thread(() -> {
               while(INSTANCE.isOn() && this.useThread.getValue()) {
                  this.updateCrystalPos();
               }

               crystalPos = null;
               tempPos = null;
            });

            try {
               thread.start();
            } catch (Exception var2) {
               Exception e = var2;
               e.printStackTrace();
            }
         }
      } else {
         this.updateCrystalPos();
      }

      if (crystalPos != null) {
         this.doCrystal(crystalPos);
      }

   }

   @EventHandler
   public void onUpdateWalking(UpdateWalkingEvent event) {
      if (!this.useThread.getValue()) {
         this.updateCrystalPos();
      }

      if (crystalPos != null) {
         this.doCrystal(crystalPos);
      }

   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (!this.useThread.getValue()) {
         this.updateCrystalPos();
      }

      if (crystalPos != null) {
         this.doCrystal(crystalPos);
      }

      if (INSTANCE.displayTarget != null && !INSTANCE.noPosTimer.passedMs(500L)) {
         Aura.doRender(matrixStack, partialTicks, INSTANCE.displayTarget, this.color.getValue(), (Aura.TargetESP)this.mode.getValue());
      }

   }

   @EventHandler
   public void onRotate(RotateEvent event) {
      if (this.rotate.getValue() && this.yawStep.getValue() && this.directionVec != null && !this.noPosTimer.passed(1000L)) {
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

   @EventHandler(
      priority = -199
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (!event.isCancelled()) {
         Packet var3 = event.getPacket();
         if (var3 instanceof UpdateSelectedSlotC2SPacket updateSelectedSlotC2SPacket) {
             if (updateSelectedSlotC2SPacket.getSelectedSlot() != this.lastHotbar) {
               this.lastHotbar = updateSelectedSlotC2SPacket.getSelectedSlot();
               this.switchTimer.reset();
            }
         }

      }
   }

   private void updateCrystalPos() {
      this.update();
      this.lastDamage = this.tempDamage;
      crystalPos = tempPos;
   }

   private void update() {
      if (!nullCheck()) {
         if (this.delayTimer.passedMs((long)this.updateDelay.getValue())) {
            if (this.eatingPause.getValue() && EntityUtil.isUsing()) {
               this.lastBreakTimer.reset();
               tempPos = null;
            } else if (this.preferAnchor.getValue() && AnchorAura.INSTANCE.currentPos != null) {
               this.lastBreakTimer.reset();
               tempPos = null;
            } else if (this.breakOnlyHasCrystal.getValue() && !mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) && !this.findCrystal()) {
               this.lastBreakTimer.reset();
               tempPos = null;
            } else if (this.switchTimer.passedMs((long)this.switchCooldown.getValue())) {
               this.delayTimer.reset();
               tempPos = null;
               this.tempDamage = 0.0F;
               ArrayList list = new ArrayList();
               Iterator var2 = CombatUtil.getEnemies(this.targetRange.getValue()).iterator();

               while(var2.hasNext()) {
                  PlayerEntity target = (PlayerEntity)var2.next();
                  if (target.hurtTime <= this.hurtTime.getValueInt()) {
                     list.add(new PlayerAndPredict(target));
                  }
               }

               PlayerAndPredict self = new PlayerAndPredict(mc.player);
               if (list.isEmpty()) {
                  this.lastBreakTimer.reset();
               } else {
                  Iterator var16 = BlockUtil.getSphere((float)this.range.getValue() + 1.0F).iterator();

                  label231:
                  while(true) {
                     BlockPos pos;
                     do {
                        do {
                           do {
                              do {
                                 if (!var16.hasNext()) {
                                    if (this.antiSurround.getValue() && SpeedMine.breakPos != null && SpeedMine.progress >= 0.9 && !BlockUtil.hasEntity(SpeedMine.breakPos, false) && this.tempDamage <= this.antiSurroundMax.getValueFloat()) {
                                       var16 = list.iterator();

                                       while(var16.hasNext()) {
                                          PlayerAndPredict pap = (PlayerAndPredict)var16.next();
                                          Direction[] var18 = Direction.values();
                                          int var19 = var18.length;

                                          for(int var20 = 0; var20 < var19; ++var20) {
                                             Direction i = var18[var20];
                                             if (i != Direction.DOWN && i != Direction.UP) {
                                                BlockPos offsetPos = (new BlockPosX(pap.player.getPos().add(0.0, 0.5, 0.0))).offset(i);
                                                if (offsetPos.equals(SpeedMine.breakPos)) {
                                                   if (this.canPlaceCrystal(offsetPos.offset(i), false, false)) {
                                                      float selfDamage = this.calculateDamage(offsetPos.offset(i), self.player, self.predict);
                                                      if ((double)selfDamage < this.maxSelf.getValue() && (!(this.noSuicide.getValue() > 0.0) || !((double)selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.noSuicide.getValue()))) {
                                                         tempPos = offsetPos.offset(i);
                                                         if (this.doCrystal.getValue() && tempPos != null) {
                                                            this.doCrystal(tempPos);
                                                         }

                                                         return;
                                                      }
                                                   }

                                                   Direction[] var22 = Direction.values();
                                                   int var11 = var22.length;

                                                   for(int var12 = 0; var12 < var11; ++var12) {
                                                      Direction ii = var22[var12];
                                                      if (ii != Direction.DOWN && ii != i && this.canPlaceCrystal(offsetPos.offset(ii), false, false)) {
                                                         float selfDamage = this.calculateDamage(offsetPos.offset(ii), self.player, self.predict);
                                                         if ((double)selfDamage < this.maxSelf.getValue() && (!(this.noSuicide.getValue() > 0.0) || !((double)selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.noSuicide.getValue()))) {
                                                            tempPos = offsetPos.offset(ii);
                                                            if (this.doCrystal.getValue() && tempPos != null) {
                                                               this.doCrystal(tempPos);
                                                            }

                                                            return;
                                                         }
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                    break label231;
                                 }

                                 pos = (BlockPos)var16.next();
                              } while(this.behindWall(pos));
                           } while(mc.player.getPos().distanceTo(pos.toCenterPos().add(0.0, -0.5, 0.0)) > this.range.getValue());
                        } while(!this.canTouch(pos.down()));
                     } while(!this.canPlaceCrystal(pos, true, false));

                     Iterator var5 = list.iterator();

                     while(true) {
                        PlayerAndPredict pap;
                        float damage;
                        while(true) {
                           float selfDamage;
                           do {
                              do {
                                 do {
                                    do {
                                       if (!var5.hasNext()) {
                                          continue label231;
                                       }

                                       pap = (PlayerAndPredict)var5.next();
                                    } while(this.lite.getValue() && liteCheck(pos.toCenterPos().add(0.0, -0.5, 0.0), pap.predict.getPos()));

                                    damage = this.calculateDamage(pos, pap.player, pap.predict);
                                 } while(tempPos != null && !(damage > this.tempDamage));

                                 selfDamage = this.calculateDamage(pos, self.player, self.predict);
                              } while((double)selfDamage > this.maxSelf.getValue());
                           } while(this.noSuicide.getValue() > 0.0 && (double)selfDamage > (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) - this.noSuicide.getValue());

                           if (!(damage < EntityUtil.getHealth(pap.player))) {
                              break;
                           }

                           if (!((double)damage < this.getDamage(pap.player))) {
                              if (!this.smart.getValue()) {
                                 break;
                              }

                              if (this.getDamage(pap.player) == this.forceMin.getValue()) {
                                 if ((double)damage < (double)selfDamage - 2.5) {
                                    continue;
                                 }
                              } else if (damage < selfDamage) {
                                 continue;
                              }
                              break;
                           }
                        }

                        this.displayTarget = pap.player;
                        tempPos = pos;
                        this.tempDamage = damage;
                     }
                  }
               }

               if (this.doCrystal.getValue() && tempPos != null) {
                  this.doCrystal(tempPos);
               }

            }
         }
      }
   }

   public boolean canPlaceCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN) && BlockUtil.getClickSideStrict(obsPos) != null && !this.hasEntityBlockCrystal(boost, ignoreCrystal, ignoreItem) && !this.hasEntityBlockCrystal(boost.up(), ignoreCrystal, ignoreItem) && (BlockUtil.getBlock(boost) == Blocks.AIR || this.hasEntityBlockCrystal(boost, false, ignoreItem) && BlockUtil.getBlock(boost) == Blocks.FIRE) && (!CombatSetting.INSTANCE.lowVersion.getValue() || BlockUtil.getBlock(boost.up()) == Blocks.AIR);
   }

   public boolean hasEntityBlockCrystal(BlockPos pos, boolean ignoreCrystal, boolean ignoreItem) {
      Iterator var4 = mc.world.getNonSpectatingEntities(Entity.class, new Box(pos)).iterator();

      Entity entity;
      do {
         do {
            do {
               do {
                  if (!var4.hasNext()) {
                     return false;
                  }

                  entity = (Entity)var4.next();
               } while(!entity.isAlive());
            } while(ignoreItem && entity instanceof ItemEntity);
         } while(entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue());

         if (!(entity instanceof EndCrystalEntity)) {
            break;
         }

         if (!ignoreCrystal) {
            return true;
         }
      } while(mc.player.canSee(entity) || mc.player.getEyePos().distanceTo(entity.getPos()) <= this.wallRange.getValue());

      return true;
   }

   public boolean behindWall(BlockPos pos) {
      Vec3d testVec;
      if (CombatSetting.INSTANCE.lowVersion.getValue()) {
         testVec = new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5);
      } else {
         testVec = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.7, (double)pos.getZ() + 0.5);
      }

      HitResult result = mc.world.raycast(new RaycastContext(EntityUtil.getEyesPos(), testVec, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
      if (result != null && result.getType() != Type.MISS) {
         return mc.player.getEyePos().distanceTo(pos.toCenterPos().add(0.0, -0.5, 0.0)) > this.wallRange.getValue();
      } else {
         return false;
      }
   }

   public static boolean liteCheck(Vec3d from, Vec3d to) {
      return !canSee(from, to) && !canSee(from, to.add(0.0, 1.8, 0.0));
   }

   public static boolean canSee(Vec3d from, Vec3d to) {
      HitResult result = mc.world.raycast(new RaycastContext(from, to, ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
      return result == null || result.getType() == Type.MISS;
   }

   private boolean canTouch(BlockPos pos) {
      Direction side = BlockUtil.getClickSideStrict(pos);
      return side != null && pos.toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5, (double)side.getVector().getY() * 0.5, (double)side.getVector().getZ() * 0.5)).distanceTo(mc.player.getEyePos()) <= this.range.getValue();
   }

   public void doCrystal(BlockPos pos) {
      if (this.canPlaceCrystal(pos, false, true)) {
         if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal()) {
            this.doPlace(pos);
         }
      } else {
         this.doBreak(pos);
      }

   }

   public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
      return this.calculateDamage(new Vec3d((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5), player, predict);
   }

   public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
      if (this.terrainIgnore.getValue()) {
         CombatUtil.terrainIgnore = true;
      }

      float damage = 0.0F;
      switch ((AnchorAura.CalcMode)this.calcMode.getValue()) {
         case Meteor -> damage = (float)MeteorExplosionUtil.explosionDamage(player, pos, predict, 6.0F);
         case Thunder -> damage = ThunderExplosionUtil.calculateDamage(pos, player, predict, 6.0F);
         case OyVey -> damage = OyveyExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6.0F);
         case Edit -> damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6.0F);
         case Mio -> damage = MioExplosionUtil.calculateDamage(pos, player, predict, 6.0F);
      }

      CombatUtil.terrainIgnore = false;
      return damage;
   }

   private double getDamage(PlayerEntity target) {
      if (!SpeedMine.INSTANCE.obsidian.isPressed() && this.slowPlace.getValue() && this.lastBreakTimer.passedMs((long)this.slowDelay.getValue()) && (!BedAura.INSTANCE.isOn() || BedAura.INSTANCE.getBed() == -1)) {
         return this.slowMinDamage.getValue();
      } else if (this.forcePlace.getValue() && (double)EntityUtil.getHealth(target) <= this.forceMaxHealth.getValue() && !SpeedMine.INSTANCE.obsidian.isPressed()) {
         return this.forceMin.getValue();
      } else {
         if (this.armorBreaker.getValue()) {
            DefaultedList armors = target.getInventory().armor;
            Iterator var3 = armors.iterator();

            while(var3.hasNext()) {
               ItemStack armor = (ItemStack)var3.next();
               if (!armor.isEmpty() && !((double)EntityUtil.getDamagePercent(armor) > this.maxDurable.getValue())) {
                  return this.armorBreakerDamage.getValue();
               }
            }
         }

         return this.minDamage.getValue();
      }
   }

   private boolean findCrystal() {
      if (this.autoSwap.getValue() == AutoCrystal.SwapMode.Off) {
         return false;
      } else {
         return this.getCrystal() != -1;
      }
   }

   private void doBreak(BlockPos pos) {
      this.lastBreakTimer.reset();
      if (this.Break.getValue()) {
         if (CombatUtil.breakTimer.passedMs((long)this.breakDelay.getValue())) {
            Iterator var2 = mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1)).iterator();
            if (var2.hasNext()) {
               EndCrystalEntity entity = (EndCrystalEntity)var2.next();
               if (this.rotate.getValue() && this.onBreak.getValue() && !this.faceVector(entity.getPos().add(0.0, 0.25, 0.0))) {
                  return;
               }

               CombatUtil.breakTimer.reset();
               mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
               EntityUtil.swingHand(Hand.MAIN_HAND, (SwingSide)this.swingMode.getValue());
               if (this.breakRemove.getValue()) {
                  mc.world.removeEntity(entity.getId(), RemovalReason.KILLED);
               }

               if (this.tempDamage >= this.minDamage.getValueFloat() && this.spam.getValue()) {
                  this.doPlace(pos);
               }
            }

         }
      }
   }

   private void doPlace(BlockPos pos) {
      if (this.place.getValue()) {
         if (mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) || mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL) || this.findCrystal()) {
            if (this.canTouch(pos.down())) {
               BlockPos obsPos = pos.down();
               Direction facing = BlockUtil.getClickSide(obsPos);
               Vec3d vec = obsPos.toCenterPos().add((double)facing.getVector().getX() * 0.5, (double)facing.getVector().getY() * 0.5, (double)facing.getVector().getZ() * 0.5);
               if (!this.rotate.getValue() || this.faceVector(vec)) {
                  if (placeTimer.passedMs((long)this.placeDelay.getValue())) {
                     placeTimer.reset();
                     if (!mc.player.getMainHandStack().getItem().equals(Items.END_CRYSTAL) && !mc.player.getOffHandStack().getItem().equals(Items.END_CRYSTAL)) {
                        if (this.findCrystal()) {
                           int old = mc.player.getInventory().selectedSlot;
                           int crystal = this.getCrystal();
                           if (crystal == -1) {
                              return;
                           }

                           this.doSwap(crystal);
                           this.placeCrystal(pos);
                           if (this.autoSwap.getValue() == AutoCrystal.SwapMode.Silent) {
                              this.doSwap(old);
                           } else if (this.autoSwap.getValue() == AutoCrystal.SwapMode.Inventory) {
                              this.doSwap(crystal);
                              EntityUtil.syncInventory();
                           }
                        }
                     } else {
                        this.placeCrystal(pos);
                     }

                  }
               }
            }
         }
      }
   }

   private void doSwap(int slot) {
      if (this.autoSwap.getValue() != AutoCrystal.SwapMode.Silent && this.autoSwap.getValue() != AutoCrystal.SwapMode.Normal) {
         if (this.autoSwap.getValue() == AutoCrystal.SwapMode.Inventory) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
         }
      } else {
         InventoryUtil.switchToSlot(slot);
      }

   }

   private int getCrystal() {
      if (this.autoSwap.getValue() != AutoCrystal.SwapMode.Silent && this.autoSwap.getValue() != AutoCrystal.SwapMode.Normal) {
         return this.autoSwap.getValue() == AutoCrystal.SwapMode.Inventory ? InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL) : -1;
      } else {
         return InventoryUtil.findItem(Items.END_CRYSTAL);
      }
   }

   public void placeCrystal(BlockPos pos) {
      boolean offhand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
      BlockPos obsPos = pos.down();
      Direction facing = BlockUtil.getClickSide(obsPos);
      BlockUtil.clickBlock(obsPos, facing, false, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, (SwingSide)this.swingMode.getValue());
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

   public enum Page {
      General,
      Interact,
      Misc,
      Rotate,
      Calc,
      Render;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{General, Interact, Misc, Rotate, Calc, Render};
      }
   }

   public enum SwapMode {
      Off,
      Normal,
      Silent,
      Inventory;

      // $FF: synthetic method
      private static SwapMode[] $values() {
         return new SwapMode[]{Off, Normal, Silent, Inventory};
      }
   }

   public class CrystalRender {
      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (AutoCrystal.crystalPos != null) {
            AutoCrystal.this.noPosTimer.reset();
            AutoCrystal.placeVec3d = AutoCrystal.crystalPos.down().toCenterPos();
         }

         if (AutoCrystal.placeVec3d != null) {
            if (AutoCrystal.this.fadeSpeed.getValue() >= 1.0) {
               AutoCrystal.this.fade = AutoCrystal.this.noPosTimer.passedMs((long)(AutoCrystal.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5;
            } else {
               AutoCrystal.this.fade = AnimateUtil.animate(AutoCrystal.this.fade, AutoCrystal.this.noPosTimer.passedMs((long)(AutoCrystal.this.startFadeTime.getValue() * 1000.0)) ? 0.0 : 0.5, AutoCrystal.this.fadeSpeed.getValue() / 10.0);
            }

            if (AutoCrystal.this.fade == 0.0) {
               AutoCrystal.curVec3d = null;
            } else {
               if (AutoCrystal.curVec3d != null && !(AutoCrystal.this.sliderSpeed.getValue() >= 1.0)) {
                  AutoCrystal.curVec3d = new Vec3d(AnimateUtil.animate(AutoCrystal.curVec3d.x, AutoCrystal.placeVec3d.x, AutoCrystal.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AutoCrystal.curVec3d.y, AutoCrystal.placeVec3d.y, AutoCrystal.this.sliderSpeed.getValue() / 10.0), AnimateUtil.animate(AutoCrystal.curVec3d.z, AutoCrystal.placeVec3d.z, AutoCrystal.this.sliderSpeed.getValue() / 10.0));
               } else {
                  AutoCrystal.curVec3d = AutoCrystal.placeVec3d;
               }

               if (AutoCrystal.this.render.getValue()) {
                  Box cbox = new Box(AutoCrystal.curVec3d, AutoCrystal.curVec3d);
                  if (AutoCrystal.this.shrink.getValue()) {
                     cbox = cbox.expand(AutoCrystal.this.fade);
                  } else {
                     cbox = cbox.expand(0.5);
                  }

                  MatrixStack matrixStack = event.getMatrixStack();
                  if (AutoCrystal.this.fill.booleanValue) {
                     Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(AutoCrystal.this.fill.getValue(), (int)((double)AutoCrystal.this.fill.getValue().getAlpha() * AutoCrystal.this.fade * 2.0)));
                  }

                  if (AutoCrystal.this.box.booleanValue) {
                     if (!AutoCrystal.this.bold.getValue()) {
                        Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(AutoCrystal.this.box.getValue(), (int)((double)AutoCrystal.this.box.getValue().getAlpha() * AutoCrystal.this.fade * 2.0)));
                     } else {
                        Render3DUtil.drawLine(cbox, ColorUtil.injectAlpha(AutoCrystal.this.box.getValue(), (int)((double)AutoCrystal.this.box.getValue().getAlpha() * AutoCrystal.this.fade * 2.0)), (float)AutoCrystal.this.lineWidth.getValueInt());
                     }
                  }
               }

               if (AutoCrystal.this.text.booleanValue && AutoCrystal.this.lastDamage > 0.0F) {
                  Render3DUtil.drawText3D("" + AutoCrystal.this.lastDamage, AutoCrystal.curVec3d, AutoCrystal.this.text.getValue());
               }

            }
         }
      }
   }

   public class PlayerAndPredict {
      final PlayerEntity player;
      final PlayerEntity predict;

      public PlayerAndPredict(PlayerEntity player) {
         this.player = player;
         if (AutoCrystal.this.predictTicks.getValueFloat() > 0.0F) {
            this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            };
            this.predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)AutoCrystal.INSTANCE.predictTicks.getValueInt(), true)));
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

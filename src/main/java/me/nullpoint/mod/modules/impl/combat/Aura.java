package me.nullpoint.mod.modules.impl.combat;

import java.awt.Color;
import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.JelloUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.asm.accessors.ILivingEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Aura extends Module {
   public static Aura INSTANCE;
   public static Entity target;
   public final EnumSetting page;
   public final SliderSetting range;
   private final BooleanSetting ghost;
   private final EnumSetting cd;
   private final SliderSetting cooldown;
   private final SliderSetting wallRange;
   private final BooleanSetting whileEating;
   private final BooleanSetting weaponOnly;
   private final EnumSetting swingMode;
   private final BooleanSetting rotate;
   private final BooleanSetting newRotate;
   private final SliderSetting yawStep;
   private final BooleanSetting checkLook;
   private final SliderSetting fov;
   private final EnumSetting targetMode;
   public final BooleanSetting Players;
   public final BooleanSetting Mobs;
   public final BooleanSetting Animals;
   public final BooleanSetting Villagers;
   public final BooleanSetting Slimes;
   private final EnumSetting mode;
   private final ColorSetting color;
   public Vec3d directionVec;
   private final Timer ghostTimer;
   private final Timer tick;
   private float lastYaw;
   private float lastPitch;
   int attackTicks;
   public boolean sweeping;

   public Aura() {
      super("Aura", "Attacks players in radius", Module.Category.Combat);
      this.page = this.add(new EnumSetting("Page", Aura.Page.General));
      this.range = this.add(new SliderSetting("Range", 6.0, 0.10000000149011612, 7.0, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.ghost = this.add(new BooleanSetting("SweepingBypass", false, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.cd = this.add(new EnumSetting("CooldownMode", Aura.Cooldown.Delay));
      this.cooldown = this.add(new SliderSetting("Cooldown", 1.100000023841858, 0.0, 1.2000000476837158, 0.01, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.wallRange = this.add(new SliderSetting("WallRange", 6.0, 0.10000000149011612, 7.0, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.whileEating = this.add(new BooleanSetting("WhileUsing", true, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.weaponOnly = this.add(new BooleanSetting("WeaponOnly", true, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.swingMode = this.add(new EnumSetting("Swing", SwingSide.Server, (v) -> {
         return this.page.getValue() == Aura.Page.General;
      }));
      this.rotate = this.add((new BooleanSetting("Rotate", true, (v) -> {
         return this.page.getValue() == Aura.Page.Rotate;
      })).setParent());
      this.newRotate = this.add(new BooleanSetting("NewRotate", true, (v) -> {
         return this.rotate.isOpen() && this.page.getValue() == Aura.Page.Rotate;
      }));
      this.yawStep = this.add(new SliderSetting("YawStep", 0.30000001192092896, 0.10000000149011612, 1.0, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == Aura.Page.Rotate;
      }));
      this.checkLook = this.add(new BooleanSetting("CheckLook", true, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.page.getValue() == Aura.Page.Rotate;
      }));
      this.fov = this.add(new SliderSetting("Fov", 5.0, 0.0, 30.0, (v) -> {
         return this.rotate.isOpen() && this.newRotate.getValue() && this.checkLook.getValue() && this.page.getValue() == Aura.Page.Rotate;
      }));
      this.targetMode = this.add(new EnumSetting("Filter", Aura.TargetMode.DISTANCE, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.Players = this.add(new BooleanSetting("Players", true, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.Mobs = this.add(new BooleanSetting("Mobs", true, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.Animals = this.add(new BooleanSetting("Animals", true, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.Villagers = this.add(new BooleanSetting("Villagers", true, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.Slimes = this.add(new BooleanSetting("Slimes", true, (v) -> {
         return this.page.getValue() == Aura.Page.Target;
      }));
      this.mode = this.add(new EnumSetting("TargetESP", Aura.TargetESP.Jello, (v) -> {
         return this.page.getValue() == Aura.Page.Render;
      }));
      this.color = this.add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), (v) -> {
         return this.page.getValue() == Aura.Page.Render;
      }));
      this.directionVec = null;
      this.ghostTimer = new Timer();
      this.tick = new Timer();
      this.lastYaw = 0.0F;
      this.lastPitch = 0.0F;
      this.sweeping = false;
      INSTANCE = this;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.tick.passed(50L)) {
         ++this.attackTicks;
         this.tick.reset();
      }

      if (target != null) {
         doRender(matrixStack, partialTicks, target, this.color.getValue(), (TargetESP)this.mode.getValue());
      }

   }

   public static void doRender(MatrixStack matrixStack, float partialTicks, Entity entity, Color color, TargetESP mode) {
      switch (mode) {
         case Box -> Render3DUtil.draw3DBox(matrixStack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0.0, 0.1, 0.0), color, false, true);
         case Jello -> JelloUtil.drawJello(matrixStack, entity, color);
      }

   }

   public String getInfo() {
      return target == null ? null : target.getName().getString();
   }

   public void onUpdate() {
      if (this.tick.passed(50L)) {
         ++this.attackTicks;
         this.tick.reset();
      }

      if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
         target = null;
      } else {
         target = this.getTarget();
         if (target != null) {
            if (this.check()) {
               this.doAura();
            }

         }
      }
   }

   @EventHandler(
      priority = 98
   )
   public void onRotate(RotateEvent event) {
      if (target != null && this.newRotate.getValue() && this.directionVec != null) {
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

   private boolean check() {
      int at = this.attackTicks;
      if (this.cd.getValue() == Aura.Cooldown.Vanilla) {
         at = ((ILivingEntity)mc.player).getLastAttackedTicks();
      }

      if (!((double)Math.max((float)at / getAttackCooldownProgressPerTick(), 0.0F) >= this.cooldown.getValue())) {
         return false;
      } else {
         if (this.ghost.getValue()) {
            if (!this.ghostTimer.passedMs(600L)) {
               return false;
            }

            if (InventoryUtil.findClassInventorySlot(SwordItem.class) == -1) {
               return false;
            }
         }

         return this.whileEating.getValue() || !mc.player.isUsingItem();
      }
   }

   public static float getAttackCooldownProgressPerTick() {
      return (float)(1.0 / mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
   }

   private void doAura() {
      if (this.check()) {
         if (!this.rotate.getValue() || this.faceVector(target.getPos().add(0.0, 1.5, 0.0))) {
            int slot = InventoryUtil.findItemInventorySlot(Items.NETHERITE_SWORD);
            if (this.ghost.getValue()) {
               this.sweeping = true;
               InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
            }

            this.ghostTimer.reset();
            if (!this.ghost.getValue() && Criticals.INSTANCE.isOn()) {
               Criticals.INSTANCE.doCrit();
            }

            mc.interactionManager.attackEntity(mc.player, target);
            EntityUtil.swingHand(Hand.MAIN_HAND, (SwingSide)this.swingMode.getValue());
            if (this.ghost.getValue()) {
               InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
               this.sweeping = false;
            }

            this.attackTicks = 0;
         }
      }
   }

   public boolean faceVector(Vec3d directionVec) {
      if (!this.newRotate.getValue()) {
         EntityUtil.faceVectorNoStay(directionVec);
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

   private Entity getTarget() {
      Entity target = null;
      double distance = this.range.getValue();
      double maxHealth = 36.0;
      Iterator var6 = mc.world.getEntities().iterator();

      while(var6.hasNext()) {
         Entity entity = (Entity)var6.next();
         if (this.isEnemy(entity) && (mc.player.canSee(entity) || !((double)mc.player.distanceTo(entity) > this.wallRange.getValue())) && CombatUtil.isValid(entity, this.range.getValue())) {
            if (target == null) {
               target = entity;
               distance = mc.player.distanceTo(entity);
               maxHealth = EntityUtil.getHealth(entity);
            } else {
               if (entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity)entity, 10)) {
                  target = entity;
                  break;
               }

               if (this.targetMode.getValue() == Aura.TargetMode.HEALTH && (double)EntityUtil.getHealth(entity) < maxHealth) {
                  target = entity;
                  maxHealth = EntityUtil.getHealth(entity);
               } else if (this.targetMode.getValue() == Aura.TargetMode.DISTANCE && (double)mc.player.distanceTo(entity) < distance) {
                  target = entity;
                  distance = mc.player.distanceTo(entity);
               }
            }
         }
      }

      return target;
   }

   private boolean isEnemy(Entity entity) {
      if (entity instanceof SlimeEntity && this.Slimes.getValue()) {
         return true;
      } else if (entity instanceof PlayerEntity && this.Players.getValue()) {
         return true;
      } else if (entity instanceof VillagerEntity && this.Villagers.getValue()) {
         return true;
      } else if (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && this.Mobs.getValue()) {
         return true;
      } else {
         return entity instanceof AnimalEntity && this.Animals.getValue();
      }
   }

   private float[] injectStep(float[] angle, float steps) {
      if (steps < 0.1F) {
         steps = 0.1F;
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
      Rotate,
      Target,
      Render;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{General, Rotate, Target, Render};
      }
   }

   public enum Cooldown {
      Vanilla,
      Delay;

      // $FF: synthetic method
      private static Cooldown[] $values() {
         return new Cooldown[]{Vanilla, Delay};
      }
   }

   private enum TargetMode {
      DISTANCE,
      HEALTH;

      // $FF: synthetic method
      private static TargetMode[] $values() {
         return new TargetMode[]{DISTANCE, HEALTH};
      }
   }

   public enum TargetESP {
      Box,
      Jello,
      None;

      // $FF: synthetic method
      private static TargetESP[] $values() {
         return new TargetESP[]{Box, Jello, None};
      }
   }
}

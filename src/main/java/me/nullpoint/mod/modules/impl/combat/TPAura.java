// Decompiled with: Procyon 0.6.0
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import java.util.List;
import com.google.common.collect.Lists;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import java.util.Collection;
import me.nullpoint.api.utils.path.PathUtils;
import me.nullpoint.api.utils.combat.CombatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import me.nullpoint.asm.accessors.ILivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import me.nullpoint.api.utils.entity.EntityUtil;
import java.util.Iterator;
import me.nullpoint.api.utils.render.Render3DUtil;
import java.awt.Color;
import me.nullpoint.asm.accessors.IEntity;
import net.minecraft.client.util.math.MatrixStack;
import me.nullpoint.api.utils.path.Vec3;
import java.util.ArrayList;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import net.minecraft.entity.LivingEntity;
import me.nullpoint.mod.modules.Module;

public class TPAura extends Module
{
   public static TPAura INSTANCE;
   public static LivingEntity target;
   public final EnumSetting<Page> page;
   public final SliderSetting range;
   private final SliderSetting cooldown;
   private final EnumSetting<Aura.Cooldown> cd;
   private final BooleanSetting whileEating;
   private final BooleanSetting weaponOnly;
   private final EnumSetting<SwingSide> swingMode;
   private final EnumSetting<TargetMode> targetMode;
   public final BooleanSetting Players;
   public final BooleanSetting Mobs;
   public final BooleanSetting Animals;
   public final BooleanSetting Villagers;
   public final BooleanSetting Slimes;
   int attackTicks;
   private final Timer tick;
   private ArrayList<Vec3> lastPath;
   public static boolean attacking;

   public TPAura() {
      super("TPAura", "Attacks players in radius", Category.Combat);
      this.page = this.add(new EnumSetting<Page>("Page", Page.General));
      this.range = this.add(new SliderSetting("Range", 60.0, 0.10000000149011612, 250.0, v -> this.page.getValue() == Page.General));
      this.cooldown = this.add(new SliderSetting("Cooldown", 1.100000023841858, 0.0, 1.2000000476837158, 0.01, v -> this.page.getValue() == Page.General));
      this.cd = this.add(new EnumSetting<Aura.Cooldown>("CooldownMode", Aura.Cooldown.Delay));
      this.whileEating = this.add(new BooleanSetting("WhileUsing", true, v -> this.page.getValue() == Page.General));
      this.weaponOnly = this.add(new BooleanSetting("WeaponOnly", true, v -> this.page.getValue() == Page.General));
      this.swingMode = this.add(new EnumSetting<SwingSide>("Swing", SwingSide.Server, v -> this.page.getValue() == Page.General));
      this.targetMode = this.add(new EnumSetting<TargetMode>("Filter", TargetMode.DISTANCE, v -> this.page.getValue() == Page.Target));
      this.Players = this.add(new BooleanSetting("Players", true, v -> this.page.getValue() == Page.Target));
      this.Mobs = this.add(new BooleanSetting("Mobs", true, v -> this.page.getValue() == Page.Target));
      this.Animals = this.add(new BooleanSetting("Animals", true, v -> this.page.getValue() == Page.Target));
      this.Villagers = this.add(new BooleanSetting("Villagers", true, v -> this.page.getValue() == Page.Target));
      this.Slimes = this.add(new BooleanSetting("Slimes", true, v -> this.page.getValue() == Page.Target));
      this.tick = new Timer();
      TPAura.INSTANCE = this;
   }

   @Override
   public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
      if (this.tick.passed(50L)) {
         ++this.attackTicks;
         this.tick.reset();
      }
      if (this.lastPath != null) {
         for (final Vec3 vec3 : this.lastPath) {
            Render3DUtil.draw3DBox(matrixStack, ((IEntity)TPAura.mc.player).getDimensions().getBoxAt(vec3.mc()), new Color(255, 255, 255, 150), true, true);
         }
      }
   }

   @Override
   public void onUpdate() {
      if (this.tick.passed(50L)) {
         ++this.attackTicks;
         this.tick.reset();
      }
      if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(TPAura.mc.player)) {
         TPAura.target = null;
         return;
      }
      TPAura.target = this.getTarget();
      if (TPAura.target == null) {
         return;
      }
      if (this.auraReady()) {
         this.doTPHit(TPAura.target);
      }
      else {
         TPAura.target = null;
      }
   }

   private boolean auraReady() {
      int at = this.attackTicks;
      if (this.cd.getValue() == Aura.Cooldown.Vanilla) {
         at = ((ILivingEntity)TPAura.mc.player).getLastAttackedTicks();
      }
      return Math.max(at / getAttackCooldownProgressPerTick(), 0.0f) >= this.cooldown.getValue() && (this.whileEating.getValue() || !TPAura.mc.player.isUsingItem());
   }

   public static float getAttackCooldownProgressPerTick() {
      return (float)(1.0 / TPAura.mc.player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED) * 20.0);
   }

   private LivingEntity getTarget() {
      LivingEntity target = null;
      double distance = this.range.getValue();
      double maxHealth = 36.0;
      for (final Entity e : TPAura.mc.world.getEntities()) {
         if (e instanceof final LivingEntity entity) {
            if (!this.isEnemy(entity)) {
               continue;
            }
            if (!CombatUtil.isValid(entity, this.range.getValue())) {
               continue;
            }
            if (target == null) {
               target = entity;
               distance = TPAura.mc.player.distanceTo(entity);
               maxHealth = EntityUtil.getHealth(entity);
            }
            else {
               if (entity instanceof PlayerEntity && EntityUtil.isArmorLow((PlayerEntity)entity, 10)) {
                  target = entity;
                  break;
               }
               if (this.targetMode.getValue() == TargetMode.HEALTH && EntityUtil.getHealth(entity) < maxHealth) {
                  target = entity;
                  maxHealth = EntityUtil.getHealth(entity);
               }
               else {
                  if (this.targetMode.getValue() != TargetMode.DISTANCE || TPAura.mc.player.distanceTo(entity) >= distance) {
                     continue;
                  }
                  target = entity;
                  distance = TPAura.mc.player.distanceTo(entity);
               }
            }
         }
      }
      return target;
   }

   private void doTPHit(final LivingEntity entity) {
      TPAura.attacking = true;
      List<Vec3> tpPath = PathUtils.computePath(TPAura.mc.player, entity);
      this.lastPath = new ArrayList<Vec3>(tpPath);
      tpPath.forEach(vec3 -> TPAura.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec3.getX(), vec3.getY(), vec3.getZ(), true)));
      TPAura.mc.interactionManager.attackEntity(TPAura.mc.player, TPAura.target);
      EntityUtil.swingHand(Hand.MAIN_HAND, this.swingMode.getValue());
      tpPath = Lists.reverse(tpPath);
      tpPath.forEach(vec3 -> TPAura.mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec3.getX(), vec3.getY(), vec3.getZ(), true)));
      TPAura.attacking = false;
      this.attackTicks = 0;
   }

   private boolean isEnemy(final Entity entity) {
      return (entity instanceof SlimeEntity && this.Slimes.getValue()) || (entity instanceof PlayerEntity && this.Players.getValue()) || (entity instanceof VillagerEntity && this.Villagers.getValue()) || (!(entity instanceof VillagerEntity) && entity instanceof MobEntity && this.Mobs.getValue()) || (entity instanceof AnimalEntity && this.Animals.getValue());
   }

   static {
      TPAura.attacking = false;
   }

   private enum TargetMode
   {
      DISTANCE("DISTANCE", 0),
      HEALTH("HEALTH", 1);

      TargetMode(final String string, final int i) {
      }
   }

   public enum Page
   {
      General("General", 0),
      Target("Target", 1);

      Page(final String string, final int i) {
      }
   }
}

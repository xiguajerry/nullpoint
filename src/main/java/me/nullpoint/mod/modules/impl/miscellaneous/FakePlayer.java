package me.nullpoint.mod.modules.impl.miscellaneous;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.AnchorAura;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FakePlayer extends Module {
   public static FakePlayer INSTANCE;
   private final StringSetting name = this.add(new StringSetting("Name", "0ay"));
   private final BooleanSetting damage = this.add(new BooleanSetting("Damage", true));
   private final BooleanSetting autoTotem = this.add(new BooleanSetting("AutoTotem", true));
   private final BooleanSetting gApple = this.add(new BooleanSetting("GApple", true));
   public static OtherClientPlayerEntity fakePlayer;
   private final me.nullpoint.api.utils.math.Timer timer = new me.nullpoint.api.utils.math.Timer();
   int pops = 0;

   public FakePlayer() {
      super("FakePlayer", Module.Category.Misc);
      this.setDescription("Spawn fakeplayer.");
      INSTANCE = this;
   }

   public String getInfo() {
      return this.name.getValue();
   }

   public void onEnable() {
      this.pops = 0;
      if (nullCheck()) {
         this.disable();
      } else {
         fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("11451466-6666-6666-6666-666666666600"), this.name.getValue()));
         fakePlayer.getInventory().clone(mc.player.getInventory());
         mc.world.addEntity(fakePlayer);
         fakePlayer.copyPositionAndRotation(mc.player);
         fakePlayer.bodyYaw = mc.player.bodyYaw;
         fakePlayer.headYaw = mc.player.headYaw;
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
      }
   }

   public void onUpdate() {
      if (fakePlayer != null && !fakePlayer.isDead() && fakePlayer.clientWorld == mc.world) {
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 9999, 2));
         fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 9999, 4));
         if (this.gApple.getValue() && this.timer.passedMs(4000L)) {
            fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 9999, 1));
            this.timer.reset();
            fakePlayer.setAbsorptionAmount(16.0F);
         }

         if (this.autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            Nullpoint.POP.onTotemPop(fakePlayer);
            fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
         }

         if (fakePlayer.isDead() && fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
            fakePlayer.setHealth(10.0F);
            (new EntityStatusS2CPacket(fakePlayer, (byte)35)).apply(mc.player.networkHandler);
         }

      } else {
         this.disable();
      }
   }

   public void onDisable() {
      if (fakePlayer != null) {
         fakePlayer.kill();
         fakePlayer.setRemoved(RemovalReason.KILLED);
         fakePlayer.onRemoved();
         fakePlayer = null;
      }
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive event) {
      if (this.damage.getValue() && fakePlayer != null && fakePlayer.hurtTime == 0) {
         if (this.autoTotem.getValue() && fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            fakePlayer.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
         }

         Packet var3 = event.getPacket();
         if (var3 instanceof ExplosionS2CPacket explosion) {
             if (MathHelper.sqrt((float)(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ())).squaredDistanceTo(fakePlayer.getPos())) > 10.0F) {
               return;
            }

            float damage;
            if (BlockUtil.getBlock(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ())) == Blocks.RESPAWN_ANCHOR) {
               damage = (float)AnchorAura.INSTANCE.getAnchorDamage(new BlockPosX(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer, fakePlayer);
            } else {
               damage = AutoCrystal.INSTANCE.calculateDamage(new Vec3d(explosion.getX(), explosion.getY(), explosion.getZ()), fakePlayer, fakePlayer);
            }

            fakePlayer.onDamaged(mc.world.getDamageSources().generic());
            if (fakePlayer.getAbsorptionAmount() >= damage) {
               fakePlayer.setAbsorptionAmount(fakePlayer.getAbsorptionAmount() - damage);
            } else {
               float damage2 = damage - fakePlayer.getAbsorptionAmount();
               fakePlayer.setAbsorptionAmount(0.0F);
               fakePlayer.setHealth(fakePlayer.getHealth() - damage2);
            }
         }

         if (fakePlayer.isDead() && fakePlayer.tryUseTotem(mc.world.getDamageSources().generic())) {
            fakePlayer.setHealth(10.0F);
            (new EntityStatusS2CPacket(fakePlayer, (byte)35)).apply(mc.player.networkHandler);
         }
      }

   }
}

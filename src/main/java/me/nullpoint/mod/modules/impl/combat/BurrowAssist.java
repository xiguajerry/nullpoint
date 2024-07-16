package me.nullpoint.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.combat.MeteorExplosionUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BurrowAssist extends Module {
   public static BurrowAssist INSTANCE;
   public static Timer delay = new Timer();
   private final SliderSetting Delay = this.add(new SliderSetting("Delay", 100, 0, 1000));
   public BooleanSetting pause = this.add(new BooleanSetting("PauseEat", true));
   public SliderSetting speed = this.add(new SliderSetting("MaxSpeed", 8, 0, 20));
   public BooleanSetting ccheck = this.add((new BooleanSetting("CheckCrystal", true)).setParent());
   private final SliderSetting cRange = this.add(new SliderSetting("Range", 5.0, 0.0, 6.0, (v) -> {
      return this.ccheck.isOpen();
   }));
   private final SliderSetting breakMinSelf = this.add(new SliderSetting("BreakSelf", 12.0, 0.0, 36.0, (v) -> {
      return this.ccheck.isOpen();
   }));
   public BooleanSetting mcheck = this.add((new BooleanSetting("CheckMine", true)).setParent());
   public BooleanSetting mself = this.add(new BooleanSetting("Self", true, (v) -> {
      return this.mcheck.isOpen();
   }));
   private final SliderSetting predictTicks = this.add(new SliderSetting("PredictTicks", 4, 0, 10));
   private final BooleanSetting terrainIgnore = this.add(new BooleanSetting("TerrainIgnore", true));
   public final HashMap playerSpeeds = new HashMap();

   public BurrowAssist() {
      super("BurrowAssist", Module.Category.Combat);
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!nullCheck()) {
         if (delay.passed((long)this.Delay.getValue())) {
            if (!this.pause.getValue() || !mc.player.isUsingItem()) {
               if (!mc.options.jumpKey.isPressed()) {
                  if (canbur()) {
                     if (mc.player.isOnGround() && this.getPlayerSpeed(mc.player) < (double)this.speed.getValueInt()) {
                        if (this.ccheck.getValue() && this.mcheck.getValue()) {
                           if (!this.findcrystal() && !this.checkmine(this.mself.getValue())) {
                              return;
                           }
                        } else if (this.ccheck.getValue() && !this.findcrystal() || this.mcheck.getValue() && !this.checkmine(this.mself.getValue())) {
                           return;
                        }

                        if (Burrow.INSTANCE.isOn()) {
                           return;
                        }

                        Burrow.INSTANCE.enable();
                        delay.reset();
                     }

                  }
               }
            }
         }
      }
   }

   public boolean findcrystal() {
      PlayerAndPredict self = new PlayerAndPredict(mc.player);
      Iterator var2 = mc.world.getEntities().iterator();

      while(var2.hasNext()) {
         Entity crystal = (Entity)var2.next();
         if (crystal instanceof EndCrystalEntity && !(EntityUtil.getEyesPos().distanceTo(crystal.getPos()) > this.cRange.getValue())) {
            float selfDamage = this.calculateDamage(crystal.getPos(), self.player, self.predict);
            if (!((double)selfDamage < this.breakMinSelf.getValue())) {
               return true;
            }
         }
      }

      return false;
   }

   public double getPlayerSpeed(PlayerEntity player) {
      return this.playerSpeeds.get(player) == null ? 0.0 : this.turnIntoKpH((Double)this.playerSpeeds.get(player));
   }

   public double turnIntoKpH(double input) {
      return (double)MathHelper.sqrt((float)input) * 71.2729367892;
   }

   public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
      if (this.terrainIgnore.getValue()) {
         CombatUtil.terrainIgnore = true;
      }

      float damage = 0.0F;
      damage = (float)MeteorExplosionUtil.crystalDamage(player, pos, predict);
      CombatUtil.terrainIgnore = false;
      return damage;
   }

   public boolean checkmine(boolean self) {
      ArrayList<BlockPos> pos = new ArrayList<>();
      pos.add(EntityUtil.getPlayerPos(true));
      pos.add(new BlockPosX(mc.player.getX() + 0.4, mc.player.getY() + 0.5, mc.player.getZ() + 0.4));
      pos.add(new BlockPosX(mc.player.getX() - 0.4, mc.player.getY() + 0.5, mc.player.getZ() + 0.4));
      pos.add(new BlockPosX(mc.player.getX() + 0.4, mc.player.getY() + 0.5, mc.player.getZ() - 0.4));
      pos.add(new BlockPosX(mc.player.getX() - 0.4, mc.player.getY() + 0.5, mc.player.getZ() - 0.4));
      Iterator var3 = (new HashMap(Nullpoint.BREAK.breakMap)).values().iterator();

      while(true) {
         MineManager.BreakData breakData;
         do {
            do {
               if (!var3.hasNext()) {
                  if (!self) {
                     return false;
                  }

                  var3 = pos.iterator();

                  BlockPos pos1;
                  do {
                     if (!var3.hasNext()) {
                        return false;
                     }

                     pos1 = (BlockPos)var3.next();
                  } while(!pos1.equals(SpeedMine.breakPos));

                  return true;
               }

               breakData = (MineManager.BreakData)var3.next();
            } while(breakData == null);
         } while(breakData.getEntity() == null);

         Iterator var5 = pos.iterator();

         while(var5.hasNext()) {
            BlockPos pos1 = (BlockPos)var5.next();
            if (pos1.equals(breakData.pos) && breakData.getEntity() != mc.player) {
               return true;
            }
         }
      }
   }

   private static boolean canbur() {
      BlockPos pos1 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
      BlockPos pos2 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() + 0.3);
      BlockPos pos3 = new BlockPosX(mc.player.getX() + 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
      BlockPos pos4 = new BlockPosX(mc.player.getX() - 0.3, mc.player.getY() + 0.5, mc.player.getZ() - 0.3);
      BlockPos playerPos = EntityUtil.getPlayerPos(true);
      return Burrow.INSTANCE.canPlace(pos1) || Burrow.INSTANCE.canPlace(pos2) || Burrow.INSTANCE.canPlace(pos3) || Burrow.INSTANCE.canPlace(pos4);
   }

   public class PlayerAndPredict {
      PlayerEntity player;
      PlayerEntity predict;

      public PlayerAndPredict(PlayerEntity player) {
         this.player = player;
         if (BurrowAssist.this.predictTicks.getValueFloat() > 0.0F) {
            this.predict = new PlayerEntity(Wrapper.mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {
               public boolean isSpectator() {
                  return false;
               }

               public boolean isCreative() {
                  return false;
               }
            };
            this.predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, (float)BurrowAssist.INSTANCE.predictTicks.getValueInt(), true)));
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

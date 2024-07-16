package me.nullpoint.mod.modules.impl.movement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TimerEvent;
import me.nullpoint.api.utils.entity.MovementUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class FastFall extends Module {
   private final EnumSetting mode;
   private final BooleanSetting noLag;
   private final SliderSetting height;
   private final Timer lagTimer;
   private boolean useTimer;
   boolean onGround;

   public FastFall() {
      super("FastFall", "Miyagi son simulator", Module.Category.Movement);
      this.mode = this.add(new EnumSetting("Mode", FastFall.Mode.Fast));
      this.noLag = this.add(new BooleanSetting("NoLag", true, (v) -> {
         return this.mode.getValue() == FastFall.Mode.Fast;
      }));
      this.height = this.add(new SliderSetting("Height", 10.0, 1.0, 20.0, 0.5));
      this.lagTimer = new Timer();
      this.onGround = false;
   }

   public void onDisable() {
      this.useTimer = false;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   public void onUpdate() {
      if ((!(this.height.getValue() > 0.0) || !((double)this.traceDown() > this.height.getValue())) && !mc.player.isInsideWall() && !mc.player.isSubmergedInWater() && !mc.player.isInLava() && !mc.player.isHoldingOntoLadder() && this.lagTimer.passedMs(1000L) && !mc.player.isFallFlying() && !Flight.INSTANCE.isOn() && !nullCheck()) {
         if (!HoleKick.isInWeb(mc.player)) {
            if (mc.player.isOnGround()) {
               if (this.mode.getValue() == FastFall.Mode.Fast) {
                  MovementUtil.setMotionY(MovementUtil.getMotionY() - (double)(this.noLag.getValue() ? 0.62F : 1.0F));
               }

               if (this.traceDown() != 0 && (double)this.traceDown() <= this.height.getValue() && this.trace()) {
                  MovementUtil.setMotionX(MovementUtil.getMotionX() * 0.05);
                  MovementUtil.setMotionZ(MovementUtil.getMotionZ() * 0.05);
               }
            }

            if (this.mode.getValue() == FastFall.Mode.Strict) {
               if (!mc.player.isOnGround()) {
                  if (this.onGround) {
                     this.useTimer = true;
                  }

                  if (MovementUtil.getMotionY() >= 0.0) {
                     this.useTimer = false;
                  }

                  this.onGround = false;
               } else {
                  this.useTimer = false;
                  MovementUtil.setMotionY(-0.08);
                  this.onGround = true;
               }
            } else {
               this.useTimer = false;
            }

         }
      }
   }

   @EventHandler
   public void onTimer(TimerEvent event) {
      if (!nullCheck()) {
         if (!mc.player.isOnGround() && this.useTimer) {
            event.set(2.5F);
         }

      }
   }

   @EventHandler
   public void onPacket(PacketEvent.Receive event) {
      if (!nullCheck() && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.lagTimer.reset();
      }

   }

   private int traceDown() {
      int retval = 0;
      int y = (int)Math.round(mc.player.getY()) - 1;

      for(int tracey = y; tracey >= 0; --tracey) {
         HitResult trace = mc.world.raycast(new RaycastContext(mc.player.getPos(), new Vec3d(mc.player.getX(), tracey, mc.player.getZ()), ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
         if (trace != null && trace.getType() == Type.BLOCK) {
            return retval;
         }

         ++retval;
      }

      return retval;
   }

   private boolean trace() {
      Box bbox = mc.player.getBoundingBox();
      Vec3d basepos = bbox.getCenter();
      double minX = bbox.minX;
      double minZ = bbox.minZ;
      double maxX = bbox.maxX;
      double maxZ = bbox.maxZ;
      Map positions = new HashMap();
      positions.put(basepos, new Vec3d(basepos.x, basepos.y - 1.0, basepos.z));
      positions.put(new Vec3d(minX, basepos.y, minZ), new Vec3d(minX, basepos.y - 1.0, minZ));
      positions.put(new Vec3d(maxX, basepos.y, minZ), new Vec3d(maxX, basepos.y - 1.0, minZ));
      positions.put(new Vec3d(minX, basepos.y, maxZ), new Vec3d(minX, basepos.y - 1.0, maxZ));
      positions.put(new Vec3d(maxX, basepos.y, maxZ), new Vec3d(maxX, basepos.y - 1.0, maxZ));
      Iterator var12 = positions.keySet().iterator();

      BlockHitResult result;
      do {
         if (!var12.hasNext()) {
            BlockState state = mc.world.getBlockState(new BlockPosX(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ()));
            return state.isAir();
         }

         Vec3d key = (Vec3d)var12.next();
         RaycastContext context = new RaycastContext(key, (Vec3d)positions.get(key), ShapeType.COLLIDER, FluidHandling.NONE, mc.player);
         result = mc.world.raycast(context);
      } while(result == null || result.getType() != Type.BLOCK);

      return false;
   }

   private enum Mode {
      Fast,
      Strict;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Fast, Strict};
      }
   }
}

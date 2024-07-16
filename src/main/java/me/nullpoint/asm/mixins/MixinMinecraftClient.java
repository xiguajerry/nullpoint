package me.nullpoint.asm.mixins;

import me.nullpoint.Nullpoint;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.exploit.MineTweak;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public abstract class MixinMinecraftClient extends ReentrantThreadExecutor {
   @Shadow
   private IntegratedServer server;
   @Shadow
   public int attackCooldown;
   @Shadow
   public ClientPlayerEntity player;
   @Shadow
   public HitResult crosshairTarget;
   @Shadow
   public ClientPlayerInteractionManager interactionManager;
   @Final
   @Shadow
   public ParticleManager particleManager;
   @Shadow
   public ClientWorld world;

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   void postWindowInit(RunArgs args, CallbackInfo ci) {
      try {
         FontRenderers.Arial = FontRenderers.createArial(15.0F);
         FontRenderers.Calibri = FontRenderers.create("calibri", 1, 11.0F);
      } catch (Exception var4) {
         Exception e = var4;
         e.printStackTrace();
      }

   }

   @Shadow
   public ClientPlayNetworkHandler getNetworkHandler() {
      return null;
   }

   @Shadow
   public ServerInfo getCurrentServerEntry() {
      return null;
   }

   /**
    * @author 我是你爹
    * @reason
    */
   @Overwrite
   private String getWindowTitle() {
      if (CombatSetting.INSTANCE == null) {
         return "FlawLess: Loading..";
      } else {
          return "nullpoint.me";
      }
   }

   @Inject(
      method = {"handleBlockBreaking"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void handleBlockBreaking(boolean breaking, CallbackInfo ci) {
      if (this.attackCooldown <= 0 && this.player.isUsingItem() && MineTweak.INSTANCE.multiTask()) {
         if (breaking && this.crosshairTarget != null && this.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.crosshairTarget;
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!this.world.getBlockState(blockPos).isAir()) {
               Direction direction = blockHitResult.getSide();
               if (this.interactionManager.updateBlockBreakingProgress(blockPos, direction)) {
                  this.particleManager.addBlockBreakingParticles(blockPos, direction);
                  this.player.swingHand(Hand.MAIN_HAND);
               }
            }
         } else {
            this.interactionManager.cancelBlockBreaking();
         }

         ci.cancel();
      }

   }

   public MixinMinecraftClient(String string) {
      super(string);
   }

   @Inject(
      at = {@At("TAIL")},
      method = {"tick()V"}
   )
   public void tickTail(CallbackInfo info) {
      Nullpoint.SERVER.run();
      if (this.world != null) {
         Nullpoint.update();
      }

   }
}

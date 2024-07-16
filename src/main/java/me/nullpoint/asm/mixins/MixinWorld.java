package me.nullpoint.asm.mixins;

import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.mod.modules.impl.exploit.MineTweak;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({World.class})
public abstract class MixinWorld {
   @Inject(
      method = {"getBlockState"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void blockStateHook(BlockPos pos, CallbackInfoReturnable cir) {
      if (Wrapper.mc.world != null && Wrapper.mc.world.isInBuildLimit(pos)) {
         WorldChunk worldChunk;
         BlockState tempState;
         if (!CombatUtil.terrainIgnore && CombatUtil.modifyPos == null) {
            if (MineTweak.INSTANCE.isActive) {
               worldChunk = Wrapper.mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
               tempState = worldChunk.getBlockState(pos);
               if (tempState.getBlock() == Blocks.BEDROCK) {
                  cir.setReturnValue(Blocks.AIR.getDefaultState());
               }
            }
         } else {
            worldChunk = Wrapper.mc.world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            tempState = worldChunk.getBlockState(pos);
            if (pos.equals(CombatUtil.modifyPos)) {
               cir.setReturnValue(CombatUtil.modifyBlockState);
               return;
            }

            if (CombatUtil.terrainIgnore) {
               if (tempState.getBlock() == Blocks.OBSIDIAN || tempState.getBlock() == Blocks.BEDROCK || tempState.getBlock() == Blocks.ENDER_CHEST || tempState.getBlock() == Blocks.RESPAWN_ANCHOR || tempState.getBlock() == Blocks.NETHERITE_BLOCK) {
                  return;
               }

               cir.setReturnValue(Blocks.AIR.getDefaultState());
            }
         }
      }

   }
}

package me.nullpoint.asm.mixins;

import java.util.Iterator;
import me.nullpoint.mod.modules.impl.movement.Velocity;
import net.minecraft.fluid.FlowableFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({FlowableFluid.class})
public class MixinFlowableFluid {
   @Redirect(
      method = {"getVelocity"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/Iterator;hasNext()Z",
   ordinal = 0
)
   )
   private boolean getVelocity_hasNext(Iterator var9) {
      return (!Velocity.INSTANCE.isOn() || !Velocity.INSTANCE.waterPush.getValue()) && var9.hasNext();
   }
}

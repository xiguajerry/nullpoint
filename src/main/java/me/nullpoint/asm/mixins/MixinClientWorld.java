package me.nullpoint.asm.mixins;

import java.awt.Color;
import java.util.function.Supplier;
import me.nullpoint.mod.modules.impl.render.Ambience;
import me.nullpoint.mod.modules.impl.render.NoRender;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientWorld.class})
public abstract class MixinClientWorld extends World {
   protected MixinClientWorld(MutableWorldProperties properties, RegistryKey registryRef, DynamicRegistryManager registryManager, RegistryEntry dimensionEntry, Supplier profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
      super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
   }

   @Inject(
      method = {"getSkyColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable info) {
      if (Ambience.INSTANCE.isOn() && Ambience.INSTANCE.sky.booleanValue) {
         Color sky = Ambience.INSTANCE.sky.getValue();
         info.setReturnValue(new Vec3d((double)sky.getRed() / 255.0, (double)sky.getGreen() / 255.0, (double)sky.getBlue() / 255.0));
      }

   }

   public float getRainGradient(float delta) {
      return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0.0F : super.getRainGradient(delta);
   }

   public float getThunderGradient(float delta) {
      return NoRender.INSTANCE.isOn() && NoRender.INSTANCE.weather.getValue() ? 0.0F : super.getThunderGradient(delta);
   }
}

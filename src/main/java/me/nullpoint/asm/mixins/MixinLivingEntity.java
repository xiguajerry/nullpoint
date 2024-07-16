package me.nullpoint.asm.mixins;

import me.nullpoint.mod.modules.impl.movement.Sprint;
import me.nullpoint.mod.modules.impl.render.ViewModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class MixinLivingEntity extends Entity {
   @Final
   @Shadow
   private static EntityAttributeModifier SPRINTING_SPEED_BOOST;

   public MixinLivingEntity(EntityType type, World world) {
      super(type, world);
   }

   @Shadow
   public @Nullable EntityAttributeInstance getAttributeInstance(EntityAttribute attribute) {
      return this.getAttributes().getCustomInstance(attribute);
   }

   @Shadow
   public AttributeContainer getAttributes() {
      return null;
   }

   @Inject(
      method = {"getHandSwingDuration"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getArmSwingAnimationEnd(CallbackInfoReturnable info) {
      if (ViewModel.INSTANCE.isOn() && ViewModel.INSTANCE.slowAnimation.getValue()) {
         info.setReturnValue(ViewModel.INSTANCE.slowAnimationVal.getValueInt());
      }

   }

   @Inject(
      method = {"setSprinting"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setSprintingHook(boolean sprinting, CallbackInfo ci) {
      if ((Object)this == MinecraftClient.getInstance().player
              && Sprint.INSTANCE.isOn() && Sprint.INSTANCE.mode.getValue() == Sprint.Mode.Rage) {
         ci.cancel();
         sprinting = Sprint.shouldSprint;
         super.setSprinting(sprinting);
         EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
         entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.getId());
         if (sprinting) {
            entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
         }
      }

   }
}

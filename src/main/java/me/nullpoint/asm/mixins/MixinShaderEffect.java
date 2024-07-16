package me.nullpoint.asm.mixins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.nullpoint.api.interfaces.IShaderEffect;
import me.nullpoint.asm.accessors.IPostProcessShader;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PostEffectProcessor.class})
public class MixinShaderEffect implements IShaderEffect {
   @Unique
   private final List fakedBufferNames = new ArrayList();
   @Shadow
   @Final
   private Map targetsByName;
   @Shadow
   @Final
   private List passes;

   public void nullpoint_nextgen_master$addFakeTargetHook(String name, Framebuffer buffer) {
      Framebuffer previousFramebuffer = (Framebuffer)this.targetsByName.get(name);
      if (previousFramebuffer != buffer) {
         if (previousFramebuffer != null) {
            Iterator var4 = this.passes.iterator();

            while(var4.hasNext()) {
               PostEffectPass pass = (PostEffectPass)var4.next();
               if (pass.input == previousFramebuffer) {
                  ((IPostProcessShader)pass).setInput(buffer);
               }

               if (pass.output == previousFramebuffer) {
                  ((IPostProcessShader)pass).setOutput(buffer);
               }
            }

            this.targetsByName.remove(name);
            this.fakedBufferNames.remove(name);
         }

         this.targetsByName.put(name, buffer);
         this.fakedBufferNames.add(name);
      }
   }

   @Inject(
      method = {"close"},
      at = {@At("HEAD")}
   )
   void deleteFakeBuffersHook(CallbackInfo ci) {
      Iterator var2 = this.fakedBufferNames.iterator();

      while(var2.hasNext()) {
         String fakedBufferName = (String)var2.next();
         this.targetsByName.remove(fakedBufferName);
      }

   }
}

package me.nullpoint.mod.modules.impl.render;

import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;

public class CameraClip extends Module {
   public static CameraClip INSTANCE;
   public final SliderSetting getDistance = this.add(new SliderSetting("Distance", 4.0, 1.0, 20.0));
   public final SliderSetting animateTime = this.add(new SliderSetting("AnimationTime", 200, 0, 1000));
   private final BooleanSetting noFront = this.add(new BooleanSetting("NoFront", false));
   private final FadeUtils animation = new FadeUtils(300L);
   boolean first = false;

   public CameraClip() {
      super("CameraClip", Module.Category.Render);
      INSTANCE = this;
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT && this.noFront.getValue()) {
         mc.options.setPerspective(Perspective.FIRST_PERSON);
      }

      this.animation.setLength(this.animateTime.getValueInt());
      if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
         if (!this.first) {
            this.first = true;
            this.animation.reset();
         }
      } else if (this.first) {
         this.first = false;
         this.animation.reset();
      }

   }

   public double getDistance() {
      double quad = mc.options.getPerspective() == Perspective.FIRST_PERSON ? 1.0 - this.animation.easeOutQuad() : this.animation.easeOutQuad();
      return 1.0 + (this.getDistance.getValue() - 1.0) * quad;
   }
}

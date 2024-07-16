package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent extends Event {
   private final float partialTicks;
   private final MatrixStack matrixStack;

   public Render3DEvent(MatrixStack matrixStack, float partialTicks) {
      super(Event.Stage.Pre);
      this.partialTicks = partialTicks;
      this.matrixStack = matrixStack;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }

   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }
}

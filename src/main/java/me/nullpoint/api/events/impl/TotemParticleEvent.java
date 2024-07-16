package me.nullpoint.api.events.impl;

import java.awt.Color;
import me.nullpoint.api.events.Event;

public class TotemParticleEvent extends Event {
   public double velocityX;
   public double velocityY;
   public double velocityZ;
   public Color color;

   public TotemParticleEvent(double velocityX, double velocityY, double velocityZ) {
      super(Event.Stage.Pre);
      this.velocityX = velocityX;
      this.velocityY = velocityY;
      this.velocityZ = velocityZ;
   }
}

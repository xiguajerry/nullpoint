package me.nullpoint.api.events.impl;

import me.nullpoint.api.events.Event;

public class TimerEvent extends Event {
   private float timer = 1.0F;
   private boolean modified;

   public TimerEvent() {
      super(Event.Stage.Pre);
   }

   public float get() {
      return this.timer;
   }

   public void set(float timer) {
      this.modified = true;
      this.timer = timer;
   }

   public boolean isModified() {
      return this.modified;
   }
}

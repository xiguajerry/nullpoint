package me.nullpoint.api.events.eventbus;

import java.util.function.Consumer;

public class ConsumerListener implements IListener {
   private final Class target;
   private final int priority;
   private final Consumer executor;

   public ConsumerListener(Class target, int priority, Consumer executor) {
      this.target = target;
      this.priority = priority;
      this.executor = executor;
   }

   public ConsumerListener(Class target, Consumer executor) {
      this(target, 0, executor);
   }

   public void call(Object event) {
      this.executor.accept(event);
   }

   public Class getTarget() {
      return this.target;
   }

   public int getPriority() {
      return this.priority;
   }

   public boolean isStatic() {
      return false;
   }
}

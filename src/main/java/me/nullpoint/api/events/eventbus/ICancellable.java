package me.nullpoint.api.events.eventbus;

public interface ICancellable {
   void setCancelled(boolean var1);

   default void cancel() {
      this.setCancelled(true);
   }

   boolean isCancelled();
}

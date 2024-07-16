package me.nullpoint.api.events.eventbus;

public interface IEventBus {
   void registerLambdaFactory(String var1, LambdaListener.Factory var2);

   Object post(Object var1);

   ICancellable post(ICancellable var1);

   void subscribe(Object var1);

   void subscribe(Class var1);

   void subscribe(IListener var1);

   void unsubscribe(Object var1);

   void unsubscribe(Class var1);

   void unsubscribe(IListener var1);
}

package me.nullpoint.api.events.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class EventBus implements IEventBus {
   private final Map listenerCache = new ConcurrentHashMap();
   private final Map staticListenerCache = new ConcurrentHashMap();
   public final Map listenerMap = new ConcurrentHashMap();
   public final List lambdaFactoryInfos = new ArrayList();

   public void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory) {
      synchronized(this.lambdaFactoryInfos) {
         this.lambdaFactoryInfos.add(new LambdaFactoryInfo(packagePrefix, factory));
      }
   }

   public Object post(Object event) {
      List listeners = (List)this.listenerMap.get(event.getClass());
      if (listeners != null) {
         Iterator var3 = listeners.iterator();

         while(var3.hasNext()) {
            IListener listener = (IListener)var3.next();
            listener.call(event);
         }
      }

      return event;
   }

   public ICancellable post(ICancellable event) {
      List listeners = (List)this.listenerMap.get(event.getClass());
      if (listeners != null) {
         event.setCancelled(false);
         Iterator var3 = listeners.iterator();

         while(var3.hasNext()) {
            IListener listener = (IListener)var3.next();
            listener.call(event);
            if (event.isCancelled()) {
               break;
            }
         }
      }

      return event;
   }

   public void subscribe(Object object) {
      this.subscribe(this.getListeners(object.getClass(), object), false);
   }

   public void subscribe(Class klass) {
      this.subscribe(this.getListeners(klass, null), true);
   }

   public void subscribe(IListener listener) {
      this.subscribe(listener, false);
   }

   private void subscribe(List listeners, boolean onlyStatic) {
      Iterator var3 = listeners.iterator();

      while(var3.hasNext()) {
         IListener listener = (IListener)var3.next();
         this.subscribe(listener, onlyStatic);
      }

   }

   private void subscribe(IListener listener, boolean onlyStatic) {
      if (onlyStatic) {
         if (listener.isStatic()) {
            this.insert((List)this.listenerMap.computeIfAbsent(listener.getTarget(), (aClass) -> {
               return new CopyOnWriteArrayList();
            }), listener);
         }
      } else {
         this.insert((List)this.listenerMap.computeIfAbsent(listener.getTarget(), (aClass) -> {
            return new CopyOnWriteArrayList();
         }), listener);
      }

   }

   private void insert(List listeners, IListener listener) {
      int i;
      for(i = 0; i < listeners.size() && listener.getPriority() <= ((IListener)listeners.get(i)).getPriority(); ++i) {
      }

      listeners.add(i, listener);
   }

   public void unsubscribe(Object object) {
      this.unsubscribe(this.getListeners(object.getClass(), object), false);
   }

   public void unsubscribe(Class klass) {
      this.unsubscribe(this.getListeners(klass, null), true);
   }

   public void unsubscribe(IListener listener) {
      this.unsubscribe(listener, false);
   }

   private void unsubscribe(List listeners, boolean staticOnly) {
      Iterator var3 = listeners.iterator();

      while(var3.hasNext()) {
         IListener listener = (IListener)var3.next();
         this.unsubscribe(listener, staticOnly);
      }

   }

   private void unsubscribe(IListener listener, boolean staticOnly) {
      List l = (List)this.listenerMap.get(listener.getTarget());
      if (l != null) {
         if (staticOnly) {
            if (listener.isStatic()) {
               l.remove(listener);
            }
         } else {
            l.remove(listener);
         }
      }

   }

   private List getListeners(Class klass, Object object) {
      Function func = (o) -> {
         List listeners = new CopyOnWriteArrayList();
         this.getListeners(listeners, klass, object);
         return listeners;
      };
      if (object == null) {
         return (List)this.staticListenerCache.computeIfAbsent(klass, func);
      } else {
         Iterator var4 = this.listenerCache.keySet().iterator();

         Object key;
         do {
            if (!var4.hasNext()) {
               List listeners = (List)func.apply(object);
               this.listenerCache.put(object, listeners);
               return listeners;
            }

            key = var4.next();
         } while(key != object);

         return (List)this.listenerCache.get(object);
      }
   }

   private void getListeners(List listeners, Class klass, Object object) {
      Method[] var4 = klass.getDeclaredMethods();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Method method = var4[var6];
         if (this.isValid(method)) {
            listeners.add(new LambdaListener(this.getLambdaFactory(klass), klass, object, method));
         }
      }

      if (klass.getSuperclass() != null) {
         this.getListeners(listeners, klass.getSuperclass(), object);
      }

   }

   private boolean isValid(Method method) {
      if (!method.isAnnotationPresent(EventHandler.class)) {
         return false;
      } else if (method.getReturnType() != Void.TYPE) {
         return false;
      } else if (method.getParameterCount() != 1) {
         return false;
      } else {
         return !method.getParameters()[0].getType().isPrimitive();
      }
   }

   private LambdaListener.Factory getLambdaFactory(Class klass) {
      synchronized(this.lambdaFactoryInfos) {
         Iterator var3 = this.lambdaFactoryInfos.iterator();

         LambdaFactoryInfo info;
         do {
            if (!var3.hasNext()) {
               throw new NoLambdaFactoryException(klass);
            }

            info = (LambdaFactoryInfo)var3.next();
         } while(!klass.getName().startsWith(info.packagePrefix));

         return info.factory;
      }
   }

   private static class LambdaFactoryInfo {
      public final String packagePrefix;
      public final LambdaListener.Factory factory;

      public LambdaFactoryInfo(String packagePrefix, LambdaListener.Factory factory) {
         this.packagePrefix = packagePrefix;
         this.factory = factory;
      }
   }
}

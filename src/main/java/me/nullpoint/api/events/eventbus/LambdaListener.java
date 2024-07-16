package me.nullpoint.api.events.eventbus;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

public class LambdaListener implements IListener {
   private static boolean isJava1dot8;
   private static Constructor lookupConstructor;
   private static Method privateLookupInMethod;
   private final Class target;
   private final boolean isStatic;
   private final int priority;
   private Consumer executor;

   public LambdaListener(Factory factory, Class klass, Object object, Method method) {
      this.target = method.getParameters()[0].getType();
      this.isStatic = Modifier.isStatic(method.getModifiers());
      this.priority = method.getAnnotation(EventHandler.class).priority();

      try {
         String name = method.getName();
         MethodHandles.Lookup lookup;
         if (isJava1dot8) {
            boolean a = lookupConstructor.isAccessible();
            lookupConstructor.setAccessible(true);
            lookup = (MethodHandles.Lookup)lookupConstructor.newInstance(klass);
            lookupConstructor.setAccessible(a);
         } else {
            lookup = factory.create(privateLookupInMethod, klass);
         }

         MethodType methodType = MethodType.methodType(Void.TYPE, method.getParameters()[0].getType());
         MethodHandle methodHandle;
         MethodType invokedType;
         if (this.isStatic) {
            methodHandle = lookup.findStatic(klass, name, methodType);
            invokedType = MethodType.methodType(Consumer.class);
         } else {
            methodHandle = lookup.findVirtual(klass, name, methodType);
            invokedType = MethodType.methodType(Consumer.class, klass);
         }

         MethodHandle lambdaFactory = LambdaMetafactory.metafactory(lookup, "accept", invokedType, MethodType.methodType(Void.TYPE, Object.class), methodHandle, methodType).getTarget();
         if (this.isStatic) {
            this.executor = (Consumer) lambdaFactory.invoke();
         } else {
            this.executor = (Consumer) lambdaFactory.invoke(object);
         }
      } catch (Throwable var11) {
         Throwable throwable = var11;
         throwable.printStackTrace();
      }

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
      return this.isStatic;
   }

   static {
      try {
         isJava1dot8 = System.getProperty("java.version").startsWith("1.8");
         if (isJava1dot8) {
            lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
         } else {
            privateLookupInMethod = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
         }
      } catch (NoSuchMethodException var1) {
         NoSuchMethodException e = var1;
         e.printStackTrace();
      }

   }

   public interface Factory {
      MethodHandles.Lookup create(Method var1, Class var2) throws InvocationTargetException, IllegalAccessException;
   }
}

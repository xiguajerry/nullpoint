package me.nullpoint.api.utils.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class MathUtil {
   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double clamp(double value, double min, double max) {
      return value < min ? min : Math.min(value, max);
   }

   public static double square(double input) {
      return input * input;
   }

   public static float random(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }

   public static double random(double min, double max) {
      return (float)(Math.random() * (max - min) + min);
   }

   public static float rad(float angle) {
      return (float)((double)angle * Math.PI / 180.0);
   }

   public static double interpolate(double previous, double current, float delta) {
      return previous + (current - previous) * (double)delta;
   }

   public static Map sortByValue(Map map, boolean descending) {
      LinkedList list = new LinkedList(map.entrySet());
      if (descending) {
         list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
      } else {
         list.sort(Entry.comparingByValue());
      }

      LinkedHashMap result = new LinkedHashMap();
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         result.put(entry.getKey(), entry.getValue());
      }

      return result;
   }

   public static float round(float value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = BigDecimal.valueOf(value);
         bd = bd.setScale(places, RoundingMode.FLOOR);
         return bd.floatValue();
      }
   }
}

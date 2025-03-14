// Decompiled with: Procyon 0.6.0
// Class Version: 17
package me.nullpoint.mod.modules.settings;

import org.jetbrains.annotations.NotNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.common.base.Converter;

public class EnumConverter extends Converter<Enum, JsonElement>
{
   private final Class<? extends Enum> clazz;

   public EnumConverter(final Class<? extends Enum> clazz) {
      this.clazz = clazz;
   }

   public static int currentEnum(final Enum clazz) {
      for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
         final Enum e = ((Enum[])clazz.getClass().getEnumConstants())[i];
         if (e.name().equalsIgnoreCase(clazz.name())) {
            return i;
         }
      }
      return -1;
   }

   public static Enum increaseEnum(final Enum clazz) {
      final int index = currentEnum(clazz);
      for (int i = 0; i < clazz.getClass().getEnumConstants().length; ++i) {
         final Enum e = ((Enum[])clazz.getClass().getEnumConstants())[i];
         if (i == index + 1) {
            return e;
         }
      }
      return ((Enum[])clazz.getClass().getEnumConstants())[0];
   }

   public static String getProperName(final Enum clazz) {
      return Character.toUpperCase(clazz.name().charAt(0)) + clazz.name().toLowerCase().substring(1);
   }

   @NotNull
   public JsonElement doForward(final Enum anEnum) {
      return new JsonPrimitive(anEnum.toString());
   }

   public Enum doBackward(final String string) {
      try {
         return Enum.valueOf(this.clazz, string);
      }
      catch (final IllegalArgumentException e) {
         return null;
      }
   }

   @NotNull
   public Enum doBackward(final JsonElement jsonElement) {
      try {
         return Enum.valueOf(this.clazz, jsonElement.getAsString());
      }
      catch (final IllegalArgumentException e) {
         return null;
      }
   }
}

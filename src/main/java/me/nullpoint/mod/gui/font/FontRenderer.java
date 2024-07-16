// Decompiled with: Procyon 0.6.0
// Class Version: 17
package me.nullpoint.mod.gui.font;

import java.lang.invoke.CallSite;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.Contract;
import me.nullpoint.api.utils.Wrapper;
import java.awt.Color;
import me.nullpoint.Nullpoint;
import java.util.Iterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.joml.Matrix4f;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import java.util.List;
import net.minecraft.client.render.Tessellator;
import java.util.function.Supplier;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import org.jetbrains.annotations.NotNull;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Random;
import java.awt.Font;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.io.Closeable;

public class FontRenderer implements Closeable
{
   private static final Char2IntArrayMap colorCodes;
   private static final int BLOCK_SIZE = 256;
   private static final Object2ObjectArrayMap<Identifier, ObjectList<DrawEntry>> GLYPH_PAGE_CACHE;
   private final float originalSize;
   private final ObjectList<GlyphMap> maps;
   private final Char2ObjectArrayMap<Glyph> allGlyphs;
   private int scaleMul;
   private Font[] fonts;
   private int previousGameScale;
   private static final char RND_START = 'a';
   private static final char RND_END = 'z';
   private static final Random RND;

   public FontRenderer(final Font[] fonts, final float sizePx) {
      this.maps = (ObjectList<GlyphMap>)new ObjectArrayList();
      this.allGlyphs = (Char2ObjectArrayMap<Glyph>)new Char2ObjectArrayMap();
      this.scaleMul = 0;
      this.previousGameScale = -1;
      Preconditions.checkArgument(fonts.length > 0, "fonts.length == 0");
      this.init(fonts, this.originalSize = sizePx);
   }

   private static int floorNearestMulN(final int x) {
      return 256 * (int)Math.floor(x / 256.0);
   }

   @NotNull
   public static String stripControlCodes(@NotNull final String text) {
      final char[] chars = text.toCharArray();
      final StringBuilder f = new StringBuilder();
      for (int i = 0; i < chars.length; ++i) {
         final char c = chars[i];
         if (c == '§') {
            ++i;
         }
         else {
            f.append(c);
         }
      }
      return f.toString();
   }

   private void sizeCheck() {
      final int gs = getGuiScale();
      if (gs != this.previousGameScale) {
         this.close();
         this.init(this.fonts, this.originalSize);
      }
   }

   private void init(final Font[] fonts, final float sizePx) {
      this.previousGameScale = getGuiScale();
      this.scaleMul = this.previousGameScale;
      this.fonts = new Font[fonts.length];
      for (int i = 0; i < fonts.length; ++i) {
         this.fonts[i] = fonts[i].deriveFont(sizePx * this.scaleMul);
      }
   }

   @NotNull
   private GlyphMap generateMap(final char from, final char to) {
      final GlyphMap gm = new GlyphMap(from, to, this.fonts, randomIdentifier());
      this.maps.add(gm);
      return gm;
   }

   private Glyph locateGlyph0(final char glyph) {
      for (final GlyphMap map : this.maps) {
         if (map.contains(glyph)) {
            return map.getGlyph(glyph);
         }
      }
      final int base = floorNearestMulN(glyph);
      final GlyphMap glyphMap = this.generateMap((char)base, (char)(base + 256));
      return glyphMap.getGlyph(glyph);
   }

   private Glyph locateGlyph1(final char glyph) {
      return this.allGlyphs.computeIfAbsent(glyph, this::locateGlyph0);
   }

   public void drawString(@NotNull final MatrixStack stack, @NotNull final String s, final float x, final float y, final float r, final float g, final float b, final float a) {
      this.drawString(stack, s, x + 1.0f, y + 1.0f, 0.0f, 0.0f, 0.0f, a, true);
      this.drawString(stack, s, x, y, r, g, b, a, false);
   }

   public void drawString(@NotNull final MatrixStack stack, @NotNull final String s, final float x, final float y, final float r, final float g, final float b, final float a, final boolean shadow) {
      this.sizeCheck();
      float r2 = r;
      float g2 = g;
      float b2 = b;
      stack.push();
      stack.translate(x, y, 0.0f);
      stack.scale(1.0f / this.scaleMul, 1.0f / this.scaleMul, 1.0f);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      final BufferBuilder bb = Tessellator.getInstance().getBuffer();
      final Matrix4f mat = stack.peek().getPositionMatrix();
      final char[] chars = s.toCharArray();
      float xOffset = 0.0f;
      float yOffset = 0.0f;
      boolean inSel = false;
      int lineStart = 0;
      for (int i = 0; i < chars.length; ++i) {
         final char c = chars[i];
         if (inSel) {
            inSel = false;
            final char c2 = Character.toUpperCase(c);
            if (FontRenderer.colorCodes.containsKey(c2) && !shadow) {
               final int ii = FontRenderer.colorCodes.get(c2);
               final int[] col = RGBIntToRGB(ii);
               r2 = col[0] / 255.0f;
               g2 = col[1] / 255.0f;
               b2 = col[2] / 255.0f;
            }
            else if (c2 == 'R') {
               r2 = r;
               g2 = g;
               b2 = b;
            }
         }
         else if (c == '§') {
            inSel = true;
         }
         else if (c == '\n') {
            yOffset += this.getStringHeight(s.substring(lineStart, i)) * this.scaleMul;
            xOffset = 0.0f;
            lineStart = i + 1;
         }
         else {
            final Glyph glyph = this.locateGlyph1(c);
            if (glyph.value() != ' ') {
               final Identifier i2 = glyph.owner().bindToTexture;
               final DrawEntry entry = new DrawEntry(xOffset, yOffset, r2, g2, b2, glyph);
               FontRenderer.GLYPH_PAGE_CACHE.computeIfAbsent(i2, integer -> new ObjectArrayList()).add(entry);
            }
            xOffset += glyph.width();
         }
      }
      for (final Identifier identifier : FontRenderer.GLYPH_PAGE_CACHE.keySet()) {
         RenderSystem.setShaderTexture(0, identifier);
         final List<DrawEntry> objects = FontRenderer.GLYPH_PAGE_CACHE.get(identifier);
         bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         for (final DrawEntry object : objects) {
            final float xo = object.atX;
            final float yo = object.atY;
            final float cr = object.r;
            final float cg = object.g;
            final float cb = object.elementCodec;
            final Glyph glyph2 = object.toDraw;
            final GlyphMap owner = glyph2.owner();
            final float w = (float)glyph2.width();
            final float h = (float)glyph2.height();
            final float u1 = glyph2.u() / (float)owner.width;
            final float v1 = glyph2.v() / (float)owner.height;
            final float u2 = (glyph2.u() + glyph2.width()) / (float)owner.width;
            final float v2 = (glyph2.v() + glyph2.height()) / (float)owner.height;
            bb.vertex(mat, xo + 0.0f, yo + h, 0.0f).texture(u1, v2).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + w, yo + h, 0.0f).texture(u2, v2).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + w, yo + 0.0f, 0.0f).texture(u2, v1).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + 0.0f, yo + 0.0f, 0.0f).texture(u1, v1).color(cr, cg, cb, a).next();
         }
         BufferRenderer.drawWithGlobalProgram(bb.end());
      }
      stack.pop();
      FontRenderer.GLYPH_PAGE_CACHE.clear();
   }

   public void drawGradientString(@NotNull final MatrixStack stack, @NotNull final String s, final float x, final float y) {
      this.sizeCheck();
      stack.push();
      stack.translate(x, y, 0.0f);
      stack.scale(1.0f / this.scaleMul, 1.0f / this.scaleMul, 1.0f);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      final BufferBuilder bb = Tessellator.getInstance().getBuffer();
      final Matrix4f mat = stack.peek().getPositionMatrix();
      final char[] chars = s.toCharArray();
      float xOffset = 0.0f;
      float yOffset = 0.0f;
      int lineStart = 0;
      float a = 1.0f;
      for (int i = 0; i < chars.length; ++i) {
         final char c = chars[i];
         final Color color = Nullpoint.GUI.getColor();
         a = color.getAlpha() / 255.0f;
         if (c == '\n') {
            yOffset += this.getStringHeight(s.substring(lineStart, i)) * this.scaleMul;
            xOffset = 0.0f;
            lineStart = i + 1;
         }
         else {
            final Glyph glyph = this.locateGlyph1(c);
            if (glyph.value() != ' ') {
               final Identifier i2 = glyph.owner().bindToTexture;
               final DrawEntry entry = new DrawEntry(xOffset, yOffset, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, glyph);
               FontRenderer.GLYPH_PAGE_CACHE.computeIfAbsent(i2, integer -> new ObjectArrayList()).add(entry);
            }
            xOffset += glyph.width();
         }
      }
      for (final Identifier identifier : FontRenderer.GLYPH_PAGE_CACHE.keySet()) {
         RenderSystem.setShaderTexture(0, identifier);
         final List<DrawEntry> objects = FontRenderer.GLYPH_PAGE_CACHE.get(identifier);
         bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         for (final DrawEntry object : objects) {
            final float xo = object.atX;
            final float yo = object.atY;
            final float cr = object.r;
            final float cg = object.g;
            final float cb = object.elementCodec;
            final Glyph glyph2 = object.toDraw;
            final GlyphMap owner = glyph2.owner();
            final float w = (float)glyph2.width();
            final float h = (float)glyph2.height();
            final float u1 = glyph2.u() / (float)owner.width;
            final float v1 = glyph2.v() / (float)owner.height;
            final float u2 = (glyph2.u() + glyph2.width()) / (float)owner.width;
            final float v2 = (glyph2.v() + glyph2.height()) / (float)owner.height;
            bb.vertex(mat, xo + 0.0f, yo + h, 0.0f).texture(u1, v2).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + w, yo + h, 0.0f).texture(u2, v2).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + w, yo + 0.0f, 0.0f).texture(u2, v1).color(cr, cg, cb, a).next();
            bb.vertex(mat, xo + 0.0f, yo + 0.0f, 0.0f).texture(u1, v1).color(cr, cg, cb, a).next();
         }
         BufferRenderer.drawWithGlobalProgram(bb.end());
      }
      stack.pop();
      FontRenderer.GLYPH_PAGE_CACHE.clear();
   }

   public void drawCenteredString(final MatrixStack stack, final String s, final float x, final float y, final float r, final float g, final float b, final float a) {
      this.drawString(stack, s, x - this.getStringWidth(s) / 2.0f, y, r, g, b, a);
   }

   public float getStringWidth(final String text) {
      final char[] c = stripControlCodes(text).toCharArray();
      float currentLine = 0.0f;
      float maxPreviousLines = 0.0f;
      for (final char c2 : c) {
         if (c2 == '\n') {
            maxPreviousLines = Math.max(currentLine, maxPreviousLines);
            currentLine = 0.0f;
         }
         else {
            final Glyph glyph = this.locateGlyph1(c2);
            final float gWidth = (glyph == null) ? 1.0f : ((float)glyph.width());
            currentLine += gWidth / this.scaleMul;
         }
      }
      return Math.max(currentLine, maxPreviousLines);
   }

   public float getStringHeight(final String text) {
      char[] c = stripControlCodes(text).toCharArray();
      if (c.length == 0) {
         c = new char[] { ' ' };
      }
      float currentLine = 0.0f;
      float previous = 0.0f;
      for (final char c2 : c) {
         if (c2 == '\n') {
            if (currentLine == 0.0f) {
               currentLine = this.locateGlyph1(' ').height() / (float)this.scaleMul;
            }
            previous += currentLine;
            currentLine = 0.0f;
         }
         else {
            final Glyph glyph = this.locateGlyph1(c2);
            currentLine = Math.max(glyph.height() / (float)this.scaleMul, currentLine);
         }
      }
      return currentLine + previous;
   }

   @Override
   public void close() {
      for (final GlyphMap map : this.maps) {
         map.destroy();
      }
      this.maps.clear();
      this.allGlyphs.clear();
   }

   public static int getGuiScale() {
      return (int)Wrapper.mc.getWindow().getScaleFactor();
   }

   @Contract(value = "_ -> new", pure = true)
   public static int[] RGBIntToRGB(final int in) {
      final int red = in >> 16 & 0xFF;
      final int green = in >> 8 & 0xFF;
      final int blue = in & 0xFF;
      return new int[] { red, green, blue };
   }

   @Contract(value = "-> new", pure = true)
   @NotNull
   public static Identifier randomIdentifier() {
      return new Identifier("nullpoint", "temp/" + randomString(32));
   }

   private static String randomString(final int length) {
      return IntStream.range(0, length).mapToObj(operand -> String.valueOf((char)FontRenderer.RND.nextInt(97, 123))).collect(Collectors.joining());
   }

   static {
      colorCodes = new Char2IntArrayMap() {
         {
            this.put('0', 0);
            this.put('1', 170);
            this.put('2', 43520);
            this.put('3', 43690);
            this.put('4', 11141120);
            this.put('5', 11141290);
            this.put('6', 16755200);
            this.put('7', 11184810);
            this.put('8', 5592405);
            this.put('9', 5592575);
            this.put('A', 5635925);
            this.put('B', 5636095);
            this.put('C', 16733525);
            this.put('D', 16733695);
            this.put('E', 16777045);
            this.put('F', 16777215);
         }
      };
      GLYPH_PAGE_CACHE = new Object2ObjectArrayMap();
      RND = new Random();
   }

   static final class DrawEntry
   {
      private final float atX;
      private final float atY;
      private final float r;
      private final float g;
      private final float elementCodec;
      private final Glyph toDraw;

      DrawEntry(final float atX, final float atY, final float r, final float g, final float b, final Glyph toDraw) {
         this.atX = atX;
         this.atY = atY;
         this.r = r;
         this.g = g;
         this.elementCodec = b;
         this.toDraw = toDraw;
      }

      @Override
      public String toString() {
         return /* invokedynamic(!) */ProcyonInvokeDynamicHelper_1.invoke(this);
      }

      @Override
      public int hashCode() {
         return /* invokedynamic(!) */ProcyonInvokeDynamicHelper_2.invoke(this);
      }

      @Override
      public boolean equals(final Object o) {
         return /* invokedynamic(!) */ProcyonInvokeDynamicHelper_3.invoke(this, o);
      }

      public float atX() {
         return this.atX;
      }

      public float atY() {
         return this.atY;
      }

      public float r() {
         return this.r;
      }

      public float g() {
         return this.g;
      }

      public float elementCodec() {
         return this.elementCodec;
      }

      public Glyph toDraw() {
         return this.toDraw;
      }

      // This helper class was generated by Procyon to approximate the behavior of an
      // 'invokedynamic' instruction that it doesn't know how to interpret.
      private static final class ProcyonInvokeDynamicHelper_1
      {
         private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
         private static MethodHandle handle;
         private static volatile int fence;

         private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_1.handle;
            if (handle != null)
               return handle;
            return ProcyonInvokeDynamicHelper_1.ensureHandle();
         }

         private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_1.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_1.handle;
            if (handle == null) {
               MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_1.LOOKUP;
               try {
                  handle = ((CallSite)ObjectMethods.bootstrap(lookup, "toString", MethodType.methodType(String.class, DrawEntry.class), DrawEntry.class, "atX;atY;r;g;b;toDraw", lookup.findGetter(DrawEntry.class, "atX", float.class), lookup.findGetter(DrawEntry.class, "atY", float.class), lookup.findGetter(DrawEntry.class, "r", float.class), lookup.findGetter(DrawEntry.class, "g", float.class), lookup.findGetter(DrawEntry.class, "b", float.class), lookup.findGetter(DrawEntry.class, "toDraw", Glyph.class))).dynamicInvoker();
               }
               catch (Throwable t) {
                  throw new UndeclaredThrowableException(t);
               }
               ProcyonInvokeDynamicHelper_1.fence = 1;
               ProcyonInvokeDynamicHelper_1.handle = handle;
               ProcyonInvokeDynamicHelper_1.fence = 0;
            }
            return handle;
         }

         private static String invoke(DrawEntry p0) {
            try {
               return (String) ProcyonInvokeDynamicHelper_1.handle().invokeExact(p0);
            }
            catch (Throwable t) {
               throw new UndeclaredThrowableException(t);
            }
         }
      }

      // This helper class was generated by Procyon to approximate the behavior of an
      // 'invokedynamic' instruction that it doesn't know how to interpret.
      private static final class ProcyonInvokeDynamicHelper_2
      {
         private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
         private static MethodHandle handle;
         private static volatile int fence;

         private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_2.handle;
            if (handle != null)
               return handle;
            return ProcyonInvokeDynamicHelper_2.ensureHandle();
         }

         private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_2.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_2.handle;
            if (handle == null) {
               MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_2.LOOKUP;
               try {
                  handle = ((CallSite)ObjectMethods.bootstrap(lookup, "hashCode", MethodType.methodType(int.class, DrawEntry.class), DrawEntry.class, "atX;atY;r;g;b;toDraw", lookup.findGetter(DrawEntry.class, "atX", float.class), lookup.findGetter(DrawEntry.class, "atY", float.class), lookup.findGetter(DrawEntry.class, "r", float.class), lookup.findGetter(DrawEntry.class, "g", float.class), lookup.findGetter(DrawEntry.class, "b", float.class), lookup.findGetter(DrawEntry.class, "toDraw", Glyph.class))).dynamicInvoker();
               }
               catch (Throwable t) {
                  throw new UndeclaredThrowableException(t);
               }
               ProcyonInvokeDynamicHelper_2.fence = 1;
               ProcyonInvokeDynamicHelper_2.handle = handle;
               ProcyonInvokeDynamicHelper_2.fence = 0;
            }
            return handle;
         }

         private static int invoke(DrawEntry p0) {
            try {
               return (int) ProcyonInvokeDynamicHelper_2.handle().invokeExact(p0);
            }
            catch (Throwable t) {
               throw new UndeclaredThrowableException(t);
            }
         }
      }

      // This helper class was generated by Procyon to approximate the behavior of an
      // 'invokedynamic' instruction that it doesn't know how to interpret.
      private static final class ProcyonInvokeDynamicHelper_3
      {
         private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
         private static MethodHandle handle;
         private static volatile int fence;

         private static MethodHandle handle() {
            final MethodHandle handle = ProcyonInvokeDynamicHelper_3.handle;
            if (handle != null)
               return handle;
            return ProcyonInvokeDynamicHelper_3.ensureHandle();
         }

         private static MethodHandle ensureHandle() {
            ProcyonInvokeDynamicHelper_3.fence = 0;
            MethodHandle handle = ProcyonInvokeDynamicHelper_3.handle;
            if (handle == null) {
               MethodHandles.Lookup lookup = ProcyonInvokeDynamicHelper_3.LOOKUP;
               try {
                  handle = ((CallSite)ObjectMethods.bootstrap(lookup, "equals", MethodType.methodType(boolean.class, DrawEntry.class, Object.class), DrawEntry.class, "atX;atY;r;g;b;toDraw", lookup.findGetter(DrawEntry.class, "atX", float.class), lookup.findGetter(DrawEntry.class, "atY", float.class), lookup.findGetter(DrawEntry.class, "r", float.class), lookup.findGetter(DrawEntry.class, "g", float.class), lookup.findGetter(DrawEntry.class, "b", float.class), lookup.findGetter(DrawEntry.class, "toDraw", Glyph.class))).dynamicInvoker();
               }
               catch (Throwable t) {
                  throw new UndeclaredThrowableException(t);
               }
               ProcyonInvokeDynamicHelper_3.fence = 1;
               ProcyonInvokeDynamicHelper_3.handle = handle;
               ProcyonInvokeDynamicHelper_3.fence = 0;
            }
            return handle;
         }

         private static boolean invoke(DrawEntry p0, Object p1) {
            try {
               return (boolean) ProcyonInvokeDynamicHelper_3.handle().invokeExact(p0, p1);
            }
            catch (Throwable t) {
               throw new UndeclaredThrowableException(t);
            }
         }
      }
   }
}

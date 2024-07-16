/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package me.nullpoint.mod.gui.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.Objects;
import me.nullpoint.mod.gui.font.FontAdapter;
import me.nullpoint.mod.gui.font.RendererFontAdapter;
import org.jetbrains.annotations.NotNull;

public class FontRenderers {
    public static FontAdapter Arial;
    public static FontAdapter Calibri;

    @NotNull
    public static RendererFontAdapter createDefault(float size, String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(0, Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("assets/minecraft/font/" + name + ".ttf"))).deriveFont(0, size), size);
    }

    public static RendererFontAdapter createArial(float size) {
        Font font = new Font("tahoma", 0, (int)size);
        return new RendererFontAdapter(font, size);
    }

    public static RendererFontAdapter create(String name, int style, float size) {
        return new RendererFontAdapter(new Font(name, style, (int)size), size);
    }
}


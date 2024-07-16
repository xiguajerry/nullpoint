/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 */
package me.nullpoint.mod.gui.clickgui.components;

import java.awt.Color;
import java.util.Objects;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.render.AnimateUtil;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import net.minecraft.client.gui.DrawContext;

public abstract class Component
implements Wrapper {
    public int defaultHeight;
    protected ClickGuiTab parent;
    private int height;
    public double currentOffset;

    public Component() {
        this.height = this.defaultHeight = 16;
        this.currentOffset = 0.0;
    }

    public boolean isVisible() {
        return true;
    }

    public int getHeight() {
        if (!this.isVisible()) {
            return 0;
        }
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ClickGuiTab getParent() {
        return this.parent;
    }

    public void setParent(ClickGuiTab parent) {
        this.parent = parent;
    }

    public abstract void update(int var1, double var2, double var4, boolean var6);

    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        this.currentOffset = offset;
        return false;
    }

    public double getTextOffsetY() {
        Objects.requireNonNull(Wrapper.mc.textRenderer);
        return (double)(this.defaultHeight - 9) / 2.0 + 1.0;
    }

    public static double animate(double current, double endPoint) {
        return Component.animate(current, endPoint, UIModule.INSTANCE.animationSpeed.getValue());
    }

    public static double animate(double current, double endPoint, double speed) {
        if (speed >= 1.0) {
            return endPoint;
        }
        if (speed == 0.0) {
            return current;
        }
        return AnimateUtil.animate(current, endPoint, speed, UIModule.INSTANCE.animMode.getValue());
    }
}


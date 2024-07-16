/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 */
package me.nullpoint.mod.gui.clickgui.particle;

import java.util.Random;
import me.nullpoint.api.utils.render.Render2DUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class Snow {
    private int x;
    private int y;
    private int fallingSpeed;
    private int size;

    public Snow(int x, int y, int fallingSpeed, int size) {
        this.x = x;
        this.y = y;
        this.fallingSpeed = fallingSpeed;
        this.size = size;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int _y) {
        this.y = _y;
    }

    public void drawSnow(DrawContext drawContext) {
        Render2DUtil.drawRect(drawContext.getMatrices(), (float)this.getX(), (float)this.getY(), (float)this.size, (float)this.size, -1714829883);
        this.setY(this.getY() + this.fallingSpeed);
        if (this.getY() > MinecraftClient.getInstance().getWindow().getScaledHeight() + 10 || this.getY() < -10) {
            this.setY(-10);
            Random rand = new Random();
            this.fallingSpeed = rand.nextInt(10) + 1;
            this.size = rand.nextInt(4) + 1;
        }
    }
}


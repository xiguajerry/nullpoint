/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.opengl.GL11
 */
package me.nullpoint.api.managers;

import java.awt.Color;
import java.util.ArrayList;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.impl.ModuleComponent;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.gui.clickgui.tabs.Tab;
import me.nullpoint.mod.gui.elements.ArmorHUD;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.UIModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import static me.nullpoint.mod.modules.impl.client.UIModule.Mode.*;

public class GuiManager
implements Wrapper {
    public final ArrayList<ClickGuiTab> tabs = new ArrayList();
    public static final ClickGuiScreen clickGui = new ClickGuiScreen();
    public final ArmorHUD armorHud = new ArmorHUD();
    public static Tab currentGrabbed = null;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private int mouseX;
    private int mouseY;

    public GuiManager() {
        int xOffset = 30;
        for (Module.Category category : Module.Category.values()) {
            ClickGuiTab tab = new ClickGuiTab(category, xOffset, 50);
            for (Module module : Nullpoint.MODULE.modules) {
                if (module.getCategory() != category) continue;
                ModuleComponent button = new ModuleComponent(module.getName(), tab, module);
                tab.addChild(button);
            }
            this.tabs.add(tab);
            xOffset += tab.getWidth() + 2;
        }
    }

    public Color getColor() {
        return UIModule.INSTANCE.color.getValue();
    }

    public void update() {
        if (this.isClickGuiOpen()) {
            for (ClickGuiTab tab : this.tabs) {
                tab.update(this.mouseX, this.mouseY, ClickGuiScreen.clicked);
            }
            this.armorHud.update(this.mouseX, this.mouseY, ClickGuiScreen.clicked);
        }
    }

    public void draw(int x, int y, DrawContext drawContext, float tickDelta) {
        MatrixStack matrixStack = drawContext.getMatrices();
        boolean mouseClicked = ClickGuiScreen.clicked;
        this.mouseX = x;
        this.mouseY = y;
        if (this.isClickGuiOpen()) {
            int dx = (int)((double)this.mouseX);
            int dy = (int)((double)this.mouseY);
            if (!mouseClicked) {
                currentGrabbed = null;
            }
            if (currentGrabbed != null) {
                currentGrabbed.moveWindow(this.lastMouseX - dx, this.lastMouseY - dy);
            }
            this.lastMouseX = dx;
            this.lastMouseY = dy;
        }
        GL11.glDisable(2884);
        GL11.glBlendFunc(770, 771);
        matrixStack.push();
        this.armorHud.draw(drawContext, tickDelta, this.getColor());
        if (this.isClickGuiOpen()) {
            double quad = UIModule.fade.getQuad(FadeUtils.Quad.In2);
            boolean s = false;
            if (quad < 1.0) {
                if (UIModule.INSTANCE.mode.getValue().equals(Pull)) {
                    quad = 1.0 - quad;
                    matrixStack.translate(0.0, -100.0 * quad, 0.0);
                } else if (UIModule.INSTANCE.mode.getValue().equals(Scale)) {
                    matrixStack.scale((float) quad, (float) quad, 1.0f);
                } else if (UIModule.INSTANCE.mode.getValue().equals(Scissor)) {
                    this.setScissorRegion(0, 0, mc.getWindow().getWidth(), (int) ((double) mc.getWindow().getHeight() * quad));
                    s = true;
                }
            }
            for (ClickGuiTab tab : this.tabs) {
                tab.draw(drawContext, tickDelta, this.getColor());
            }
            if (s) {
                GL11.glDisable(3089);
            }
        }
        matrixStack.pop();
        GL11.glEnable(2884);
    }

    public void setScissorRegion(int x, int y, int width, int height) {
        double scaledY = mc.getWindow().getHeight() - (y + height);
        GL11.glEnable(3089);
        GL11.glScissor(x, (int)scaledY, width, height);
    }

    public boolean isClickGuiOpen() {
        return GuiManager.mc.currentScreen instanceof ClickGuiScreen;
    }

    public enum HackName {
        MoonEmoji,
        nullpoint,
        MadCat,
        Moon,
        MoonGod,
        Mio

    }
}


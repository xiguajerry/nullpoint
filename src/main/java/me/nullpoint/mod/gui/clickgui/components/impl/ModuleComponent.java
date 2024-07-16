/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.InputUtil
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.opengl.GL11
 */
package me.nullpoint.mod.gui.clickgui.components.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.components.impl.BindComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.BooleanComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.ColorComponents;
import me.nullpoint.mod.gui.clickgui.components.impl.EnumComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.SliderComponent;
import me.nullpoint.mod.gui.clickgui.components.impl.StringComponent;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.Setting;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public class ModuleComponent
extends Component {
    private final String text;
    private final Module module;
    private final ClickGuiTab parent;
    private boolean popped = false;
    private int expandedHeight = this.defaultHeight;
    private final List<Component> settingsList = new ArrayList<Component>();
    boolean hovered = false;
    public boolean isPopped = false;
    public double currentWidth = 0.0;

    public List<Component> getSettingsList() {
        return this.settingsList;
    }

    public ModuleComponent(String text, ClickGuiTab parent, Module module) {
        this.text = text;
        this.parent = parent;
        this.module = module;
        for (Setting setting : this.module.getSettings()) {
            Component c = setting.hide ? null : (setting instanceof SliderSetting ? new SliderComponent(this.parent, (SliderSetting)setting) : (setting instanceof BooleanSetting ? new BooleanComponent(this.parent, (BooleanSetting)setting) : (setting instanceof BindSetting ? new BindComponent(this.parent, (BindSetting)setting) : (setting instanceof EnumSetting ? new EnumComponent(this.parent, (EnumSetting)setting) : (setting instanceof ColorSetting ? new ColorComponents(this.parent, (ColorSetting)setting) : (setting instanceof StringSetting ? new StringComponent(this.parent, (StringSetting)setting) : null))))));
            if (c == null) continue;
            this.settingsList.add(c);
        }
        this.RecalculateExpandedHeight();
    }

    @Override
    public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
        int parentX = this.parent.getX();
        int parentY = this.parent.getY();
        int parentWidth = this.parent.getWidth();
        if (this.popped) {
            int i = offset + this.defaultHeight + 1;
            for (Component children : this.settingsList) {
                children.update(i, mouseX, mouseY, mouseClicked);
                i += children.getHeight();
            }
        }
        boolean bl = this.hovered = mouseX >= (double)parentX && mouseX <= (double)(parentX + parentWidth) && mouseY >= (double)(parentY + offset) && mouseY <= (double)(parentY + offset + this.defaultHeight - 1);
        if (this.hovered && GuiManager.currentGrabbed == null) {
            if (mouseClicked) {
                ClickGuiScreen.clicked = false;
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), 340)) {
                    this.module.drawnSetting.setValue(!this.module.drawnSetting.getValue());
                } else {
                    this.module.toggle();
                }
            }
            if (ClickGuiScreen.rightClicked) {
                ClickGuiScreen.rightClicked = false;
                this.popped = !this.popped;
            }
        }
        this.RecalculateExpandedHeight();
        if (this.popped) {
            this.setHeight(this.expandedHeight);
        } else {
            this.setHeight(this.defaultHeight);
        }
    }

    @Override
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        int parentX = this.parent.getX();
        int parentY = this.parent.getY();
        int parentWidth = this.parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();
        this.currentOffset = ModuleComponent.animate(this.currentOffset, offset);
        if (UIModule.fade.getQuad(FadeUtils.Quad.Out) >= 1.0 && UIModule.INSTANCE.scissor.getValue()) {
            this.setScissorRegion(parentX * 2, (int)(((double)parentY + this.currentOffset + (double)this.defaultHeight) * 2.0), parentWidth * 2, mc.getWindow().getHeight() - (int)((double)parentY + this.currentOffset + (double)this.defaultHeight));
        }
        if (this.popped) {
            this.isPopped = true;
            int i = offset + this.defaultHeight + 1;
            for (Component children : this.settingsList) {
                if (children.isVisible()) {
                    children.draw(i, drawContext, partialTicks, color, false);
                    i += children.getHeight();
                    continue;
                }
                if (children instanceof SliderComponent sliderComponent) {
                    sliderComponent.renderSliderPosition = 0.0;
                } else if (children instanceof BooleanComponent booleanComponent) {
                    booleanComponent.currentWidth = 0.0;
                } else if (children instanceof ColorComponents colorComponents) {
                    colorComponents.currentWidth = 0.0;
                }
                children.currentOffset = i - this.defaultHeight;
            }
        } else if (this.isPopped) {
            boolean finish2 = true;
            boolean finish = false;
            for (Component children : this.settingsList) {
                if (!children.isVisible()) continue;
                if (!children.draw((int)this.currentOffset, drawContext, partialTicks, color, true)) {
                    finish = true;
                    continue;
                }
                finish2 = false;
            }
            if (finish && finish2) {
                this.isPopped = false;
            }
        } else {
            for (Component children : this.settingsList) {
                children.currentOffset = this.currentOffset;
            }
        }
        if (UIModule.fade.getQuad(FadeUtils.Quad.Out) >= 1.0 && UIModule.INSTANCE.scissor.getValue()) {
            GL11.glDisable(3089);
        }
        this.currentWidth = ModuleComponent.animate(this.currentWidth, this.module.isOn() ? (double)parentWidth - 2.0 : 0.0, UIModule.INSTANCE.booleanSpeed.getValue());
        if (UIModule.INSTANCE.moduleEnd.booleanValue) {
            Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, (int)((double)parentY + this.currentOffset), (float)this.currentWidth, this.defaultHeight - 1, UIModule.INSTANCE.moduleEnable.getValue(), UIModule.INSTANCE.moduleEnd.getValue());
        } else {
            Render2DUtil.drawRect(matrixStack, (float)(parentX + 1), (float)((int)((double)parentY + this.currentOffset)), (float)this.currentWidth, (float)(this.defaultHeight - 1), UIModule.INSTANCE.moduleEnable.getValue());
        }
        Render2DUtil.drawRect(matrixStack, (float)(parentX + 1), (float)((int)((double)parentY + this.currentOffset)), (float)(parentWidth - 2), (float)(this.defaultHeight - 1), this.hovered ? UIModule.INSTANCE.mhColor.getValue() : UIModule.INSTANCE.mbgColor.getValue());
        if (this.hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), 340)) {
            TextUtil.drawString(drawContext, "Drawn " + (this.module.drawnSetting.getValue() ? "\u00a7aOn" : "\u00a7cOff"), parentX + 4, (float)((double)parentY + this.getTextOffsetY() + this.currentOffset) - 1.0f, -1);
        } else {
            TextUtil.drawString(drawContext, this.text, parentX + 4, (float)((double)parentY + this.getTextOffsetY() + this.currentOffset) - 1.0f, this.module.isOn() ? UIModule.INSTANCE.enableText.getValue().getRGB() : UIModule.INSTANCE.disableText.getValue().getRGB());
        }
        if (UIModule.INSTANCE.bindC.booleanValue && this.module.getBind().getKey() != -1) {
            String bindText = "[" + this.module.getBind().getBind() + "]";
            TextUtil.drawStringWithScale(drawContext, bindText, (float)(parentX + 5) + TextUtil.getWidth(this.text), (float)((double)parentY + this.getTextOffsetY() + this.currentOffset - (double)(TextUtil.getHeight() / 4.0f)), UIModule.INSTANCE.bindC.getValue(), 0.5f);
        }
        if (UIModule.INSTANCE.gearColor.booleanValue) {
            if (this.isPopped) {
                TextUtil.drawString(drawContext, "\u2026", parentX + parentWidth - 12, (double)parentY + this.getTextOffsetY() + this.currentOffset - 3.0, UIModule.INSTANCE.gearColor.getValue().getRGB());
            } else {
                TextUtil.drawString(drawContext, "+", parentX + parentWidth - 11, (double)parentY + this.getTextOffsetY() + this.currentOffset, UIModule.INSTANCE.gearColor.getValue().getRGB());
            }
        }
        return true;
    }

    public void setScissorRegion(int x, int y, int width, int height) {
        if (y > mc.getWindow().getHeight()) {
            return;
        }
        double scaledY = mc.getWindow().getHeight() - (y + height);
        GL11.glEnable(3089);
        GL11.glScissor(x, (int)scaledY, width, height);
    }

    public void RecalculateExpandedHeight() {
        int height = this.defaultHeight;
        for (Component children : this.settingsList) {
            if (children == null || !children.isVisible()) continue;
            height += children.getHeight();
        }
        this.expandedHeight = height;
    }
}


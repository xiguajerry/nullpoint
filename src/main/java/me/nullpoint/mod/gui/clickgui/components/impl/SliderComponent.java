/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 */
package me.nullpoint.mod.gui.clickgui.components.impl;

import java.awt.Color;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class SliderComponent
extends Component {
    private final ClickGuiTab parent;
    private double currentSliderPosition;
    final SliderSetting setting;
    private boolean clicked = false;
    private boolean hover = false;
    private boolean firstUpdate = true;
    public double renderSliderPosition = 0.0;

    public SliderComponent(ClickGuiTab parent, SliderSetting setting) {
        this.parent = parent;
        this.setting = setting;
    }

    @Override
    public boolean isVisible() {
        if (this.setting.visibility != null) {
            return this.setting.visibility.test(null);
        }
        return true;
    }

    @Override
    public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
        if (this.firstUpdate) {
            this.currentSliderPosition = (float)((this.setting.getValue() - this.setting.getMinimum()) / this.setting.getRange());
            this.firstUpdate = false;
        }
        int parentX = this.parent.getX();
        int parentY = this.parent.getY();
        int parentWidth = this.parent.getWidth();
        if (mouseX >= (double)parentX && mouseX <= (double)(parentX + parentWidth - 2) && mouseY >= (double)(parentY + offset) && mouseY <= (double)(parentY + offset + this.defaultHeight - 2)) {
            this.hover = true;
            if (GuiManager.currentGrabbed == null && this.isVisible() && (mouseClicked || ClickGuiScreen.hoverClicked && this.clicked)) {
                this.clicked = true;
                ClickGuiScreen.hoverClicked = true;
                ClickGuiScreen.clicked = false;
                this.currentSliderPosition = (float)Math.min((mouseX - 1.0 - (double)parentX) / (double)(parentWidth - 3), 1.0);
                this.currentSliderPosition = Math.max(0.0, this.currentSliderPosition);
                this.setting.setValue(this.currentSliderPosition * this.setting.getRange() + this.setting.getMinimum());
            }
        } else {
            this.clicked = false;
            this.hover = false;
        }
    }

    @Override
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        int parentX = this.parent.getX();
        int parentY = this.parent.getY();
        int parentWidth = this.parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();
        this.currentOffset = SliderComponent.animate(this.currentOffset, offset);
        if (back && Math.abs(this.currentOffset - (double)offset) <= 0.5) {
            this.renderSliderPosition = 0.0;
            return false;
        }
        this.renderSliderPosition = SliderComponent.animate(this.renderSliderPosition, Math.floor((double)(parentWidth - 2) * this.currentSliderPosition), UIModule.INSTANCE.sliderSpeed.getValue());
        Render2DUtil.drawRect(matrixStack, (float)(parentX + 1), (float)((int)((double)parentY + this.currentOffset - 1.0)), (float)((int)this.renderSliderPosition), (float)(this.defaultHeight - 1), this.hover ? UIModule.INSTANCE.mainHover.getValue() : color);
        if (this.setting == null) {
            return true;
        }
        Object value = (double)this.setting.getValueInt() == this.setting.getValue() ? String.valueOf(this.setting.getValueInt()) : String.valueOf(this.setting.getValueFloat());
        value = value + this.setting.getSuffix();
        TextUtil.drawString(drawContext, this.setting.getName(), parentX + 4, (float)((double)parentY + this.getTextOffsetY() + this.currentOffset - 2.0), 0xFFFFFF);
        TextUtil.drawString(drawContext, (String)value, (float)(parentX + parentWidth) - TextUtil.getWidth((String)value) - 5.0f, (float)((double)parentY + this.getTextOffsetY() + this.currentOffset - 2.0), 0xFFFFFF);
        return true;
    }
}


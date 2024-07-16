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
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.gui.clickgui.components.Component;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class StringComponent
extends Component {
    private final StringSetting setting;
    boolean hover = false;
    private final Timer timer = new Timer();
    boolean elementCodec;

    public StringComponent(ClickGuiTab parent, StringSetting setting) {
        this.setting = setting;
        this.parent = parent;
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
        if (GuiManager.currentGrabbed == null && this.isVisible()) {
            int parentX = this.parent.getX();
            int parentY = this.parent.getY();
            int parentWidth = this.parent.getWidth();
            if (mouseX >= (double)(parentX + 1) && mouseX <= (double)(parentX + parentWidth - 1) && mouseY >= (double)(parentY + offset) && mouseY <= (double)(parentY + offset + this.defaultHeight - 2)) {
                this.hover = true;
                if (mouseClicked) {
                    ClickGuiScreen.clicked = false;
                    this.setting.setListening(!this.setting.isListening());
                }
            } else {
                if (mouseClicked && this.setting.isListening()) {
                    this.setting.setListening(false);
                }
                this.hover = false;
            }
        } else {
            if (this.setting.isListening()) {
                this.setting.setListening(false);
            }
            this.hover = false;
        }
    }

    @Override
    public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
        String name;
        if (this.timer.passed(1000L)) {
            this.elementCodec = !this.elementCodec;
            this.timer.reset();
        }
        if (back) {
            this.setting.setListening(false);
        }
        int parentX = this.parent.getX();
        int parentY = this.parent.getY();
        this.currentOffset = StringComponent.animate(this.currentOffset, offset);
        if (back && Math.abs(this.currentOffset - (double)offset) <= 0.5) {
            return false;
        }
        int y = (int)((double)this.parent.getY() + this.currentOffset - 2.0);
        int width = this.parent.getWidth();
        MatrixStack matrixStack = drawContext.getMatrices();
        Object text = this.setting.getValue();
        if (this.setting.isListening() && this.elementCodec) {
            text = text + "_";
        }
        String string = name = this.setting.isListening() ? "[E]" : this.setting.getName();
        if (this.hover) {
            Render2DUtil.drawRect(matrixStack, (float)parentX + 1.0f, (float)y + 1.0f, (float)width - 3.0f, (float)this.defaultHeight - 1.0f, UIModule.INSTANCE.shColor.getValue());
        }
        TextUtil.drawString(drawContext, (String)text, (float)(parentX + 4) + TextUtil.getWidth(name) / 2.0f, (float)((double)parentY + this.getTextOffsetY() + this.currentOffset) - 2.0f, 0xFFFFFF);
        TextUtil.drawStringWithScale(drawContext, name, (float)(parentX + 4), (float)((double)parentY + this.getTextOffsetY() + this.currentOffset - 2.0), -1, 0.5f);
        return true;
    }
}


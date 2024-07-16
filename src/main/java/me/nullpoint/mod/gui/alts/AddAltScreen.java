/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.client.gui.widget.TextFieldWidget
 *  net.minecraft.text.Text
 */
package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import me.nullpoint.mod.gui.alts.AltScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AddAltScreen
extends Screen {
    private final AltScreen parent;
    private TextFieldWidget textFieldAltUsername;

    public AddAltScreen(AltScreen parentScreen) {
        super(Text.of("Alt Manager"));
        this.parent = parentScreen;
    }

    public void init() {
        super.init();
        this.textFieldAltUsername = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 76, 200, 20, Text.of("Enter Name"));
        this.textFieldAltUsername.setText("");
        this.addDrawableChild(this.textFieldAltUsername);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Add Alt"), b -> this.onButtonAltAddPressed()).dimensions(this.width / 2 - 100, this.height / 2 + 24, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> this.onButtonCancelPressed()).dimensions(this.width / 2 - 100, this.height / 2 + 46, 200, 20).build());
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawCenteredTextWithShadow(this.textRenderer, "Add Alternate Account", this.width / 2, 20, 0xFFFFFF);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, "Username:", this.width / 2 - 100, this.height / 2 - 90, 0xFFFFFF);
        super.render(drawContext, mouseX, mouseY, delta);
    }

    private void onButtonAltAddPressed() {
        Alt alt = new Alt(this.textFieldAltUsername.getText());
        Nullpoint.ALT.addAlt(alt);
        this.parent.refreshAltList();
    }

    public void onButtonCancelPressed() {
        this.client.setScreen(this.parent);
    }
}


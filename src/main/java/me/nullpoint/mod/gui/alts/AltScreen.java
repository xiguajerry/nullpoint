/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.widget.ButtonWidget
 *  net.minecraft.text.Text
 */
package me.nullpoint.mod.gui.alts;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.alts.Alt;
import me.nullpoint.mod.gui.alts.AddAltScreen;
import me.nullpoint.mod.gui.alts.AltSelectionList;
import me.nullpoint.mod.gui.alts.DirectLoginAltScreen;
import me.nullpoint.mod.gui.alts.EditAltScreen;
import me.nullpoint.mod.gui.alts.TokenLoginScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AltScreen
extends Screen {
    private final Screen parentScreen;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private AltSelectionList altListSelector;

    public AltScreen(Screen parentScreen) {
        super(Text.of("Alt Manager"));
        this.parentScreen = parentScreen;
    }

    public void init() {
        super.init();
        this.altListSelector = new AltSelectionList(this, this.client, this.width, this.height, 32, this.height - 64);
        this.altListSelector.updateAlts();
        this.addDrawableChild(this.altListSelector);
        this.deleteButton = ButtonWidget.builder(Text.of("Delete Alt"), b -> this.deleteSelected()).dimensions(this.width / 2 - 154, this.height - 28, 100, 20).build();
        this.deleteButton.active = false;
        this.addDrawableChild(this.deleteButton);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Token Login"), b -> this.client.setScreen(new TokenLoginScreen(this))).dimensions(this.width / 2 - 154, this.height - 52, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Direct Login"), b -> this.client.setScreen(new DirectLoginAltScreen(this))).dimensions(this.width / 2 - 50, this.height - 52, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Add Alt"), b -> this.client.setScreen(new AddAltScreen(this))).dimensions(this.width / 2 + 54, this.height - 52, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), b -> this.client.setScreen(this.parentScreen)).dimensions(this.width / 2 + 54, this.height - 28, 100, 20).build());
        this.editButton = ButtonWidget.builder(Text.of("Edit Alt"), b -> this.editSelected()).dimensions(this.width / 2 - 50, this.height - 28, 100, 20).build();
        this.editButton.active = false;
        this.addDrawableChild(this.editButton);
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
        this.altListSelector.render(drawContext, mouseX, mouseY, partialTicks);
        super.render(drawContext, mouseX, mouseY, partialTicks);
        drawContext.drawCenteredTextWithShadow(this.textRenderer, "Currently Logged Into: " + MinecraftClient.getInstance().getSession().getUsername(), this.width / 2, 20, 0xFFFFFF);
    }

    public void close() {
        this.client.setScreen(this.parentScreen);
    }

    public void refreshAltList() {
        this.client.setScreen(new AltScreen(this.parentScreen));
    }

    public void setSelected(AltSelectionList.Entry selected) {
        this.altListSelector.setSelected(selected);
        this.setEdittable();
    }

    protected void setEdittable() {
        this.editButton.active = true;
        this.deleteButton.active = true;
    }

    public void loginToSelected() {
        AltSelectionList.Entry altselectionlist$entry = this.altListSelector.getSelectedOrNull();
        if (altselectionlist$entry == null) {
            return;
        }
        Alt alt = ((AltSelectionList.NormalEntry)altselectionlist$entry).getAltData();
        Nullpoint.ALT.loginCracked(alt.getEmail());
    }

    public void editSelected() {
        Alt alt = ((AltSelectionList.NormalEntry)this.altListSelector.getSelectedOrNull()).getAltData();
        if (alt == null) {
            return;
        }
        this.client.setScreen(new EditAltScreen(this, alt));
    }

    public void deleteSelected() {
        Alt alt = ((AltSelectionList.NormalEntry)this.altListSelector.getSelectedOrNull()).getAltData();
        if (alt == null) {
            return;
        }
        Nullpoint.ALT.removeAlt(alt);
        this.refreshAltList();
    }
}


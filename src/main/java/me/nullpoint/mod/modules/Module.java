package me.nullpoint.mod.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.mod.Mod;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
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

public abstract class Module extends Mod {
   private String description;
   private final Category category;
   private final BindSetting bindSetting;
   public final BooleanSetting drawnSetting;
   public boolean state;
   private final List<Setting> settings;

   public Module(String name, Category category) {
      this(name, "", category);
   }

   public Module(String name, String description, Category category) {
      super(name);
      this.settings = new ArrayList<>();
      this.category = category;
      this.description = description;
      ModuleManager.lastLoadMod = this;
      this.bindSetting = new BindSetting("Key", name.equalsIgnoreCase("UI") ? 89 : -1);
      this.drawnSetting = this.add(new BooleanSetting("Drawn", true));
      this.drawnSetting.hide();
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public Category getCategory() {
      return this.category;
   }

   public BindSetting getBind() {
      return this.bindSetting;
   }

   public boolean isOn() {
      return this.state;
   }

   public boolean isOff() {
      return !this.isOn();
   }

   public void toggle() {
      if (this.isOn()) {
         this.disable();
      } else {
         this.enable();
      }

   }

   public void enable() {
      if (!this.state) {
         if (!nullCheck() && this.drawnSetting.getValue()) {
            switch ((ChatSetting.Style)ChatSetting.INSTANCE.messageStyle.getValue()) {
               case Mio -> CommandManager.sendChatMessageWidthId("\u00a72[+] \u00a7f" + this.getName(), -1);
               case Basic -> CommandManager.sendChatMessageWidthId("\u00a7f" + this.getName() + " \u00a7aEnabled", -1);
               case Future -> CommandManager.sendChatMessageWidthId("\u00a77" + this.getName() + " toggled \u00a72on \u00a7f", -1);
               case Earth -> CommandManager.sendChatMessageWidthIdNoSync("\u00a7l" + this.getName() + " \u00a7aEnabled", -1);
            }
         }

         this.state = true;
         Nullpoint.EVENT_BUS.subscribe(this);
         this.onToggle();

         try {
            this.onEnable();
         } catch (Exception var2) {
            Exception e = var2;
            e.printStackTrace();
         }

      }
   }

   public void disable() {
      if (this.state) {
         if (!nullCheck() && this.drawnSetting.getValue()) {
            switch ((ChatSetting.Style)ChatSetting.INSTANCE.messageStyle.getValue()) {
               case Mio -> CommandManager.sendChatMessageWidthId("\u00a74[-] \u00a7f" + this.getName(), -1);
               case Basic -> CommandManager.sendChatMessageWidthId("\u00a7f" + this.getName() + " \u00a7cDisabled", -1);
               case Future -> CommandManager.sendChatMessageWidthId("\u00a77" + this.getName() + " toggled \u00a74off \u00a7f", -1);
               case Earth -> CommandManager.sendChatMessageWidthIdNoSync("\u00a7l" + this.getName() + " \u00a7cDisabled", -1);
            }
         }

         this.state = false;
         Nullpoint.EVENT_BUS.unsubscribe(this);
         this.onToggle();
         this.onDisable();
      }
   }

   public void setState(boolean state) {
      if (this.state != state) {
         if (state) {
            this.enable();
         } else {
            this.disable();
         }

      }
   }

   public boolean setBind(String rkey) {
      if (rkey.equalsIgnoreCase("none")) {
         this.bindSetting.setKey(-1);
         return true;
      } else {
         int key;
         try {
            key = InputUtil.fromTranslationKey("key.keyboard." + rkey.toLowerCase()).getCode();
         } catch (NumberFormatException var4) {
            if (!nullCheck()) {
               CommandManager.sendChatMessage("\u00a7c[!] \u00a7fBad key!");
            }

            return false;
         }

         if (rkey.equalsIgnoreCase("none")) {
            key = -1;
         }

         if (key == 0) {
            return false;
         } else {
            this.bindSetting.setKey(key);
            return true;
         }
      }
   }

   public void addSetting(Setting setting) {
      this.settings.add(setting);
   }

   public StringSetting add(StringSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public ColorSetting add(ColorSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public SliderSetting add(SliderSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public BooleanSetting add(BooleanSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public EnumSetting add(EnumSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public BindSetting add(BindSetting setting) {
      this.addSetting(setting);
      return setting;
   }

   public List<Setting> getSettings() {
      return this.settings;
   }

   public boolean hasSettings() {
      return !this.settings.isEmpty();
   }

   public static boolean nullCheck() {
      return mc.player == null || mc.world == null;
   }

   public void onDisable() {
   }

   public void onEnable() throws IOException {
   }

   public void onToggle() {
   }

   public void onUpdate() {
   }

   public void onLogin() {
   }

   public void onLogout() {
   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
   }

   public final boolean isCategory(Category category) {
      return category == this.category;
   }

   public String getArrayName() {
      String var10000 = this.getName();
      return var10000 + this.getArrayInfo();
   }

   public String getArrayInfo() {
      return this.getInfo() == null ? "" : " \u00a77[" + this.getInfo() + "\u00a77]";
   }

   public String getInfo() {
      return null;
   }

   public enum Category {
      Combat,
      Misc,
      Render,
      Movement,
      Player,
      Exploit,
      Client;

      // $FF: synthetic method
      private static Category[] $values() {
         return new Category[]{Combat, Misc, Render, Movement, Player, Exploit, Client};
      }
   }
}

package me.nullpoint.mod.modules.impl.client;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class HUD extends Module {
   public static HUD INSTANCE = new HUD();
   private final EnumSetting page;
   public final BooleanSetting armor;
   public final SliderSetting lagTime;
   public final BooleanSetting lowerCase;
   private final BooleanSetting grayColors;
   private final BooleanSetting renderingUp;
   private final BooleanSetting watermark;
   public final SliderSetting offset;
   public final StringSetting watermarkString;
   private final BooleanSetting watermarkShort;
   private final BooleanSetting watermarkVerColor;
   private final SliderSetting waterMarkY;
   private final BooleanSetting idWatermark;
   private final BooleanSetting textRadar;
   private final SliderSetting updatedelay;
   private final BooleanSetting health;
   private final BooleanSetting coords;
   private final BooleanSetting direction;
   private final BooleanSetting lag;
   private final BooleanSetting greeter;
   private final EnumSetting greeterMode;
   private final BooleanSetting greeterNameColor;
   private final StringSetting greeterText;
   private final BooleanSetting potions;
   private final BooleanSetting potionColor;
   private final BooleanSetting pvphud;
   public final SliderSetting pvphudoffset;
   private final BooleanSetting totemtext;
   private final BooleanSetting potiontext;
   private final BooleanSetting ping;
   private final BooleanSetting speed;
   private final BooleanSetting tps;
   private final BooleanSetting fps;
   private final BooleanSetting time;
   private final EnumSetting colorMode;
   private final SliderSetting rainbowSpeed;
   private final SliderSetting saturation;
   private final SliderSetting pulseSpeed;
   private final SliderSetting rainbowDelay;
   private final ColorSetting color;
   private final BooleanSetting sync;
   private final Timer timer;
   private Map players;
   private int counter;
   int progress;
   int pulseProgress;

   public HUD() {
      super("HUD", "HUD elements drawn on your screen", Module.Category.Client);
      this.page = this.add(new EnumSetting("Page", HUD.Page.GLOBAL));
      this.armor = this.add(new BooleanSetting("Armor", true, (v) -> {
         return this.page.getValue() == HUD.Page.GLOBAL;
      }));
      this.lagTime = this.add(new SliderSetting("LagTime", 1000, 0, 2000, (v) -> {
         return this.page.getValue() == HUD.Page.GLOBAL;
      }));
      this.lowerCase = this.add(new BooleanSetting("LowerCase", false, (v) -> {
         return this.page.getValue() == HUD.Page.GLOBAL;
      }));
      this.grayColors = this.add(new BooleanSetting("Gray", true, (v) -> {
         return this.page.getValue() == HUD.Page.GLOBAL;
      }));
      this.renderingUp = this.add(new BooleanSetting("RenderingUp", true, (v) -> {
         return this.page.getValue() == HUD.Page.GLOBAL;
      }));
      this.watermark = this.add((new BooleanSetting("Watermark", true, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      })).setParent());
      this.offset = this.add(new SliderSetting("Offset", 8.0, 0.0, 100.0, -1.0, (v) -> {
         return this.watermark.isOpen() && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.watermarkString = this.add(new StringSetting("Text", "NullPoint", (v) -> {
         return this.watermark.isOpen() && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.watermarkShort = this.add(new BooleanSetting("Shorten", false, (v) -> {
         return this.watermark.isOpen() && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.watermarkVerColor = this.add(new BooleanSetting("VerColor", true, (v) -> {
         return this.watermark.isOpen() && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.waterMarkY = this.add(new SliderSetting("Height", 2, 2, 12, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.watermark.isOpen();
      }));
      this.idWatermark = this.add(new BooleanSetting("IdWatermark", true, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.textRadar = this.add((new BooleanSetting("TextRadar", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      })).setParent());
      this.updatedelay = this.add(new SliderSetting("UpdateDelay", 5, 0, 1000, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.textRadar.isOpen();
      }));
      this.health = this.add(new BooleanSetting("Health", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.textRadar.isOpen();
      }));
      this.coords = this.add(new BooleanSetting("Position(XYZ)", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.direction = this.add(new BooleanSetting("Direction", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.lag = this.add(new BooleanSetting("LagNotifier", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.greeter = this.add((new BooleanSetting("Welcomer", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      })).setParent());
      this.greeterMode = this.add(new EnumSetting("Mode", HUD.GreeterMode.PLAYER, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.greeter.isOpen();
      }));
      this.greeterNameColor = this.add(new BooleanSetting("NameColor", true, (v) -> {
         return this.greeter.isOpen() && this.greeterMode.getValue() == HUD.GreeterMode.PLAYER && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.greeterText = this.add(new StringSetting("WelcomerText", "i sniff coke and smoke dope i got 2 habbits", (v) -> {
         return this.greeter.isOpen() && this.greeterMode.getValue() == HUD.GreeterMode.CUSTOM && this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.potions = this.add((new BooleanSetting("Potions", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      })).setParent());
      this.potionColor = this.add(new BooleanSetting("PotionColor", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.potions.isOpen();
      }));
      this.pvphud = this.add((new BooleanSetting("PVPHud", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      })).setParent());
      this.pvphudoffset = this.add(new SliderSetting("PVPHUDOffset", 8.0, 0.0, 100.0, -1.0, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.pvphud.isOpen();
      }));
      this.totemtext = this.add(new BooleanSetting("TotemText", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.pvphud.isOpen();
      }));
      this.potiontext = this.add(new BooleanSetting("PotionText", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS && this.pvphud.isOpen();
      }));
      this.ping = this.add(new BooleanSetting("Ping", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.speed = this.add(new BooleanSetting("Speed", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.tps = this.add(new BooleanSetting("TPS", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.fps = this.add(new BooleanSetting("FPS", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.time = this.add(new BooleanSetting("Time", false, (v) -> {
         return this.page.getValue() == HUD.Page.ELEMENTS;
      }));
      this.colorMode = this.add(new EnumSetting("ColorMode", HUD.ColorMode.Pulse, (v) -> {
         return this.page.getValue() == HUD.Page.Color;
      }));
      this.rainbowSpeed = this.add(new SliderSetting("RainbowSpeed", 200, 1, 400, (v) -> {
         return (this.colorMode.getValue() == HUD.ColorMode.Rainbow || this.colorMode.getValue() == HUD.ColorMode.PulseRainbow) && this.page.getValue() == HUD.Page.Color;
      }));
      this.saturation = this.add(new SliderSetting("Saturation", 130.0, 1.0, 255.0, (v) -> {
         return (this.colorMode.getValue() == HUD.ColorMode.Rainbow || this.colorMode.getValue() == HUD.ColorMode.PulseRainbow) && this.page.getValue() == HUD.Page.Color;
      }));
      this.pulseSpeed = this.add(new SliderSetting("PulseSpeed", 100, 1, 400, (v) -> {
         return (this.colorMode.getValue() == HUD.ColorMode.Pulse || this.colorMode.getValue() == HUD.ColorMode.PulseRainbow) && this.page.getValue() == HUD.Page.Color;
      }));
      this.rainbowDelay = this.add(new SliderSetting("Delay", 350, 0, 600, (v) -> {
         return this.colorMode.getValue() == HUD.ColorMode.Rainbow && this.page.getValue() == HUD.Page.Color;
      }));
      this.color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 255), (v) -> {
         return this.colorMode.getValue() != HUD.ColorMode.Rainbow && this.page.getValue() == HUD.Page.Color;
      }));
      this.sync = this.add(new BooleanSetting("Sync", false, (v) -> {
         return this.page.getValue() == HUD.Page.Color;
      }));
      this.timer = new Timer();
      this.players = new HashMap();
      this.counter = 20;
      this.progress = 0;
      this.pulseProgress = 0;
      INSTANCE = this;
   }

   public void onUpdate() {
      if (this.timer.passed(this.updatedelay.getValue())) {
         this.players = this.getTextRadarMap();
         this.timer.reset();
      }

      this.progress -= this.rainbowSpeed.getValueInt();
      this.pulseProgress -= this.pulseSpeed.getValueInt();
   }

   public void onRender2D(DrawContext drawContext, float tickDelta) {
      if (!nullCheck()) {
         this.counter = 20;
         int width = mc.getWindow().getScaledWidth();
         int height = mc.getWindow().getScaledHeight();
         if (this.armor.getValue()) {
            Nullpoint.GUI.armorHud.draw(drawContext, tickDelta, null);
         }

         if (this.pvphud.getValue()) {
            this.drawpvphud(drawContext, this.pvphudoffset.getValueInt());
         }

         if (this.textRadar.getValue()) {
            this.drawTextRadar(drawContext, this.watermark.getValue() ? (int)(this.waterMarkY.getValue() + 2.0) : 2);
         }

         String grayString;
         String domainString;
         String fpsText;
         String var10000;
         if (this.watermark.getValue()) {
            var10000 = this.watermarkString.getValue();
            grayString = var10000 + " ";
            domainString = this.watermarkVerColor.getValue() ? "\u00a7f" : "";
            fpsText = domainString + (this.watermarkShort.getValue() ? "" : "v2.1.3");
            drawContext.drawTextWithShadow(mc.textRenderer, (this.lowerCase.getValue() ? grayString.toLowerCase() : grayString) + fpsText, this.offset.getValueInt(), 2 + this.offset.getValueInt(), this.getColor(this.counter));
            ++this.counter;
         }

         if (this.idWatermark.getValue()) {
            grayString = "NullPoint ";
            domainString = "v2.1.3";
            float offset = (float)mc.getWindow().getScaledHeight() / 2.0F - 30.0F;
            drawContext.drawTextWithShadow(mc.textRenderer, grayString + domainString, 2, (int)offset, this.getColor(this.counter));
            ++this.counter;
         }

         grayString = this.grayColors.getValue() ? "\u00a77" : "";
         int i = mc.currentScreen instanceof ChatScreen && this.renderingUp.getValue() ? 13 : (this.renderingUp.getValue() ? -2 : 0);
         Iterator var8;
         StatusEffectInstance potionEffect;
         String str;
         ArrayList effects;
         String str1;
         if (this.renderingUp.getValue()) {
            if (this.potions.getValue()) {
               effects = new ArrayList(mc.player.getStatusEffects());

               for(var8 = effects.iterator(); var8.hasNext(); ++this.counter) {
                  potionEffect = (StatusEffectInstance)var8.next();
                  str = this.getColoredPotionString(potionEffect);
                  i += 10;
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str.toLowerCase() : str, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2, height - 2 - i, this.potionColor.getValue() ? potionEffect.getEffectType().getColor() : this.getColor(this.counter));
               }
            }

            if (this.speed.getValue()) {
               fpsText = grayString + "Speed \u00a7f" + Nullpoint.SPEED.getSpeedKpH() + " km/h";
               i += 10;
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, height - 2 - i, this.getColor(this.counter));
               ++this.counter;
            }

            if (this.time.getValue()) {
               fpsText = grayString + "Time \u00a7f" + (new SimpleDateFormat("h:mm a")).format(new Date());
               i += 10;
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, height - 2 - i, this.getColor(this.counter));
               ++this.counter;
            }

            if (this.tps.getValue()) {
               fpsText = grayString + "TPS \u00a7f" + Nullpoint.SERVER.getTPS();
               i += 10;
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, height - 2 - i, this.getColor(this.counter));
               ++this.counter;
            }

            fpsText = grayString + "FPS \u00a7f" + Nullpoint.FPS.getFps();
            str1 = grayString + "Ping \u00a7f" + Nullpoint.SERVER.getPing();
            if (mc.textRenderer.getWidth(str1) > mc.textRenderer.getWidth(fpsText)) {
               if (this.ping.getValue()) {
                  i += 10;
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str1.toLowerCase() : str1, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2, height - 2 - i, this.getColor(this.counter));
                  ++this.counter;
               }

               if (this.fps.getValue()) {
                  i += 10;
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, height - 2 - i, this.getColor(this.counter));
               }
            } else {
               if (this.fps.getValue()) {
                  i += 10;
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, height - 2 - i, this.getColor(this.counter));
                  ++this.counter;
               }

               if (this.ping.getValue()) {
                  i += 10;
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str1.toLowerCase() : str1, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2, height - 2 - i, this.getColor(this.counter));
               }
            }
         } else {
            if (this.potions.getValue()) {
               effects = new ArrayList(mc.player.getStatusEffects());

               for(var8 = effects.iterator(); var8.hasNext(); ++this.counter) {
                  potionEffect = (StatusEffectInstance)var8.next();
                  str = this.getColoredPotionString(potionEffect);
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str.toLowerCase() : str, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str.toLowerCase()) : mc.textRenderer.getWidth(str)) - 2, 2 + i++ * 10, this.potionColor.getValue() ? potionEffect.getEffectType().getColor() : this.getColor(this.counter));
               }
            }

            if (this.speed.getValue()) {
               fpsText = grayString + "Speed \u00a7f" + Nullpoint.SPEED.getSpeedKpH() + " km/h";
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, 2 + i++ * 10, this.getColor(this.counter));
               ++this.counter;
            }

            if (this.time.getValue()) {
               fpsText = grayString + "Time \u00a7f" + (new SimpleDateFormat("h:mm a")).format(new Date());
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, 2 + i++ * 10, this.getColor(this.counter));
               ++this.counter;
            }

            if (this.tps.getValue()) {
               fpsText = grayString + "TPS \u00a7f" + Nullpoint.SERVER.getTPS();
               drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, 2 + i++ * 10, this.getColor(this.counter));
               ++this.counter;
            }

            fpsText = grayString + "FPS \u00a7f" + Nullpoint.FPS.getFps();
            str1 = grayString + "Ping \u00a7f" + Nullpoint.SERVER.getPing();
            if (mc.textRenderer.getWidth(str1) > mc.textRenderer.getWidth(fpsText)) {
               if (this.ping.getValue()) {
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str1.toLowerCase() : str1, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2, 2 + i++ * 10, this.getColor(this.counter));
                  ++this.counter;
               }

               if (this.fps.getValue()) {
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, 2 + i++ * 10, this.getColor(this.counter));
               }
            } else {
               if (this.fps.getValue()) {
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(fpsText.toLowerCase()) : mc.textRenderer.getWidth(fpsText)) - 2, 2 + i++ * 10, this.getColor(this.counter));
                  ++this.counter;
               }

               if (this.ping.getValue()) {
                  drawContext.drawTextWithShadow(mc.textRenderer, this.lowerCase.getValue() ? str1.toLowerCase() : str1, width - (this.lowerCase.getValue() ? mc.textRenderer.getWidth(str1.toLowerCase()) : mc.textRenderer.getWidth(str1)) - 2, 2 + i++ * 10, this.getColor(this.counter));
               }
            }
         }

         boolean inHell = mc.world.getRegistryKey().equals(World.NETHER);
         int posX = (int)mc.player.getX();
         int posY = (int)mc.player.getY();
         int posZ = (int)mc.player.getZ();
         float nether = !inHell ? 0.125F : 8.0F;
         int hposX = (int)(mc.player.getX() * (double)nether);
         int hposZ = (int)(mc.player.getZ() * (double)nether);
         int yawPitch = (int)MathHelper.wrapDegrees(mc.player.getYaw());
         int p = this.coords.getValue() ? 0 : 11;
         i = mc.currentScreen instanceof ChatScreen ? 14 : 0;
         var10000 = this.lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ";
         String coordinates = var10000 + "\u00a7f" + (inHell ? posX + ", " + posY + ", " + posZ + "\u00a77 [\u00a7f" + hposX + ", " + hposZ + "\u00a77]\u00a7f" : posX + ", " + posY + ", " + posZ + "\u00a77 [\u00a7f" + hposX + ", " + hposZ + "\u00a77]");
         String direction = this.direction.getValue() ? Nullpoint.ROTATE.getDirection4D(false) : "";
         String yaw = this.direction.getValue() ? (this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + "\u00a7f" + yawPitch : "";
         String coords = this.coords.getValue() ? coordinates : "";
         i += 10;
         if (mc.currentScreen instanceof ChatScreen && this.direction.getValue()) {
            yaw = "";
            direction = (this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + "\u00a7f" + yawPitch + "\u00a77 " + this.getFacingDirectionShort();
         }

         drawContext.drawTextWithShadow(mc.textRenderer, direction, 2, height - i - 11 + p, this.getColor(this.counter));
         ++this.counter;
         drawContext.drawTextWithShadow(mc.textRenderer, yaw, 2, height - i - 22 + p, this.getColor(this.counter));
         ++this.counter;
         drawContext.drawTextWithShadow(mc.textRenderer, coords, 2, height - i, this.getColor(this.counter));
         ++this.counter;
         if (this.greeter.getValue()) {
            this.drawWelcomer(drawContext);
         }

         if (this.lag.getValue()) {
            this.drawLagOMeter(drawContext);
         }

      }
   }

   private void drawWelcomer(DrawContext drawContext) {
      int width = mc.getWindow().getScaledWidth();
      String nameColor = this.greeterNameColor.getValue() ? String.valueOf(Formatting.WHITE) : "";
      String text = this.lowerCase.getValue() ? "Welcome, ".toLowerCase() : "Welcome, ";
      if (this.greeterMode.getValue() == HUD.GreeterMode.PLAYER) {
         if (this.greeter.getValue()) {
            text = text + nameColor + mc.getSession().getUsername();
         }

         drawContext.drawTextWithShadow(mc.textRenderer, text + "\u00a70 :')", (int)((float)width / 2.0F - (float)mc.textRenderer.getWidth(text) / 2.0F + 2.0F), 2, this.getColor(this.counter));
         ++this.counter;
      } else {
         String lel = this.greeterText.getValue();
         if (this.greeter.getValue()) {
            lel = this.greeterText.getValue();
         }

         drawContext.drawTextWithShadow(mc.textRenderer, lel, (int)((float)width / 2.0F - (float)mc.textRenderer.getWidth(lel) / 2.0F + 2.0F), 2, this.getColor(this.counter));
         ++this.counter;
      }

   }

   private void drawpvphud(DrawContext drawContext, int yOffset) {
      double x = (double)mc.getWindow().getWidth() / 4.0;
      double y = (double)mc.getWindow().getHeight() / 4.0 + (double)yOffset;
      Objects.requireNonNull(mc.textRenderer);
      int textHeight = 9 + 1;
      Formatting var10000 = Formatting.WHITE;
      String t1 = "Totem " + var10000 + InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
      var10000 = Formatting.WHITE;
      String t2 = "Potion " + var10000 + InventoryUtil.getPotCount(StatusEffects.RESISTANCE);
      List effects = new ArrayList(mc.player.getStatusEffects());
      if (this.totemtext.getValue()) {
         drawContext.drawTextWithShadow(mc.textRenderer, t1, (int)(x - (double)(mc.textRenderer.getWidth(t1) / 2)), (int)y, this.getColor(this.counter));
         ++this.counter;
         y += textHeight;
      }

      if (this.potiontext.getValue()) {
         drawContext.drawTextWithShadow(mc.textRenderer, t2, (int)(x - (double)(mc.textRenderer.getWidth(t2) / 2)), (int)y, this.getColor(this.counter));
         ++this.counter;
         y += textHeight;
      }

      Iterator var11 = effects.iterator();

      while(var11.hasNext()) {
         StatusEffectInstance potionEffect = (StatusEffectInstance)var11.next();
         if (potionEffect.getEffectType() == StatusEffects.RESISTANCE && potionEffect.getAmplifier() + 1 > 1) {
            String str = this.getColoredPotionTimeString(potionEffect);
            String t3 = "PotionTime " + Formatting.WHITE + str;
            if (this.potiontext.getValue()) {
               drawContext.drawTextWithShadow(mc.textRenderer, t3, (int)(x - (double)(mc.textRenderer.getWidth(t3) / 2)), (int)y, this.getColor(this.counter));
               ++this.counter;
               y += textHeight;
            }
         }
      }

   }

   private void drawLagOMeter(DrawContext drawContext) {
      int width = mc.getWindow().getScaledWidth();
      if (Nullpoint.SERVER.isServerNotResponding()) {
         String var10000 = this.lowerCase.getValue() ? "Server is lagging for ".toLowerCase() : "Server is lagging for ";
         String text = "\u00a74" + var10000 + MathUtil.round((float)Nullpoint.SERVER.serverRespondingTime() / 1000.0F, 1) + "s.";
         drawContext.drawTextWithShadow(mc.textRenderer, text, (int)((float)width / 2.0F - (float)mc.textRenderer.getWidth(text) / 2.0F + 2.0F), 20, this.getColor(this.counter));
         ++this.counter;
      }

   }

   private void drawTextRadar(DrawContext drawContext, int yOffset) {
      if (!this.players.isEmpty()) {
         Objects.requireNonNull(mc.textRenderer);
         int y = 9 + 7 + yOffset;

         int textHeight;
         for(Iterator var4 = this.players.entrySet().iterator(); var4.hasNext(); y += textHeight) {
            Map.Entry player = (Map.Entry)var4.next();
            String text = player.getKey() + " ";
            Objects.requireNonNull(mc.textRenderer);
            textHeight = 9 + 1;
            drawContext.drawTextWithShadow(mc.textRenderer, text, 2, y, this.getColor(this.counter));
            ++this.counter;
         }
      }

   }

   private Map getTextRadarMap() {
      Map retval = new HashMap();
      DecimalFormat dfDistance = new DecimalFormat("#.#");
      dfDistance.setRoundingMode(RoundingMode.CEILING);
      StringBuilder distanceSB = new StringBuilder();
      Iterator var4 = mc.world.getPlayers().iterator();

      while(var4.hasNext()) {
         PlayerEntity player = (PlayerEntity)var4.next();
         if (!player.isInvisible() && !player.getName().equals(mc.player.getName())) {
            int distanceInt = (int)mc.player.distanceTo(player);
            String distance = dfDistance.format(distanceInt);
            if (distanceInt >= 25) {
               distanceSB.append(Formatting.GREEN);
            } else if (distanceInt > 10) {
               distanceSB.append(Formatting.YELLOW);
            } else {
               distanceSB.append(Formatting.RED);
            }

            distanceSB.append(distance);
            String var10001 = this.health.getValue() ? this.getHealthColor(player) + String.valueOf(round2(player.getAbsorptionAmount() + player.getHealth())) + " " : "";
            retval.put(var10001 + (Nullpoint.FRIEND.isFriend(player) ? Formatting.AQUA : Formatting.RESET) + player.getName().getString() + " " + Formatting.WHITE + "[" + Formatting.RESET + distanceSB + "m" + Formatting.WHITE + "] " + Formatting.GREEN, (int)mc.player.distanceTo(player));
            distanceSB.setLength(0);
         }
      }

      if (!retval.isEmpty()) {
         retval = MathUtil.sortByValue(retval, false);
      }

      return retval;
   }

   public static float round2(double value) {
      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(1, RoundingMode.HALF_UP);
      return bd.floatValue();
   }

   private Formatting getHealthColor(@NotNull PlayerEntity entity) {
      int health = (int)((float)((int)entity.getHealth()) + entity.getAbsorptionAmount());
      if (health <= 15 && health > 7) {
         return Formatting.YELLOW;
      } else {
         return health > 15 ? Formatting.GREEN : Formatting.RED;
      }
   }

   private String getFacingDirectionShort() {
      int dirnumber = Nullpoint.ROTATE.getYaw4D();
      if (dirnumber == 0) {
         return "(+Z)";
      } else if (dirnumber == 1) {
         return "(-X)";
      } else if (dirnumber == 2) {
         return "(-Z)";
      } else {
         return dirnumber == 3 ? "(+X)" : "Loading...";
      }
   }

   private String getColoredPotionString(StatusEffectInstance effect) {
      StatusEffect potion = effect.getEffectType();
      String var10000 = potion.getName().getString();
      return var10000 + " " + (effect.getAmplifier() + 1) + " \u00a7f" + StatusEffectUtil.getDurationText(effect, 1.0F, mc.world.getTickManager().getTickRate()).getString();
   }

   private String getColoredPotionTimeString(StatusEffectInstance effect) {
      return StatusEffectUtil.getDurationText(effect, 1.0F, mc.world.getTickManager().getTickRate()).getString();
   }

   private int getColor(int counter) {
      if (this.colorMode.getValue() != HUD.ColorMode.Custom) {
         return this.rainbow(counter).getRGB();
      } else {
         return this.sync.getValue() ? UIModule.INSTANCE.color.getValue().getRGB() : this.color.getValue().getRGB();
      }
   }

   private Color rainbow(int delay) {
      double rainbowState = Math.ceil(((double)this.progress + (double)delay * this.rainbowDelay.getValue()) / 20.0);
      if (this.colorMode.getValue() == HUD.ColorMode.Pulse) {
         return this.sync.getValue() ? this.pulseColor(UIModule.INSTANCE.color.getValue(), delay) : this.pulseColor(this.color.getValue(), delay);
      } else {
         return this.colorMode.getValue() == HUD.ColorMode.Rainbow ? Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0F, 1.0F) : this.pulseColor(Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValueFloat() / 255.0F, 1.0F), delay);
      }
   }

   private Color pulseColor(Color color, int index) {
      float[] hsb = new float[3];
      Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
      float brightness = Math.abs(((float)((long)this.pulseProgress % 2000L) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979F) ^ 2127476077) + (float)index / 14.0F * Float.intBitsToFloat(Float.floatToIntBits(0.09192204F) ^ 2109489567)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098F) ^ 2135501525) - Float.intBitsToFloat(Float.floatToIntBits(6.46708F) ^ 2135880274));
      brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923F) ^ 2123889075) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195F) ^ 2134044341) * brightness;
      hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331F) ^ 2137404452);
      return ColorUtil.injectAlpha(new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])), color.getAlpha());
   }

   private enum Page {
      ELEMENTS,
      GLOBAL,
      Color;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{ELEMENTS, GLOBAL, Color};
      }
   }

   private enum GreeterMode {
      PLAYER,
      CUSTOM;

      // $FF: synthetic method
      private static GreeterMode[] $values() {
         return new GreeterMode[]{PLAYER, CUSTOM};
      }
   }

   private enum ColorMode {
      Custom,
      Pulse,
      Rainbow,
      PulseRainbow;

      // $FF: synthetic method
      private static ColorMode[] $values() {
         return new ColorMode[]{Custom, Pulse, Rainbow, PulseRainbow};
      }
   }
}

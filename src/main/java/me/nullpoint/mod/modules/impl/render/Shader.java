package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.managers.ShaderManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class Shader extends Module {
   public static Shader INSTANCE;
   private final EnumSetting page;
   public final EnumSetting mode;
   public final EnumSetting skyMode;
   public final SliderSetting speed;
   public final ColorSetting fill;
   public final SliderSetting maxSample;
   public final SliderSetting divider;
   public final SliderSetting radius;
   public final SliderSetting smoothness;
   public final SliderSetting alpha;
   public final BooleanSetting sky;
   private final BooleanSetting hands;
   public final SliderSetting maxRange;
   private final BooleanSetting self;
   private final BooleanSetting players;
   private final BooleanSetting friends;
   private final BooleanSetting crystals;
   private final BooleanSetting creatures;
   private final BooleanSetting monsters;
   private final BooleanSetting ambients;
   private final BooleanSetting items;
   private final BooleanSetting others;
   public final SliderSetting factor;
   public final SliderSetting gradient;
   public final SliderSetting octaves;
   public final ColorSetting smoke1;
   public final ColorSetting smoke2;
   public final ColorSetting smoke3;

   public Shader() {
      super("Shader", Module.Category.Render);
      this.page = this.add(new EnumSetting("Page", Shader.Page.Shader));
      this.mode = this.add(new EnumSetting("Mode", ShaderManager.Shader.Solid, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.skyMode = this.add(new EnumSetting("SkyMode", ShaderManager.Shader.Solid, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.speed = this.add(new SliderSetting("Speed", 4.0, 0.0, 20.0, 0.1, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.fill = this.add(new ColorSetting("Color", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.maxSample = this.add(new SliderSetting("MaxSample", 10.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.divider = this.add(new SliderSetting("Divider", 150.0, 0.0, 300.0, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.radius = this.add(new SliderSetting("Radius", 2.0, 0.0, 6.0, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.smoothness = this.add(new SliderSetting("Smoothness", 1.0, 0.0, 1.0, 0.01, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.alpha = this.add(new SliderSetting("GlowAlpha", 255, 0, 255, (v) -> {
         return this.page.getValue() == Shader.Page.Shader;
      }));
      this.sky = this.add(new BooleanSetting("Sky", false, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.hands = this.add(new BooleanSetting("Hands", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.maxRange = this.add(new SliderSetting("MaxRange", 64, 16, 512, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.self = this.add(new BooleanSetting("Self", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.players = this.add(new BooleanSetting("Players", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.friends = this.add(new BooleanSetting("Friends", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.crystals = this.add(new BooleanSetting("Crystals", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.creatures = this.add(new BooleanSetting("Creatures", false, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.monsters = this.add(new BooleanSetting("Monsters", false, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.ambients = this.add(new BooleanSetting("Ambients", false, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.items = this.add(new BooleanSetting("Items", true, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.others = this.add(new BooleanSetting("Others", false, (v) -> {
         return this.page.getValue() == Shader.Page.Target;
      }));
      this.factor = this.add(new SliderSetting("GradientFactor", 2.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      this.gradient = this.add(new SliderSetting("Gradient", 2.0, 0.0, 20.0, (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      this.octaves = this.add(new SliderSetting("Octaves", 10, 5, 30, (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      this.smoke1 = this.add(new ColorSetting("Smoke1", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      this.smoke2 = this.add(new ColorSetting("Smoke2", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      this.smoke3 = this.add(new ColorSetting("Smoke3", new Color(255, 255, 255), (v) -> {
         return this.page.getValue() == Shader.Page.Legacy;
      }));
      INSTANCE = this;
   }

   public String getInfo() {
      return this.mode.getValue().name();
   }

   public boolean shouldRender(Entity entity) {
      if (entity == null) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if ((double)MathHelper.sqrt((float)mc.player.squaredDistanceTo(entity.getPos())) > this.maxRange.getValue()) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         if (entity == mc.player) {
            return this.self.getValue();
         } else {
            return Nullpoint.FRIEND.isFriend((PlayerEntity)entity) ? this.friends.getValue() : this.players.getValue();
         }
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystals.getValue();
      } else if (entity instanceof ItemEntity) {
         return this.items.getValue();
      } else {
         boolean var10000;
         switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_CREATURE:
               var10000 = this.creatures.getValue();
               break;
            case MONSTER:
               var10000 = this.monsters.getValue();
               break;
            case AMBIENT:
            case WATER_AMBIENT:
               var10000 = this.ambients.getValue();
               break;
            default:
               var10000 = this.others.getValue();
         }

         return var10000;
      }
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.hands.getValue()) {
         Nullpoint.SHADER.renderShader(() -> {
            mc.gameRenderer.renderHand(matrixStack, mc.gameRenderer.getCamera(), mc.getTickDelta());
         }, (ShaderManager.Shader)this.mode.getValue());
      }

   }

   public void onToggle() {
      Nullpoint.SHADER.reloadShaders();
   }

   public void onLogin() {
      Nullpoint.SHADER.reloadShaders();
   }

   private enum Page {
      Shader,
      Target,
      Legacy;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{Shader, Target, Legacy};
      }
   }
}

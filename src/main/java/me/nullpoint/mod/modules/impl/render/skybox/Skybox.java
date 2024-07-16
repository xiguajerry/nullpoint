package me.nullpoint.mod.modules.impl.render.skybox;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Map;
import me.nullpoint.mod.modules.Beta;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.fabricmc.fabric.impl.client.rendering.DimensionRenderingRegistryImpl;
import net.minecraft.world.World;

@Beta
public class Skybox extends Module {
   public static Skybox INSTANCE;
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(0.77F, 0.31F, 0.73F)));
   public final ColorSetting color2 = this.add(new ColorSetting("Color2", new Color(0.77F, 0.31F, 0.73F)));
   public final ColorSetting color3 = this.add(new ColorSetting("Color3", new Color(0.77F, 0.31F, 0.73F)));
   public final ColorSetting color4 = this.add(new ColorSetting("Color4", new Color(0.77F, 0.31F, 0.73F)));
   public final ColorSetting color5 = this.add(new ColorSetting("Color5", new Color(255, 255, 255)));
   final BooleanSetting stars = this.add(new BooleanSetting("Stars", true));
   public static final CustomSkyRenderer skyRenderer = new CustomSkyRenderer();

   public Skybox() {
      super("Skybox", "Custom skybox", Module.Category.Render);
      INSTANCE = this;
   }

   public void onEnable() {
      try {
         Field field = DimensionRenderingRegistryImpl.class.getDeclaredField("SKY_RENDERERS");
         field.setAccessible(true);
         Map SKY_RENDERERS = (Map)field.get(null);
         SKY_RENDERERS.putIfAbsent(World.OVERWORLD, skyRenderer);
         SKY_RENDERERS.putIfAbsent(World.NETHER, skyRenderer);
         SKY_RENDERERS.putIfAbsent(World.END, skyRenderer);
      } catch (Exception var3) {
         Exception e = var3;
         e.printStackTrace();
      }

   }

   public void onDisable() {
      try {
         Field field = DimensionRenderingRegistryImpl.class.getDeclaredField("SKY_RENDERERS");
         field.setAccessible(true);
         Map SKY_RENDERERS = (Map)field.get(null);
         SKY_RENDERERS.remove(World.OVERWORLD, skyRenderer);
         SKY_RENDERERS.remove(World.NETHER, skyRenderer);
         SKY_RENDERERS.remove(World.END, skyRenderer);
      } catch (Exception var3) {
         Exception e = var3;
         e.printStackTrace();
      }

   }
}

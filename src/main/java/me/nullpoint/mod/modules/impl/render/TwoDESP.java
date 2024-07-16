package me.nullpoint.mod.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;

public class TwoDESP extends Module {
   private final EnumSetting page;
   private final BooleanSetting outline;
   private final BooleanSetting renderHealth;
   private final BooleanSetting renderArmor;
   private final SliderSetting durascale;
   private final BooleanSetting drawItem;
   private final BooleanSetting drawItemC;
   private final BooleanSetting font;
   private final BooleanSetting players;
   private final BooleanSetting friends;
   private final BooleanSetting crystals;
   private final BooleanSetting creatures;
   private final BooleanSetting monsters;
   private final BooleanSetting ambients;
   private final BooleanSetting others;
   private final ColorSetting playersC;
   private final ColorSetting friendsC;
   private final ColorSetting crystalsC;
   private final ColorSetting creaturesC;
   private final ColorSetting monstersC;
   private final ColorSetting ambientsC;
   private final ColorSetting othersC;
   public final ColorSetting armorDuraColor;
   public final ColorSetting textcolor;
   public final ColorSetting countColor;
   public final ColorSetting hHealth;
   public final ColorSetting mHealth;
   public final ColorSetting lHealth;

   public TwoDESP() {
      super("2DESP", Module.Category.Render);
      this.page = this.add(new EnumSetting("Settings", TwoDESP.Page.Target));
      this.outline = this.add(new BooleanSetting("Outline", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting;
      }));
      this.renderHealth = this.add(new BooleanSetting("renderHealth", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting;
      }));
      this.renderArmor = this.add(new BooleanSetting("Armor Dura", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting;
      }));
      this.durascale = this.add(new SliderSetting("DuraScale", 1.0, 0.0, 2.0, 0.1, (v) -> {
         return this.renderArmor.getValue();
      }));
      this.drawItem = this.add(new BooleanSetting("draw Item Name", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting;
      }));
      this.drawItemC = this.add(new BooleanSetting("draw Item Count", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting && this.drawItem.getValue();
      }));
      this.font = this.add(new BooleanSetting("CustomFont", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Setting;
      }));
      this.players = this.add(new BooleanSetting("Players", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.friends = this.add(new BooleanSetting("Friends", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.crystals = this.add(new BooleanSetting("Crystals", true, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.creatures = this.add(new BooleanSetting("Creatures", false, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.monsters = this.add(new BooleanSetting("Monsters", false, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.ambients = this.add(new BooleanSetting("Ambients", false, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.others = this.add(new BooleanSetting("Others", false, (v) -> {
         return this.page.getValue() == TwoDESP.Page.Target;
      }));
      this.playersC = this.add(new ColorSetting("PlayersBox", new Color(16749056), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.friendsC = this.add(new ColorSetting("FriendsBox", new Color(3211008), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.crystalsC = this.add(new ColorSetting("CrystalsBox", new Color(48127), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.creaturesC = this.add(new ColorSetting("CreaturesBox", new Color(10527910), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.monstersC = this.add(new ColorSetting("MonstersBox", new Color(16711680), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.ambientsC = this.add(new ColorSetting("AmbientsBox", new Color(8061183), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.othersC = this.add(new ColorSetting("OthersBox", new Color(16711778), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.armorDuraColor = this.add(new ColorSetting("Armor Dura Color", new Color(3145472), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.textcolor = this.add(new ColorSetting("Item Name Color", new Color(255, 255, 255, 255), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color && this.drawItem.getValue();
      }));
      this.countColor = this.add(new ColorSetting("Item Count Color", new Color(255, 255, 0, 255), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color && this.drawItemC.getValue();
      }));
      this.hHealth = this.add(new ColorSetting("High Health Color", new Color(0, 255, 0, 255), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.mHealth = this.add(new ColorSetting("Mid Health Color", new Color(255, 255, 0, 255), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
      this.lHealth = this.add(new ColorSetting("Low Health Color", new Color(255, 0, 0, 255), (v) -> {
         return this.page.getValue() == TwoDESP.Page.Color;
      }));
   }

   public void onRender2D(DrawContext context, float tickDelta) {
      Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
      BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
      Render2DUtil.setupRender();
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      Iterator var5 = mc.world.getEntities().iterator();

      Entity ent;
      while(var5.hasNext()) {
         ent = (Entity)var5.next();
         if (this.shouldRender(ent)) {
            this.drawBox(bufferBuilder, ent, matrix, context);
         }
      }

      BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
      Render2DUtil.endRender();
      var5 = mc.world.getEntities().iterator();

      while(var5.hasNext()) {
         ent = (Entity)var5.next();
         if (this.shouldRender(ent)) {
            this.drawText(ent, context);
         }
      }

   }

   public boolean shouldRender(Entity entity) {
      if (entity == null) {
         return false;
      } else if (mc.player == null) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) {
            return false;
         } else {
            return Nullpoint.FRIEND.isFriend((PlayerEntity)entity) ? this.friends.getValue() : this.players.getValue();
         }
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystals.getValue();
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

   public Color getEntityColor(Entity entity) {
      if (entity == null) {
         return new Color(-1);
      } else if (entity instanceof PlayerEntity) {
         return Nullpoint.FRIEND.isFriend((PlayerEntity)entity) ? this.friendsC.getValue() : this.playersC.getValue();
      } else if (entity instanceof EndCrystalEntity) {
         return this.crystalsC.getValue();
      } else {
         Color var10000;
         switch (entity.getType().getSpawnGroup()) {
            case CREATURE:
            case WATER_CREATURE:
               var10000 = this.creaturesC.getValue();
               break;
            case MONSTER:
               var10000 = this.monstersC.getValue();
               break;
            case AMBIENT:
            case WATER_AMBIENT:
               var10000 = this.ambientsC.getValue();
               break;
            default:
               var10000 = this.othersC.getValue();
         }

         return var10000;
      }
   }

   public void drawBox(BufferBuilder bufferBuilder, @NotNull Entity ent, Matrix4f matrix, DrawContext context) {
      double x = ent.prevX + (ent.getX() - ent.prevX) * (double)mc.getTickDelta();
      double y = ent.prevY + (ent.getY() - ent.prevY) * (double)mc.getTickDelta();
      double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)mc.getTickDelta();
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
      Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
      Color col = this.getEntityColor(ent);
      Vector4d position = null;
      Vec3d[] var16 = vectors;
      int var17 = vectors.length;

      for(int var18 = 0; var18 < var17; ++var18) {
         Vec3d vector = var16[var18];
         vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         if (this.outline.getValue()) {
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 1.0), (float)posY, (float)(posX + 0.5), (float)(endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 1.0), (float)(posY - 0.5), (float)(endPosX + 0.5), (float)(posY + 0.5 + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(endPosX - 0.5 - 0.5), (float)posY, (float)(endPosX + 0.5), (float)(endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 1.0), (float)(endPosY - 0.5 - 0.5), (float)(endPosX + 0.5), (float)(endPosY + 0.5), Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)(posX + 0.5 - 0.5), (float)endPosY, col, col, col, col);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)posX, (float)(endPosY - 0.5), (float)endPosX, (float)endPosY, col, col, col, col);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 0.5), (float)posY, (float)endPosX, (float)(posY + 0.5), col, col, col, col);
            Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(endPosX - 0.5), (float)posY, (float)endPosX, (float)endPosY, col, col, col, col);
         }

         if (ent instanceof LivingEntity lent) {
             if (lent.getHealth() != 0.0F && this.renderHealth.getValue()) {
               Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 4.0), (float)posY, (float)posX - 3.0F, (float)endPosY, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
               Color color = this.getcolor(lent.getHealth());
               Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(posX - 4.0), (float)(endPosY + (posY - endPosY) * (double)lent.getHealth() / (double)lent.getMaxHealth()), (float)posX - 3.0F, (float)endPosY, color, color, color, color);
            }
         }

         if (ent instanceof PlayerEntity player) {
             if (this.renderArmor.getValue()) {
               double height = (endPosY - posY) / 4.0;
               ArrayList stacks = new ArrayList();
               stacks.add(player.getInventory().armor.get(3));
               stacks.add(player.getInventory().armor.get(2));
               stacks.add(player.getInventory().armor.get(1));
               stacks.add(player.getInventory().armor.get(0));
               int i = -1;
               Iterator var29 = stacks.iterator();

               while(var29.hasNext()) {
                  ItemStack armor = (ItemStack)var29.next();
                  ++i;
                  if (!armor.isEmpty()) {
                     float durability = (float)(armor.getMaxDamage() - armor.getDamage());
                     int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0F);
                     double finalH = height * (double)(percent / 100);
                     Render2DUtil.setRectPoints(bufferBuilder, matrix, (float)(endPosX + 1.5), (float)((double)((float)posY) + height * (double)i + 1.2 * (double)(i + 1)), (float)endPosX + 3.0F, (float)((int)(posY + height * (double)i + 1.2 * (double)(i + 1) + finalH)), this.armorDuraColor.getValue(), this.armorDuraColor.getValue(), this.armorDuraColor.getValue(), this.armorDuraColor.getValue());
                  }
               }
            }
         }
      }

   }

   public void drawText(Entity ent, DrawContext context) {
      double x = ent.prevX + (ent.getX() - ent.prevX) * (double)mc.getTickDelta();
      double y = ent.prevY + (ent.getY() - ent.prevY) * (double)mc.getTickDelta();
      double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)mc.getTickDelta();
      Box axisAlignedBB2 = ent.getBoundingBox();
      Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + x - 0.05, axisAlignedBB2.minY - ent.getY() + y, axisAlignedBB2.minZ - ent.getZ() + z - 0.05, axisAlignedBB2.maxX - ent.getX() + x + 0.05, axisAlignedBB2.maxY - ent.getY() + y + 0.15, axisAlignedBB2.maxZ - ent.getZ() + z + 0.05);
      Vec3d[] vectors = new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
      this.getEntityColor(ent);
      Vector4d position = null;
      Vec3d[] var14 = vectors;
      int var15 = vectors.length;

      for(int var16 = 0; var16 < var15; ++var16) {
         Vec3d vector = var14[var16];
         vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
         if (vector.z > 0.0 && vector.z < 1.0) {
            if (position == null) {
               position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }

            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
         }
      }

      if (position != null) {
         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         double endPosY = position.w;
         int i;
         if (ent instanceof ItemEntity entity) {
             if (this.drawItem.getValue()) {
               float diff = (float)((endPosX - posX) / 2.0);
               float textWidth = FontRenderers.Arial.getWidth(entity.getDisplayName().getString());
               float tagX = (float)((posX + (double) diff - (double) (textWidth / 2.0F)));
               i = entity.getStack().getCount();
               context.drawText(mc.textRenderer, entity.getDisplayName().getString(), (int)tagX, (int)(posY - 10.0), this.textcolor.getValue().getRGB(), false);
               if (this.drawItemC.getValue()) {
                  context.drawText(mc.textRenderer, "x" + i, (int)(tagX + (float)mc.textRenderer.getWidth(entity.getDisplayName().getString() + " ")), (int)posY - 10, this.countColor.getValue().getRGB(), false);
               }
            }
         }

         if (ent instanceof PlayerEntity player) {
             if (this.renderArmor.getValue()) {
               double height = (endPosY - posY) / 4.0;
               ArrayList stacks = new ArrayList();
               stacks.add(player.getInventory().armor.get(3));
               stacks.add(player.getInventory().armor.get(2));
               stacks.add(player.getInventory().armor.get(1));
               stacks.add(player.getInventory().armor.get(0));
               i = -1;
               Iterator var27 = stacks.iterator();

               while(var27.hasNext()) {
                  ItemStack armor = (ItemStack)var27.next();
                  ++i;
                  if (!armor.isEmpty()) {
                     float durability = (float)(armor.getMaxDamage() - armor.getDamage());
                     int percent = (int)(durability / (float)armor.getMaxDamage() * 100.0F);
                     double finalH = height * (double)(percent / 100);
                     context.drawItem(armor, (int)(endPosX + 4.0), (int)(posY + height * (double)i + 1.2 * (double)(i + 1) + finalH / 2.0));
                  }
               }
            }
         }
      }

   }

   public static float getRotations(Vec2f vec) {
      if (mc.player == null) {
         return 0.0F;
      } else {
         double x = (double)vec.x - mc.player.getPos().x;
         double z = (double)vec.y - mc.player.getPos().z;
         return (float)(-(Math.atan2(x, z) * 57.29577951308232));
      }
   }

   public Color getcolor(float health) {
      if (health >= 20.0F) {
         return this.hHealth.getValue();
      } else {
         return 20.0F > health && health > 10.0F ? this.mHealth.getValue() : this.lHealth.getValue();
      }
   }

   public enum Page {
      Setting,
      Target,
      Color;

      // $FF: synthetic method
      private static Page[] $values() {
         return new Page[]{Setting, Target, Color};
      }
   }
}

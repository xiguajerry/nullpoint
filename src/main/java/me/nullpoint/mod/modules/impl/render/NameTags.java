package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.render.Render2DUtil;
import me.nullpoint.api.utils.render.TextUtil;
import me.nullpoint.mod.gui.font.FontRenderers;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.player.FreeCam;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;

public class NameTags extends Module {
   public static NameTags INSTANCE;
   private final SliderSetting scale = this.add(new SliderSetting("Scale", 0.6800000071525574, 0.10000000149011612, 2.0, 0.01));
   private final SliderSetting minScale = this.add(new SliderSetting("MinScale", 0.20000000298023224, 0.10000000149011612, 1.0, 0.01));
   private final SliderSetting scaled = this.add(new SliderSetting("Scaled", 1.0, 0.0, 2.0, 0.01));
   private final SliderSetting offset = this.add(new SliderSetting("Offset", 0.3149999976158142, 0.0010000000474974513, 1.0, 0.001));
   private final SliderSetting height = this.add(new SliderSetting("Height", 0.0, -3.0, 3.0, 0.01));
   private final BooleanSetting gamemode = this.add(new BooleanSetting("Gamemode", false));
   private final BooleanSetting ping = this.add(new BooleanSetting("Ping", false));
   private final BooleanSetting health = this.add(new BooleanSetting("Health", true));
   private final BooleanSetting getDistance = this.add(new BooleanSetting("Distance", true));
   private final BooleanSetting pops = this.add(new BooleanSetting("TotemPops", true));
   private final BooleanSetting enchants = this.add(new BooleanSetting("Enchants", true));
   private final ColorSetting outline = this.add((new ColorSetting("Outline", new Color(-1711276033, true))).injectBoolean(true));
   private final ColorSetting rect = this.add((new ColorSetting("Rect", new Color(-1728053247, true))).injectBoolean(true));
   private final ColorSetting friendColor = this.add(new ColorSetting("FriendColor", new Color(-14811363, true)));
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(-1, true)));
   public final EnumSetting font;
   private final SliderSetting armorHeight;
   private final SliderSetting armorScale;
   private final EnumSetting armorMode;

   public NameTags() {
      super("NameTags", Module.Category.Render);
      this.font = this.add(new EnumSetting("FontMode", NameTags.Font.Fast));
      this.armorHeight = this.add(new SliderSetting("ArmorHeight", 0.30000001192092896, -10.0, 10.0));
      this.armorScale = this.add(new SliderSetting("ArmorScale", 0.8999999761581421, 0.10000000149011612, 2.0, 0.009999999776482582));
      this.armorMode = this.add(new EnumSetting("ArmorMode", NameTags.Armor.Full));
      INSTANCE = this;
   }

   public void onRender2D(DrawContext context, float tickDelta) {
      Iterator var3 = mc.world.getPlayers().iterator();

      while(true) {
         PlayerEntity ent;
         Vec3d vector;
         Vec3d preVec;
         do {
            do {
               do {
                  if (!var3.hasNext()) {
                     return;
                  }

                  ent = (PlayerEntity)var3.next();
               } while(ent == mc.player && mc.options.getPerspective().isFirstPerson() && FreeCam.INSTANCE.isOff());

               double x = ent.prevX + (ent.getX() - ent.prevX) * (double)mc.getTickDelta();
               double y = ent.prevY + (ent.getY() - ent.prevY) * (double)mc.getTickDelta();
               double z = ent.prevZ + (ent.getZ() - ent.prevZ) * (double)mc.getTickDelta();
               vector = new Vec3d(x, y + this.height.getValue() + ent.getBoundingBox().getLengthY() + 0.3, z);
               preVec = vector;
               vector = TextUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            } while(!(vector.z > 0.0));
         } while(!(vector.z < 1.0));

         Vector4d position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
         position.x = Math.min(vector.x, position.x);
         position.y = Math.min(vector.y, position.y);
         position.z = Math.max(vector.x, position.z);
         String final_string = "";
         if (this.ping.getValue()) {
            final_string = final_string + getEntityPing(ent) + "ms ";
         }

         if (this.gamemode.getValue()) {
            final_string = final_string + this.translateGamemode(getEntityGamemode(ent)) + " ";
         }

         final_string = final_string + Formatting.RESET + ent.getName().getString();
         if (this.health.getValue()) {
            final_string = final_string + " " + this.getHealthColor(ent) + round2(ent.getAbsorptionAmount() + ent.getHealth());
         }

         if (this.getDistance.getValue()) {
            final_string = final_string + " " + Formatting.RESET + String.format("%.1f", mc.player.distanceTo(ent)) + "m";
         }

         if (this.pops.getValue() && Nullpoint.POP.getPop(ent.getName().getString()) != 0) {
            final_string = final_string + " \u00a7bPop " + Formatting.LIGHT_PURPLE + Nullpoint.POP.getPop(ent.getName().getString());
         }

         double posX = position.x;
         double posY = position.y;
         double endPosX = position.z;
         float diff = (float)(endPosX - posX) / 2.0F;
         float textWidth;
         if (this.font.getValue() == NameTags.Font.Fancy) {
            textWidth = FontRenderers.Arial.getWidth(final_string);
         } else {
            textWidth = (float)mc.textRenderer.getWidth(final_string);
         }

         float tagX = (float)((posX + (double) diff - (double) (textWidth / 2.0F)));
         ArrayList stacks = new ArrayList();
         stacks.add(ent.getMainHandStack());
         stacks.add(ent.getInventory().armor.get(3));
         stacks.add(ent.getInventory().armor.get(2));
         stacks.add(ent.getInventory().armor.get(1));
         stacks.add(ent.getInventory().armor.get(0));
         stacks.add(ent.getOffHandStack());
         context.getMatrices().push();
         context.getMatrices().translate(tagX - 2.0F + (textWidth + 4.0F) / 2.0F, (float)(posY - 13.0) + 6.5F, 0.0F);
         float size = (float)Math.max(1.0 - (double)MathHelper.sqrt((float)mc.cameraEntity.squaredDistanceTo(preVec)) * 0.01 * this.scaled.getValue(), 0.0);
         context.getMatrices().scale(Math.max(this.scale.getValueFloat() * size, this.minScale.getValueFloat()), Math.max(this.scale.getValueFloat() * size, this.minScale.getValueFloat()), 1.0F);
         context.getMatrices().translate(0.0F, this.offset.getValueFloat() * MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(preVec)), 0.0F);
         context.getMatrices().translate(-(tagX - 2.0F + (textWidth + 4.0F) / 2.0F), -((float)(posY - 13.0 + 6.5)), 0.0F);
         float item_offset = 0.0F;
         if (this.armorMode.getValue() != NameTags.Armor.None) {
            int count = 0;

            for(Iterator var28 = stacks.iterator(); var28.hasNext(); item_offset += 18.0F) {
               ItemStack armorComponent = (ItemStack)var28.next();
               ++count;
               if (!armorComponent.isEmpty()) {
                  context.getMatrices().push();
                  context.getMatrices().translate(tagX - 2.0F + (textWidth + 4.0F) / 2.0F, (float)(posY - 13.0) + 6.5F, 0.0F);
                  context.getMatrices().scale(this.armorScale.getValueFloat(), this.armorScale.getValueFloat(), 1.0F);
                  context.getMatrices().translate(-(tagX - 2.0F + (textWidth + 4.0F) / 2.0F), -((float)(posY - 13.0 + 6.5)), 0.0F);
                  context.getMatrices().translate(posX - 52.5 + (double)item_offset, (float)(posY - 29.0) + this.armorHeight.getValueFloat(), 0.0);
                  float durability = (float)(armorComponent.getMaxDamage() - armorComponent.getDamage());
                  int percent = (int)(durability / (float)armorComponent.getMaxDamage() * 100.0F);
                  Color color;
                  if (percent <= 33) {
                     color = Color.RED;
                  } else if (percent <= 66) {
                     color = Color.ORANGE;
                  } else {
                     color = Color.GREEN;
                  }

                  int index;
                  switch ((Armor)this.armorMode.getValue()) {
                     case OnlyArmor:
                        if (count > 1 && count < 6) {
                           DiffuseLighting.disableGuiDepthLighting();
                           context.drawItem(armorComponent, 0, 0);
                           context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                        }
                        break;
                     case Item:
                        DiffuseLighting.disableGuiDepthLighting();
                        context.drawItem(armorComponent, 0, 0);
                        context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                        break;
                     case Full:
                        DiffuseLighting.disableGuiDepthLighting();
                        context.drawItem(armorComponent, 0, 0);
                        context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                        if (armorComponent.getMaxDamage() > 0) {
                           if (this.font.getValue() == NameTags.Font.Fancy) {
                              FontRenderers.Arial.drawString(context.getMatrices(), String.valueOf(percent), 9.0F - FontRenderers.Arial.getWidth(String.valueOf(percent)) / 2.0F, -FontRenderers.Arial.getFontHeight() + 3.0F, color.getRGB());
                           } else {
                              TextRenderer var10001 = mc.textRenderer;
                              String var10002 = String.valueOf(percent);
                              int var10003 = 9 - mc.textRenderer.getWidth(String.valueOf(percent)) / 2;
                              Objects.requireNonNull(mc.textRenderer);
                              context.drawText(var10001, var10002, var10003, -9 + 1, color.getRGB(), true);
                           }
                        }
                        break;
                     case Durability:
                        context.drawItemInSlot(mc.textRenderer, armorComponent, 0, 0);
                        if (armorComponent.getMaxDamage() > 0) {
                           if (!armorComponent.isItemBarVisible()) {
                              int i = armorComponent.getItemBarStep();
                              int j = armorComponent.getItemBarColor();
                              index = 2;
                              int l = 13;
                              context.fill(RenderLayer.getGuiOverlay(), index, l, index + 13, l + 2, -16777216);
                              context.fill(RenderLayer.getGuiOverlay(), index, l, index + i, l + 1, j | -16777216);
                           }

                           if (this.font.getValue() == NameTags.Font.Fancy) {
                              FontRenderers.Arial.drawString(context.getMatrices(), String.valueOf(percent), 9.0F - FontRenderers.Arial.getWidth(String.valueOf(percent)) / 2.0F, 7.0F, color.getRGB());
                           } else {
                              context.drawText(mc.textRenderer, String.valueOf(percent), 9 - mc.textRenderer.getWidth(String.valueOf(percent)) / 2, 5, color.getRGB(), true);
                           }
                        }
                  }

                  context.getMatrices().pop();
                  if (this.enchants.getValue()) {
                     float enchantmentY = 0.0F;
                     NbtList enchants = armorComponent.getEnchantments();

                     for(index = 0; index < enchants.size(); ++index) {
                        String id = enchants.getCompound(index).getString("id");
                        short level = enchants.getCompound(index).getShort("lvl");
                        String encName;
                        switch (id) {
                           case "minecraft:blast_protection":
                              encName = "B" + level;
                              break;
                           case "minecraft:protection":
                              encName = "P" + level;
                              break;
                           case "minecraft:thorns":
                              encName = "T" + level;
                              break;
                           case "minecraft:sharpness":
                              encName = "S" + level;
                              break;
                           case "minecraft:efficiency":
                              encName = "E" + level;
                              break;
                           case "minecraft:unbreaking":
                              encName = "U" + level;
                              break;
                           case "minecraft:power":
                              encName = "PO" + level;
                              break;
                           default:
                              continue;
                        }

                        if (this.font.getValue() == NameTags.Font.Fancy) {
                           FontRenderers.Arial.drawString(context.getMatrices(), encName, posX - 50.0 + (double)item_offset, (float)posY - 45.0F + enchantmentY, -1);
                        } else {
                           context.getMatrices().push();
                           context.getMatrices().translate(posX - 50.0 + (double)item_offset, posY - 45.0 + (double)enchantmentY, 0.0);
                           context.drawText(mc.textRenderer, encName, 0, 0, -1, true);
                           context.getMatrices().pop();
                        }

                        enchantmentY -= 8.0F;
                     }
                  }
               }
            }
         }

         if (this.rect.booleanValue) {
            Render2DUtil.drawRect(context.getMatrices(), tagX - 2.0F, (float)(posY - 13.0), textWidth + 4.0F, 11.0F, this.rect.getValue());
         }

         if (this.outline.booleanValue) {
            Render2DUtil.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), textWidth + 6.0F, 1.0F, this.outline.getValue());
            Render2DUtil.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 2.0), textWidth + 6.0F, 1.0F, this.outline.getValue());
            Render2DUtil.drawRect(context.getMatrices(), tagX - 3.0F, (float)(posY - 14.0), 1.0F, 12.0F, this.outline.getValue());
            Render2DUtil.drawRect(context.getMatrices(), tagX + textWidth + 2.0F, (float)(posY - 14.0), 1.0F, 12.0F, this.outline.getValue());
         }

         if (this.font.getValue() == NameTags.Font.Fancy) {
            FontRenderers.Arial.drawString(context.getMatrices(), final_string, tagX, (float)posY - 10.0F, Nullpoint.FRIEND.isFriend(ent) ? this.friendColor.getValue().getRGB() : this.color.getValue().getRGB());
         } else {
            context.getMatrices().push();
            context.getMatrices().translate(tagX, (float)posY - 11.0F, 0.0F);
            context.drawText(mc.textRenderer, final_string, 0, 0, Nullpoint.FRIEND.isFriend(ent) ? this.friendColor.getValue().getRGB() : this.color.getValue().getRGB(), true);
            context.getMatrices().pop();
         }

         context.getMatrices().pop();
      }
   }

   public static String getEntityPing(PlayerEntity entity) {
      if (mc.getNetworkHandler() == null) {
         return "-1";
      } else {
         PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         if (playerListEntry == null) {
            return "-1";
         } else {
            int ping = playerListEntry.getLatency();
            Formatting color = Formatting.GREEN;
            if (ping >= 100) {
               color = Formatting.YELLOW;
            }

            if (ping >= 250) {
               color = Formatting.RED;
            }

            String var10000 = color.toString();
            return var10000 + ping;
         }
      }
   }

   public static GameMode getEntityGamemode(PlayerEntity entity) {
      if (entity == null) {
         return null;
      } else {
         PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
         return playerListEntry == null ? null : playerListEntry.getGameMode();
      }
   }

   private String translateGamemode(GameMode gamemode) {
      if (gamemode == null) {
         return "\u00a77[BOT]";
      } else {
         String var10000;
         switch (gamemode) {
            case SURVIVAL -> var10000 = "\u00a7b[S]";
            case CREATIVE -> var10000 = "\u00a7c[C]";
            case SPECTATOR -> var10000 = "\u00a77[SP]";
            case ADVENTURE -> var10000 = "\u00a7e[A]";
            default -> throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   private Formatting getHealthColor(@NotNull PlayerEntity entity) {
      int health = (int)((float)((int)entity.getHealth()) + entity.getAbsorptionAmount());
      if (health >= 30) {
         return Formatting.DARK_GREEN;
      } else if (health >= 24) {
         return Formatting.GREEN;
      } else if (health >= 18) {
         return Formatting.YELLOW;
      } else if (health >= 12) {
         return Formatting.GOLD;
      } else {
         return health >= 6 ? Formatting.RED : Formatting.DARK_RED;
      }
   }

   public static float round2(double value) {
      BigDecimal bd = new BigDecimal(value);
      bd = bd.setScale(1, RoundingMode.HALF_UP);
      return bd.floatValue();
   }

   public enum Font {
      Fancy,
      Fast;

      // $FF: synthetic method
      private static Font[] $values() {
         return new Font[]{Fancy, Fast};
      }
   }

   public enum Armor {
      None,
      Full,
      Durability,
      Item,
      OnlyArmor;

      // $FF: synthetic method
      private static Armor[] $values() {
         return new Armor[]{None, Full, Durability, Item, OnlyArmor};
      }
   }
}

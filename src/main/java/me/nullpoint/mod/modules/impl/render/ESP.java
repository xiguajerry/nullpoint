package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {
   private final ColorSetting item = this.add((new ColorSetting("Item", new Color(255, 255, 255, 100))).injectBoolean(true));
   private final ColorSetting player = this.add((new ColorSetting("Player", new Color(255, 255, 255, 100))).injectBoolean(true));
   private final ColorSetting chest = this.add((new ColorSetting("Chest", new Color(255, 255, 255, 100))).injectBoolean(false));

   public ESP() {
      super("ESP", Module.Category.Render);
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      if (this.item.booleanValue || this.player.booleanValue) {
         Iterator var3 = mc.world.getEntities().iterator();

         label41:
         while(true) {
            while(true) {
               if (!var3.hasNext()) {
                  break label41;
               }

               Entity entity = (Entity)var3.next();
               Color color;
               if (entity instanceof ItemEntity && this.item.booleanValue) {
                  color = this.item.getValue();
                  Render3DUtil.draw3DBox(matrixStack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))), color, false, true);
               } else if (entity instanceof PlayerEntity && this.player.booleanValue) {
                  color = this.player.getValue();
                  Render3DUtil.draw3DBox(matrixStack, ((IEntity)entity).getDimensions().getBoxAt(new Vec3d(MathUtil.interpolate(entity.lastRenderX, entity.getX(), partialTicks), MathUtil.interpolate(entity.lastRenderY, entity.getY(), partialTicks), MathUtil.interpolate(entity.lastRenderZ, entity.getZ(), partialTicks))).expand(0.0, 0.1, 0.0), color, false, true);
               }
            }
         }
      }

      if (this.chest.booleanValue) {
         ArrayList blockEntities = BlockUtil.getTileEntities();
         Iterator var8 = blockEntities.iterator();

         while(var8.hasNext()) {
            BlockEntity blockEntity = (BlockEntity)var8.next();
            if (blockEntity instanceof ChestBlockEntity) {
               Box box = new Box(blockEntity.getPos());
               Render3DUtil.draw3DBox(matrixStack, box, this.chest.getValue());
            }
         }
      }

   }
}

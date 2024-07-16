// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockPosX;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class BlockerESP extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
   private final BooleanSetting box = this.add(new BooleanSetting("Box", true));
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   private final BooleanSetting burrow = this.add(new BooleanSetting("Burrow", true));
   private final BooleanSetting surround = this.add(new BooleanSetting("Surround", true));
   final List<BlockPos> renderList = new ArrayList();

   public BlockerESP() {
      super("BlockerESP", Module.Category.Render);
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      this.renderList.clear();
      float pOffset = (float)CombatSetting.getOffset();

      for(Entity player : CombatUtil.getEnemies(10.0D)) {
         if (this.burrow.getValue()) {
            float[] offset = new float[]{-pOffset, 0.0F, pOffset};

            for(float x : offset) {
               for(float z : offset) {
                  BlockPos tempPos;
                  if (this.isObsidian(tempPos = new BlockPosX(player.getPos().add(x, 0.0D, z)))) {
                     this.renderList.add(tempPos);
                  }

                  BlockPosX var23;
                  if (this.isObsidian(var23 = new BlockPosX(player.getPos().add(x, 0.5D, z)))) {
                     this.renderList.add(var23);
                  }
               }
            }
         }

         if (this.surround.getValue()) {
            BlockPos pos = EntityUtil.getEntityPos(player, true);
            if (BlockUtil.isHole(pos)) {
               for(Direction i : Direction.values()) {
                  if (i != Direction.UP && i != Direction.DOWN && this.isObsidian(pos.offset(i))) {
                     this.renderList.add(pos.offset(i));
                  }
               }
            }
         }
      }

      for(BlockPos pos : this.renderList) {
         Render3DUtil.draw3DBox(matrixStack, new Box(pos), this.color.getValue(), this.outline.getValue(), this.box.getValue());
      }

   }

   private boolean isObsidian(BlockPos pos) {
      return (BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST) && !this.renderList.contains(pos);
   }
}
 
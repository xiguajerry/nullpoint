// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.miscellaneous;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

public class BaseFinder extends Module {
   private static final File basedata = new File("./BaseData.txt");
   private static final File chestdata = new File("./ChestData.txt");
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 15, 0, 30));
   private final SliderSetting count = this.add(new SliderSetting("Count", 50, 1, 2000));
   private final BooleanSetting log = this.add(new BooleanSetting("SaveChestLog", true));
   private final Timer timer = new Timer();

   public BaseFinder() {
      super("BaseFinder", Module.Category.Misc);
   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      for(BlockEntity blockEntity : BlockUtil.getTileEntities()) {
         if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity) {
            Box box = new Box(blockEntity.getPos());
            Render3DUtil.draw3DBox(matrixStack, box, this.color.getValue());
         }
      }

      if (this.timer.passed((long)this.delay.getValueInt() * 20L)) {
         int chest = 0;

         for(BlockEntity blockEntity : BlockUtil.getTileEntities()) {
            if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof TrappedChestBlockEntity) {
               ++chest;
               if (this.log.getValue()) {
                  writePacketData(chestdata, "FindChest:" + blockEntity.getPos());
               }
            }
         }

         if ((double)chest >= this.count.getValue()) {
            this.timer.reset();
            writePacketData(basedata, "Find:" + mc.player.getPos() + " Count:" + chest);
            CommandManager.sendChatMessage("Find:" + mc.player.getPos() + " Count:" + chest);
            chest = 0;
         }

      }
   }

   private static void writePacketData(File file, String data) {
      (new Thread(() -> {
         try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(data);
            writer.newLine();
            writer.close();
         } catch (IOException var3) {
         }

      })).start();
   }
}

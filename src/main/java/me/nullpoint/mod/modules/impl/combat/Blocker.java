// Decompiled with: CFR 0.152
// Class Version: 17
package me.nullpoint.mod.modules.impl.combat;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.combat.FeetTrap;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Blocker
        extends Module {
   public static Blocker INSTANCE;
   final Timer timer = new Timer();
   private final EnumSetting<Page> page = this.add(new EnumSetting<Page>("Page", Page.General));
   private final SliderSetting delay = this.add(new SliderSetting("PlaceDelay", 50, 0, 500, v -> this.page.getValue() == Page.General));
   private final SliderSetting blocksPer = this.add(new SliderSetting("BlocksPer", 1, 1, 8, v -> this.page.getValue() == Page.General));
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true, v -> this.page.getValue() == Page.General));
   private final BooleanSetting breakCrystal = this.add(new BooleanSetting("Break", true, v -> this.page.getValue() == Page.General));
   private final BooleanSetting inventorySwap = this.add(new BooleanSetting("InventorySwap", true, v -> this.page.getValue() == Page.General));
   private final BooleanSetting bevelCev = this.add(new BooleanSetting("BevelCev", true, v -> this.page.getValue() == Page.Target));
   private final BooleanSetting feet = this.add(new BooleanSetting("Feet", true, v -> this.page.getValue() == Page.Target).setParent());
   private final BooleanSetting onlySurround = this.add(new BooleanSetting("OnlySurround", true, v -> this.page.getValue() == Page.Target && this.feet.isOpen()));
   private final BooleanSetting inAirPause = this.add(new BooleanSetting("InAirPause", false, v -> this.page.getValue() == Page.Check));
   private final BooleanSetting detectMining = this.add(new BooleanSetting("DetectMining", true, v -> this.page.getValue() == Page.Check));
   private final BooleanSetting eatingPause = this.add(new BooleanSetting("EatingPause", true, v -> this.page.getValue() == Page.Check));
   public final BooleanSetting render = this.add(new BooleanSetting("Render", true, v -> this.page.getValue() == Page.Render));
   final ColorSetting box = this.add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> this.page.getValue() == Page.Render).injectBoolean(true));
   final ColorSetting fill = this.add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> this.page.getValue() == Page.Render).injectBoolean(true));
   public final SliderSetting fadeTime = this.add(new SliderSetting("FadeTime", 500, 0, 5000, v -> this.render.getValue()));
   private final List<BlockPos> placePos = new ArrayList<BlockPos>();
   private int placeProgress = 0;
   private BlockPos playerBP;

   public Blocker() {
      super("Blocker", Module.Category.Combat);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new BlockerRenderer());
   }

   @Override
   public void onUpdate() {
      if (!this.timer.passedMs(this.delay.getValue())) {
         return;
      }
      if (this.eatingPause.getValue() && EntityUtil.isUsing()) {
         return;
      }
      this.placeProgress = 0;
      if (this.playerBP != null && !this.playerBP.equals(EntityUtil.getPlayerPos(true))) {
         this.placePos.clear();
      }
      this.playerBP = EntityUtil.getPlayerPos(true);
      if (this.bevelCev.getValue()) {
         for (Direction direction : Direction.values()) {
            BlockPos blockerPos;
            if (direction == Direction.DOWN || this.isBedrock(this.playerBP.offset(direction).up()) || !this.crystalHere(blockerPos = this.playerBP.offset(direction).up(2)) || this.placePos.contains(blockerPos)) continue;
            this.placePos.add(blockerPos);
         }
      }
      if (this.getObsidian() == -1) {
         return;
      }
      if (this.inAirPause.getValue() && !Blocker.mc.player.isOnGround()) {
         return;
      }
      this.placePos.removeIf(pos -> !BlockUtil.clientCanPlace(pos, true));
      if (this.feet.getValue() && (!this.onlySurround.getValue() || FeetTrap.INSTANCE.isOn())) {
         for (Direction direction : Direction.values()) {
            BlockPos surroundPos;
            if (direction == Direction.DOWN || direction == Direction.UP || this.isBedrock(surroundPos = this.playerBP.offset(direction)) || !BlockUtil.isMining(surroundPos)) continue;
            for (Direction direction2 : Direction.values()) {
               if (direction2 == Direction.DOWN || direction2 == Direction.UP) continue;
               BlockPos defensePos = this.playerBP.offset(direction).offset(direction2);
               if (this.breakCrystal.getValue()) {
                  CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), false);
               }
               if (!BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) continue;
               this.tryPlaceObsidian(defensePos);
            }
            BlockPos defensePos = this.playerBP.offset(direction).up();
            if (this.breakCrystal.getValue()) {
               CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), false);
            }
            if (!BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) continue;
            this.tryPlaceObsidian(defensePos);
         }
      }
      for (BlockPos defensePos : this.placePos) {
         if (this.breakCrystal.getValue() && this.crystalHere(defensePos)) {
            CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), false);
         }
         if (!BlockUtil.canPlace(defensePos, 6.0, this.breakCrystal.getValue())) continue;
         this.tryPlaceObsidian(defensePos);
      }
   }

   private boolean crystalHere(BlockPos pos) {
      return Blocker.mc.world.getNonSpectatingEntities(EndCrystalEntity.class, new Box(pos)).stream().anyMatch(entity -> entity.getBlockPos().equals(pos));
   }

   private boolean isBedrock(BlockPos pos) {
      return Blocker.mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
   }

   private void tryPlaceObsidian(BlockPos pos) {
      if (!((double)this.placeProgress < this.blocksPer.getValue())) {
         return;
      }
      if (this.detectMining.getValue() && BlockUtil.isMining(pos)) {
         return;
      }
      int oldSlot = Blocker.mc.player.getInventory().selectedSlot;
      int block = this.getObsidian();
      if (block == -1) {
         return;
      }
      this.doSwap(block);
      BlockUtil.placeBlock(pos, this.rotate.getValue());
      if (this.inventorySwap.getValue()) {
         this.doSwap(block);
         EntityUtil.syncInventory();
      } else {
         this.doSwap(oldSlot);
      }
      ++this.placeProgress;
      BlockerRenderer.addBlock(pos);
      this.timer.reset();
   }

   private void doSwap(int slot) {
      if (this.inventorySwap.getValue()) {
         InventoryUtil.inventorySwap(slot, Blocker.mc.player.getInventory().selectedSlot);
      } else {
         InventoryUtil.switchToSlot(slot);
      }
   }

   private int getObsidian() {
      if (this.inventorySwap.getValue()) {
         return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
      }
      return InventoryUtil.findBlock(Blocks.OBSIDIAN);
   }

   public enum Page {
      General,
      Target,
      Check,
      Render

   }

   public class BlockerRenderer {
      public static final HashMap<BlockPos, placePosition> renderMap = new HashMap();

      public static void addBlock(BlockPos pos) {
         renderMap.put(pos, new placePosition(pos));
      }

      @EventHandler
      public void onRender3D(Render3DEvent event) {
         if (!Blocker.INSTANCE.render.getValue()) {
            return;
         }
         if (renderMap.isEmpty()) {
            return;
         }
         boolean shouldClear = true;
         for (placePosition placePosition2 : renderMap.values()) {
            if (!BlockUtil.clientCanPlace(placePosition2.pos, true)) {
               placePosition2.isAir = false;
            }
            if (!placePosition2.timer.passedMs((long)(Blocker.this.delay.getValue() + 100.0)) && placePosition2.isAir) {
               placePosition2.firstFade.reset();
            }
            if (placePosition2.firstFade.getQuad(FadeUtils.Quad.In2) == 1.0) continue;
            shouldClear = false;
            MatrixStack matrixStack = event.getMatrixStack();
            if (Blocker.INSTANCE.fill.booleanValue) {
               Render3DUtil.drawFill(matrixStack, new Box(placePosition2.pos), ColorUtil.injectAlpha(Blocker.INSTANCE.fill.getValue(), (int)((double)Blocker.this.fill.getValue().getAlpha() * (1.0 - placePosition2.firstFade.getQuad(FadeUtils.Quad.In2)))));
            }
            if (!Blocker.INSTANCE.box.booleanValue) continue;
            Render3DUtil.drawBox(matrixStack, new Box(placePosition2.pos), ColorUtil.injectAlpha(Blocker.INSTANCE.box.getValue(), (int)((double)Blocker.this.box.getValue().getAlpha() * (1.0 - placePosition2.firstFade.getQuad(FadeUtils.Quad.In2)))));
         }
         if (shouldClear) {
            renderMap.clear();
         }
      }

      public static class placePosition {
         public final FadeUtils firstFade;
         public final BlockPos pos;
         public final Timer timer;
         public boolean isAir;

         public placePosition(BlockPos placePos) {
            this.firstFade = new FadeUtils((long)Blocker.INSTANCE.fadeTime.getValue());
            this.pos = placePos;
            this.timer = new Timer();
            this.isAir = true;
         }
      }
   }
}

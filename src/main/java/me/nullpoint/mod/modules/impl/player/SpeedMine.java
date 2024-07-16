// Decompiled with: FernFlower
// Class Version: 17
package me.nullpoint.mod.modules.impl.player;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.ClickBlockEvent;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.utils.combat.CombatUtil;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.entity.InventoryUtil;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.api.utils.world.BlockUtil;
import me.nullpoint.asm.accessors.IPlayerMoveC2SPacket;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.combat.AnchorAura;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SpeedMine extends Module {
   public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.LAVA_CAULDRON, Blocks.LAVA, Blocks.WATER_CAULDRON, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER, Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME);
   private final SliderSetting delay = this.add(new SliderSetting("Delay", 50.0D, 0.0D, 500.0D, 1.0D));
   private final SliderSetting damage = this.add(new SliderSetting("Damage", 0.7F, 0.0D, 2.0D, 0.01D));
   private final SliderSetting range = this.add(new SliderSetting("Range", 6.0D, 3.0D, 10.0D, 0.1D));
   private final SliderSetting maxBreak = this.add(new SliderSetting("MaxBreak", 3.0D, 0.0D, 20.0D, 1.0D));
   public final BooleanSetting preferWeb = this.add(new BooleanSetting("PreferWeb", true));
   private final BooleanSetting instant = this.add(new BooleanSetting("Instant", false));
   private final BooleanSetting cancelPacket = this.add(new BooleanSetting("CancelPacket", false));
   private final BooleanSetting wait = this.add(new BooleanSetting("Wait", true, (v) -> !this.instant.getValue()));
   private final BooleanSetting mineAir = this.add(new BooleanSetting("MineAir", true, (v) -> this.wait.getValue()));
   public final BooleanSetting farCancel = this.add(new BooleanSetting("FarCancel", false));
   public final BooleanSetting hotBar = this.add(new BooleanSetting("HotbarSwap", false));
   public final BooleanSetting ghostHand = this.add(new BooleanSetting("GhostHand", true));
   private final BooleanSetting checkGround = this.add(new BooleanSetting("CheckGround", true));
   private final BooleanSetting onlyGround = this.add(new BooleanSetting("OnlyGround", true));
   private final BooleanSetting doubleBreak = this.add(new BooleanSetting("DoubleBreak", true));
   private final BooleanSetting usingPause = this.add(new BooleanSetting("UsingPause", false));
   private final BooleanSetting swing = this.add(new BooleanSetting("Swing", true));
   private final BooleanSetting endSwing = this.add(new BooleanSetting("EndSwing", false));
   private final BooleanSetting bypassGround = this.add(new BooleanSetting("BypassGround", true));
   private final SliderSetting bypassTime = this.add(new SliderSetting("BypassTime", 400, 0, 2000, (v) -> this.bypassGround.getValue()));
   private final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
   private final SliderSetting time = this.add(new SliderSetting("Time", 100, 0, 2000, (v) -> this.rotate.getValue()));
   private final BooleanSetting switchReset = this.add(new BooleanSetting("SwitchReset", false));
   private final BooleanSetting crystal = this.add((new BooleanSetting("Crystal", false)).setParent());
   private final BooleanSetting onlyHeadBomber = this.add(new BooleanSetting("OnlyHeadBomber", false, (v) -> this.crystal.isOpen()));
   private final BooleanSetting waitPlace = this.add(new BooleanSetting("WaitPlace", false, (v) -> this.crystal.isOpen()));
   private final BooleanSetting spamPlace = this.add(new BooleanSetting("SpamPlace", false, (v) -> this.crystal.isOpen()));
   private final BooleanSetting afterBreak = this.add(new BooleanSetting("AfterBreak", true, (v) -> this.crystal.isOpen()));
   private final BooleanSetting checkDamage = this.add(new BooleanSetting("DetectProgress", true, (v) -> this.crystal.isOpen()));
   private final SliderSetting crystalDamage = this.add(new SliderSetting("Progress", 0.7F, 0.0D, 1.0D, 0.01D, (v) -> this.crystal.isOpen() && this.checkDamage.getValue()));
   public final BindSetting obsidian = this.add(new BindSetting("Obsidian", -1));
   private final BindSetting enderChest = this.add(new BindSetting("EnderChest", -1));
   private final SliderSetting placeDelay = this.add(new SliderSetting("PlaceDelay", 100, 0, 1000));
   private final EnumSetting<FadeUtils.Quad> quad = this.add(new EnumSetting("Quad", FadeUtils.Quad.In));
   private final BooleanSetting autoColor = this.add(new BooleanSetting("AutoColor", true));
   public final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
   public final ColorSetting endColor = this.add(new ColorSetting("EndColor", new Color(25, 255, 50, 100))).injectBoolean(false);
   public final ColorSetting endboxColor = this.add(new ColorSetting("EndBoxColor", new Color(25, 255, 50, 100), (v) -> this.endColor.booleanValue));
   public final ColorSetting doubleColor = this.add(new ColorSetting("DoubleColor", new Color(88, 94, 255, 100), (v) -> this.doubleBreak.getValue()));
   private final BooleanSetting bold = this.add(new BooleanSetting("Bold", false)).setParent();
   private final SliderSetting lineWidth = this.add(new SliderSetting("LineWidth", 4, 1, 5, (v) -> this.bold.isOpen()));
   private final BooleanSetting text = this.add(new BooleanSetting("Text", true));
   private final BooleanSetting box = this.add(new BooleanSetting("Box", true));
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   int lastSlot = -1;
   public static SpeedMine INSTANCE;
   public static BlockPos breakPos;
   public static BlockPos secondPos;
   public static double progress = 0.0D;
   public static double secondProgress = 0.0D;
   private final Timer mineTimer = new Timer();
   private final FadeUtils animationTime = new FadeUtils(1000L);
   private final FadeUtils secondAnim = new FadeUtils(1000L);
   private boolean startMine = false;
   private int breakNumber = 0;
   public final Timer secondTimer = new Timer();
   private final Timer delayTimer = new Timer();
   private final Timer placeTimer = new Timer();
   public static boolean sendGroundPacket = false;
   static DecimalFormat df = new DecimalFormat("0.0");

   public SpeedMine() {
      super("SpeedMine", Module.Category.Combat);
      INSTANCE = this;
   }

   public String getInfo() {
      return this.instant.getValue() ? "Instant" : "Aborted";
   }

   private int findCrystal() {
      return !this.hotBar.getValue() ? InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL) : InventoryUtil.findItem(Items.END_CRYSTAL);
   }

   private int findBlock(Block block) {
      return !this.hotBar.getValue() ? InventoryUtil.findBlockInventorySlot(block) : InventoryUtil.findBlock(block);
   }

   private void doSwap(int slot, int inv) {
      if (this.hotBar.getValue()) {
         InventoryUtil.switchToSlot(slot);
      } else {
         InventoryUtil.inventorySwap(inv, mc.player.getInventory().selectedSlot);
      }

   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      this.update();
      if (!mc.player.isCreative()) {
         if (secondPos != null) {
            int slot = this.getTool(secondPos);
            if (slot == -1) {
               slot = mc.player.getInventory().selectedSlot;
            }

            double breakTime = this.getBreakTime(secondPos, slot);
            secondProgress = (double)this.secondTimer.getPassedTimeMs() / breakTime;
            if (this.isAir(secondPos)) {
               secondPos = null;
               return;
            }

            double iProgress = secondProgress > 1.0D ? 1.0D : secondProgress;
            double ease = (1.0D - this.secondAnim.getQuad(this.quad.getValue())) * 0.5D;
            if (!this.bold.getValue()) {
               Render3DUtil.draw3DBox(matrixStack, (new Box(secondPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.doubleColor.getValue(), (int)((double)this.doubleColor.getValue().getAlpha() * iProgress)), this.outline.getValue(), this.box.getValue());
            } else {
               Render3DUtil.drawLine((new Box(secondPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.doubleColor.getValue(), (int)((double)this.doubleColor.getValue().getAlpha() * iProgress)), (float)this.lineWidth.getValueInt());
               Render3DUtil.drawFill(matrixStack, (new Box(secondPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.doubleColor.getValue(), (int)((double)this.doubleColor.getValue().getAlpha() * iProgress)));
            }
         } else {
            secondProgress = 0.0D;
         }

         if (breakPos != null) {
            int slot = this.getTool(breakPos);
            if (slot == -1) {
               slot = mc.player.getInventory().selectedSlot;
            }

            double breakTime = this.getBreakTime(breakPos, slot);
            progress = (double)this.mineTimer.getPassedTimeMs() / breakTime;
            this.animationTime.setLength((long)this.getBreakTime(breakPos, slot));
            double ease = (1.0D - this.animationTime.getQuad(this.quad.getValue())) * 0.5D;
            Color color = this.color.getValue();
            double iProgress = progress > 1.0D ? 1.0D : progress;
            if (!this.bold.getValue()) {
               Render3DUtil.draw3DBox(matrixStack, (new Box(breakPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.autoColor.getValue() ? new Color((int)(255.0D * iProgress), (int)(255.0D * iProgress), 0) : (!this.endColor.booleanValue ? color : (iProgress >= 1.0D ? this.endColor.getValue() : color)), (int)(!this.endColor.booleanValue ? (double)color.getAlpha() * iProgress : (iProgress >= 1.0D ? (double)this.endColor.getValue().getAlpha() * iProgress : (double)color.getAlpha() * iProgress))), this.outline.getValue(), this.box.getValue());
            } else {
               Render3DUtil.drawLine((new Box(breakPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.autoColor.getValue() ? new Color((int)(255.0D * iProgress), (int)(255.0D * iProgress), 0) : (!this.endColor.booleanValue ? color : (iProgress >= 1.0D ? this.endboxColor.getValue() : color)), (int)(!this.endColor.booleanValue ? (double)color.getAlpha() * iProgress : (iProgress >= 1.0D ? (double)this.endColor.getValue().getAlpha() * iProgress : (double)color.getAlpha() * iProgress))), (float)this.lineWidth.getValueInt());
               Render3DUtil.drawFill(matrixStack, (new Box(breakPos)).shrink(ease, ease, ease).shrink(-ease, -ease, -ease), ColorUtil.injectAlpha(this.autoColor.getValue() ? new Color((int)(255.0D * iProgress), (int)(255.0D * iProgress), 0) : (!this.endColor.booleanValue ? color : (iProgress >= 1.0D ? this.endColor.getValue() : color)), (int)(!this.endColor.booleanValue ? (double)color.getAlpha() * iProgress : (iProgress >= 1.0D ? (double)this.endColor.getValue().getAlpha() * iProgress : (double)color.getAlpha() * iProgress))));
            }

            if (this.text.getValue()) {
               if (this.isAir(breakPos)) {
                  Render3DUtil.drawText3D("Waiting", breakPos.toCenterPos(), -1);
               } else if ((double)((int)this.mineTimer.getPassedTimeMs()) < breakTime) {
                  Render3DUtil.drawText3D(df.format(progress * 100.0D) + "%", breakPos.toCenterPos(), -1);
               } else {
                  Render3DUtil.drawText3D("100.0%", breakPos.toCenterPos(), -1);
               }
            }
         } else {
            progress = 0.0D;
         }
      } else {
         progress = 0.0D;
         secondProgress = 0.0D;
      }

   }

   public void onLogin() {
      this.startMine = false;
      breakPos = null;
      secondPos = null;
   }

   public void onDisable() {
      this.startMine = false;
      breakPos = null;
   }

   public void onUpdate() {
      this.update();
   }

   public void update() {
      if (!nullCheck()) {
         if (mc.player.isDead()) {
            secondPos = null;
         }

         if (secondPos != null && this.secondTimer.passed(this.getBreakTime(secondPos, mc.player.getInventory().selectedSlot, 1.3D))) {
            secondPos = null;
         }

         if (secondPos != null && this.isAir(secondPos)) {
            secondPos = null;
         }

         if (mc.player.isCreative()) {
            this.startMine = false;
            this.breakNumber = 0;
            breakPos = null;
         } else if (breakPos == null) {
            this.breakNumber = 0;
            this.startMine = false;
         } else {
            if (this.isAir(breakPos)) {
               this.breakNumber = 0;
            }

            if ((!((double)this.breakNumber > this.maxBreak.getValue() - 1.0D) || !(this.maxBreak.getValue() > 0.0D)) && (this.wait.getValue() || !this.isAir(breakPos) || this.instant.getValue())) {
               if (godBlocks.contains(mc.world.getBlockState(breakPos).getBlock())) {
                  breakPos = null;
                  this.startMine = false;
               } else if (!this.usingPause.getValue() || !EntityUtil.isUsing()) {
                  if ((double)MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(breakPos.toCenterPos())) > this.range.getValue()) {
                     if (this.farCancel.getValue()) {
                        this.startMine = false;
                        this.breakNumber = 0;
                        breakPos = null;
                     }

                  } else if (!breakPos.equals(AnchorAura.INSTANCE.currentPos)) {
                     if (this.hotBar.getValue() || mc.currentScreen == null || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof ClickGuiScreen) {
                        int slot = this.getTool(breakPos);
                        if (slot == -1) {
                           slot = mc.player.getInventory().selectedSlot;
                        }

                        if (this.isAir(breakPos)) {
                           if (this.shouldCrystal()) {
                              for(Direction facing : Direction.values()) {
                                 CombatUtil.attackCrystal(breakPos.offset(facing), this.rotate.getValue(), true);
                              }
                           }

                           if (this.placeTimer.passedMs(this.placeDelay.getValue()) && BlockUtil.canPlace(breakPos) && mc.currentScreen == null) {
                              if (this.enderChest.isPressed()) {
                                 int eChest = this.findBlock(Blocks.ENDER_CHEST);
                                 if (eChest != -1) {
                                    int oldSlot = mc.player.getInventory().selectedSlot;
                                    this.doSwap(eChest, eChest);
                                    BlockUtil.placeBlock(breakPos, this.rotate.getValue(), true);
                                    this.doSwap(oldSlot, eChest);
                                    this.placeTimer.reset();
                                 }
                              } else if (this.obsidian.isPressed()) {
                                 int obsidian = this.findBlock(Blocks.OBSIDIAN);
                                 if (obsidian != -1) {
                                    boolean hasCrystal = false;
                                    if (this.shouldCrystal()) {
                                       for(Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(breakPos.up()))) {
                                          if (entity instanceof EndCrystalEntity) {
                                             hasCrystal = true;
                                             break;
                                          }
                                       }
                                    }

                                    if (!hasCrystal || this.spamPlace.getValue()) {
                                       int oldSlot = mc.player.getInventory().selectedSlot;
                                       this.doSwap(obsidian, obsidian);
                                       BlockUtil.placeBlock(breakPos, this.rotate.getValue(), true);
                                       this.doSwap(oldSlot, obsidian);
                                       this.placeTimer.reset();
                                    }
                                 }
                              }
                           }

                           this.breakNumber = 0;
                        } else if (canPlaceCrystal(breakPos.up(), true)) {
                           if (this.waitPlace.getValue()) {
                              for(Direction i : Direction.values()) {
                                 if (breakPos.offset(i).equals(AutoCrystal.crystalPos)) {
                                    if (AutoCrystal.INSTANCE.canPlaceCrystal(AutoCrystal.crystalPos, false, false)) {
                                       return;
                                    }
                                    break;
                                 }
                              }
                           }

                           if (this.shouldCrystal()) {
                              if (this.placeTimer.passedMs(this.placeDelay.getValue())) {
                                 if (this.checkDamage.getValue()) {
                                    if ((double)this.mineTimer.getPassedTimeMs() / this.getBreakTime(breakPos, slot) >= this.crystalDamage.getValue()) {
                                       int crystal = this.findCrystal();
                                       if (crystal != -1) {
                                          int oldSlot = mc.player.getInventory().selectedSlot;
                                          this.doSwap(crystal, crystal);
                                          BlockUtil.placeCrystal(breakPos.up(), this.rotate.getValue());
                                          this.doSwap(oldSlot, crystal);
                                          this.placeTimer.reset();
                                          if (this.waitPlace.getValue()) {
                                             return;
                                          }
                                       }
                                    }
                                 } else {
                                    int crystal = this.findCrystal();
                                    if (crystal != -1) {
                                       int oldSlot = mc.player.getInventory().selectedSlot;
                                       this.doSwap(crystal, crystal);
                                       BlockUtil.placeCrystal(breakPos.up(), this.rotate.getValue());
                                       this.doSwap(oldSlot, crystal);
                                       this.placeTimer.reset();
                                       if (this.waitPlace.getValue()) {
                                          return;
                                       }
                                    }
                                 }
                              } else if (this.startMine) {
                                 return;
                              }
                           }
                        }

                        if (this.delayTimer.passedMs((long)this.delay.getValue())) {
                           if (this.startMine) {
                              if (this.isAir(breakPos)) {
                                 return;
                              }

                              if (this.onlyGround.getValue() && !mc.player.isOnGround()) {
                                 return;
                              }

                              if (this.mineTimer.passedMs((long)this.getBreakTime(breakPos, slot))) {
                                 int old = mc.player.getInventory().selectedSlot;
                                 boolean shouldSwitch;
                                 if (this.hotBar.getValue()) {
                                    shouldSwitch = slot != old;
                                 } else {
                                    if (slot < 9) {
                                       slot += 36;
                                    }

                                    shouldSwitch = old + 36 != slot;
                                 }

                                 if (shouldSwitch) {
                                    if (this.hotBar.getValue()) {
                                       InventoryUtil.switchToSlot(slot);
                                    } else {
                                       InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                                    }
                                 }

                                 if (this.rotate.getValue()) {
                                    EntityUtil.facePosSide(breakPos, BlockUtil.getClickSide(breakPos));
                                 }

                                 if (this.endSwing.getValue()) {
                                    EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
                                 }

                                 mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
                                 if (shouldSwitch && this.ghostHand.getValue()) {
                                    if (this.hotBar.getValue()) {
                                       InventoryUtil.switchToSlot(old);
                                    } else {
                                       InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
                                       EntityUtil.syncInventory();
                                    }
                                 }

                                 ++this.breakNumber;
                                 this.delayTimer.reset();
                                 if (this.afterBreak.getValue() && this.shouldCrystal()) {
                                    for(Direction facing : Direction.values()) {
                                       CombatUtil.attackCrystal(breakPos.offset(facing), this.rotate.getValue(), true);
                                    }
                                 }
                              }
                           } else {
                              if (!this.mineAir.getValue() && this.isAir(breakPos)) {
                                 return;
                              }

                              this.animationTime.setLength((long)this.getBreakTime(breakPos, slot));
                              this.mineTimer.reset();
                              if (this.swing.getValue()) {
                                 EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
                              }

                              mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
                              this.delayTimer.reset();
                           }

                        }
                     }
                  }
               }
            } else {
               if (breakPos.equals(secondPos)) {
                  secondPos = null;
               }

               this.startMine = false;
               this.breakNumber = 0;
               breakPos = null;
            }
         }
      }
   }

   @EventHandler
   public void onAttackBlock(ClickBlockEvent event) {
      if (!nullCheck() && !mc.player.isCreative()) {
         event.cancel();
         if (!godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock())) {
            if (!event.getBlockPos().equals(breakPos)) {
               breakPos = event.getBlockPos();
               this.mineTimer.reset();
               this.animationTime.reset();
               if (!godBlocks.contains(mc.world.getBlockState(event.getBlockPos()).getBlock())) {
                  this.startMine();
               }
            }
         }
      }
   }

   public static boolean canPlaceCrystal(BlockPos pos, boolean ignoreItem) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN) && BlockUtil.getClickSideStrict(obsPos) != null && noEntity(boost, ignoreItem) && noEntity(boost.up(), ignoreItem) && (!CombatSetting.INSTANCE.lowVersion.getValue() || BlockUtil.getBlock(boost.up()) == Blocks.AIR);
   }

   public static boolean noEntity(BlockPos pos, boolean ignoreItem) {
      for(Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
         if ((!(entity instanceof ItemEntity) || !ignoreItem) && (!(entity instanceof ArmorStandEntity) || !CombatSetting.INSTANCE.obsMode.getValue())) {
            return false;
         }
      }

      return true;
   }

   public void mine(BlockPos pos) {
      if (!nullCheck() && !mc.player.isCreative()) {
         if (!this.isOff()) {
            if (!godBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
               if (!pos.equals(breakPos)) {
                  if (breakPos == null || !this.preferWeb.getValue() || BlockUtil.getBlock(breakPos) != Blocks.COBWEB) {
                     breakPos = pos;
                     this.mineTimer.reset();
                     this.animationTime.reset();
                     this.startMine();
                  }
               }
            }
         }
      }
   }

   private boolean shouldCrystal() {
      return this.crystal.getValue() && (!this.onlyHeadBomber.getValue() || this.obsidian.isPressed());
   }

   private void startMine() {
      if (this.rotate.getValue()) {
         Vec3i vec3i = BlockUtil.getClickSide(breakPos).getVector();
         EntityUtil.faceVector(breakPos.toCenterPos().add(new Vec3d((double)vec3i.getX() * 0.5D, (double)vec3i.getY() * 0.5D, (double)vec3i.getZ() * 0.5D)));
      }

      mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
      if (this.doubleBreak.getValue()) {
         if (secondPos == null || this.isAir(secondPos)) {
            int slot = this.getTool(breakPos);
            if (slot == -1) {
               slot = mc.player.getInventory().selectedSlot;
            }

            double breakTime = this.getBreakTime(breakPos, slot, 1.0D);
            this.secondAnim.reset();
            this.secondAnim.setLength((long)breakTime);
            this.secondTimer.reset();
            secondPos = breakPos;
         }

         mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
         mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getClickSide(breakPos)));
      }

      if (this.swing.getValue()) {
         EntityUtil.swingHand(Hand.MAIN_HAND, CombatSetting.INSTANCE.swingMode.getValue());
      }

      this.breakNumber = 0;
   }

   public int getTool(BlockPos pos) {
      if (this.hotBar.getValue()) {
         int index = -1;
         float CurrentFastest = 1.0F;

         for(int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != ItemStack.EMPTY) {
               float digSpeed = (float)EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
               float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
               if (digSpeed + destroySpeed > CurrentFastest) {
                  CurrentFastest = digSpeed + destroySpeed;
                  index = i;
               }
            }
         }

         return index;
      } else {
         AtomicInteger slot = new AtomicInteger();
         slot.set(-1);
         float CurrentFastest = 1.0F;

         for(Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            if (!(entry.getValue().getItem() instanceof AirBlockItem)) {
               float digSpeed = (float)EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, entry.getValue());
               float destroySpeed = entry.getValue().getMiningSpeedMultiplier(mc.world.getBlockState(pos));
               if (digSpeed + destroySpeed > CurrentFastest) {
                  CurrentFastest = digSpeed + destroySpeed;
                  slot.set(entry.getKey());
               }
            }
         }

         return slot.get();
      }
   }

   @EventHandler(
           priority = -100
   )
   public void onRotate(RotateEvent event) {
      if (!nullCheck() && !mc.player.isCreative()) {
         if (!this.onlyGround.getValue() || mc.player.isOnGround()) {
            if (this.rotate.getValue() && breakPos != null && !this.isAir(breakPos) && this.time.getValue() > 0.0D) {
               if ((double)MathHelper.sqrt((float)EntityUtil.getEyesPos().squaredDistanceTo(breakPos.toCenterPos())) > this.range.getValue()) {
                  return;
               }

               int slot = this.getTool(breakPos);
               if (slot == -1) {
                  slot = mc.player.getInventory().selectedSlot;
               }

               double breakTime = this.getBreakTime(breakPos, slot) - this.time.getValue();
               if (breakTime <= 0.0D || this.mineTimer.passedMs((long)breakTime)) {
                  facePosFacing(breakPos, BlockUtil.getClickSide(breakPos), event);
               }
            }

         }
      }
   }

   public static void facePosFacing(BlockPos pos, Direction side, RotateEvent event) {
      Vec3d hitVec = pos.toCenterPos().add(new Vec3d((double)side.getVector().getX() * 0.5D, (double)side.getVector().getY() * 0.5D, (double)side.getVector().getZ() * 0.5D));
      faceVector(hitVec, event);
   }

   private static void faceVector(Vec3d vec, RotateEvent event) {
      float[] rotations = EntityUtil.getLegitRotations(vec);
      event.setRotation(rotations[0], rotations[1]);
   }

   @EventHandler(
           priority = -200
   )
   public void onPacketSend(PacketEvent.Send event) {
      if (!nullCheck() && !mc.player.isCreative()) {
         if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (this.bypassGround.getValue() && breakPos != null && !this.isAir(breakPos) && this.bypassTime.getValue() > 0.0D && MathHelper.sqrt((float)breakPos.toCenterPos().squaredDistanceTo(EntityUtil.getEyesPos())) <= this.range.getValueFloat() + 2.0F) {
               int slot = this.getTool(breakPos);
               if (slot == -1) {
                  slot = mc.player.getInventory().selectedSlot;
               }

               double breakTime = this.getBreakTime(breakPos, slot) - this.bypassTime.getValue();
               if (breakTime <= 0.0D || this.mineTimer.passedMs((long)breakTime)) {
                  sendGroundPacket = true;
                  ((IPlayerMoveC2SPacket)event.getPacket()).setOnGround(true);
               }
            } else {
               sendGroundPacket = false;
            }

         } else {
            Packet breakTime = event.getPacket();
            if (breakTime instanceof UpdateSelectedSlotC2SPacket packet) {
                if (packet.getSelectedSlot() != this.lastSlot) {
                  this.lastSlot = packet.getSelectedSlot();
                  if (this.switchReset.getValue()) {
                     this.startMine = false;
                     this.mineTimer.reset();
                     this.animationTime.reset();
                  }
               }

            } else if (event.getPacket() instanceof PlayerActionC2SPacket) {
               if (((PlayerActionC2SPacket)event.getPacket()).getAction() == Action.START_DESTROY_BLOCK) {
                  if (breakPos == null || !((PlayerActionC2SPacket)event.getPacket()).getPos().equals(breakPos)) {
                     if (this.cancelPacket.getValue()) {
                        event.cancel();
                     }

                     return;
                  }

                  this.startMine = true;
               } else if (((PlayerActionC2SPacket)event.getPacket()).getAction() == Action.STOP_DESTROY_BLOCK) {
                  if (breakPos == null || !((PlayerActionC2SPacket)event.getPacket()).getPos().equals(breakPos)) {
                     if (this.cancelPacket.getValue()) {
                        event.cancel();
                     }

                     return;
                  }

                  if (!this.instant.getValue()) {
                     this.startMine = false;
                  }
               }

            }
         }
      }
   }

   public final double getBreakTime(BlockPos pos, int slot) {
      return this.getBreakTime(pos, slot, this.damage.getValue());
   }

   public final double getBreakTime(BlockPos pos, int slot, double damage) {
      return (double)(1.0F / this.getBlockStrength(pos, mc.player.getInventory().getStack(slot)) / 20.0F * 1000.0F) * damage;
   }

   private boolean canBreak(BlockPos pos) {
      BlockState blockState = mc.world.getBlockState(pos);
      Block block = blockState.getBlock();
      return block.getHardness() != -1.0F;
   }

   public float getBlockStrength(BlockPos position, ItemStack itemStack) {
      BlockState state = mc.world.getBlockState(position);
      float hardness = state.getHardness(mc.world, position);
      if (hardness < 0.0F) {
         return 0.0F;
      } else {
         return !this.canBreak(position) ? this.getDigSpeed(state, itemStack) / hardness / 100.0F : this.getDigSpeed(state, itemStack) / hardness / 30.0F;
      }
   }

   public float getDigSpeed(BlockState state, ItemStack itemStack) {
      float digSpeed = this.getDestroySpeed(state, itemStack);
      if (digSpeed > 1.0F) {
         int efficiencyModifier = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
         if (efficiencyModifier > 0 && !itemStack.isEmpty()) {
            digSpeed = (float)((double)digSpeed + StrictMath.pow(efficiencyModifier, 2.0D) + 1.0D);
         }
      }

      if (mc.player.hasStatusEffect(StatusEffects.HASTE)) {
         digSpeed *= 1.0F + (float)(mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
         float fatigueScale;
         switch(mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
            case 0:
               fatigueScale = 0.3F;
               break;
            case 1:
               fatigueScale = 0.09F;
               break;
            case 2:
               fatigueScale = 0.0027F;
               break;
            default:
               fatigueScale = 8.1E-4F;
         }

         digSpeed *= fatigueScale;
      }

      if (mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
         digSpeed /= 5.0F;
      }

      if (!mc.player.isOnGround() && INSTANCE.checkGround.getValue()) {
         digSpeed /= 5.0F;
      }

      return digSpeed < 0.0F ? 0.0F : digSpeed;
   }

   public float getDestroySpeed(BlockState state, ItemStack itemStack) {
      float destroySpeed = 1.0F;
      if (itemStack != null && !itemStack.isEmpty()) {
         destroySpeed *= itemStack.getMiningSpeedMultiplier(state);
      }

      return destroySpeed;
   }

   private boolean isAir(BlockPos breakPos) {
      return mc.world.isAir(breakPos) || BlockUtil.getBlock(breakPos) == Blocks.FIRE && BlockUtil.hasCrystal(breakPos);
   }
}

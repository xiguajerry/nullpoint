package me.nullpoint.mod.modules.impl.client;

import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.Placement;
import me.nullpoint.mod.modules.settings.SwingSide;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;

public class CombatSetting extends Module {
   public static CombatSetting INSTANCE;
   public final BooleanSetting invSwapBypass = this.add(new BooleanSetting("InvSwapBypass", true));
   public final BooleanSetting lowVersion = this.add(new BooleanSetting("1.12", false));
   public final BooleanSetting rotateSync = this.add(new BooleanSetting("RotateSync", true));
   public final BooleanSetting packetPlace = this.add(new BooleanSetting("PacketPlace", true));
   public final BooleanSetting randomPitch = this.add(new BooleanSetting("RandomPitch", false));
   public final BooleanSetting rotations = this.add(new BooleanSetting("ShowRotations", true));
   public final BooleanSetting attackRotate = this.add(new BooleanSetting("AttackRotate", false));
   public final EnumSetting<Placement> placement;
   public final SliderSetting rotateTime;
   public final SliderSetting attackDelay;
   public final SliderSetting boxSize;
   public final BooleanSetting inventorySync;
   public final EnumSetting<SwingSide> swingMode;
   public final BooleanSetting obsMode;

   public CombatSetting() {
      super("CombatSetting", Module.Category.Client);
      this.placement = this.add(new EnumSetting("Placement", Placement.Vanilla));
      this.rotateTime = this.add(new SliderSetting("RotateTime", 0.5, 0.0, 1.0, 0.01));
      this.attackDelay = this.add(new SliderSetting("AttackDelay", 0.2, 0.0, 1.0, 0.01));
      this.boxSize = this.add(new SliderSetting("BoxSize", 0.6, 0.0, 1.0, 0.01));
      this.inventorySync = this.add(new BooleanSetting("InventorySync", false));
      this.swingMode = this.add(new EnumSetting("SwingMode", SwingSide.Server));
      this.obsMode = this.add(new BooleanSetting("OBSServer", false));
      INSTANCE = this;
   }

   public static double getOffset() {
      return INSTANCE != null ? INSTANCE.boxSize.getValue() / 2.0 : 0.3;
   }

   public void enable() {
      this.state = true;
   }

   public void disable() {
      this.state = true;
   }

   public boolean isOn() {
      return true;
   }
}

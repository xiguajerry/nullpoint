package me.nullpoint;

import java.lang.invoke.MethodHandles;
import me.nullpoint.api.events.eventbus.EventBus;
import me.nullpoint.api.managers.AltManager;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.managers.ConfigManager;
import me.nullpoint.api.managers.FPSManager;
import me.nullpoint.api.managers.FriendManager;
import me.nullpoint.api.managers.GuiManager;
import me.nullpoint.api.managers.MineManager;
import me.nullpoint.api.managers.ModuleManager;
import me.nullpoint.api.managers.PopManager;
import me.nullpoint.api.managers.RotateManager;
import me.nullpoint.api.managers.ServerManager;
import me.nullpoint.api.managers.ShaderManager;
import me.nullpoint.api.managers.SpeedManager;
import me.nullpoint.api.managers.TimerManager;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import net.fabricmc.api.ModInitializer;

public final class Nullpoint implements ModInitializer {
   public static final String LOG_NAME = "NullPoint";
   public static final String VERSION = "v2.1.3";
   public static String PREFIX = ";";
   public static final EventBus EVENT_BUS = new EventBus();
   public static ModuleManager MODULE;
   public static CommandManager COMMAND;
   public static AltManager ALT;
   public static GuiManager GUI;
   public static ConfigManager CONFIG;
   public static RotateManager ROTATE;
   public static MineManager BREAK;
   public static PopManager POP;
   public static SpeedManager SPEED;
   public static FriendManager FRIEND;
   public static TimerManager TIMER;
   public static ShaderManager SHADER;
   public static FPSManager FPS;
   public static ServerManager SERVER;
   public static boolean loaded = false;

   public void onInitialize() {
      load();
   }

   public static void update() {
      MODULE.onUpdate();
      GUI.update();
      POP.update();
   }

   public static void load() {
      System.out.println("[NullPoint] Starting Client");
      System.out.println("[NullPoint] Register eventbus");
      EVENT_BUS.registerLambdaFactory("me.nullpoint", (lookupInMethod, klass) -> {
         return (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup());
      });
      System.out.println("[NullPoint] Reading Settings");
      CONFIG = new ConfigManager();
      PREFIX = CONFIG.getString("prefix", ";");
      System.out.println("[NullPoint] Initializing Modules");
      MODULE = new ModuleManager();
      System.out.println("[NullPoint] Initializing Commands");
      COMMAND = new CommandManager();
      System.out.println("[NullPoint] Initializing GUI");
      GUI = new GuiManager();
      System.out.println("[NullPoint] Loading Alts");
      ALT = new AltManager();
      System.out.println("[NullPoint] Loading Friends");
      FRIEND = new FriendManager();
      System.out.println("[NullPoint] Loading RunManager");
      ROTATE = new RotateManager();
      System.out.println("[NullPoint] Loading BreakManager");
      BREAK = new MineManager();
      System.out.println("[NullPoint] Loading PopManager");
      POP = new PopManager();
      System.out.println("[NullPoint] Loading TimerManager");
      TIMER = new TimerManager();
      System.out.println("[NullPoint] Loading ShaderManager");
      SHADER = new ShaderManager();
      System.out.println("[NullPoint] Loading FPSManager");
      FPS = new FPSManager();
      System.out.println("[NullPoint] Loading ServerManager");
      SERVER = new ServerManager();
      System.out.println("[NullPoint] Loading SpeedManager");
      SPEED = new SpeedManager();
      System.out.println("[NullPoint] Loading Settings");
      CONFIG.loadSettings();
      System.out.println("[NullPoint] Initialized and ready to play!");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         if (loaded) {
            save();
         }

      }));
      loaded = true;
   }

   public static void unload() {
      loaded = false;
      if (AutoCrystal.thread != null && AutoCrystal.thread.isAlive()) {
         AutoCrystal.thread.stop();
      }

      System.out.println("[NullPoint] Unloading..");
      EVENT_BUS.listenerMap.clear();
      ConfigManager.resetModule();
      CONFIG = null;
      MODULE = null;
      COMMAND = null;
      GUI = null;
      ALT = null;
      FRIEND = null;
      ROTATE = null;
      POP = null;
      TIMER = null;
      System.out.println("[NullPoint] Unloading success!");
   }

   public static void save() {
      System.out.println("[NullPoint] Saving...");
      CONFIG.saveSettings();
      FRIEND.saveFriends();
      ALT.saveAlts();
      System.out.println("[NullPoint] Saving success!");
   }
}

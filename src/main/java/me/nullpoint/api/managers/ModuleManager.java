/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.util.math.MatrixStack
 *  org.lwjgl.opengl.GL11
 */
package me.nullpoint.api.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.Mod;
import me.nullpoint.mod.gui.clickgui.ClickGuiScreen;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import me.nullpoint.mod.modules.impl.client.ChatTranslator;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import me.nullpoint.mod.modules.impl.client.HUD;
import me.nullpoint.mod.modules.impl.client.Indicator;
import me.nullpoint.mod.modules.impl.client.ModuleList;
import me.nullpoint.mod.modules.impl.client.UIModule;
import me.nullpoint.mod.modules.impl.combat.AnchorAssist;
import me.nullpoint.mod.modules.impl.combat.AnchorAura;
import me.nullpoint.mod.modules.impl.combat.AntiPiston;
import me.nullpoint.mod.modules.impl.combat.AntiRegear;
import me.nullpoint.mod.modules.impl.combat.AntiWeakness;
import me.nullpoint.mod.modules.impl.combat.Aura;
import me.nullpoint.mod.modules.impl.combat.AutoAnchor;
import me.nullpoint.mod.modules.impl.combat.AutoCity;
import me.nullpoint.mod.modules.impl.combat.AutoCrystal;
import me.nullpoint.mod.modules.impl.combat.AutoEXP;
import me.nullpoint.mod.modules.impl.combat.AutoPot;
import me.nullpoint.mod.modules.impl.combat.AutoRegear;
import me.nullpoint.mod.modules.impl.combat.AutoTotem;
import me.nullpoint.mod.modules.impl.combat.AutoTrap;
import me.nullpoint.mod.modules.impl.combat.BedAura;
import me.nullpoint.mod.modules.impl.combat.Blocker;
import me.nullpoint.mod.modules.impl.combat.Burrow;
import me.nullpoint.mod.modules.impl.combat.BurrowAssist;
import me.nullpoint.mod.modules.impl.combat.Criticals;
import me.nullpoint.mod.modules.impl.combat.FeetTrap;
import me.nullpoint.mod.modules.impl.combat.HoleFiller;
import me.nullpoint.mod.modules.impl.combat.HoleKick;
import me.nullpoint.mod.modules.impl.combat.PistonCrystal;
import me.nullpoint.mod.modules.impl.combat.Quiver;
import me.nullpoint.mod.modules.impl.combat.SelfFlatten;
import me.nullpoint.mod.modules.impl.combat.SilentDouble;
import me.nullpoint.mod.modules.impl.combat.TPAura;
import me.nullpoint.mod.modules.impl.combat.WebAura;
import me.nullpoint.mod.modules.impl.exploit.AntiHunger;
import me.nullpoint.mod.modules.impl.exploit.AutoDupe;
import me.nullpoint.mod.modules.impl.exploit.Blink;
import me.nullpoint.mod.modules.impl.exploit.Clip;
import me.nullpoint.mod.modules.impl.exploit.FakeLag;
import me.nullpoint.mod.modules.impl.exploit.ForceSync;
import me.nullpoint.mod.modules.impl.exploit.HitboxDesync;
import me.nullpoint.mod.modules.impl.exploit.MineTweak;
import me.nullpoint.mod.modules.impl.exploit.PacketControl;
import me.nullpoint.mod.modules.impl.exploit.RaytraceBypass;
import me.nullpoint.mod.modules.impl.exploit.ServerLagger;
import me.nullpoint.mod.modules.impl.miscellaneous.AntiSpam;
import me.nullpoint.mod.modules.impl.miscellaneous.AutoEZ;
import me.nullpoint.mod.modules.impl.miscellaneous.AutoSpam;
import me.nullpoint.mod.modules.impl.miscellaneous.BaseFinder;
import me.nullpoint.mod.modules.impl.miscellaneous.BedCrafter;
import me.nullpoint.mod.modules.impl.miscellaneous.ChatEncrypter;
import me.nullpoint.mod.modules.impl.miscellaneous.ChatSuffix;
import me.nullpoint.mod.modules.impl.miscellaneous.ExceptionPatcher;
import me.nullpoint.mod.modules.impl.miscellaneous.FakePlayer;
import me.nullpoint.mod.modules.impl.miscellaneous.MCF;
import me.nullpoint.mod.modules.impl.miscellaneous.MCP;
import me.nullpoint.mod.modules.impl.miscellaneous.NoServerRP;
import me.nullpoint.mod.modules.impl.miscellaneous.NoSoundLag;
import me.nullpoint.mod.modules.impl.miscellaneous.Ping;
import me.nullpoint.mod.modules.impl.miscellaneous.PopCounter;
import me.nullpoint.mod.modules.impl.miscellaneous.PortalGui;
import me.nullpoint.mod.modules.impl.miscellaneous.ShulkerViewer;
import me.nullpoint.mod.modules.impl.miscellaneous.SilentDisconnect;
import me.nullpoint.mod.modules.impl.miscellaneous.Timer;
import me.nullpoint.mod.modules.impl.movement.AntiVoid;
import me.nullpoint.mod.modules.impl.movement.AutoWalk;
import me.nullpoint.mod.modules.impl.movement.BlockStrafe;
import me.nullpoint.mod.modules.impl.movement.ElytraFly;
import me.nullpoint.mod.modules.impl.movement.ElytraFlyBypass;
import me.nullpoint.mod.modules.impl.movement.ElytraFlyPlus;
import me.nullpoint.mod.modules.impl.movement.EntityControl;
import me.nullpoint.mod.modules.impl.movement.FastFall;
import me.nullpoint.mod.modules.impl.movement.FastWeb;
import me.nullpoint.mod.modules.impl.movement.Flight;
import me.nullpoint.mod.modules.impl.movement.HoleSnap;
import me.nullpoint.mod.modules.impl.movement.InventoryMove;
import me.nullpoint.mod.modules.impl.movement.NoSlow;
import me.nullpoint.mod.modules.impl.movement.PacketFly;
import me.nullpoint.mod.modules.impl.movement.SafeWalk;
import me.nullpoint.mod.modules.impl.movement.Speed;
import me.nullpoint.mod.modules.impl.movement.Sprint;
import me.nullpoint.mod.modules.impl.movement.Step;
import me.nullpoint.mod.modules.impl.movement.Strafe;
import me.nullpoint.mod.modules.impl.movement.TickShift;
import me.nullpoint.mod.modules.impl.movement.Velocity;
import me.nullpoint.mod.modules.impl.player.AutoArmor;
import me.nullpoint.mod.modules.impl.player.AutoRespawn;
import me.nullpoint.mod.modules.impl.player.BowBomb;
import me.nullpoint.mod.modules.impl.player.FastUse;
import me.nullpoint.mod.modules.impl.player.FreeCam;
import me.nullpoint.mod.modules.impl.player.NoFall;
import me.nullpoint.mod.modules.impl.player.NoRotateSet;
import me.nullpoint.mod.modules.impl.player.NoSwap;
import me.nullpoint.mod.modules.impl.player.PacketEat;
import me.nullpoint.mod.modules.impl.player.PearlClip;
import me.nullpoint.mod.modules.impl.player.Reach;
import me.nullpoint.mod.modules.impl.player.Replenish;
import me.nullpoint.mod.modules.impl.player.Scaffold;
import me.nullpoint.mod.modules.impl.player.SpeedMine;
import me.nullpoint.mod.modules.impl.player.SpinBot;
import me.nullpoint.mod.modules.impl.player.XCarry;
import me.nullpoint.mod.modules.impl.player.freelook.FreeLook;
import me.nullpoint.mod.modules.impl.render.Ambience;
import me.nullpoint.mod.modules.impl.render.AspectRatio;
import me.nullpoint.mod.modules.impl.render.BlockHighLight;
import me.nullpoint.mod.modules.impl.render.BlockerESP;
import me.nullpoint.mod.modules.impl.render.BreakESP;
import me.nullpoint.mod.modules.impl.render.CameraClip;
import me.nullpoint.mod.modules.impl.render.Crosshair;
import me.nullpoint.mod.modules.impl.render.CrystalChams;
import me.nullpoint.mod.modules.impl.render.CrystalPlaceESP;
import me.nullpoint.mod.modules.impl.render.CustomFov;
import me.nullpoint.mod.modules.impl.render.DesyncESP;
import me.nullpoint.mod.modules.impl.render.ESP;
import me.nullpoint.mod.modules.impl.render.HitMarker;
import me.nullpoint.mod.modules.impl.render.HotbarAnimation;
import me.nullpoint.mod.modules.impl.render.LogoutSpots;
import me.nullpoint.mod.modules.impl.render.NameTags;
import me.nullpoint.mod.modules.impl.render.NoInterp;
import me.nullpoint.mod.modules.impl.render.NoRender;
import me.nullpoint.mod.modules.impl.render.PlaceRender;
import me.nullpoint.mod.modules.impl.render.PopChams;
import me.nullpoint.mod.modules.impl.render.Shader;
import me.nullpoint.mod.modules.impl.render.SwingModifer;
import me.nullpoint.mod.modules.impl.render.TotemAnimation;
import me.nullpoint.mod.modules.impl.render.TotemParticle;
import me.nullpoint.mod.modules.impl.render.Trajectories;
import me.nullpoint.mod.modules.impl.render.TwoDESP;
import me.nullpoint.mod.modules.impl.render.ViewModel;
import me.nullpoint.mod.modules.impl.render.Zoom;
import me.nullpoint.mod.modules.impl.render.skybox.Skybox;
import me.nullpoint.mod.modules.settings.Setting;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public class ModuleManager
implements Wrapper {
    public final ArrayList<Module> modules = new ArrayList();
    public final HashMap<Module.Category, Integer> categoryModules = new HashMap();
    public static Mod lastLoadMod;

    public ModuleManager() {
        this.addModule(new Flight());
        this.addModule(new MineTweak());
        this.addModule(new AutoRespawn());
        this.addModule(new AutoAnchor());
        this.addModule(new AutoArmor());
        this.addModule(new HoleFiller());
        this.addModule(new FastWeb());
        this.addModule(new WebAura());
        this.addModule(new XCarry());
        this.addModule(new ElytraFly());
        this.addModule(new PlaceRender());
        this.addModule(new BowBomb());
        this.addModule(new SilentDouble());
        this.addModule(new AntiWeakness());
        this.addModule(new AutoEZ());
        this.addModule(new AntiSpam());
        this.addModule(new ChatEncrypter());
        this.addModule(new ElytraFlyBypass());
        this.addModule(new ElytraFlyPlus());
        this.addModule(new PacketFly());
        this.addModule(new AutoSpam());
        this.addModule(new AutoCity());
        this.addModule(new EntityControl());
        this.addModule(new AutoDupe());
        this.addModule(new SilentDisconnect());
        this.addModule(new HoleSnap());
        this.addModule(new NoServerRP());
        this.addModule(new PacketEat());
        this.addModule(new Step());
        this.addModule(new Shader());
        this.addModule(new ServerLagger());
        this.addModule(new Strafe());
        this.addModule(new BurrowAssist());
        this.addModule(new Zoom());
        this.addModule(new PearlClip());
        this.addModule(new Scaffold());
        this.addModule(new BedAura());
        this.addModule(new DesyncESP());
        this.addModule(new RaytraceBypass());
        this.addModule(new CrystalChams());
        this.addModule(new NoRender());
        this.addModule(new FastUse());
        this.addModule(new ModuleList());
        this.addModule(new Reach());
        this.addModule(new HotbarAnimation());
        this.addModule(new PopCounter());
        this.addModule(new SpeedMine());
        this.addModule(new UIModule());
        this.addModule(new CameraClip());
        this.addModule(new CustomFov());
        this.addModule(new ChatSuffix());
        this.addModule(new FastFall());
        this.addModule(new AutoTrap());
        this.addModule(new AspectRatio());
        this.addModule(new PacketControl());
        this.addModule(new BlockHighLight());
        this.addModule(new BlockerESP());
        this.addModule(new Blocker());
        this.addModule(new HUD());
        this.addModule(new PopChams());
        this.addModule(new Burrow());
        this.addModule(new AutoCrystal());
        this.addModule(new Sprint());
        this.addModule(new TickShift());
        this.addModule(new AutoEXP());
        this.addModule(new AntiPiston());
        this.addModule(new AntiRegear());
        this.addModule(new Blink());
        this.addModule(new NameTags());
        this.addModule(new HoleKick());
        this.addModule(new NoSwap());
        this.addModule(new Velocity());
        this.addModule(new Trajectories());
        this.addModule(new AntiHunger());
        this.addModule(new LogoutSpots());
        this.addModule(new PortalGui());
        this.addModule(new CombatSetting());
        this.addModule(new SelfFlatten());
        this.addModule(new NoInterp());
        this.addModule(new AntiVoid());
        this.addModule(new HitMarker());
        this.addModule(new TotemAnimation());
        this.addModule(new Crosshair());
        this.addModule(new Indicator());
        this.addModule(new Speed());
        this.addModule(new ExceptionPatcher());
        this.addModule(new ViewModel());
        this.addModule(new AnchorAssist());
        this.addModule(new SwingModifer());
        this.addModule(new NoSoundLag());
        this.addModule(new CrystalPlaceESP());
        this.addModule(new InventoryMove());
        this.addModule(new FakePlayer());
        this.addModule(new FreeCam());
        this.addModule(new Ping());
        this.addModule(new FeetTrap());
        this.addModule(new Aura());
        this.addModule(new TPAura());
        this.addModule(new ESP());
        this.addModule(new Criticals());
        this.addModule(new ShulkerViewer());
        this.addModule(new AutoTotem());
        this.addModule(new Quiver());
        this.addModule(new AutoWalk());
        this.addModule(new SpinBot());
        this.addModule(new AutoRegear());
        this.addModule(new PistonCrystal());
        this.addModule(new SafeWalk());
        this.addModule(new MCF());
        this.addModule(new Ambience());
        this.addModule(new AnchorAura());
        this.addModule(new Skybox());
        this.addModule(new NoSlow());
        this.addModule(new ForceSync());
        this.addModule(new NoFall());
        this.addModule(new NoRotateSet());
        this.addModule(new Clip());
        this.addModule(new MCP());
        this.addModule(new ChatSetting());
        this.addModule(new ChatTranslator());
        this.addModule(new BlockStrafe());
        this.addModule(new BedCrafter());
        this.addModule(new Timer());
        this.addModule(new BaseFinder());
        this.addModule(new TotemParticle());
        this.addModule(new AutoPot());
        this.addModule(new TwoDESP());
        this.addModule(new BreakESP());
        this.addModule(new FakeLag());
        this.addModule(new FreeLook());
        this.addModule(new HitboxDesync());
        this.addModule(new Replenish());
        this.modules.sort(Comparator.comparing(Mod::getName));
    }

    public boolean setBind(int eventKey) {
        if (eventKey == -1 || eventKey == 0) {
            return false;
        }
        AtomicBoolean set = new AtomicBoolean(false);
        this.modules.forEach(module -> {
            for (Setting setting : module.getSettings()) {
                BindSetting bind;
                if (!(setting instanceof BindSetting) || !(bind = (BindSetting)setting).isListening()) continue;
                bind.setKey(eventKey);
                bind.setListening(false);
                if (bind.getBind().equals("DELETE")) {
                    bind.setKey(-1);
                }
                set.set(true);
            }
        });
        return set.get();
    }

    public void onKeyReleased(int eventKey) {
        if (eventKey == -1 || eventKey == 0) {
            return;
        }
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && module.getBind().isHoldEnable() && module.getBind().hold) {
                module.toggle();
                module.getBind().hold = false;
            }
            module.getSettings().stream().filter(setting -> setting instanceof BindSetting).map(setting -> (BindSetting)setting).filter(bindSetting -> bindSetting.getKey() == eventKey).forEach(bindSetting -> bindSetting.setPressed(false));
        });
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == -1 || eventKey == 0 || ModuleManager.mc.currentScreen instanceof ClickGuiScreen) {
            return;
        }
        this.modules.forEach(module -> {
            if (module.getBind().getKey() == eventKey && ModuleManager.mc.currentScreen == null) {
                module.toggle();
                module.getBind().hold = true;
            }
            module.getSettings().stream().filter(setting -> setting instanceof BindSetting).map(setting -> (BindSetting)setting).filter(bindSetting -> bindSetting.getKey() == eventKey).forEach(bindSetting -> bindSetting.setPressed(true));
        });
    }

    public void onUpdate() {
        this.modules.stream().filter(Module::isOn).forEach(module -> {
            try {
                module.onUpdate();
            }
            catch (Exception e) {
                e.printStackTrace();
                CommandManager.sendChatMessage("\u00a74[!] " + e.getMessage());
            }
        });
    }

    public void onLogin() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onLogin);
    }

    public void onLogout() {
        this.modules.stream().filter(Module::isOn).forEach(Module::onLogout);
    }

    public void render2D(DrawContext drawContext) {
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender2D(drawContext, MinecraftClient.getInstance().getTickDelta()));
    }

    public void render3D(MatrixStack matrixStack) {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glEnable(2884);
        GL11.glDisable(2929);
        matrixStack.push();
        this.modules.stream().filter(Module::isOn).forEach(module -> module.onRender3D(matrixStack, mc.getTickDelta()));
        Nullpoint.EVENT_BUS.post(new Render3DEvent(matrixStack, mc.getTickDelta()));
        matrixStack.pop();
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public void addModule(Module module) {
        module.add(module.getBind());
        this.modules.add(module);
        this.categoryModules.put(module.getCategory(), this.categoryModules.getOrDefault(module.getCategory(), 0) + 1);
    }

    public void disableAll() {
        for (Module module : this.modules) {
            module.disable();
        }
    }

    public Module getModuleByName(String string) {
        for (Module module : this.modules) {
            if (!module.getName().equalsIgnoreCase(string)) continue;
            return module;
        }
        return null;
    }
}


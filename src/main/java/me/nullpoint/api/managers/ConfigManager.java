/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  org.apache.commons.io.IOUtils
 */
package me.nullpoint.api.managers;

import com.google.common.base.Splitter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.gui.clickgui.tabs.ClickGuiTab;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.Setting;
import me.nullpoint.mod.modules.settings.impl.BindSetting;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import me.nullpoint.mod.modules.settings.impl.EnumSetting;
import me.nullpoint.mod.modules.settings.impl.SliderSetting;
import me.nullpoint.mod.modules.settings.impl.StringSetting;
import org.apache.commons.io.IOUtils;

public class ConfigManager
implements Wrapper {
    public static File options = new File(ConfigManager.mc.runDirectory, "nullpoint_options.txt");
    private final Hashtable<String, String> settings = new Hashtable();

    public ConfigManager() {
        this.readSettings();
    }

    public static void resetModule() {
        for (Module module : Nullpoint.MODULE.modules) {
            module.setState(false);
        }
    }

    public void loadSettings() {
        for (Module module : Nullpoint.MODULE.modules) {
            for (Setting setting : module.getSettings()) {
                setting.loadSetting();
            }
            module.setState(Nullpoint.CONFIG.getBoolean(module.getName() + "_state", false));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void saveSettings() {
        PrintWriter printwriter = null;
        try {
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options), StandardCharsets.UTF_8));
            printwriter.println("prefix:" + Nullpoint.PREFIX);
            for (ClickGuiTab tab : Nullpoint.GUI.tabs) {
                printwriter.println(tab.getTitle() + "_x:" + tab.getX());
                printwriter.println(tab.getTitle() + "_y:" + tab.getY());
            }
            printwriter.println("armor_x:" + Nullpoint.GUI.armorHud.getX());
            printwriter.println("armor_y:" + Nullpoint.GUI.armorHud.getY());
            for (Module module : Nullpoint.MODULE.modules) {
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting bs) {
                        printwriter.println(bs.getLine() + ":" + bs.getValue());
                        continue;
                    }
                    if (setting instanceof SliderSetting ss) {
                        printwriter.println(ss.getLine() + ":" + ss.getValue());
                        continue;
                    }
                    if (setting instanceof BindSetting bs) {
                        printwriter.println(bs.getLine() + ":" + bs.getKey());
                        printwriter.println(bs.getLine() + "_hold:" + bs.isHoldEnable());
                        continue;
                    }
                    if (setting instanceof EnumSetting es) {
                        printwriter.println(es.getLine() + ":" + es.getValue().name());
                        continue;
                    }
                    if (setting instanceof ColorSetting cs) {
                        printwriter.println(cs.getLine() + ":" + cs.getValue().getRGB());
                        printwriter.println(cs.getLine() + "Rainbow:" + cs.isRainbow);
                        if (!cs.injectBoolean) continue;
                        printwriter.println(cs.getLine() + "Boolean:" + cs.booleanValue);
                        continue;
                    }
                    if (!(setting instanceof StringSetting ss)) continue;
                    printwriter.println(ss.getLine() + ":" + ss.getValue());
                }
                printwriter.println(module.getName() + "_state:" + module.isOn());
            }
            IOUtils.closeQuietly(printwriter);
        }
        catch (Exception exception) {
            System.out.println("[NullPoint] Failed to save settings");
        }
        finally {
            IOUtils.closeQuietly(printwriter);
        }
    }

    public void readSettings() {
        Splitter COLON_SPLITTER = Splitter.on(':');
        try {
            if (!options.exists()) {
                return;
            }
            List<String> list = IOUtils.readLines(new FileInputStream(options), StandardCharsets.UTF_8);
            for (String s : list) {
                try {
                    Iterator<String> iterator = COLON_SPLITTER.limit(2).split(s).iterator();
                    this.settings.put(iterator.next(), iterator.next());
                }
                catch (Exception var10) {
                    System.out.println("Skipping bad option: " + s);
                }
            }
        }
        catch (Exception exception) {
            System.out.println("[NullPoint] Failed to load settings");
        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static boolean isFloat(String str) {
        String pattern = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$";
        return str.matches(pattern);
    }

    public int getInt(String setting, int defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !ConfigManager.isInteger(s)) {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    public float getFloat(String setting, float defaultValue) {
        String s = this.settings.get(setting);
        if (s == null || !ConfigManager.isFloat(s)) {
            return defaultValue;
        }
        return Float.parseFloat(s);
    }

    public boolean getBoolean(String setting) {
        String s = this.settings.get(setting);
        return Boolean.parseBoolean(s);
    }

    public boolean getBoolean(String setting, boolean defaultValue) {
        if (this.settings.get(setting) != null) {
            String s = this.settings.get(setting);
            return Boolean.parseBoolean(s);
        }
        return defaultValue;
    }

    public String getString(String setting) {
        return this.settings.get(setting);
    }

    public String getString(String setting, String defaultValue) {
        if (this.settings.get(setting) == null) {
            return defaultValue;
        }
        return this.settings.get(setting);
    }
}


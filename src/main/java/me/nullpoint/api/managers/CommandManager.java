/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 */
package me.nullpoint.api.managers;

import java.lang.reflect.Field;
import java.util.HashMap;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.interfaces.IChatHud;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.mod.commands.Command;
import me.nullpoint.mod.commands.impl.AimCommand;
import me.nullpoint.mod.commands.impl.BindCommand;
import me.nullpoint.mod.commands.impl.ClipCommand;
import me.nullpoint.mod.commands.impl.FriendCommand;
import me.nullpoint.mod.commands.impl.GamemodeCommand;
import me.nullpoint.mod.commands.impl.HelpCommand;
import me.nullpoint.mod.commands.impl.LoadCommand;
import me.nullpoint.mod.commands.impl.PrefixCommand;
import me.nullpoint.mod.commands.impl.RejoinCommand;
import me.nullpoint.mod.commands.impl.ReloadAllCommand;
import me.nullpoint.mod.commands.impl.ReloadCommand;
import me.nullpoint.mod.commands.impl.SaveCommand;
import me.nullpoint.mod.commands.impl.TeleportCommand;
import me.nullpoint.mod.commands.impl.Toggle2Command;
import me.nullpoint.mod.commands.impl.ToggleCommand;
import me.nullpoint.mod.commands.impl.WatermarkCommand;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.impl.client.ChatSetting;
import net.minecraft.text.Text;

public class CommandManager
implements Wrapper {
    public static final String syncCode = "\u00a7(";
    private final HashMap<String, Command> commands = new HashMap();
    public final AimCommand aim = new AimCommand();
    public final BindCommand bind = new BindCommand();
    public final ClipCommand clip = new ClipCommand();
    public final FriendCommand friend = new FriendCommand();
    public final GamemodeCommand gamemode = new GamemodeCommand();
    public final HelpCommand help = new HelpCommand();
    public final PrefixCommand prefix = new PrefixCommand();
    public final LoadCommand load = new LoadCommand();
    public final RejoinCommand rejoin = new RejoinCommand();
    public final ReloadCommand reload = new ReloadCommand();
    public final ReloadAllCommand reloadHack = new ReloadAllCommand();
    public final SaveCommand save = new SaveCommand();
    public final TeleportCommand tp = new TeleportCommand();
    public final Toggle2Command t = new Toggle2Command();
    public final ToggleCommand toggle = new ToggleCommand();
    public final WatermarkCommand watermark = new WatermarkCommand();

    public CommandManager() {
        try {
            for (Field field : CommandManager.class.getDeclaredFields()) {
                if (!Command.class.isAssignableFrom(field.getType())) continue;
                Command cmd = (Command)field.get(this);
                this.commands.put(cmd.getName(), cmd);
            }
        }
        catch (Exception e) {
            System.out.println("Error initializing NullPoint commands.");
            System.out.println(e.getStackTrace().toString());
        }
    }

    public Command getCommandBySyntax(String string) {
        return this.commands.get(string);
    }

    public HashMap<String, Command> getCommands() {
        return this.commands;
    }

    public int getNumOfCommands() {
        return this.commands.size();
    }

    public void command(String[] commandIn) {
        Command command = this.commands.get(commandIn[0].substring(Nullpoint.PREFIX.length()).toLowerCase());
        if (command == null) {
            CommandManager.sendChatMessage("\u00a7c[!] \u00a7fInvalid Command! Type \u00a7ehelp \u00a7ffor a list of commands.");
        } else {
            String[] parameterList = new String[commandIn.length - 1];
            System.arraycopy(commandIn, 1, parameterList, 0, commandIn.length - 1);
            if (parameterList.length == 1 && parameterList[0].equals("help")) {
                command.sendUsage();
                return;
            }
            command.runCommand(parameterList);
        }
    }

    public static void sendChatMessage(String message) {
        if (Module.nullCheck()) {
            return;
        }
        String startCode = "";
        String endCode = "";
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Mio) {
            startCode = "[";
            endCode = "]";
        }
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Earth) {
            startCode = "<";
            endCode = ">";
        }
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Custom) {
            startCode = ChatSetting.INSTANCE.start.getValue();
            endCode = ChatSetting.INSTANCE.end.getValue();
        }
        CommandManager.mc.inGameHud.getChatHud().addMessage(Text.of("\u00a7(\u00a7r" + startCode + ChatSetting.INSTANCE.hackName.getValue() + "\u00a7r" + endCode + "\u00a7f " + message));
    }

    public static void sendChatMessageWidthId(String message, int id) {
        if (Module.nullCheck()) {
            return;
        }
        String startCode = "";
        String endCode = "";
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Mio) {
            startCode = "[";
            endCode = "]";
        }
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Earth) {
            startCode = "<";
            endCode = ">";
        }
        if (ChatSetting.INSTANCE.messageCode.getValue() == ChatSetting.code.Custom) {
            startCode = ChatSetting.INSTANCE.start.getValue();
            endCode = ChatSetting.INSTANCE.end.getValue();
        }
        ((IChatHud)CommandManager.mc.inGameHud.getChatHud()).nullpoint_nextgen_master$add(Text.of("\u00a7(\u00a7r" + startCode + ChatSetting.INSTANCE.hackName.getValue() + "\u00a7r" + endCode + "\u00a7f " + message), id);
    }

    public static void sendChatMessageWidthIdNoSync(String message, int id) {
        if (Module.nullCheck()) {
            return;
        }
        ((IChatHud)CommandManager.mc.inGameHud.getChatHud()).nullpoint_nextgen_master$add(Text.of("\u00a7f" + message), id);
    }
}


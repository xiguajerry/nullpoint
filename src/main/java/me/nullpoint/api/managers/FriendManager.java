/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.entity.player.PlayerEntity
 *  org.apache.commons.io.IOUtils
 */
package me.nullpoint.api.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.io.IOUtils;

public class FriendManager
implements Wrapper {
    public static final ArrayList<String> friendList = new ArrayList();

    public FriendManager() {
        this.readFriends();
    }

    public static boolean isFriend(String name) {
        return friendList.contains(name);
    }

    public static void removeFriend(String name) {
        friendList.remove(name);
    }

    public void addFriend(String name) {
        if (!friendList.contains(name)) {
            friendList.add(name);
        }
    }

    public void friend(String name) {
        if (friendList.contains(name)) {
            friendList.remove(name);
        } else {
            friendList.add(name);
        }
    }

    public void readFriends() {
        try {
            File friendFile = new File(FriendManager.mc.runDirectory, "nullpoint_friends.txt");
            if (!friendFile.exists()) {
                throw new IOException("File not found! Could not load friends...");
            }
            List<String> list = IOUtils.readLines(new FileInputStream(friendFile), StandardCharsets.UTF_8);
            for (String s : list) {
                this.addFriend(s);
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void saveFriends() {
        PrintWriter printwriter = null;
        try {
            File friendFile = new File(FriendManager.mc.runDirectory, "nullpoint_friends.txt");
            System.out.println("[NullPoint] Saving Friends");
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(friendFile), StandardCharsets.UTF_8));
            for (String str : friendList) {
                printwriter.println(str);
            }
        }
        catch (Exception exception) {
            System.out.println("[nullpoint] Failed to save friends");
        }
        printwriter.close();
    }

    public void loadFriends() throws IOException {
        String modName = "nullpoint_friends.json";
        Path modPath = Paths.get(modName);
        if (!Files.exists(modPath)) {
            return;
        }
        this.loadPath(modPath);
    }

    private void loadPath(Path path) throws IOException {
        InputStream stream = Files.newInputStream(path);
        try {
            this.loadFile(new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject());
        }
        catch (IllegalStateException e) {
            this.loadFile(new JsonObject());
        }
        stream.close();
    }

    private void loadFile(JsonObject input) {
        for (Map.Entry entry : input.entrySet()) {
            JsonElement element = (JsonElement)entry.getValue();
            try {
                this.addFriend(element.getAsString());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveFriendsOld() throws IOException {
        String modName = "nullpoint_friends.json";
        Path outputFile = Paths.get(modName);
        if (!Files.exists(outputFile)) {
            Files.createFile(outputFile);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this.writeFriends());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile)));
        writer.write(json);
        writer.close();
    }

    public JsonObject writeFriends() {
        JsonObject object = new JsonObject();
        JsonParser jp = new JsonParser();
        for (String str : friendList) {
            try {
                object.add(str.replace(" ", "_"), jp.parse(str.replace(" ", "_")));
            }
            catch (Exception exception) {}
        }
        return object;
    }

    public boolean isFriend(PlayerEntity entity) {
        return FriendManager.isFriend(entity.getName().getString());
    }
}


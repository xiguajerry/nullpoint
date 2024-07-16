/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.session.Session
 *  net.minecraft.client.session.Session$AccountType
 *  org.apache.commons.io.IOUtils
 */
package me.nullpoint.api.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.nullpoint.api.alts.Alt;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.asm.accessors.IMinecraftClient;
import net.minecraft.client.session.Session;
import org.apache.commons.io.IOUtils;

public class AltManager
implements Wrapper {
    private final ArrayList<Alt> alts = new ArrayList();

    public AltManager() {
        this.readAlts();
    }

    public void readAlts() {
        try {
            File altFile = new File(AltManager.mc.runDirectory, "nullpoint_alts.txt");
            if (!altFile.exists()) {
                throw new IOException("File not found! Could not load alts...");
            }
            List<String> list = IOUtils.readLines(new FileInputStream(altFile), StandardCharsets.UTF_8);
            for (String s : list) {
                this.alts.add(new Alt(s));
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void saveAlts() {
        PrintWriter printwriter = null;
        try {
            File altFile = new File(AltManager.mc.runDirectory, "nullpoint_alts.txt");
            System.out.println("[NullPoint] Saving Alts");
            printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(altFile), StandardCharsets.UTF_8));
            for (Alt alt : this.alts) {
                printwriter.println(alt.getEmail());
            }
        }
        catch (Exception exception) {
            System.out.println("[NullPoint] Failed to save alts");
        }
        printwriter.close();
    }

    public void addAlt(Alt alt) {
        this.alts.add(alt);
    }

    public void removeAlt(Alt alt) {
        this.alts.remove(alt);
    }

    public ArrayList<Alt> getAlts() {
        return this.alts;
    }

    public void loginCracked(String alt) {
        try {
            ((IMinecraftClient)mc).setSession(new Session(alt, UUID.fromString("66123666-1234-5432-6666-667563866600"), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loginToken(String name, String token, String uuid) {
        try {
            ((IMinecraftClient)mc).setSession(new Session(name, UUID.fromString(uuid), token, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}


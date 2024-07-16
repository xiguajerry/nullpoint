/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 */
package me.nullpoint.api.managers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.MathUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.api.utils.render.JelloUtil;
import me.nullpoint.mod.modules.impl.client.HUD;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class ServerManager
implements Wrapper {
    private final Timer timeDelay = new Timer();
    private final ArrayDeque<Float> tpsResult = new ArrayDeque(20);
    private long time;
    private long tickTime;
    private float tps;
    boolean worldNull = true;

    public ServerManager() {
        Nullpoint.EVENT_BUS.subscribe(this);
    }

    public float getTPS() {
        return ServerManager.round2(this.tps);
    }

    public float getCurrentTPS() {
        return ServerManager.round2(20.0f * ((float)this.tickTime / 1000.0f));
    }

    public float getTPSFactor() {
        return (float)this.tickTime / 1000.0f;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ChatMessageS2CPacket)) {
            this.timeDelay.reset();
        }
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            if (this.time != 0L) {
                this.tickTime = System.currentTimeMillis() - this.time;
                if (this.tpsResult.size() > 20) {
                    this.tpsResult.poll();
                }
                this.tpsResult.add(Float.valueOf(20.0f * (1000.0f / (float)this.tickTime)));
                float average = 0.0f;
                for (Float value : this.tpsResult) {
                    average += MathUtil.clamp(value.floatValue(), 0.0f, 20.0f);
                }
                this.tps = average / (float)this.tpsResult.size();
            }
            this.time = System.currentTimeMillis();
        }
    }

    public long serverRespondingTime() {
        return this.timeDelay.getPassedTimeMs();
    }

    public boolean isServerNotResponding() {
        return this.timeDelay.passedMs(HUD.INSTANCE.lagTime.getValue());
    }

    public int getPing() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(ServerManager.mc.player.getUuid());
        int ping = playerListEntry == null ? 0 : playerListEntry.getLatency();
        return ping;
    }

    public void run() {
        JelloUtil.updateJello();
        if (this.worldNull && ServerManager.mc.world != null) {
            Nullpoint.MODULE.onLogin();
            this.worldNull = false;
        } else if (!this.worldNull && ServerManager.mc.world == null) {
            Nullpoint.MODULE.onLogout();
            this.worldNull = true;
        }
    }
}


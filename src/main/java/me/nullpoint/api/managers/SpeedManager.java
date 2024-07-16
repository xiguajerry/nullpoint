/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.MathHelper
 */
package me.nullpoint.api.managers;

import java.util.HashMap;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SpeedManager
implements Wrapper {
    public double speedometerCurrentSpeed;
    public final HashMap<PlayerEntity, Double> playerSpeeds = new HashMap();

    public SpeedManager() {
        Nullpoint.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void updateWalking(UpdateWalkingEvent event) {
        this.updateValues();
    }

    public void updateValues() {
        double distTraveledLastTickX = SpeedManager.mc.player.getX() - SpeedManager.mc.player.prevX;
        double distTraveledLastTickZ = SpeedManager.mc.player.getZ() - SpeedManager.mc.player.prevZ;
        this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
        this.updatePlayers();
    }

    public void updatePlayers() {
        for (PlayerEntity player : SpeedManager.mc.world.getPlayers()) {
            if (!((double)SpeedManager.mc.player.distanceTo(player) < 400.0)) continue;
            double distTraveledLastTickX = player.getX() - player.prevX;
            double distTraveledLastTickZ = player.getZ() - player.prevZ;
            double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
            this.playerSpeeds.put(player, playerSpeed);
        }
    }

    public double getPlayerSpeed(PlayerEntity player) {
        if (this.playerSpeeds.get(player) == null) {
            return 0.0;
        }
        return this.turnIntoKpH(this.playerSpeeds.get(player));
    }

    public double getSpeedKpH() {
        double speedometerkphdouble = this.turnIntoKpH(this.speedometerCurrentSpeed);
        speedometerkphdouble = (double)Math.round(10.0 * speedometerkphdouble) / 10.0;
        return speedometerkphdouble;
    }

    public double turnIntoKpH(double input) {
        return (double)MathHelper.sqrt((float)input) * 71.2729367892;
    }
}


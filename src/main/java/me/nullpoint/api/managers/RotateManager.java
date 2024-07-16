/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package me.nullpoint.api.managers;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.Event;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.RotateEvent;
import me.nullpoint.api.events.impl.UpdateWalkingEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.entity.EntityUtil;
import me.nullpoint.api.utils.math.Timer;
import me.nullpoint.asm.accessors.IPlayerMoveC2SPacket;
import me.nullpoint.mod.modules.impl.client.CombatSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotateManager
implements Wrapper {
    public float rotateYaw = 0.0f;
    public float rotatePitch = 0.0f;
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    public static UpdateWalkingEvent lastEvent;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;
    public float lastYaw = 0.0f;
    public float lastPitch = 0.0f;

    public RotateManager() {
        Nullpoint.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority=101)
    public void onRotation(RotateEvent event) {
        if (RotateManager.mc.player == null) {
            return;
        }
        if (directionVec != null && !ROTATE_TIMER.passed((long)(CombatSetting.INSTANCE.rotateTime.getValue() * 1000.0))) {
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
        }
    }

    @EventHandler(priority=-200)
    public void onPacketSend(PacketEvent.Send event) {
        PlayerMoveC2SPacket packet;
        Object t;
        if (RotateManager.mc.player == null || event.isCancelled()) {
            return;
        }
        if (!EntityUtil.rotating && CombatSetting.INSTANCE.rotateSync.getValue() && (t = event.getPacket()) instanceof PlayerMoveC2SPacket) {
            packet = (PlayerMoveC2SPacket)t;
            if (!packet.changesLook()) {
                return;
            }
            float yaw = packet.getYaw(114514.0f);
            float pitch = packet.getPitch(114514.0f);
            if (yaw == RotateManager.mc.player.getYaw() && pitch == RotateManager.mc.player.getPitch()) {
                ((IPlayerMoveC2SPacket)event.getPacket()).setYaw(this.rotateYaw);
                ((IPlayerMoveC2SPacket)event.getPacket()).setPitch(this.rotatePitch);
            }
        }
        if ((t = event.getPacket()) instanceof PlayerMoveC2SPacket) {
            packet = (PlayerMoveC2SPacket)t;
            if (!packet.changesLook()) {
                return;
            }
            this.lastYaw = packet.getYaw(this.lastYaw);
            this.lastPitch = packet.getPitch(this.lastPitch);
            this.setRotation(this.lastYaw, this.lastPitch, false);
        }
    }

    @EventHandler(priority=100)
    public void onReceivePacket(PacketEvent.Receive event) {
        Object t = event.getPacket();
        if (t instanceof PlayerPositionLookS2CPacket packet) {
            this.lastYaw = packet.getYaw();
            this.lastPitch = packet.getPitch();
            this.setRotation(this.lastYaw, this.lastPitch, true);
        }
    }

    @EventHandler
    public void onUpdateWalkingPost(UpdateWalkingEvent event) {
        if (event.getStage() == Event.Stage.Post) {
            this.setRotation(this.lastYaw, this.lastPitch, false);
        }
    }

    public void setRotation(float yaw, float pitch, boolean force) {
        if (RotateManager.mc.player == null) {
            return;
        }
        if (RotateManager.mc.player.age == this.ticksExisted && !force) {
            return;
        }
        this.ticksExisted = RotateManager.mc.player.age;
        prevPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public int getYaw4D() {
        return MathHelper.floor((double)(RotateManager.mc.player.getYaw() * 4.0f / 360.0f) + 0.5) & 3;
    }

    public String getDirection4D(boolean northRed) {
        int yaw = this.getYaw4D();
        if (yaw == 0) {
            return "South (+Z)";
        }
        if (yaw == 1) {
            return "West (-X)";
        }
        if (yaw == 2) {
            return (northRed ? "\u00c2\u00a7c" : "") + "North (-Z)";
        }
        if (yaw == 3) {
            return "East (+X)";
        }
        return "Loading...";
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float offset;
        double zDif;
        float result = offsetIn;
        double xDif = RotateManager.mc.player.getX() - RotateManager.mc.player.prevX;
        if (xDif * xDif + (zDif = RotateManager.mc.player.getZ() - RotateManager.mc.player.prevZ) * zDif > 0.002500000176951289) {
            offset = (float)MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            result = 95.0f < wrap && wrap < 265.0f ? offset - 180.0f : offset;
        }
        if (RotateManager.mc.player.handSwingProgress > 0.0f) {
            result = yaw;
        }
        if ((offset = MathHelper.wrapDegrees(yaw - (result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f))) < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }
        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }
        return result;
    }
}


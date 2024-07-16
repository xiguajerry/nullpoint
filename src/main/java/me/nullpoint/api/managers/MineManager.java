/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 */
package me.nullpoint.api.managers;

import java.util.HashMap;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.utils.Wrapper;
import me.nullpoint.api.utils.math.FadeUtils;
import me.nullpoint.mod.modules.impl.render.BreakESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MineManager
implements Wrapper {
    public final HashMap<Integer, BreakData> breakMap = new HashMap();

    public MineManager() {
        Nullpoint.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        Object t = event.getPacket();
        if (t instanceof BlockBreakingProgressS2CPacket packet) {
            if (packet.getPos() == null) {
                return;
            }
            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (this.breakMap.containsKey(packet.getEntityId()) && this.breakMap.get(Integer.valueOf(packet.getEntityId())).pos.equals(packet.getPos())) {
                return;
            }
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float)breakData.getEntity().getEyePos().squaredDistanceTo(packet.getPos().toCenterPos())) > 8.0f) {
                return;
            }
            this.breakMap.put(packet.getEntityId(), breakData);
        }
    }

    public boolean isMining(BlockPos pos) {
        boolean mining = false;
        for (BreakData breakData : new HashMap<Integer, BreakData>(this.breakMap).values()) {
            if (breakData.getEntity() == null || breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 7.0 || !breakData.pos.equals(pos)) continue;
            mining = true;
            break;
        }
        return mining;
    }

    public static class BreakData {
        public final BlockPos pos;
        public final int entityId;
        public final FadeUtils fade;

        public BreakData(BlockPos pos, int entityId) {
            this.pos = pos;
            this.entityId = entityId;
            this.fade = new FadeUtils((long)BreakESP.INSTANCE.animationTime.getValue());
        }

        public Entity getEntity() {
            if (Wrapper.mc.world == null) {
                return null;
            }
            Entity entity = Wrapper.mc.world.getEntityById(this.entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}


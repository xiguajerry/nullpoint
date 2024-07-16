package me.nullpoint.mod.modules.impl.render;

import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.managers.CommandManager;
import me.nullpoint.api.utils.render.ColorUtil;
import me.nullpoint.api.utils.render.Render3DUtil;
import me.nullpoint.asm.accessors.IEntity;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import me.nullpoint.mod.modules.settings.impl.ColorSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.util.math.Vec3d;

public class LogoutSpots extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", new Color(255, 255, 255, 100)));
   private final BooleanSetting box = this.add(new BooleanSetting("Box", true));
   private final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   private final BooleanSetting text = this.add(new BooleanSetting("Text", true));
   private final BooleanSetting message = this.add(new BooleanSetting("Message", true));
   private final Map playerCache = Maps.newConcurrentMap();
   private final Map logoutCache = Maps.newConcurrentMap();

   public LogoutSpots() {
      super("LogoutSpots", Module.Category.Render);
   }

   @EventHandler
   public void onPacketReceive(PacketEvent.Receive event) {
      Packet var4 = event.getPacket();
      String var10000;
      Iterator var6;
      UUID uuid;
      PlayerEntity player;
      Iterator var9;
      if (var4 instanceof PlayerListS2CPacket packet) {
         if (packet.getActions().contains(Action.ADD_PLAYER)) {
            var9 = packet.getPlayerAdditionEntries().iterator();

            while(var9.hasNext()) {
               PlayerListS2CPacket.Entry addedPlayer = (PlayerListS2CPacket.Entry)var9.next();
               var6 = this.logoutCache.keySet().iterator();

               while(var6.hasNext()) {
                  uuid = (UUID)var6.next();
                  if (uuid.equals(addedPlayer.profile().getId())) {
                     player = (PlayerEntity)this.logoutCache.get(uuid);
                     if (this.message.getValue()) {
                        var10000 = player.getName().getString();
                        CommandManager.sendChatMessage("\u00a7e[!] \u00a7b" + var10000 + " \u00a7alogged back at X: " + (int)player.getX() + " Y: " + (int)player.getY() + " Z: " + (int)player.getZ());
                     }

                     this.logoutCache.remove(uuid);
                  }
               }
            }
         }

         this.playerCache.clear();
      } else {
         var4 = event.getPacket();
         if (var4 instanceof PlayerRemoveS2CPacket packet) {
            var9 = packet.profileIds().iterator();

            while(var9.hasNext()) {
               UUID uuid2 = (UUID)var9.next();
               var6 = this.playerCache.keySet().iterator();

               while(var6.hasNext()) {
                  uuid = (UUID)var6.next();
                  if (uuid.equals(uuid2)) {
                     player = (PlayerEntity)this.playerCache.get(uuid);
                     if (!this.logoutCache.containsKey(uuid)) {
                        if (this.message.getValue()) {
                           var10000 = player.getName().getString();
                           CommandManager.sendChatMessage("\u00a7e[!] \u00a7b" + var10000 + " \u00a7clogged out at X: " + (int)player.getX() + " Y: " + (int)player.getY() + " Z: " + (int)player.getZ());
                        }

                        this.logoutCache.put(uuid, player);
                     }
                  }
               }
            }

            this.playerCache.clear();
         }
      }

   }

   public void onEnable() {
      this.playerCache.clear();
      this.logoutCache.clear();
   }

   public void onUpdate() {
      Iterator var1 = mc.world.getPlayers().iterator();

      while(var1.hasNext()) {
         PlayerEntity player = (PlayerEntity)var1.next();
         if (player != null && !player.equals(mc.player)) {
            this.playerCache.put(player.getGameProfile().getId(), player);
         }
      }

   }

   public void onRender3D(MatrixStack matrixStack, float partialTicks) {
      Iterator var3 = this.logoutCache.keySet().iterator();

      while(var3.hasNext()) {
         UUID uuid = (UUID)var3.next();
         PlayerEntity data = (PlayerEntity)this.logoutCache.get(uuid);
         if (data != null) {
            Render3DUtil.draw3DBox(matrixStack, ((IEntity)data).getDimensions().getBoxAt(data.getPos()), this.color.getValue(), this.outline.getValue(), this.box.getValue());
            if (this.text.getValue()) {
               Render3DUtil.drawText3D(data.getName().getString(), new Vec3d(data.getX(), ((IEntity)data).getDimensions().getBoxAt(data.getPos()).maxY + 0.5, data.getZ()), ColorUtil.injectAlpha(this.color.getValue(), 255));
            }
         }
      }

   }
}

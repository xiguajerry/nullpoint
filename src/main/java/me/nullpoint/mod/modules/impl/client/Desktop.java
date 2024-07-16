package me.nullpoint.mod.modules.impl.client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PacketEvent;
import me.nullpoint.api.events.impl.TotemEvent;
import me.nullpoint.api.managers.FriendManager;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

public class Desktop extends Module {
   private static final InputStream inputStream = Nullpoint.class.getClassLoader().getResourceAsStream("assets/minecraft/icon.png");
   private static final Image image;
   final TrayIcon icon;
   private final BooleanSetting onlyTabbed;
   private final BooleanSetting visualRange;
   private final BooleanSetting selfPop;
   private final BooleanSetting mention;
   private final BooleanSetting dm;
   private final List<PlayerEntity> knownPlayers;
   private List<PlayerEntity> players;

   public Desktop() {
      super("Desktop", "Desktop notifications", Module.Category.Client);
      this.icon = new TrayIcon(image, "NullPoint");
      this.onlyTabbed = this.add(new BooleanSetting("OnlyTabbed", false));
      this.visualRange = this.add(new BooleanSetting("VisualRange", true));
      this.selfPop = this.add(new BooleanSetting("TotemPop", true));
      this.mention = this.add(new BooleanSetting("Mention", true));
      this.dm = this.add(new BooleanSetting("DM", true));
      this.knownPlayers = new ArrayList();
   }

   public void onDisable() {
      this.knownPlayers.clear();
      this.removeIcon();
   }

   public void onEnable() {
      this.addIcon();
   }

   public void onUpdate() {
      if (!nullCheck() && this.visualRange.getValue()) {
         try {
            if (this.onlyTabbed.getValue()) {
               return;
            }
         } catch (Exception var4) {
         }

         this.players = mc.world.getPlayers().stream().filter(Objects::nonNull).collect(Collectors.toList());

         try {
            Iterator var1 = this.players.iterator();

            while(var1.hasNext()) {
               Entity entity = (Entity)var1.next();
               if (entity instanceof PlayerEntity && !entity.getName().equals(mc.player.getName()) && !this.knownPlayers.contains(entity)) {
                  FriendManager var10000 = Nullpoint.FRIEND;
                  if (!FriendManager.isFriend(entity.getName().getString())) {
                     this.knownPlayers.add((PlayerEntity) entity);
                     this.icon.displayMessage("NullPoint", entity.getName() + " has entered your visual range!", MessageType.INFO);
                  }
               }
            }
         } catch (Exception var5) {
         }

         try {
            this.knownPlayers.removeIf((entityx) -> {
               return entityx instanceof PlayerEntity && !entityx.getName().equals(mc.player.getName()) && !this.players.contains(entityx);
            });
         } catch (Exception var3) {
         }

      }
   }

   @EventHandler
   public void onTotemPop(TotemEvent event) {
      if (!nullCheck() && event.getPlayer() == mc.player && this.selfPop.getValue()) {
         this.icon.displayMessage("NullPoint", "You are popping!", MessageType.WARNING);
      }
   }

   @EventHandler
   public void onClientChatReceived(PacketEvent.Receive event) {
      if (!nullCheck()) {
         Packet var3 = event.getPacket();
         if (var3 instanceof GameMessageS2CPacket e) {
             String message = String.valueOf(e.content());
            if (message.contains(mc.player.getName().getString()) && this.mention.getValue()) {
               this.icon.displayMessage("NullPoint", "New chat mention!", MessageType.INFO);
            }

            if (message.contains("whispers:") && this.dm.getValue()) {
               this.icon.displayMessage("NullPoint", "New direct message!", MessageType.INFO);
            }
         }

      }
   }

   private void addIcon() {
      SystemTray tray = SystemTray.getSystemTray();
      this.icon.setImageAutoSize(true);
      this.icon.setToolTip("NullPointv2.1.3");

      try {
         tray.add(this.icon);
      } catch (AWTException var3) {
         AWTException e = var3;
         e.printStackTrace();
      }

   }

   private void removeIcon() {
      SystemTray tray = SystemTray.getSystemTray();
      tray.remove(this.icon);
   }

   static {
      try {
         image = ImageIO.read(inputStream);
      } catch (IOException var1) {
         IOException e = var1;
         throw new RuntimeException(e);
      }
   }
}

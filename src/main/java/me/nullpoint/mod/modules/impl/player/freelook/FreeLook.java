package me.nullpoint.mod.modules.impl.player.freelook;

import me.nullpoint.Nullpoint;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.Render3DEvent;
import me.nullpoint.mod.modules.Module;

public class FreeLook extends Module {
   public static FreeLook INSTANCE;
   private final CameraState camera = new CameraState();

   public FreeLook() {
      super("FreeLook", Module.Category.Player);
      INSTANCE = this;
      Nullpoint.EVENT_BUS.subscribe(new FreeLookUpdate());
   }

   public CameraState getCameraState() {
      return this.camera;
   }

   public class FreeLookUpdate {
      @EventHandler
      public void onRender3D(Render3DEvent event) {
         CameraState camera = FreeLook.this.getCameraState();
         boolean doLock = FreeLook.this.isOn() && !camera.doLock;
         boolean doUnlock = !FreeLook.this.isOn() && camera.doLock;
         if (doLock) {
            if (!camera.doTransition) {
               camera.lookYaw = camera.originalYaw();
               camera.lookPitch = camera.originalPitch();
            }

            camera.doLock = true;
         }

         if (doUnlock) {
            camera.doLock = false;
            camera.doTransition = true;
            camera.transitionInitialYaw = camera.lookYaw;
            camera.transitionInitialPitch = camera.lookPitch;
         }

      }
   }
}

package me.nullpoint.mod.modules.impl.miscellaneous;

import java.util.ArrayList;
import java.util.Iterator;
import me.nullpoint.api.events.eventbus.EventHandler;
import me.nullpoint.api.events.impl.PlaySoundEvent;
import me.nullpoint.mod.modules.Module;
import me.nullpoint.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class NoSoundLag extends Module {
   public static NoSoundLag INSTANCE;
   private final BooleanSetting equip = this.add(new BooleanSetting("ArmorEquip", true));
   private final BooleanSetting explode = this.add(new BooleanSetting("Explode", true));
   private final BooleanSetting attack = this.add(new BooleanSetting("Attack", true));
   static final ArrayList armor = new ArrayList();

   public NoSoundLag() {
      super("NoSoundLag", Module.Category.Misc);
      INSTANCE = this;
   }

   @EventHandler
   public void onPlaySound(PlaySoundEvent event) {
      if (this.equip.getValue()) {
         Iterator var2 = armor.iterator();

         while(var2.hasNext()) {
            SoundEvent se = (SoundEvent)var2.next();
            if (event.sound.getId() == se.getId()) {
               event.cancel();
               return;
            }
         }
      }

      if (this.explode.getValue() && event.sound.getId() == SoundEvents.ENTITY_GENERIC_EXPLODE.getId()) {
         event.cancel();
      } else if (this.attack.getValue() && (event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.getId() || event.sound.getId() == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG.getId())) {
         event.cancel();
      }
   }

   static {
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_TURTLE);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_CHAIN);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_GOLD);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_IRON);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
      armor.add(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
   }
}

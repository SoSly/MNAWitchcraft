package org.sosly.witchcraft.events.entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RitualWitchHandler {
    
    @SubscribeEvent
    public static void onWitchTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Witch witch)) {
            return;
        }
        
        Level level = witch.level();
        if (level.isClientSide()) {
            return;
        }
        
        if (!witch.getPersistentData().getBoolean("mnaw:ritual_witch")) {
            return;
        }
        
        witch.setPos(witch.getX(), witch.getY() + 0.02, witch.getZ());
        witch.setDeltaMovement(0, 0, 0);
        
        long endTime = witch.getPersistentData().getLong("mnaw:ritual_end_time");
        if (endTime > 0 && level.getGameTime() >= endTime) {
            witch.remove(Entity.RemovalReason.DISCARDED);
        }
    }
}
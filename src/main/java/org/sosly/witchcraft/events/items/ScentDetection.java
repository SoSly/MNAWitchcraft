package org.sosly.witchcraft.events.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.utils.ScentedItemHelper;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScentDetection {
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        
        Player player = event.player;
        
        if (player.tickCount % 20 != 0) {
            return;
        }
        
        if (ScentedItemHelper.hasAnyScentedItem(player)) {
            if (!player.hasEffect(EffectRegistry.NICE_SMELL.get())) {
                player.addEffect(new MobEffectInstance(EffectRegistry.NICE_SMELL.get(), 100, 0, false, false));
            } else {
                MobEffectInstance existingEffect = player.getEffect(EffectRegistry.NICE_SMELL.get());
                if (existingEffect != null && existingEffect.getDuration() < 60) {
                    player.addEffect(new MobEffectInstance(EffectRegistry.NICE_SMELL.get(), 100, 0, false, false));
                }
            }
        }
    }
}
package org.sosly.witchcraft.events.client;

import net.minecraft.client.color.item.ItemColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.ItemRegistry;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemColorHandlers {
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor moonlightColor = (stack, tintIndex) -> {
            if (tintIndex == 0) {
                return 0xC0D0E6;
            }
            return -1;
        };
        
        event.register(moonlightColor, ItemRegistry.CONDENSED_MOONLIGHT_BOTTLE.get());
    }
}
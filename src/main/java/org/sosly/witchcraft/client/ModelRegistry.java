package org.sosly.witchcraft.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelRegistry {
    
    @SubscribeEvent
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(Witchcraft.MOD_ID, "item/grimoire_witch_open"));
        event.register(new ResourceLocation(Witchcraft.MOD_ID, "item/grimoire_witch_closed"));
    }
}
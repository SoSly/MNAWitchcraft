package org.sosly.witchcraft.events.client;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.entities.renderers.FlyingBroomEntityRenderer;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EntityClientInit {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(EntityTypeRegistry.FLYING_BROOM.get(), FlyingBroomEntityRenderer::new);
        });
    }
}
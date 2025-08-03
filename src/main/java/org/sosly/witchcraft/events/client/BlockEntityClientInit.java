package org.sosly.witchcraft.events.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.blocks.alchemy.WitchsCauldronRenderer;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BlockEntityClientInit {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(EntityRegistry.WITCHS_CAULDRON.get(), WitchsCauldronRenderer::new);
    }
}
package org.sosly.witchcraft.renderers;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.blocks.alchemy.WitchsCauldronRenderer;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.renderers.entities.FlyingBroomEntityRenderer;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RendererRegistry {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(EntityTypeRegistry.FLYING_BROOM.get(), FlyingBroomEntityRenderer::new);
            BlockEntityRenderers.register(EntityRegistry.WITCHS_CAULDRON.get(), WitchsCauldronRenderer::new);
        });
    }

}
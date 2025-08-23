package org.sosly.witchcraft.fluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.blocks.BlockRegistry;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, Witchcraft.MOD_ID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Witchcraft.MOD_ID);
    
    public static final RegistryObject<FluidType> CONDENSED_MOONLIGHT_TYPE = FLUID_TYPES.register("condensed_moonlight",
            CondensedMoonlightFluidType::new);
    
    public static final RegistryObject<FlowingFluid> CONDENSED_MOONLIGHT_SOURCE = FLUIDS.register("condensed_moonlight",
            () -> new CondensedMoonlightFluid.Source());
    
    public static final RegistryObject<FlowingFluid> CONDENSED_MOONLIGHT_FLOWING = FLUIDS.register("condensed_moonlight_flowing",
            () -> new CondensedMoonlightFluid.Flowing());
    
    public static final RegistryObject<LiquidBlock> CONDENSED_MOONLIGHT_BLOCK = BlockRegistry.BLOCKS.register("condensed_moonlight",
            () -> new LiquidBlock(CONDENSED_MOONLIGHT_SOURCE, BlockBehaviour.Properties.copy(Blocks.WATER)
                    .noCollission()
                    .strength(100.0F)
                    .noLootTable()
                    .lightLevel((state) -> 6)));
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CONDENSED_MOONLIGHT_SOURCE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CONDENSED_MOONLIGHT_FLOWING.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CONDENSED_MOONLIGHT_BLOCK.get(), RenderType.translucent());
    }
}
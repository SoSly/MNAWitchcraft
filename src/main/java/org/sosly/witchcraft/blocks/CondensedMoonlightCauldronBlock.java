package org.sosly.witchcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.sosly.witchcraft.fluids.FluidRegistry;

import java.util.Map;
import java.util.function.Predicate;

public class CondensedMoonlightCauldronBlock extends LayeredCauldronBlock {
    
    public static final Map<Item, CauldronInteraction> CONDENSED_MOONLIGHT_CAULDRON_INTERACTION = CauldronInteraction.newInteractionMap();
    
    public CondensedMoonlightCauldronBlock(Properties properties) {
        super(properties, (precipitation) -> false, CONDENSED_MOONLIGHT_CAULDRON_INTERACTION);
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Emit light based on fill level (similar to the fluid block's light level of 6)
        return state.getValue(LEVEL) * 2; // 2, 4, or 6 light level
    }
    
    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return false; // Moonlight can't drip from stalactites
    }
    
    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        // Do nothing - moonlight isn't affected by rain
    }
}
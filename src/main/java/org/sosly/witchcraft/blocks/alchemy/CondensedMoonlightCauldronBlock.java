package org.sosly.witchcraft.blocks.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.util.Map;

public class CondensedMoonlightCauldronBlock extends LayeredCauldronBlock {
    
    public static final Map<Item, CauldronInteraction> CONDENSED_MOONLIGHT_CAULDRON_INTERACTION = CauldronInteraction.newInteractionMap();
    
    public CondensedMoonlightCauldronBlock(Properties properties) {
        super(properties, (precipitation) -> false, CONDENSED_MOONLIGHT_CAULDRON_INTERACTION);
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LEVEL) * 2;
    }
    
    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return false;
    }
    
    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
    }
}
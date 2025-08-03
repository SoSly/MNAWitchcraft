package org.sosly.witchcraft.blocks.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.blocks.EntityRegistry;

/**
 * Moonlight variant of the Witch's Cauldron.
 * Contains moonlight for brewing and emits light.
 * Can continue collecting moonlight at night until full.
 */
public class MoonlightWitchsCauldronBlock extends AbstractWitchsCauldronBlock {
    
    public MoonlightWitchsCauldronBlock() {
        super();
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LEVEL) * 2;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != EntityRegistry.WITCHS_CAULDRON.get()) {
            return null;
        }
        return (level1, pos, state1, be) -> MoonlightCauldronHelper.moonlightTick(level1, pos, state1, (WitchsCauldronBlockEntity) be);
    }
}
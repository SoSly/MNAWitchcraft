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
 * Water variant of the Witch's Cauldron.
 * Contains water for brewing potions and other alchemical processes.
 */
public class WaterWitchsCauldronBlock extends AbstractWitchsCauldronBlock {
    
    public WaterWitchsCauldronBlock() {
        super();
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != EntityRegistry.WITCHS_CAULDRON.get()) {
            return null;
        }
        return (level1, pos, state1, be) -> WitchsCauldronBlockEntity.tick(level1, pos, state1, (WitchsCauldronBlockEntity) be);
    }
}
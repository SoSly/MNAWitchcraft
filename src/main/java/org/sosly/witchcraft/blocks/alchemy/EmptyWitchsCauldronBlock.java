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
import org.sosly.witchcraft.ServerConfig;

/**
 * Empty variant of the Witch's Cauldron.
 * Can collect moonlight from the night sky when placed outdoors.
 */
public class EmptyWitchsCauldronBlock extends AbstractWitchsCauldronBlock {
    
    public EmptyWitchsCauldronBlock() {
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
        return (level1, pos, state1, be) -> emptyTick(level1, pos, state1, (WitchsCauldronBlockEntity) be);
    }
    
    private static void emptyTick(Level level, BlockPos pos, BlockState state, WitchsCauldronBlockEntity blockEntity) {
        WitchsCauldronBlockEntity.tick(level, pos, state, blockEntity);
        
        if (!level.isClientSide && level.random.nextInt(ServerConfig.witchsCauldronMoonlightCollectionChance) == 0) {
            if (!level.isDay() && level.canSeeSky(pos.above())) {
                blockEntity.setFluid(WitchsCauldronBlockEntity.FluidType.MOONLIGHT, 1);
            }
        }
    }
}
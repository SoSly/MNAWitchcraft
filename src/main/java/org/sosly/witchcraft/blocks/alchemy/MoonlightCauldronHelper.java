package org.sosly.witchcraft.blocks.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.sosly.witchcraft.ServerConfig;

/**
 * Helper class for moonlight cauldron behaviors.
 * Provides moonlight collection logic for moonlight-filled cauldrons.
 */
public class MoonlightCauldronHelper {
    
    public static void moonlightTick(Level level, BlockPos pos, BlockState state, WitchsCauldronBlockEntity blockEntity) {
        WitchsCauldronBlockEntity.tick(level, pos, state, blockEntity);
        
        if (level.isClientSide) {
            return;
        }
        
        tickMoonlightCollection(level, pos, blockEntity);
    }
    
    private static void tickMoonlightCollection(Level level, BlockPos pos, WitchsCauldronBlockEntity blockEntity) {
        if (level.isDay() || !level.canSeeSky(pos.above())) {
            return;
        }
        
        if (blockEntity.getFluidLevel() >= 3) {
            return;
        }
        
        if (level.random.nextInt(ServerConfig.witchsCauldronMoonlightCollectionChance) != 0) {
            return;
        }
        
        blockEntity.setFluidLevel(blockEntity.getFluidLevel() + 1);
        blockEntity.updateBlockStateLevel();
        
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        level.getLightEngine().checkBlock(pos);
    }
}
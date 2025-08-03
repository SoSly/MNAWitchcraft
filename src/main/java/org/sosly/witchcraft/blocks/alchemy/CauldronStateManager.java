package org.sosly.witchcraft.blocks.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.sosly.witchcraft.blocks.BlockRegistry;

/**
 * Manages state transitions between different Witch's Cauldron block variants.
 * 
 * This class handles the complex logic of switching between Empty, Water, and Moonlight
 * cauldron blocks while preserving the BlockEntity data.
 */
public class CauldronStateManager {
    
    /**
     * Switch to the empty cauldron block variant.
     * Preserves BlockEntity data during the transition.
     */
    public static void switchToEmptyBlock(Level level, BlockPos pos, WitchsCauldronBlockEntity currentEntity) {
        if (level.isClientSide) {
            return;
        }
        
        CompoundTag tag = currentEntity.saveWithoutMetadata();
        currentEntity.saveAdditional(tag);
        
        BlockState newState = BlockRegistry.EMPTY_WITCHS_CAULDRON.get().defaultBlockState();
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
        
        BlockEntity newBE = level.getBlockEntity(pos);
        if (newBE instanceof WitchsCauldronBlockEntity newCauldron) {
            newCauldron.load(tag);
        }
    }
    
    /**
     * Switch to the appropriate filled cauldron block variant.
     * Automatically selects between water and moonlight variants based on fluid type.
     */
    public static void switchToFilledBlock(Level level, BlockPos pos, WitchsCauldronBlockEntity currentEntity, 
                                         WitchsCauldronBlockEntity.FluidType fluidType, int fluidLevel) {
        if (level.isClientSide) {
            return;
        }
        
        CompoundTag tag = currentEntity.saveWithoutMetadata();
        currentEntity.saveAdditional(tag);
        
        BlockState newState;
        if (fluidType == WitchsCauldronBlockEntity.FluidType.MOONLIGHT) {
            newState = BlockRegistry.MOONLIGHT_WITCHS_CAULDRON.get().defaultBlockState()
                    .setValue(AbstractWitchsCauldronBlock.LEVEL, Math.max(1, Math.min(3, fluidLevel)));
        } else {
            newState = BlockRegistry.WITCHS_CAULDRON.get().defaultBlockState()
                    .setValue(AbstractWitchsCauldronBlock.LEVEL, Math.max(1, Math.min(3, fluidLevel)));
        }
        
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(newState));
        
        BlockEntity newBE = level.getBlockEntity(pos);
        if (newBE instanceof WitchsCauldronBlockEntity newCauldron) {
            newCauldron.load(tag);
        }
    }
    
    /**
     * Update the LEVEL property of the current cauldron block without changing the block type.
     * Used for fluid level changes within the same block variant.
     */
    public static void updateBlockLevel(Level level, BlockPos pos, BlockState currentState, int fluidLevel) {
        if (level.isClientSide) {
            return;
        }
        
        if (currentState.hasProperty(AbstractWitchsCauldronBlock.LEVEL)) {
            int newLevel = Math.max(1, Math.min(3, fluidLevel));
            if (currentState.getValue(AbstractWitchsCauldronBlock.LEVEL) != newLevel) {
                level.setBlock(pos, currentState.setValue(AbstractWitchsCauldronBlock.LEVEL, newLevel), 3);
            }
        }
    }
}
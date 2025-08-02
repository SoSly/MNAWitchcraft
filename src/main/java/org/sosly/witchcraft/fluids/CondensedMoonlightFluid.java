package org.sosly.witchcraft.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.items.ItemRegistry;

public abstract class CondensedMoonlightFluid extends ForgeFlowingFluid {
    
    protected CondensedMoonlightFluid() {
        super(new Properties(
                FluidRegistry.CONDENSED_MOONLIGHT_TYPE,
                FluidRegistry.CONDENSED_MOONLIGHT_SOURCE,
                FluidRegistry.CONDENSED_MOONLIGHT_FLOWING
        )
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1)
                .block(FluidRegistry.CONDENSED_MOONLIGHT_BLOCK)
                .bucket(ItemRegistry.CONDENSED_MOONLIGHT_BUCKET)
                .tickRate(5)); // Same as water
    }
    
    @Override
    public boolean isSource(@NotNull FluidState state) {
        return false;
    }
    
    @Override
    public int getAmount(@NotNull FluidState state) {
        return state.getValue(LEVEL);
    }
    
    @Override
    protected boolean canConvertToSource(Level level) {
        return false; // No infinite sources
    }
    
    @Override
    protected void beforeDestroyingBlock(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // Default behavior
    }
    
    @Override
    protected int getSlopeFindDistance(@NotNull LevelReader level) {
        return 8; // Same as water
    }
    
    @Override
    protected int getDropOff(@NotNull LevelReader level) {
        return 1;
    }
    
    @Override
    public @NotNull Item getBucket() {
        return ItemRegistry.CONDENSED_MOONLIGHT_BUCKET.get();
    }
    
    @Override
    protected boolean canBeReplacedWith(@NotNull FluidState fluidState, @NotNull BlockGetter blockReader, @NotNull BlockPos pos, @NotNull Fluid fluid, @NotNull Direction direction) {
        return false;
    }
    
    @Override
    public int getTickDelay(@NotNull LevelReader level) {
        return 5; // Same as water
    }
    
    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }
    
    @Override
    protected @NotNull BlockState createLegacyBlock(@NotNull FluidState state) {
        return FluidRegistry.CONDENSED_MOONLIGHT_BLOCK.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    
    public static class Source extends CondensedMoonlightFluid {
        @Override
        public int getAmount(@NotNull FluidState state) {
            return 8;
        }
        
        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }
    }
    
    public static class Flowing extends CondensedMoonlightFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        
        @Override
        public int getAmount(@NotNull FluidState state) {
            return state.getValue(LEVEL);
        }
        
        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }
    }
}
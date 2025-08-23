package org.sosly.witchcraft.blocks.sympathy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.blocks.entities.BoundPoppetEntity;

public class BoundPoppetBlock extends PoppetBlock implements EntityBlock {
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        BoundPoppetEntity be = (BoundPoppetEntity) level.getBlockEntity(pos);
        if (be == null || tag == null) {
            super.setPlacedBy(level, pos, state, placer, stack);
            return;
        }
        
        be.load(tag);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundPoppetEntity(pos, state);
    }
}

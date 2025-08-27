package org.sosly.witchcraft.blocks.sympathy;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.sosly.witchcraft.blocks.entities.BoundPoppetEntity;

public class BoundPoppetBlock extends PoppetBlock implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        BoundPoppetEntity be = (BoundPoppetEntity) level.getBlockEntity(pos);
        if (be == null) {
            LOGGER.error("Failed to get BoundPoppetEntity at {} during block placement", pos);
            super.setPlacedBy(level, pos, state, placer, stack);
            return;
        }
        if (tag == null) {
            LOGGER.warn("BoundPoppetBlock placed without NBT data at {} - creating empty bound poppet", pos);
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

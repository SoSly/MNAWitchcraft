package org.sosly.witchcraft.blocks.entities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.sosly.witchcraft.blocks.EntityRegistry;

import java.util.UUID;

public class BoundPoppetEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    UUID target;
    String type;

    public BoundPoppetEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.BOUND_POPPET.get(), pos, state);
    }

    public UUID target() {
        return target;
    }

    public String type() {
        return type;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (target != null) {
            tag.putUUID("target", target);
        }
        if (type != null) {
            tag.putString("type", type);
        }
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        if (tag.hasUUID("target")) {
            target = tag.getUUID("target");
        } else {
            LOGGER.warn("BoundPoppetEntity at {} loaded without target UUID", worldPosition);
            target = null;
        }
        if (tag.contains("type")) {
            type = tag.getString("type");
        } else {
            LOGGER.warn("BoundPoppetEntity at {} loaded without type string", worldPosition);
            type = null;
        }
        super.load(tag);
    }
}

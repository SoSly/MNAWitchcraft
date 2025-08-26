package org.sosly.witchcraft.capabilities.broom;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;

import javax.annotation.Nullable;
import java.util.UUID;

public class BroomCapability implements IBroomCapability {
    private UUID bondedBroomId = null;
    private boolean broomsUnlocked = false;
    private BlockPos lastKnownPosition = null;
    private ResourceLocation lastKnownDimension = null;

    @Override
    @Nullable
    public UUID getBondedBroomId() {
        return bondedBroomId;
    }

    @Override
    public void setBondedBroomId(@Nullable UUID entityId) {
        this.bondedBroomId = entityId;
    }

    @Override
    public boolean hasBondedBroom() {
        return bondedBroomId != null;
    }

    @Override
    public boolean broomsUnlocked() {
        return broomsUnlocked;
    }

    @Override
    public void setBroomsUnlocked(boolean unlocked) {
        this.broomsUnlocked = unlocked;
    }

    @Override
    @Nullable
    public BlockPos getLastKnownPosition() {
        return lastKnownPosition;
    }

    @Override
    public void setLastKnownPosition(@Nullable BlockPos position) {
        this.lastKnownPosition = position;
    }

    @Override
    @Nullable
    public ResourceLocation getLastKnownDimension() {
        return lastKnownDimension;
    }

    @Override
    public void setLastKnownDimension(@Nullable ResourceLocation dimension) {
        this.lastKnownDimension = dimension;
    }

    @Override
    public void reset() {
        this.bondedBroomId = null;
        this.lastKnownPosition = null;
        this.lastKnownDimension = null;
    }
}
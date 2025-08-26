package org.sosly.witchcraft.api.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.Witchcraft;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IBroomCapability {
    ResourceLocation BROOM_CAPABILITY = new ResourceLocation(Witchcraft.MOD_ID, "broom");

    @Nullable
    UUID getBondedBroomId();

    void setBondedBroomId(@Nullable UUID entityId);

    boolean hasBondedBroom();

    boolean broomsUnlocked();

    void setBroomsUnlocked(boolean unlocked);

    @Nullable
    BlockPos getLastKnownPosition();

    void setLastKnownPosition(@Nullable BlockPos position);

    @Nullable
    ResourceLocation getLastKnownDimension();

    void setLastKnownDimension(@Nullable ResourceLocation dimension);

    void reset();
}
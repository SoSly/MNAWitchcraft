package org.sosly.witchcraft.capabilities.broom;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;

import java.util.UUID;

public class BroomProvider implements ICapabilitySerializable<Tag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Capability<IBroomCapability> BROOM = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<IBroomCapability> holder = LazyOptional.of(BroomCapability::new);

    @Override
    public Tag serializeNBT() {
        IBroomCapability instance = holder.orElse(new BroomCapability());
        CompoundTag nbt = new CompoundTag();
        
        if (instance.getBondedBroomId() != null) {
            nbt.putUUID("bondedBroom", instance.getBondedBroomId());
        }
        if (instance.broomsUnlocked()) {
            nbt.putBoolean("broomsUnlocked", true);
        }
        if (instance.getLastKnownPosition() != null) {
            nbt.putLong("lastKnownPos", instance.getLastKnownPosition().asLong());
        }
        if (instance.getLastKnownDimension() != null) {
            nbt.putString("lastKnownDimension", instance.getLastKnownDimension().toString());
        }
        
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IBroomCapability instance = holder.orElse(new BroomCapability());
        
        if (!(nbt instanceof CompoundTag cnbt)) {
            LOGGER.warn("Invalid NBT type for broom capability deserialization");
            return;
        }
        
        if (cnbt.hasUUID("bondedBroom")) {
            instance.setBondedBroomId(cnbt.getUUID("bondedBroom"));
        }
        instance.setBroomsUnlocked(cnbt.getBoolean("broomsUnlocked"));
        if (cnbt.contains("lastKnownPos")) {
            instance.setLastKnownPosition(BlockPos.of(cnbt.getLong("lastKnownPos")));
        }
        if (cnbt.contains("lastKnownDimension")) {
            instance.setLastKnownDimension(new ResourceLocation(cnbt.getString("lastKnownDimension")));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BROOM.orEmpty(cap, holder);
    }

    @Nullable
    public static IBroomCapability getBroomCapability(Player player) {
        if (player == null) {
            LOGGER.warn("Cannot get broom capability: player is null");
            return null;
        }
        
        LazyOptional<IBroomCapability> cap = player.getCapability(BROOM);
        if (!cap.isPresent()) {
            LOGGER.warn("Player {} missing broom capability", player.getName().getString());
            return null;
        }
        
        return cap.resolve().orElse(null);
    }

    @Nullable
    public static IBroomCapability getBroomCapability(Player player, UUID broomId) {
        IBroomCapability broom = getBroomCapability(player);
        if (broom == null) {
            return null;
        }
        
        if (broomId == null) {
            LOGGER.warn("Cannot validate broom bond: broomId is null");
            return null;
        }
        
        if (!broomId.equals(broom.getBondedBroomId())) {
            LOGGER.warn("Player {} trying to access broom they are not bonded to", player.getName().getString());
            return null;
        }
        
        return broom;
    }
}
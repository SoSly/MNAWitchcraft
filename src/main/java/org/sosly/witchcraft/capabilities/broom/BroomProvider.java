package org.sosly.witchcraft.capabilities.broom;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;

public class BroomProvider implements ICapabilitySerializable<Tag> {
    public static final Capability<IBroomCapability> BROOM = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<IBroomCapability> holder = LazyOptional.of(BroomCapability::new);

    @Override
    public Tag serializeNBT() {
        IBroomCapability instance = holder.orElse(new BroomCapability());
        CompoundTag nbt = new CompoundTag();
        
        if (instance.getBroomEntityId() != null) {
            nbt.putUUID("broomEntityId", instance.getBroomEntityId());
        }
        
        nbt.putInt("brushTier", instance.getBrushTier().getTier());
        nbt.putString("handleWood", instance.getHandleWood().toString());
        nbt.putInt("ribbonColor", instance.getRibbonColor());
        nbt.putInt("storageLevel", instance.getStorageLevel().getLevel());
        nbt.put("charms", instance.getCharmHandler().serializeNBT());
        nbt.put("storage", instance.getStorageHandler().serializeNBT());
        
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IBroomCapability instance = holder.orElse(new BroomCapability());
        if (nbt instanceof CompoundTag cnbt) {
            if (cnbt.hasUUID("broomEntityId")) {
                instance.setBroomEntityId(cnbt.getUUID("broomEntityId"));
            }
            
            if (cnbt.contains("brushTier")) {
                instance.setBrushTier(IBroomCapability.BrushTier.fromTier(cnbt.getInt("brushTier")));
            }
            
            if (cnbt.contains("handleWood")) {
                instance.setHandleWood(new ResourceLocation(cnbt.getString("handleWood")));
            }
            
            if (cnbt.contains("ribbonColor")) {
                instance.setRibbonColor(cnbt.getInt("ribbonColor"));
            }
            
            if (cnbt.contains("storageLevel")) {
                instance.setStorageLevel(IBroomCapability.StorageLevel.fromLevel(cnbt.getInt("storageLevel")));
            }
            
            if (cnbt.contains("charms")) {
                instance.getCharmHandler().deserializeNBT(cnbt.getCompound("charms"));
            }
            
            if (cnbt.contains("storage")) {
                instance.getStorageHandler().deserializeNBT(cnbt.getCompound("storage"));
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BROOM.orEmpty(cap, holder);
    }
}
package org.sosly.witchcraft.capabilities.coven;

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
import org.sosly.witchcraft.api.capabilities.ICovenCapability;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class CovenProvider implements ICapabilitySerializable<Tag> {
    public static final Capability<ICovenCapability> COVEN = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<ICovenCapability> holder = LazyOptional.of(CovenCapability::new);

    @Override
    public Tag serializeNBT() {
        ICovenCapability instance = holder.orElse(new CovenCapability());
        CompoundTag nbt = new CompoundTag();
        if (instance.hasMalice()) {
            nbt.putBoolean("malice", true);
        }
        if (instance.getBondedBroomId() != null) {
            nbt.putUUID("bondedBroom", instance.getBondedBroomId());
        }
        for (int tier = 3; tier <= 5; tier++) {
            Map<ResourceLocation, Boolean> progress = instance.getTierEffectsProgress(tier);

            if (progress != null) {
                CompoundTag tierTag = new CompoundTag();
                tierTag.putInt("size", progress.size());

                AtomicInteger index = new AtomicInteger(0);
                progress.forEach((effectId, completed) -> {
                    int i = index.getAndIncrement();
                    tierTag.putString("effect_" + i, effectId.toString());
                    tierTag.putBoolean("completed_" + i, completed);
                });

                nbt.put("tier_" + tier, tierTag);
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        ICovenCapability instance = holder.orElse(new CovenCapability());
        if (nbt instanceof CompoundTag cnbt) {
            instance.setMalice(cnbt.getBoolean("malice"));
            if (cnbt.hasUUID("bondedBroom")) {
                instance.setBondedBroomId(cnbt.getUUID("bondedBroom"));
            }
            for (int tier = 3; tier <= 5; tier++) {
                if (cnbt.contains("tier_" + tier)) {
                    CompoundTag tierTag = cnbt.getCompound("tier_" + tier);
                    Set<ResourceLocation> effects = new HashSet<>();
                    for (int i = 0; i < tierTag.getInt("size"); i++) {
                        ResourceLocation effectId = new ResourceLocation(tierTag.getString("effect_" + i));
                        effects.add(effectId);
                    }
                    instance.setTierEffectsRequired(tier, effects);
                    
                    for (int i = 0; i < tierTag.getInt("size"); i++) {
                        if (tierTag.getBoolean("completed_" + i)) {
                            ResourceLocation effectId = new ResourceLocation(tierTag.getString("effect_" + i));
                            instance.markEffectCompleted(tier, effectId);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return COVEN.orEmpty(cap, holder);
    }
}

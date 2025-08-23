package org.sosly.witchcraft.capabilities.coven;

import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CovenCapability implements ICovenCapability {
    private boolean malice = false;
    private final Map<Integer, Map<ResourceLocation, Boolean>> tierEffectsProgress = new HashMap<>();
    private UUID bondedBroomId = null;

    @Override
    public boolean hasMalice() {
        return malice;
    }

    @Override
    public void setMalice(boolean hasMalice) {
        malice = hasMalice;
    }

    @Override
    public Collection<ResourceLocation> getTierEffectsRequired(int tier) {
        Map<ResourceLocation, Boolean> progress = tierEffectsProgress.get(tier);

        if (progress == null) {
            return null;
        }

        return progress.keySet();
    }

    @Override
    public void setTierEffectsRequired(int tier, Set<ResourceLocation> effects) {
        Map<ResourceLocation, Boolean> progress = new HashMap<>();

        for (ResourceLocation effect : effects) {
            progress.put(effect, false);
        }

        tierEffectsProgress.put(tier, progress);
    }

    @Override
    public void markEffectCompleted(int tier, ResourceLocation effectId) {
        Map<ResourceLocation, Boolean> progress = tierEffectsProgress.get(tier);

        if (progress != null && progress.containsKey(effectId)) {
            progress.put(effectId, true);
        }
    }

    @Override
    public boolean isEffectCompleted(int tier, ResourceLocation effectId) {
        Map<ResourceLocation, Boolean> progress = tierEffectsProgress.get(tier);

        return progress != null && progress.getOrDefault(effectId, false);
    }

    @Override
    public boolean areAllEffectsCompleted(int tier) {
        Map<ResourceLocation, Boolean> progress = tierEffectsProgress.get(tier);

        if (progress == null || progress.isEmpty()) {
            return false;
        }

        return progress.values().stream().allMatch(completed -> completed);
    }

    @Override
    public Map<ResourceLocation, Boolean> getTierEffectsProgress(int tier) {
        return tierEffectsProgress.get(tier);
    }

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
}

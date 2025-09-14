package org.sosly.witchcraft.capabilities.coven;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CovenCapability implements ICovenCapability {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean malice = false;
    private final Map<Integer, Map<ResourceLocation, Boolean>> tierEffectsProgress = new HashMap<>();

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
        if (effects == null || effects.isEmpty()) {
            LOGGER.warn("Attempted to set null or empty effects for tier {}", tier);
            return;
        }
        
        Map<ResourceLocation, Boolean> progress = new HashMap<>();

        for (ResourceLocation effect : effects) {
            progress.put(effect, false);
        }

        tierEffectsProgress.put(tier, progress);
    }

    @Override
    public void markEffectCompleted(int tier, ResourceLocation effectId) {
        Map<ResourceLocation, Boolean> progress = tierEffectsProgress.get(tier);

        if (progress == null) {
            LOGGER.warn("Attempted to mark effect {} completed for tier {} but no progress map exists", effectId, tier);
            return;
        }
        
        if (!progress.containsKey(effectId)) {
            LOGGER.warn("Attempted to mark unknown effect {} completed for tier {}", effectId, tier);
            return;
        }
        
        progress.put(effectId, true);
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
    public void copyFrom(ICovenCapability other) {
        malice = other.hasMalice();
        
        tierEffectsProgress.clear();
        for (int tier = 3; tier <= 5; tier++) {
            Map<ResourceLocation, Boolean> otherProgress = other.getTierEffectsProgress(tier);
            if (otherProgress != null) {
                tierEffectsProgress.put(tier, new HashMap<>(otherProgress));
            }
        }
    }
}

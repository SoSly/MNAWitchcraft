package org.sosly.witchcraft.capabilities.armor;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.api.capabilities.IMoonthreadArmorData;

/**
 * Capability provider for Moonthread armor data.
 * This provider does NOT persist data - cooldowns are reset when players disconnect.
 */
public class MoonthreadArmorProvider implements ICapabilityProvider {
    public static final Capability<IMoonthreadArmorData> MOONTHREAD_ARMOR_DATA = CapabilityManager.get(new CapabilityToken<>() {});
    private final LazyOptional<IMoonthreadArmorData> holder = LazyOptional.of(MoonthreadArmorData::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return MOONTHREAD_ARMOR_DATA.orEmpty(cap, holder);
    }
}
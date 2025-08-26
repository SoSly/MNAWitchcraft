package org.sosly.witchcraft.capabilities;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.api.capabilities.IMoonthreadArmorData;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistry {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IBroomCapability.class);
        event.register(ICovenCapability.class);
        event.register(IMoonthreadArmorData.class);
    }
}

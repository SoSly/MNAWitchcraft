package org.sosly.witchcraft.factions;

import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.CastingResourceIDs;
import org.sosly.witchcraft.factions.resources.Essence;
import org.sosly.witchcraft.factions.resources.Malice;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FactionRegistry {
    public static final IFaction COVEN = new Coven();
    public static final IFaction DARK_COVEN = new DarkCoven();

    @SubscribeEvent
    public static void registerFactions(RegisterEvent event) {
        event.register(((IForgeRegistry) Registries.Factions.get()).getRegistryKey(), (helper) -> {
            helper.register(new ResourceLocation(Witchcraft.MOD_ID, "coven"), COVEN);
            helper.register(new ResourceLocation(Witchcraft.MOD_ID, "dark_coven"), DARK_COVEN);
        });
    }

    @SubscribeEvent
    public static void registerResources(FMLCommonSetupEvent event) {
        CastingResourceRegistry.Instance.register(CastingResourceIDs.ESSENCE, Essence.class);
        CastingResourceRegistry.Instance.register(CastingResourceIDs.MALICE, Malice.class);
    }

    public static boolean isWitch(IPlayerProgression p) {
        return p.getAlliedFaction() == FactionRegistry.COVEN || p.getAlliedFaction() == FactionRegistry.DARK_COVEN;
    }

    @Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void registerResourceGuis(FMLClientSetupEvent event) {
            CastingResourceGuiRegistry.Instance.registerResourceGui(CastingResourceIDs.ESSENCE, new Essence.ResourceGui());
            CastingResourceGuiRegistry.Instance.registerResourceGui(CastingResourceIDs.MALICE, new Malice.ResourceGui());
        }
    }
}

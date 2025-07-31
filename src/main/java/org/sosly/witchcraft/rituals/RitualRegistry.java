package org.sosly.witchcraft.rituals;

import com.mna.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.rituals.effects.BrokenSympathyRitual;
import org.sosly.witchcraft.rituals.effects.HedgeRitual;
import org.sosly.witchcraft.rituals.effects.SympathyRitual;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RitualRegistry {
    @SubscribeEvent
    public static void registerRitualEffects(RegisterEvent event) {
        event.register(Registries.RitualEffect.get().getRegistryKey(), helper -> {
            helper.register("rituals.broken_sympathy", new BrokenSympathyRitual(new ResourceLocation(Witchcraft.MOD_ID, "rituals/broken_sympathy")));
            helper.register("rituals.hedge", new HedgeRitual(new ResourceLocation(Witchcraft.MOD_ID, "rituals/hedge")));
            helper.register("rituals.sympathy", new SympathyRitual(new ResourceLocation(Witchcraft.MOD_ID, "rituals/sympathy")));
        });
    }
}

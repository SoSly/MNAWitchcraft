package org.sosly.witchcraft.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.effects.beneficial.BrokenSympathyEffect;
import org.sosly.witchcraft.effects.beneficial.NiceSmellEffect;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Witchcraft.MOD_ID);
    public static final RegistryObject<BrokenSympathyEffect> BROKEN_SYMPATHY = EFFECTS.register("broken_sympathy", BrokenSympathyEffect::new);
    public static final RegistryObject<NiceSmellEffect> NICE_SMELL = EFFECTS.register("nice_smell", NiceSmellEffect::new);
}

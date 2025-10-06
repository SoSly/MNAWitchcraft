package org.sosly.witchcraft.events.spells;

import com.mojang.logging.LogUtils;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.SpellCastEvent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DirectSpellDemonstration {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        SpellContext context = event.getContext();
        if (context == null) {
            return;
        }

        if (context.isClientSide()) {
            return;
        }

        Entity target = context.getSpawnedTargetEntity();
        if (!(target instanceof Witch)) {
            return;
        }

        if (!(event.getSource().getCaster() instanceof Player caster)) {
            return;
        }

        ICovenCapability covenCap = caster.getCapability(CovenProvider.COVEN).orElse(null);
        if (covenCap == null) {
            LOGGER.error("Failed to retrieve coven capability for player {}", caster.getName().getString());
            return;
        }

        IPlayerProgression progression = caster.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            LOGGER.error("Failed to retrieve player progression capability for player {}", caster.getName().getString());
            return;
        }

        int nextTier = progression.getTier() + 1;
        if (nextTier < 3 || nextTier > 5) {
            LOGGER.debug("Player {} tier progression {} is outside valid coven range (3-5)",
                caster.getName().getString(), nextTier);
            return;
        }

        if (covenCap.getTierEffectsRequired(nextTier) == null) {
            LOGGER.debug("No tier effects defined for tier {} for player {}", nextTier, caster.getName().getString());
            return;
        }

        ISpellDefinition spell = event.getSpell();
        if (spell == null) {
            LOGGER.warn("No valid spell found in spell cast event for player {}", caster.getName().getString());
            return;
        }

        spell.iterateComponents(component -> {
            SpellEffect effect = component.getPart();
            ResourceLocation effectId = effect.getRegistryName();

            if (covenCap.getTierEffectsRequired(nextTier).contains(effectId)) {
                covenCap.markEffectCompleted(nextTier, effectId);
                LOGGER.info("Player {} completed spell effect {} for tier {} by direct casting on witch",
                    caster.getName().getString(), effectId, nextTier);

                if (covenCap.areAllEffectsCompleted(nextTier)) {
                    LOGGER.info("Player {} has completed all requirements for tier {}!",
                        caster.getName().getString(), nextTier);
                }
            }
        });
    }
}

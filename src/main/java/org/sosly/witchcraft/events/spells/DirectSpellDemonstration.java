package org.sosly.witchcraft.events.spells;

import com.mna.api.events.ComponentApplyingEvent;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.targeting.SpellTarget;
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
    public static void onSpellCast(ComponentApplyingEvent event) {
        SpellContext context = event.getContext();
        if (context == null) {
            return;
        }

        if (context.isClientSide()) {
            return;
        }

        SpellTarget target = event.getTarget();
        if (!(target.getLivingEntity() instanceof Witch)) {
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

        ISpellComponent component = event.getComponent();
        if (component == null) {
            LOGGER.debug("No valid component found in component applying event for player {}", caster.getName().getString());
            return;
        }

        ResourceLocation effectId = component.getRegistryName();
        if (!covenCap.getTierEffectsRequired(nextTier).contains(effectId)) {
            return;
        }

        covenCap.markEffectCompleted(nextTier, effectId);
        LOGGER.info("Player {} completed component effect {} for tier {} by direct casting on witch",
            caster.getName().getString(), effectId, nextTier);
    }
}

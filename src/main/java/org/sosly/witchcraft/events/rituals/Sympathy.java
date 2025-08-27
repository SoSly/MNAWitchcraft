package org.sosly.witchcraft.events.rituals;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.RitualCompleteEvent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.items.sympathy.BoundPoppetItem;
import org.sosly.witchcraft.rituals.effects.SympathyRitual;
import org.sosly.witchcraft.utils.SympathyHelper;

import java.util.List;

/**
 * Handles tracking spell effects cast on witches through the Sympathy Ritual
 */
@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Sympathy {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void SympathyProgression(RitualCompleteEvent event) {
        if (event.getHandlers().stream().noneMatch(handler -> handler instanceof SympathyRitual)) {
            return;
        }
        
        Player caster = event.getCaster();
        if (caster == null || caster.level().isClientSide()) {
            return;
        }
        
        ICovenCapability covenCap = caster.getCapability(CovenProvider.COVEN).orElse(null);
        if (covenCap == null) {
            LOGGER.error("Failed to retrieve coven capability for player {}", caster.getName().getString());
            return;
        }
        
        ItemStack boundPoppet = findBoundPoppet(event.getCollectedReagents());
        if (boundPoppet.isEmpty()) {
            LOGGER.warn("No bound poppet found in sympathy ritual reagents for player {}", caster.getName().getString());
            return;
        }
        
        ServerLevel level = (ServerLevel) caster.level();
        Entity target = SympathyHelper.getBoundEntity(boundPoppet, level);
        
        if (target == null) {
            LOGGER.warn("Bound poppet target entity not found for player {}", caster.getName().getString());
            return;
        }
        
        if (!(target instanceof Witch)) {
            LOGGER.warn("Bound poppet target is not a witch ({}), cannot progress coven tier for player {}", 
                target.getClass().getSimpleName(), caster.getName().getString());
            return;
        }
        
        ISpellDefinition spell = getSpellFromReagents(event.getCollectedReagents(), caster);
        if (spell == null) {
            LOGGER.warn("No valid spell found in sympathy ritual reagents for player {}", caster.getName().getString());
            return;
        }
        
        IPlayerProgression progression = caster.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            LOGGER.error("Failed to retrieve player progression capability for player {}", caster.getName().getString());
            return;
        }
        
        int nextTier = progression.getTier() + 1;
        if (nextTier < 3 || nextTier > 5) {
            LOGGER.warn("Player {} tier progression {} is outside valid coven range (3-5)", 
                caster.getName().getString(), nextTier);
            return;
        }
        
        if (covenCap.getTierEffectsRequired(nextTier) == null) {
            LOGGER.warn("No tier effects defined for tier {} for player {}", nextTier, caster.getName().getString());
            return;
        }
        
        spell.iterateComponents(component -> {
            SpellEffect effect = component.getPart();
            ResourceLocation effectId = effect.getRegistryName();
            
            if (covenCap.getTierEffectsRequired(nextTier).contains(effectId)) {
                covenCap.markEffectCompleted(nextTier, effectId);
                LOGGER.info("Player {} completed spell effect {} for tier {}", 
                    caster.getName().getString(), effectId, nextTier);
                
                if (covenCap.areAllEffectsCompleted(nextTier)) {
                    LOGGER.info("Player {} has completed all requirements for tier {}!", 
                        caster.getName().getString(), nextTier);
                }
            }
        });
    }
    
    /**
     * Finds a bound poppet in the reagent list
     */
    private static ItemStack findBoundPoppet(List<ItemStack> reagents) {
        return reagents.stream()
                .filter(stack -> stack.getItem() instanceof BoundPoppetItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }
    
    /**
     * Gets the spell definition from the reagents
     */
    private static ISpellDefinition getSpellFromReagents(List<ItemStack> reagents, Player caster) {
        for (ItemStack stack : reagents) {
            if (stack.getItem() instanceof com.mna.api.spells.ICanContainSpell spellItem) {
                return spellItem.getSpell(stack, caster);
            }
        }
        return null;
    }
}
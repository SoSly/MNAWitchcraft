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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOGGER = LogManager.getLogger(Sympathy.class);
    
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
            return;
        }
        
        ItemStack boundPoppet = findBoundPoppet(event.getCollectedReagents());
        if (boundPoppet.isEmpty()) {
            return;
        }
        
        ServerLevel level = (ServerLevel) caster.level();
        Entity target = SympathyHelper.getBoundEntity(boundPoppet, level);
        if (!(target instanceof Witch)) {
            return;
        }
        
        ISpellDefinition spell = getSpellFromReagents(event.getCollectedReagents(), caster);
        if (spell == null) {
            return;
        }
        
        IPlayerProgression progression = caster.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return;
        }
        
        int nextTier = progression.getTier() + 1;
        if (nextTier < 3 || nextTier > 5) {
            return;
        }
        
        if (covenCap.getTierEffectsRequired(nextTier) == null) {
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
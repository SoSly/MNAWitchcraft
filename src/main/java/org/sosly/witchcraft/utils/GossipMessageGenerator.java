package org.sosly.witchcraft.utils;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.sosly.witchcraft.Config;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GossipMessageGenerator {
    
    /**
     * Generates a gossip message for a witch based on the player and their current progress
     * @param player The player carrying the scented item
     * @param scentedItem The type of scented item carried
     * @return The gossip message component
     */
    public static Component generateGossipMessage(Player player, String scentedItem) {
        // Get player's coven capability
        ICovenCapability covenCap = player.getCapability(CovenProvider.COVEN).orElse(null);
        if (covenCap == null) {
            return generateGeneralGossip(scentedItem, player);
        }
        
        // Get player's progression tier
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return generateGeneralGossip(scentedItem, player);
        }
        
        int currentTier = progression.getTier();
        int nextTier = currentTier + 1;
        
        // Only provide spell hints for tiers 3-5
        if (nextTier < 3 || nextTier > 5) {
            return generateNonSpellGossip(scentedItem, player);
        }
        
        Map<ResourceLocation, Boolean> tierProgress = covenCap.getTierEffectsProgress(nextTier);
        if (tierProgress == null || tierProgress.isEmpty()) {
            return generateNonSpellGossip(scentedItem, player);
        }
        
        // Find incomplete effects
        List<ResourceLocation> incompleteEffects = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Boolean> entry : tierProgress.entrySet()) {
            if (!entry.getValue()) {
                incompleteEffects.add(entry.getKey());
            }
        }
        
        if (incompleteEffects.isEmpty()) {
            // Check if M&A tier is also complete (1.0f means 100% complete)
            float mnaProgress = progression.getTierProgress(player.level());
            if (mnaProgress < 1.0f) {
                // M&A tier not complete, generate normal gossip
                return generateNonSpellGossip(scentedItem, player);
            }
            // Both MNAW and M&A tier complete - hint about going deeper
            return generateRitualHintGossip(scentedItem, player);
        }
        
        // Use config for spell hint chance (1 in N)
        if (player.getRandom().nextInt(Config.witchGossipSpellHintChance) != 0) {
            return generateNonSpellGossip(scentedItem, player);
        }
        
        // Shuffle the list manually using player's RandomSource to ensure true randomness
        for (int i = incompleteEffects.size() - 1; i > 0; i--) {
            int j = player.getRandom().nextInt(i + 1);
            ResourceLocation temp = incompleteEffects.get(i);
            incompleteEffects.set(i, incompleteEffects.get(j));
            incompleteEffects.set(j, temp);
        }
        ResourceLocation hintEffect = incompleteEffects.get(0);
        return generateSpellHintGossip(scentedItem, hintEffect, player.getName().getString());
    }
    
    private static Component generateNonSpellGossip(String scentedItem, Player player) {
        int gossipType = player.getRandom().nextInt(3);
        return switch (gossipType) {
            case 0 -> generateCovenLoreGossip(scentedItem, player);
            case 1 -> generateFactionCommentary(scentedItem, player);
            default -> generateGeneralGossip(scentedItem, player);
        };
    }
    
    private static Component generateSpellHintGossip(String scentedItem, ResourceLocation effectId, String playerName) {
        // The effect ResourceLocation IS the translation key
        // Example: mna:components/bind_wounds is literally the key in en_us.json
        String effectTranslationKey = effectId.toString();
        
        // Generate hint message
        return Component.translatable("mnaw.gossip.spell_hint", 
                Component.translatable(ScentedItemHelper.getScentedItemTranslationKey(scentedItem)),
                playerName,
                Component.translatable(effectTranslationKey));
    }
    
    private static Component generateCovenLoreGossip(String scentedItem, Player player) {
        // Pick from available lore messages
        int loreIndex = player.getRandom().nextInt(6) + 1; // 6 lore messages
        return Component.translatable("mnaw.gossip.coven_lore." + loreIndex,
                Component.translatable(ScentedItemHelper.getScentedItemTranslationKey(scentedItem)));
    }
    
    private static Component generateFactionCommentary(String scentedItem, Player player) {
        // Pick from faction commentary
        int commentIndex = player.getRandom().nextInt(4) + 1; // 4 faction comments
        return Component.translatable("mnaw.gossip.faction." + commentIndex,
                Component.translatable(ScentedItemHelper.getScentedItemTranslationKey(scentedItem)));
    }
    
    private static Component generateGeneralGossip(String scentedItem, Player player) {
        // Pick from general gossip
        int generalIndex = player.getRandom().nextInt(5) + 1; // Assume 5 general messages
        return Component.translatable("mnaw.gossip.general." + generalIndex,
                Component.translatable(ScentedItemHelper.getScentedItemTranslationKey(scentedItem)));
    }
    
    private static Component generateRitualHintGossip(String scentedItem, Player player) {
        // When all spells are complete, hint about the Ritual of the Hedge
        return Component.translatable("mnaw.gossip.ritual_hint",
                Component.translatable(ScentedItemHelper.getScentedItemTranslationKey(scentedItem)),
                player.getName().getString());
    }
}
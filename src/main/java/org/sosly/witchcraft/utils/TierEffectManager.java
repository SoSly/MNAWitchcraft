package org.sosly.witchcraft.utils;

import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosly.witchcraft.factions.FactionRegistry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TierEffectManager is a utility class for the CovenCapability.
 * This class manages the SpellEffects that a coven witch must cast on a Witch mob
 * in order to progress to the next tier of magic.
 */
public class TierEffectManager {
    private static final Logger LOGGER = LogManager.getLogger(TierEffectManager.class);

    private static final int TIER_3_REQUIREMENTS = 3;
    private static final int TIER_4_REQUIREMENTS = 4;
    private static final int TIER_5_REQUIREMENTS = 5;

    /**
     * Generates random spell effect requirements for a specific tier
     * @param tier the tier to generate requirements for (3-5)
     * @param rand the random source
     * @param player the player to generate requirements for (used for faction filtering)
     * @return a Set of ResourceLocations representing required spell effects
     */
    public static Set<ResourceLocation> generateTierRequirements(int tier, RandomSource rand, Player player) {
        List<ResourceLocation> availableEffects = getAllPotionEffectComponents(tier, player);

        if (availableEffects.isEmpty()) {
            LOGGER.warn("No potion effects found for tier {}", tier);
            return new HashSet<>();
        }

        int requiredCount = switch (tier) {
            case 3 -> TIER_3_REQUIREMENTS;
            case 4 -> TIER_4_REQUIREMENTS;
            case 5 -> TIER_5_REQUIREMENTS;
            default -> 0;
        };

        Set<ResourceLocation> requirements = new HashSet<>();
        List<ResourceLocation> shuffled = new ArrayList<>(availableEffects);
        Collections.shuffle(shuffled, new Random(rand.nextLong()));

        for (int i = 0; i < Math.min(requiredCount, shuffled.size()); i++) {
            requirements.add(shuffled.get(i));
        }

        LOGGER.info("Generated {} requirements for tier {}: {}", requirements.size(), tier, requirements);
        return requirements;
    }

    /**
     * Gets all potion effect components available for a specific tier and player faction
     * @param tier the tier to get components for
     * @param player the player to check faction requirements against
     * @return a List of ResourceLocations for available potion effect components
     */
    private static List<ResourceLocation> getAllPotionEffectComponents(int tier, Player player) {
        IForgeRegistry<SpellEffect> registry = Registries.SpellEffect.get();

        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElse(null);
        IFaction playerFaction = progression != null ? progression.getAlliedFaction() : null;

        return registry.getEntries().stream()
                .filter(entry -> {
                    SpellEffect effect = entry.getValue();
                    if (!(effect instanceof PotionEffectComponent)) {
                        return false;
                    }

                    if (effect.getTier(player.level()) != (tier - 1)) {
                        return false;
                    }

                    IFaction requiredFaction = effect.getFactionRequirement();
                    if (requiredFaction != null) {
                        if (playerFaction == null) {
                            return false;
                        }
                        if (playerFaction != requiredFaction) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(entry -> entry.getKey().location())
                .collect(Collectors.toList());
    }
}

package org.sosly.witchcraft.api.capabilities;

import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.Witchcraft;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ICovenCapability tracks a coven witch's progression.
 */
public interface ICovenCapability {
    ResourceLocation COVEN_CAPABILITY = new ResourceLocation(Witchcraft.MOD_ID, "coven");

    /**
     * @return {@code true} if the witch has completed the Ritual of Malice
     */
    boolean hasMalice();

    /**
     * @param hasMalice should be set to {@code true} when the witch completes the Ritual of Malice
     */
    void setMalice(boolean hasMalice);

    /**
     * Gets the list of Spell Components that the coven witch must successfully cast on a Witch mob
     * in order to progress to the next tier of magic.  If a tier returns an empty collection, the
     * witch has completed all the required effects for that tier.
     * @param tier is the tier of magic to check for {@code [3-5]}
     * @return a {@code Collection<ResourceLocation>} of MnA Spell Component IDs
     */
    Collection<ResourceLocation> getTierEffectsRequired(int tier);

    /**
     * Sets the list of Spell Components that the coven witch must successfully cast on a Witch mob
     * in order to progress to the next tier of magic.
     * @param tier is the tier of magic to set {@code [3-5]}
     * @param effects is a {@code Set<ResourceLocation>} of MnA Spell Component IDs
     */
    void setTierEffectsRequired(int tier, Set<ResourceLocation> effects);

    /**
     * Marks a spell effect as completed for a specific tier
     * @param tier is the tier of magic {@code [3-5]}
     * @param effectId is the {@code ResourceLocation} ID of the spell effect that was successfully cast
     */
    void markEffectCompleted(int tier, ResourceLocation effectId);

    /**
     * Checks if a specific spell effect has been completed for a tier
     * @param tier is the tier of magic {@code [3-5]}
     * @param effectId is the {@code ResourceLocation} ID of the spell effect to check
     * @return {@code true} if the effect has been completed
     */
    boolean isEffectCompleted(int tier, ResourceLocation effectId);

    /**
     * Checks if all required spell effects have been completed for a tier
     * @param tier is the tier of magic {@code [3-5]}
     * @return {@code true} if all effects for the tier have been completed
     */
    boolean areAllEffectsCompleted(int tier);

    /**
     * Gets the progress map for a specific tier showing which effects have been completed
     * @param tier is the tier of magic {@code [3-5]}
     * @return a {@code Map<ResourceLocation, Boolean>} showing completion status, or null if no requirements set
     */
    Map<ResourceLocation, Boolean> getTierEffectsProgress(int tier);

    /**
     * @return the UUID of the bonded broom entity, or null if no broom is bonded
     */
    @Nullable
    UUID getBondedBroomId();

    /**
     * @param entityId the UUID of the broom entity to bond, or null to unbond
     */
    void setBondedBroomId(@Nullable UUID entityId);

    /**
     * @return true if a broom is currently bonded to this player
     */
    boolean hasBondedBroom();
}

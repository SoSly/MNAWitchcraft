package org.sosly.witchcraft.api.capabilities;

import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.Witchcraft;

/**
 * IMoonthreadArmorData tracks combat cooldowns for Moonthread armor set bonuses.
 * This capability does not persist data and is reset when players disconnect.
 */
public interface IMoonthreadArmorData {
    ResourceLocation MOONTHREAD_ARMOR_DATA = new ResourceLocation(Witchcraft.MOD_ID, "moonthread_armor_data");

    /**
     * Gets the timestamp when the harmful effect nullification cooldown will end
     * @return timestamp in milliseconds when the cooldown expires, or 0 if no cooldown is active
     */
    long getHarmfulEffectNullificationCooldown();

    /**
     * Sets the timestamp when the harmful effect nullification cooldown will end
     * @param timestamp the time in milliseconds when the cooldown expires
     */
    void setHarmfulEffectNullificationCooldown(long timestamp);

    /**
     * Gets the timestamp when the temporary immunity will end
     * @return timestamp in milliseconds when immunity expires, or 0 if no immunity is active
     */
    long getTemporaryImmunityTimestamp();

    /**
     * Sets the timestamp when the temporary immunity will end
     * @param timestamp the time in milliseconds when immunity expires
     */
    void setTemporaryImmunityTimestamp(long timestamp);

    /**
     * Checks if the harmful effect nullification cooldown is currently active
     * @return true if the cooldown is active (player cannot nullify effects), false otherwise
     */
    boolean isNullificationCooldownActive();

    /**
     * Checks if the player currently has temporary immunity to harmful effects
     * @return true if the player has temporary immunity, false otherwise
     */
    boolean hasTemporaryImmunity();
}

package org.sosly.witchcraft.capabilities.armor;

import org.sosly.witchcraft.api.capabilities.IMoonthreadArmorData;

public class MoonthreadArmorData implements IMoonthreadArmorData {
    private long harmfulEffectNullificationCooldown = 0L;
    private long temporaryImmunityTimestamp = 0L;

    @Override
    public long getHarmfulEffectNullificationCooldown() {
        return harmfulEffectNullificationCooldown;
    }

    @Override
    public void setHarmfulEffectNullificationCooldown(long timestamp) {
        this.harmfulEffectNullificationCooldown = timestamp;
    }

    @Override
    public long getTemporaryImmunityTimestamp() {
        return temporaryImmunityTimestamp;
    }

    @Override
    public void setTemporaryImmunityTimestamp(long timestamp) {
        this.temporaryImmunityTimestamp = timestamp;
    }

    @Override
    public boolean isNullificationCooldownActive() {
        long currentTime = System.currentTimeMillis();
        return harmfulEffectNullificationCooldown > currentTime;
    }

    @Override
    public boolean hasTemporaryImmunity() {
        long currentTime = System.currentTimeMillis();
        return temporaryImmunityTimestamp > currentTime;
    }
}
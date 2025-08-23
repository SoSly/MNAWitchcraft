package org.sosly.witchcraft.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import org.sosly.witchcraft.effects.EffectRegistry;

public class BrokenSympathyRitual extends RitualEffect {
    protected final int DURATION_MINUTES = 5;
    protected final int DURATION = 20 * 60 * DURATION_MINUTES;

    public BrokenSympathyRitual(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext ctx) {
        Player player = ctx.getCaster();
        if (player == null) {
            return false;
        }

        player.addEffect(new MobEffectInstance(EffectRegistry.BROKEN_SYMPATHY.get(), DURATION));
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return 0;
    }
}

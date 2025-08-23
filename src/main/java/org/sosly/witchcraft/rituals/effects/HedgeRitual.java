package org.sosly.witchcraft.rituals.effects;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.factions.FactionRegistry;
import org.sosly.witchcraft.utils.TierEffectManager;

public class HedgeRitual extends RitualEffect {
    public HedgeRitual(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        Player caster = context.getCaster();
        if (caster == null) {
            return Component.literal("No caster found");
        }

        IPlayerProgression progression = caster.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return Component.literal("Progression could not be found...this is a problem.");
        }

        ICovenCapability covenCap = caster.getCapability(CovenProvider.COVEN).orElse(null);
        if (covenCap == null) {
            return Component.literal("Coven capability could not be found");
        }

        int currentTier = progression.getTier();
        int nextTier = currentTier + 1;

        if (currentTier < 2 || currentTier > 4) {
            return Component.translatable("ritual.mnaw.hedge.tier_invalid");
        }

        if (progression.getTierProgress(context.getLevel()) < 1.0F) {
            return Component.translatable("ritual.mna.progression.not_ready");
        }

        if (!covenCap.areAllEffectsCompleted(nextTier)) {
            return Component.translatable("ritual.mnaw.hedge.spells_not_complete");
        }

        if (progression.hasAlliedFaction() && progression.getAlliedFaction() != FactionRegistry.COVEN) {
            return Component.translatable("event.mna.faction_ritual_failed");
        }

        return null;
    }

    @Override
    public boolean applyStartCheckInCreative() {
        return true;
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Player caster = context.getCaster();
        if (caster == null || caster.getUUID() == null) {
            return false;
        }

        ServerLevel level = (ServerLevel) context.getLevel();
        Vec3 ritualCenter = Vec3.atBottomCenterOf(context.getCenter());
        
        Witch witch = EntityType.WITCH.spawn(level, null, caster, 
            context.getCenter().above(), MobSpawnType.EVENT, false, false);
        
        if (witch == null) {
            return false;
        }

        witch.setTarget(null);
        witch.setPersistenceRequired();
        witch.setNoGravity(true);
        
        witch.targetSelector.removeAllGoals(goal -> true);
        witch.goalSelector.removeAllGoals(goal -> true);
        
        double dx = caster.getX() - witch.getX();
        double dz = caster.getZ() - witch.getZ();
        float yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        witch.setYRot(yaw);
        witch.yBodyRot = yaw;
        witch.yHeadRot = yaw;
        
        witch.getPersistentData().putBoolean("mnaw:ritual_witch", true);
        witch.getPersistentData().putLong("mnaw:ritual_end_time", level.getGameTime() + 120);

        spawnParticles(level, ritualCenter);
        level.playSound(null, context.getCenter(), SoundEvents.WITCH_AMBIENT, 
            SoundSource.NEUTRAL, 1.0F, 1.0F);

        IPlayerProgression progression = caster.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        ICovenCapability covenCap = caster.getCapability(CovenProvider.COVEN).orElse(null);
        
        if (progression == null || covenCap == null) {
            witch.discard();
            return false;
        }

        if (!progression.hasAlliedFaction()) {
            progression.setAlliedFaction(FactionRegistry.COVEN, caster);
        }

        int currentTier = progression.getTier();
        int nextTier = currentTier + 1;
        
        if (nextTier > 5) {
            caster.sendSystemMessage(Component.translatable("ritual.mnaw.hedge.success"));
            level.playSound(null, context.getCenter(), SoundEvents.WITCH_CELEBRATE, 
                SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        }
        
        progression.setTier(nextTier, caster);
        
        if (nextTier >= 5) {
            caster.sendSystemMessage(Component.translatable("ritual.mnaw.hedge.success"));
            level.playSound(null, context.getCenter(), SoundEvents.WITCH_CELEBRATE, 
                SoundSource.NEUTRAL, 1.0F, 1.0F);
            return true;
        }
        
        var requirements = TierEffectManager.generateTierRequirements(nextTier + 1, 
            caster.getRandom(), level);
        covenCap.setTierEffectsRequired(nextTier + 1, requirements);

        caster.sendSystemMessage(Component.translatable("ritual.mnaw.hedge.success"));
        
        level.playSound(null, context.getCenter(), SoundEvents.WITCH_CELEBRATE, 
            SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 120;
    }

    private void spawnParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i < 50; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 3.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 3.0;
            
            level.sendParticles(ParticleTypes.WITCH,
                center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                1, 0, 0, 0, 0.05);
                
            if (i % 3 != 0) {
                continue;
            }
            
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                1, 0, 0, 0, 0.02);
        }
    }

}
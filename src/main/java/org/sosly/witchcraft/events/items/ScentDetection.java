package org.sosly.witchcraft.events.items;

import com.mojang.logging.LogUtils;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.utils.ScentedItemHelper;
import org.sosly.witchcraft.utils.TierEffectManager;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScentDetection {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        
        Player player = event.player;
        
        if (player.tickCount % 20 != 0) {
            return;
        }
        
        if (!ScentedItemHelper.hasAnyScentedItem(player)) {
            return;
        }
        
        if (!player.hasEffect(EffectRegistry.NICE_SMELL.get())) {
            player.addEffect(new MobEffectInstance(EffectRegistry.NICE_SMELL.get(), 100, 0, false, false));
            return;
        }
        
        MobEffectInstance existingEffect = player.getEffect(EffectRegistry.NICE_SMELL.get());
        if (existingEffect == null || existingEffect.getDuration() >= 60) {
            return;
        }
        
        player.addEffect(new MobEffectInstance(EffectRegistry.NICE_SMELL.get(), 100, 0, false, false));
        
        generateRequirementsIfNeeded(player);
    }
    
    private static void generateRequirementsIfNeeded(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElse(null);
        if (progression == null) {
            LOGGER.warn("Player {} missing progression capability when generating tier requirements", 
                player.getName().getString());
            return;
        }
        
        int nextTier = progression.getTier() + 1;
        if (nextTier < 3 || nextTier > 5) {
            return;
        }
        
        ICovenCapability covenCap = player.getCapability(CovenProvider.COVEN)
                .orElse(null);
        if (covenCap == null) {
            LOGGER.error("Player {} missing coven capability when generating tier requirements", 
                player.getName().getString());
            return;
        }
        
        if (covenCap.getTierEffectsRequired(nextTier) == null) {
            var requirements = TierEffectManager.generateTierRequirements(nextTier, player.getRandom(), player.level());
            covenCap.setTierEffectsRequired(nextTier, requirements);
        }
    }
}
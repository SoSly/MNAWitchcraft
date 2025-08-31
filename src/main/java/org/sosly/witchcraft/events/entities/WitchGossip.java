package org.sosly.witchcraft.events.entities;

import com.mojang.logging.LogUtils;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.sosly.witchcraft.config.ServerConfig;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.factions.FactionRegistry;
import org.sosly.witchcraft.utils.GossipMessageGenerator;
import org.sosly.witchcraft.utils.ScentedItemHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WitchGossip {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LAST_GOSSIP_TIME_KEY = "mnaw:last_gossip_time";

    @SubscribeEvent
    public static void onWitchUpdate(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Witch witch)) {
            return;
        }
        
        Level level = witch.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        if (witch.tickCount % 20 != 0) {
            return;
        }
        
        long currentTime = level.getGameTime();
        long lastGossipTime = witch.getPersistentData().getLong(LAST_GOSSIP_TIME_KEY);
        int cooldownTicks = ServerConfig.witchGossipCooldown * 20;
        
        if (lastGossipTime != 0 && currentTime - lastGossipTime < cooldownTicks) {
            return;
        }
        
        if (witch.getTarget() != null) {
            LOGGER.debug("Witch has target {}, skipping gossip", witch.getTarget().getName().getString());
            return;
        }
        
        double attackRange = getWitchAttackRange(witch);
        double gossipRange = ServerConfig.witchGossipDistance;
        AABB gossipBox = witch.getBoundingBox().inflate(gossipRange);
        List<Player> playersInRange = level.getEntitiesOfClass(Player.class, gossipBox);
        
        List<Player> niceSmellPlayers = playersInRange.stream()
                .filter(p -> p.hasEffect(EffectRegistry.NICE_SMELL.get()))
                .filter(p -> witch.distanceToSqr(p) > attackRange * attackRange)
                .filter(p -> isValidGossipTrigger(p))
                .toList();
        
        LOGGER.debug("Found {} players in range, {} with nice smell effect eligible for gossip", playersInRange.size(), niceSmellPlayers.size());
        
        if (niceSmellPlayers.isEmpty()) {
            return;
        }
        
        Player targetPlayer = niceSmellPlayers.get(witch.getRandom().nextInt(niceSmellPlayers.size()));
        
        String scentedItem = ScentedItemHelper.getRandomCarriedScentedItem(targetPlayer);
        if (scentedItem == null) {
            LOGGER.debug("Target player {} has no scented items, cannot gossip", targetPlayer.getName().getString());
            return;
        }
        
        LOGGER.debug("Witch gossiping with {} about {}", targetPlayer.getName().getString(), scentedItem);
        
        performGossip(serverLevel, witch, targetPlayer, scentedItem, playersInRange);
        witch.getPersistentData().putLong(LAST_GOSSIP_TIME_KEY, currentTime);
        LOGGER.debug("Gossip completed, cooldown set for {} ticks", cooldownTicks);
    }
    
    private static double getWitchAttackRange(Witch witch) {
        // TODO: Extract actual attack range from RangedAttackGoal using reflection or access transformer
        return 10.0;
    }
    
    private static boolean isValidGossipTrigger(Player player) {
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            LOGGER.warn("Player {} missing progression capability during gossip validation", player.getName().getString());
            return true;
        }
        
        IFaction faction = progression.getAlliedFaction();
        if (faction == null) {
            return true;
        }
        
        return faction == FactionRegistry.COVEN || faction == FactionRegistry.DARK_COVEN;
    }
    
    private static void performGossip(ServerLevel level, Witch witch, Player targetPlayer, String scentedItem, List<Player> playersInRange) {
        Component gossipMessage = GossipMessageGenerator.generateGossipMessage(targetPlayer, scentedItem);
        Component witchName = Component.translatable("entity.minecraft.witch");
        Component formattedMessage = Component.literal("<")
                .append(witchName)
                .append("> ")
                .append(gossipMessage);
        
        for (Player player : playersInRange) {
            player.sendSystemMessage(formattedMessage);
        }
    }
}
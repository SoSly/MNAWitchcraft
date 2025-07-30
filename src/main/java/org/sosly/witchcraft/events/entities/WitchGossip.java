package org.sosly.witchcraft.events.entities;

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
import org.sosly.witchcraft.Config;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.factions.FactionRegistry;
import org.sosly.witchcraft.utils.GossipMessageGenerator;
import org.sosly.witchcraft.utils.ScentedItemHelper;

import java.util.List;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WitchGossip {
    // NBT key for storing last gossip time
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
        
        // Only check every second
        if (witch.tickCount % 20 != 0) {
            return;
        }
        
        // Check if witch is on cooldown
        long currentTime = level.getGameTime();
        long lastGossipTime = witch.getPersistentData().getLong(LAST_GOSSIP_TIME_KEY);
        
        // Convert config seconds to ticks
        int cooldownTicks = Config.witchGossipCooldown * 20;
        
        if (lastGossipTime != 0 && currentTime - lastGossipTime < cooldownTicks) {
            return;
        }
        
        // If witch has a target, don't gossip
        if (witch.getTarget() != null) {
            return;
        }
        
        // Get the witch's attack range
        double attackRange = getWitchAttackRange(witch);
        
        // Find players in gossip range (configured distance)
        double gossipRange = Config.witchGossipDistance;
        AABB gossipBox = witch.getBoundingBox().inflate(gossipRange);
        List<Player> playersInRange = level.getEntitiesOfClass(Player.class, gossipBox);
        
        // Filter for players with nice smell effect and not in attack range
        // Only trigger gossip for players with no faction or in Coven/Dark Coven
        List<Player> niceSmellPlayers = playersInRange.stream()
                .filter(p -> p.hasEffect(EffectRegistry.NICE_SMELL.get()))
                .filter(p -> witch.distanceToSqr(p) > attackRange * attackRange)
                .filter(p -> isValidGossipTrigger(p))
                .toList();
        
        if (niceSmellPlayers.isEmpty()) {
            return;
        }
        
        // Pick a random player to gossip about
        Player targetPlayer = niceSmellPlayers.get(witch.getRandom().nextInt(niceSmellPlayers.size()));
        
        // Get the scented item they're carrying
        String scentedItem = ScentedItemHelper.getRandomCarriedScentedItem(targetPlayer);
        if (scentedItem == null) {
            return; // Shouldn't happen if they have the effect, but just in case
        }
        
        // Trigger gossip
        performGossip(serverLevel, witch, targetPlayer, scentedItem, playersInRange);
        
        // Set cooldown in persistent data
        witch.getPersistentData().putLong(LAST_GOSSIP_TIME_KEY, currentTime);
    }
    
    private static double getWitchAttackRange(Witch witch) {
        // TODO: Extract actual attack range from RangedAttackGoal using reflection or access transformer
        return 10.0; // Vanilla witch default
    }
    
    private static boolean isValidGossipTrigger(Player player) {
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null) {
            return true; // No progression capability, allow gossip
        }
        
        IFaction faction = progression.getAlliedFaction();
        if (faction == null) {
            return true; // No faction, allow gossip
        }
        
        // Only trigger gossip for Coven or Dark Coven members (or no faction)
        // Use the same check as FactionRegistry.isWitch
        return faction == FactionRegistry.COVEN || faction == FactionRegistry.DARK_COVEN;
    }
    
    private static void performGossip(ServerLevel level, Witch witch, Player targetPlayer, String scentedItem, List<Player> playersInRange) {
        // Generate the gossip message based on the target player
        Component gossipMessage = GossipMessageGenerator.generateGossipMessage(targetPlayer, scentedItem);
        
        // Format the message to show it's from a witch
        Component witchName = Component.translatable("entity.minecraft.witch");
        Component formattedMessage = Component.literal("<")
                .append(witchName)
                .append("> ")
                .append(gossipMessage);
        
        // Send to all players in gossip range
        for (Player player : playersInRange) {
            player.sendSystemMessage(formattedMessage);
        }
    }
}
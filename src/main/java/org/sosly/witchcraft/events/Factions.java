package org.sosly.witchcraft.events;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.api.capabilities.IMoonthreadArmorData;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.capabilities.armor.MoonthreadArmorProvider;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.items.sympathy.BoundPoppetItem;
import org.sosly.witchcraft.utils.TierEffectManager;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Factions {
    private static final Logger LOGGER = LogManager.getLogger(Factions.class);
    
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<?> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ICovenCapability.COVEN_CAPABILITY, new CovenProvider());
            event.addCapability(IMoonthreadArmorData.MOONTHREAD_ARMOR_DATA, new MoonthreadArmorProvider());
        }
    }
    
    /**
     * Detects when a player crafts a bound poppet
     */
    @SubscribeEvent
    public static void onPoppetCraft(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();
        
        if (!(craftedItem.getItem() instanceof BoundPoppetItem)) {
            return;
        }
        
        generateRequirementsIfNeeded(player);
    }
    
    /**
     * Detects when a player uses a needle
     */
    @SubscribeEvent
    public static void onNeedleUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        
        if (heldItem.getItem() != ItemRegistry.BLOODY_NEEDLE.get() &&
            heldItem.getItem() != ItemInit.VINTEUM_NEEDLE.get()) {
            return;
        }
        
        generateRequirementsIfNeeded(player);
    }
    
    /**
     * Generates tier requirements for the player if they don't already have them
     * @param player the player to generate requirements for
     */
    private static void generateRequirementsIfNeeded(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .orElse(null);
        if (progression == null) {
            return;
        }
        
        // Only generate for tiers 3-5
        int nextTier = progression.getTier() + 1;
        if (nextTier < 3 || nextTier > 5) {
            return;
        }
        
        ICovenCapability covenCap = player.getCapability(CovenProvider.COVEN)
                .orElse(null);
        if (covenCap == null) {
            return;
        }
        
        // Generate requirements for the next tier if not already set
        if (covenCap.getTierEffectsRequired(nextTier) == null) {
            var requirements = TierEffectManager.generateTierRequirements(nextTier, player.getRandom(), player.level());
            covenCap.setTierEffectsRequired(nextTier, requirements);
            LOGGER.info("Generated tier {} requirements for player {}: {}", 
                nextTier, player.getName().getString(), requirements);
        }
    }
}

package org.sosly.witchcraft.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScentedItemHelper {
    private static final Map<ResourceLocation, String> SCENTED_ITEMS = new HashMap<>();
    
    static {
        // M&A flowers
        SCENTED_ITEMS.put(new ResourceLocation("mna", "wakebloom"), "wakebloom");
        SCENTED_ITEMS.put(new ResourceLocation("mna", "aum"), "aum");
        SCENTED_ITEMS.put(new ResourceLocation("mna", "cerublossom"), "cerublossom");
        SCENTED_ITEMS.put(new ResourceLocation("mna", "desert_nova"), "desert_nova");
        SCENTED_ITEMS.put(new ResourceLocation("mna", "tarma_root"), "tarma_root");
        
        // Future herbs can be added here
    }
    
    /**
     * Gets all scented items (flowers/herbs) carried by the player
     * @param player The player to check
     * @return A list of scented item type names the player is carrying
     */
    public static List<String> getCarriedScentedItems(Player player) {
        Set<String> scentedItems = new HashSet<>();
        
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            
            String itemType = getScentedItemType(stack.getItem());
            if (itemType != null) {
                scentedItems.add(itemType);
            }
        }
        
        // Check offhand
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            String itemType = getScentedItemType(offhand.getItem());
            if (itemType != null) {
                scentedItems.add(itemType);
            }
        }
        
        return new ArrayList<>(scentedItems);
    }
    
    /**
     * Checks if the player is carrying any scented item
     * @param player The player to check
     * @return true if the player has at least one scented item
     */
    public static boolean hasAnyScentedItem(Player player) {
        return !getCarriedScentedItems(player).isEmpty();
    }
    
    /**
     * Gets a random scented item type from those the player is carrying
     * @param player The player to check
     * @return A random scented item type name, or null if none carried
     */
    public static String getRandomCarriedScentedItem(Player player) {
        List<String> items = getCarriedScentedItems(player);
        if (items.isEmpty()) {
            return null;
        }
        return items.get(player.getRandom().nextInt(items.size()));
    }
    
    /**
     * Gets the scented item type for a given item
     * @param item The item to check
     * @return The scented item type name, or null if not a known scented item
     */
    private static String getScentedItemType(Item item) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return SCENTED_ITEMS.get(itemId);
    }
    
    /**
     * Gets the Item object for a scented item type
     * @param itemType The scented item type identifier
     * @return The Item, or null if not found
     */
    public static Item getScentedItem(String itemType) {
        for (Map.Entry<ResourceLocation, String> entry : SCENTED_ITEMS.entrySet()) {
            if (entry.getValue().equals(itemType)) {
                return ForgeRegistries.ITEMS.getValue(entry.getKey());
            }
        }
        return null;
    }
    
    /**
     * Gets the localized name key for a scented item type
     * @param itemType The scented item type identifier
     * @return The translation key for the item name
     */
    public static String getScentedItemTranslationKey(String itemType) {
        Item item = getScentedItem(itemType);
        return item != null ? item.getDescriptionId() : "item.unknown";
    }
}
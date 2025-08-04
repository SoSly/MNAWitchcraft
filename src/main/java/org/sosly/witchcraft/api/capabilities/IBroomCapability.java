package org.sosly.witchcraft.api.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.sosly.witchcraft.Witchcraft;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * IBroomCapability tracks a player's bonded broom data.
 * Each player can only have one bonded broom at a time.
 */
public interface IBroomCapability {
    ResourceLocation BROOM_CAPABILITY = new ResourceLocation(Witchcraft.MOD_ID, "broom");

    enum BrushTier {
        OVERWORLD(1, "Overworld only"),
        NETHER(2, "Overworld + Nether"),
        UNIVERSAL(3, "All dimensions");

        private final int tier;
        private final String description;

        BrushTier(int tier, String description) {
            this.tier = tier;
            this.description = description;
        }

        public int getTier() {
            return tier;
        }

        public String getDescription() {
            return description;
        }

        public static BrushTier fromTier(int tier) {
            return switch (tier) {
                case 2 -> NETHER;
                case 3 -> UNIVERSAL;
                default -> OVERWORLD;
            };
        }
    }

    enum StorageLevel {
        NONE(0, 0),
        SMALL(1, 9),
        MEDIUM(2, 18),
        LARGE(3, 27);

        private final int level;
        private final int slots;

        StorageLevel(int level, int slots) {
            this.level = level;
            this.slots = slots;
        }

        public int getLevel() {
            return level;
        }

        public int getSlots() {
            return slots;
        }

        public static StorageLevel fromLevel(int level) {
            return switch (level) {
                case 1 -> SMALL;
                case 2 -> MEDIUM;
                case 3 -> LARGE;
                default -> NONE;
            };
        }
    }

    /**
     * @return the UUID of the bonded broom entity, or null if no broom is bonded
     */
    @Nullable
    UUID getBroomEntityId();

    /**
     * @param entityId the UUID of the broom entity to bond, or null to unbond
     */
    void setBroomEntityId(@Nullable UUID entityId);

    /**
     * @return the brush tier determining dimension access
     */
    BrushTier getBrushTier();

    /**
     * @param tier the brush tier
     */
    void setBrushTier(BrushTier tier);

    /**
     * @return the handle wood type as a ResourceLocation (e.g., "minecraft:oak")
     */
    ResourceLocation getHandleWood();

    /**
     * @param wood the handle wood type ResourceLocation
     */
    void setHandleWood(ResourceLocation wood);

    /**
     * @return the ribbon color as an int (follows vanilla dye color format)
     */
    int getRibbonColor();

    /**
     * @param color the ribbon color int
     */
    void setRibbonColor(int color);

    /**
     * @return the storage level for future inventory expansion
     */
    StorageLevel getStorageLevel();

    /**
     * @param level the storage level
     */
    void setStorageLevel(StorageLevel level);

    /**
     * Gets the ItemStackHandler for charm slots
     * @return the charm slots handler (3 slots)
     */
    ItemStackHandler getCharmHandler();

    /**
     * Gets the ItemStackHandler for storage inventory
     * @return the storage inventory handler (size depends on storage level)
     */
    ItemStackHandler getStorageHandler();

    /**
     * @return true if a broom is currently bonded to this player
     */
    boolean hasBondedBroom();

    /**
     * Clears all broom data, unbonding any existing broom
     */
    void clearBroomData();
}
package org.sosly.witchcraft.capabilities.broom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;

import javax.annotation.Nullable;
import java.util.UUID;

public class BroomCapability implements IBroomCapability {
    @Nullable
    private UUID broomEntityId = null;
    private BrushTier brushTier = BrushTier.OVERWORLD;
    private ResourceLocation handleWood = new ResourceLocation("minecraft", "oak");
    private int ribbonColor = 0xFFFFFF;
    private StorageLevel storageLevel = StorageLevel.NONE;
    private final ItemStackHandler charmSlots = new ItemStackHandler(3);
    private ItemStackHandler storageInventory = new ItemStackHandler(0);

    @Override
    @Nullable
    public UUID getBroomEntityId() {
        return broomEntityId;
    }

    @Override
    public void setBroomEntityId(@Nullable UUID entityId) {
        this.broomEntityId = entityId;
    }

    @Override
    public BrushTier getBrushTier() {
        return brushTier;
    }

    @Override
    public void setBrushTier(BrushTier tier) {
        this.brushTier = tier;
    }

    @Override
    public ResourceLocation getHandleWood() {
        return handleWood;
    }

    @Override
    public void setHandleWood(ResourceLocation wood) {
        this.handleWood = wood;
    }

    @Override
    public int getRibbonColor() {
        return ribbonColor;
    }

    @Override
    public void setRibbonColor(int color) {
        this.ribbonColor = color;
    }

    @Override
    public StorageLevel getStorageLevel() {
        return storageLevel;
    }

    @Override
    public void setStorageLevel(StorageLevel level) {
        this.storageLevel = level;
        updateStorageSize();
    }

    @Override
    public ItemStackHandler getCharmHandler() {
        return charmSlots;
    }

    @Override
    public ItemStackHandler getStorageHandler() {
        return storageInventory;
    }

    @Override
    public boolean hasBondedBroom() {
        return broomEntityId != null;
    }

    @Override
    public void clearBroomData() {
        broomEntityId = null;
        brushTier = BrushTier.OVERWORLD;
        handleWood = new ResourceLocation("minecraft", "oak");
        ribbonColor = 0xFFFFFF;
        storageLevel = StorageLevel.NONE;
        for (int i = 0; i < 3; i++) {
            charmSlots.setStackInSlot(i, ItemStack.EMPTY);
        }
        storageInventory = new ItemStackHandler(0);
    }

    private void updateStorageSize() {
        int newSize = storageLevel.getSlots();
        if (newSize != storageInventory.getSlots()) {
            ItemStackHandler newInventory = new ItemStackHandler(newSize);
            for (int i = 0; i < Math.min(storageInventory.getSlots(), newSize); i++) {
                newInventory.setStackInSlot(i, storageInventory.getStackInSlot(i));
            }
            storageInventory = newInventory;
        }
    }
}
package org.sosly.witchcraft.guis.containers;

import com.mna.gui.containers.slots.SlotNoPickup;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.sosly.witchcraft.guis.ContainerRegistry;
import org.sosly.witchcraft.guis.slots.PotionSlot;
import org.sosly.witchcraft.inventories.PotionPouchInventory;

public class PotionPouchContainer extends AbstractPlayerInventoryContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private PotionPouchInventory inventory;
    private final boolean isClientside;

    public PotionPouchContainer(int i, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(i, playerInventory, new PotionPouchInventory(playerInventory.getItem(playerInventory.selected)));
    }

    public PotionPouchContainer(int windowId, Inventory playerInventory, PotionPouchInventory potionPouchInventory) {
        super(ContainerRegistry.POTION_POUCH.get(), windowId);
        this.isClientside = playerInventory.player.level().isClientSide();
        inventory = potionPouchInventory;
        this.initializeSlots(playerInventory);
    }

    @Override
    public void broadcastChanges() {
        if (this.isClientside) {
            super.broadcastChanges();
            return;
        }
        
        inventory.writeItemStack();
        super.broadcastChanges();
    }

    public PotionPouchInventory getPotionPouchInventory() {
        return inventory;
    }

    @Override
    public int getSlotCount() {
        return inventory.getSlots();
    }

    public int getSlotSize(int idx, ItemStack stack) {
        return inventory.getStackLimit(idx, stack);
    }

    protected void initializeSlots(Inventory playerInventory) {
        int slotIndex = 0;

        int row = 0;
        int col;
        for (col = 0; col < 5; ++col) {
            int slotX = 40 + col * 18;
            int slotY = 102 + row * 18;
            this.addSlot(new PotionSlot(inventory, slotIndex++, slotX, slotY));
        }

        for (col = 0; col < 5; ++col) {
            int slotX = 40 + col * 18;
            int slotY = 102 - (32 * (row + 1));
            this.addSlot(new SlotNoPickup(inventory, slotIndex++, slotX, slotY));
        }

        if (inventory.getPouch().getOrCreateTag().getInt("conveyance") != 1) {
            super.initializeSlots(playerInventory, slotIndex);
            return;
        }
        
        int slotX = 40 + (5 * 18) + 14;
        int slotY = 102 - (32 * (row + 1));
        this.addSlot(new SlotItemHandler(inventory, slotIndex++, slotX, slotY));
        super.initializeSlots(playerInventory, slotIndex);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}

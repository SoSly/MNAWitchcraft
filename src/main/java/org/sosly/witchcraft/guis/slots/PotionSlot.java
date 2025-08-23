package org.sosly.witchcraft.guis.slots;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.inventories.PotionPouchInventory;

public class PotionSlot extends SlotItemHandler {
    PotionPouchInventory inventory;
    public PotionSlot(PotionPouchInventory itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.inventory = itemHandler;
    }

    @Override
    public int getMaxStackSize() {
        return inventory.getSlotLimit(getSlotIndex());
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            return inventory.getSlotLimit(getSlotIndex());
        }
        
        return super.getMaxStackSize(stack);
    }
}

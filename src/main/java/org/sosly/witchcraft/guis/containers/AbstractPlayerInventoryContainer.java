package org.sosly.witchcraft.guis.containers;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractPlayerInventoryContainer extends AbstractContainerMenu {
    protected AbstractPlayerInventoryContainer(@Nullable MenuType<?> menuType, int container) {
        super(menuType, container);
    }

    void initializeSlots(Inventory playerInventory, int slotIndex) {
        int col;
        for (int row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                int slot = col + row * 9 + 9;
                int slotX = 48 + col * 18;
                int slotY = 129 + row * 18;

                this.addSlot(new Slot(playerInventory, slot, slotX, slotY));

                ++slotIndex;
            }
        }

        for(col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 187));
            ++slotIndex;
        }
    }

    abstract int getSlotCount();
    abstract int getSlotSize(int idx, ItemStack stack);

    private boolean isStackable(int slot, ItemStack stack) {
        if (slot < getSlotCount()) {
            return getSlotSize(slot, stack) > 1;
        }
        return stack.isStackable();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int idx) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(idx);
        int playerSlots = getSlotCount();
        if (!slot.hasItem()) {
            return itemstack;
        }
        
        ItemStack slotStack = slot.getItem();
        itemstack = slotStack.copy();
        if (idx < playerSlots) {
            if (!this.moveItemStackTo(slotStack, playerSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(slotStack, 0, playerSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return itemstack;
    }

    @Override
    protected boolean moveItemStackTo(@NotNull ItemStack stack, int startIdx, int endIdx, boolean reverse) {
        boolean flag = false;
        int i = startIdx;
        if (reverse) {
            i = endIdx - 1;
        }

        if (isStackable(startIdx, stack)) {
            while(!stack.isEmpty()) {
                if (reverse && i < startIdx) {
                    break;
                }
                
                if (!reverse && i >= endIdx) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = reverse ? Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) : slot.getMaxStackSize();
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverse) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverse) {
                i = endIdx - 1;
            } else {
                i = startIdx;
            }

            while(true) {
                if (reverse && i < startIdx) {
                    break;
                }
                
                if (!reverse && i >= endIdx) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    int maxStackSize = reverse ? Math.min(slot1.getMaxStackSize(), stack.getMaxStackSize()) : slot1.getMaxStackSize();
                    if (stack.getCount() > maxStackSize) {
                        slot1.setByPlayer(stack.split(maxStackSize));
                    } else {
                        slot1.setByPlayer(stack.split(stack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverse) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }
}

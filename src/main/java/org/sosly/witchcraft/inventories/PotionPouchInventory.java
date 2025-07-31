package org.sosly.witchcraft.inventories;

import com.mna.items.ItemInit;
import com.mna.items.ritual.PlayerCharm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.items.alchemy.PotionPouchItem;

import java.util.UUID;

public class PotionPouchInventory extends ItemStackHandler {
    protected final ItemStack pouch;
    protected static int potionSlots = 5;
    protected static int patchSlots = 5;

    protected final int depthPatchSlot = 5;
    protected final int conveyancePatchSlot = 6;
    protected final int pocketPatchSlot = 7;
    protected final int speedPatchSlot = 8;
    protected final int collectionPatchSlot = 9;
    protected final int conveyanceContentSlot = 10;

    public PotionPouchInventory(ItemStack item) {
        super(getSlots(item));
        this.pouch = item;
        readItemStack();
    }

    public ItemStack getPouch() {
        return pouch;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < potionSlots) {
            return PotionPouchItem.getPotionDepth(pouch);
        }
        return 64;
    }

    @Override
    public int getSlots() {
        return getSlots(pouch);
    }

    public static int getSlots(ItemStack pouch) {
        int slots = potionSlots + patchSlots;
        if (pouch.getOrCreateTag().getInt("conveyance") == 1) {
            slots++;
        }
        return slots;
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        if (slot < potionSlots) {
            return super.getStackInSlot(slot);
        }
        if (slot == depthPatchSlot && pouch.getOrCreateTag().contains("depth")) {
            switch (pouch.getOrCreateTag().getInt("depth")) {
                case 1:
                    return new ItemStack(ItemInit.PATCH_DEPTH.get());
                case 2:
                    return new ItemStack(ItemInit.PATCH_DEPTH_2.get());
            }
        }
        if (slot == conveyancePatchSlot && pouch.getOrCreateTag().contains("conveyance")) {
            return new ItemStack(ItemInit.PATCH_CONVEYANCE.get());
        }
//        if (slot == pocketPatchSlot && pouch.getOrCreateTag().contains("pocket")) {
//            return new ItemStack(ItemInit.PATCH_POCKET.get());
//        }
        if (slot == speedPatchSlot && pouch.getOrCreateTag().contains("speed")) {
            switch (pouch.getOrCreateTag().getInt("speed")) {
                case 1:
                    return new ItemStack(ItemInit.PATCH_SPEED.get());
                case 2:
                    return new ItemStack(ItemInit.PATCH_SPEED_2.get());
                case 3:
                    return new ItemStack(ItemInit.PATCH_SPEED_3.get());
            }
        }
        if (slot == collectionPatchSlot && pouch.getOrCreateTag().contains("collection")) {
            return new ItemStack(ItemInit.PATCH_COLLECTION.get());
        }

        if (slot == conveyanceContentSlot) {
            return super.getStackInSlot(slot);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot < potionSlots) {
            super.setStackInSlot(slot, stack);
            writeItemStack();
        }
        if (slot == conveyanceContentSlot) {
            if (stack.isEmpty()) {
                pouch.getOrCreateTag().remove("conveyance_target");
            } else {
                UUID target = ((PlayerCharm)stack.getItem()).getPlayerUUID(stack);
                if (target != null) {
                    pouch.getOrCreateTag().putUUID("conveyance_target", target);
                }
            }

            super.setStackInSlot(slot, stack);
            writeItemStack();
        }
    }

    @Override
    public int getStackLimit(int slot, @NotNull ItemStack stack) {
        if (slot < potionSlots) {
            return PotionPouchItem.getPotionDepth(pouch);
        }
        return stack.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack itemStack) {
        if (slot < potionSlots) {
            return PotionUtils.getPotion(itemStack) != Potions.EMPTY;
        }
        if (slot == conveyanceContentSlot) {
            return itemStack.getItem() instanceof PlayerCharm;
        }
        return false;
    }

    public void readItemStack() {
        if (pouch.getTag() != null && pouch.getTag().contains("contents")) {
            this.deserializeNBT(pouch.getTag().getCompound("contents"));
        }
    }

    @Override
    public void setSize(int size) {
        super.setSize(getSlots());
    }

    public void writeItemStack() {
        pouch.getOrCreateTag().put("contents", serializeNBT());
    }
}

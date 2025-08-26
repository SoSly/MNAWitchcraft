package org.sosly.witchcraft.recipes;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.items.tools.FlyingBroom;

public class FlyingBroomDyeingRecipe extends CustomRecipe {
    
    public FlyingBroomDyeingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack broomStack = ItemStack.EMPTY;
        ItemStack dyeStack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof FlyingBroom) {
                if (!broomStack.isEmpty()) {
                    return false;
                }
                broomStack = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                if (!dyeStack.isEmpty()) {
                    return false;
                }
                dyeStack = stack;
            } else {
                return false;
            }
        }

        return !broomStack.isEmpty() && !dyeStack.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack broomStack = ItemStack.EMPTY;
        DyeColor dyeColor = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof FlyingBroom) {
                broomStack = stack;
            } else if (stack.getItem() instanceof DyeItem dyeItem) {
                dyeColor = dyeItem.getDyeColor();
            }
        }

        if (broomStack.isEmpty() || dyeColor == null) {
            return ItemStack.EMPTY;
        }

        return FlyingBroom.dyeBroom(broomStack, dyeColor);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.FLYING_BROOM_DYEING.get();
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < remainingItems.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.hasCraftingRemainingItem()) {
                remainingItems.set(i, stack.getCraftingRemainingItem());
            }
        }

        return remainingItems;
    }
}
package org.sosly.witchcraft.recipes;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.items.tools.FlyingBroom;

public class FlyingBroomHandleWoodRecipe extends CustomRecipe {
    
    public FlyingBroomHandleWoodRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack broomStack = ItemStack.EMPTY;
        ItemStack logStack = ItemStack.EMPTY;

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
            } else if (stack.getItem() instanceof BlockItem blockItem && isStrippedLog(blockItem.getBlock())) {
                if (!logStack.isEmpty()) {
                    return false;
                }
                logStack = stack;
            } else {
                return false;
            }
        }

        return !broomStack.isEmpty() && !logStack.isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack broomStack = ItemStack.EMPTY;
        Block strippedLog = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof FlyingBroom) {
                broomStack = stack;
            } else if (stack.getItem() instanceof BlockItem blockItem && isStrippedLog(blockItem.getBlock())) {
                strippedLog = blockItem.getBlock();
            }
        }

        if (broomStack.isEmpty() || strippedLog == null) {
            return ItemStack.EMPTY;
        }

        return FlyingBroom.changeHandleWood(broomStack, strippedLog);
    }

    private boolean isStrippedLog(Block block) {
        ResourceLocation blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) {
            return false;
        }
        
        String blockPath = blockId.getPath();
        return (blockPath.startsWith("stripped_") && 
                (blockPath.endsWith("_log") || blockPath.endsWith("_stem"))) ||
               blockPath.equals("stripped_bamboo_block");
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.FLYING_BROOM_HANDLE_WOOD.get();
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
package org.sosly.witchcraft.compat;

import com.mysticalchemy.crucible.CrucibleTile;
import com.mysticalchemy.init.BlockInit;
import com.mysticalchemy.init.RecipeInit;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.sosly.witchcraft.items.alchemy.WitchEyeItem.createDummyCraftingInventory;

/**
 * Compatibility class for Mystic Alchemy integration.
 * This class is only loaded when Mystic Alchemy interactions are actually needed,
 * preventing early class loading issues.
 */
public class MysticAlchemyCompat {
    
    public static void revealAlchemicalProperties(Level level, ItemStack item, List<Component> tooltips) {
        var recipes = level.getRecipeManager();
        Optional<PotionIngredientRecipe> recipe = recipes.getRecipeFor(
            RecipeInit.POTION_RECIPE_TYPE.get(), 
            createDummyCraftingInventory(item), 
            level
        );
        
        if (recipe.isEmpty()) {
            return;
        }

        tooltips.add(Component.translatable("item.mnaw.witch_eye/tooltip")
                .withStyle(ChatFormatting.LIGHT_PURPLE)
                .withStyle(ChatFormatting.ITALIC));

        Map<MobEffect, Float> effects = recipe.get().getEffects();
        
        // Debug: Check if effects map is empty
        if (effects.isEmpty()) {
            tooltips.add(Component.literal("  [Debug: Recipe found but no effects loaded]")
                    .withStyle(ChatFormatting.RED));
            tooltips.add(Component.literal("  [This suggests MA integration hasn't initialized]")
                    .withStyle(ChatFormatting.RED));
        } else {
            effects.forEach((effect, value) -> {
                MutableComponent component = Component.literal("  - ");
                component.append(effect.getDisplayName());
                component.append(": ");
                component.append(String.valueOf(value));
                component.withStyle(ChatFormatting.LIGHT_PURPLE);
                tooltips.add(component);
            });
        }
    }
    
    public static CrucibleTile getCrucibleTile(Level level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof CrucibleTile crucible) {
            return crucible;
        }
        return null;
    }
    
    public static BlockState getEmptyCrucibleState() {
        return BlockInit.EMPTY_CRUCIBLE.get().defaultBlockState();
    }
    
    public static Map<MobEffect, Float> getProminentEffects(CrucibleTile crucible) {
        return crucible.getProminentEffects();
    }
}
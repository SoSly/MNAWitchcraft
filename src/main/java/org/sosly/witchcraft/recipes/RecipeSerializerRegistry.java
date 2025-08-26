package org.sosly.witchcraft.recipes;

import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;

public class RecipeSerializerRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Witchcraft.MOD_ID);
    
    public static final RegistryObject<RecipeSerializer<FlyingBroomDyeingRecipe>> FLYING_BROOM_DYEING = 
        SERIALIZERS.register("flying_broom_dyeing", 
            () -> new SimpleCraftingRecipeSerializer<>(
                (id, category) -> new FlyingBroomDyeingRecipe(id, CraftingBookCategory.EQUIPMENT)
            )
        );
    
    public static final RegistryObject<RecipeSerializer<FlyingBroomHandleWoodRecipe>> FLYING_BROOM_HANDLE_WOOD = 
        SERIALIZERS.register("flying_broom_handle_wood", 
            () -> new SimpleCraftingRecipeSerializer<>(
                (id, category) -> new FlyingBroomHandleWoodRecipe(id, CraftingBookCategory.EQUIPMENT)
            )
        );
}
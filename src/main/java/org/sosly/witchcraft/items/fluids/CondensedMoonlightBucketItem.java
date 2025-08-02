package org.sosly.witchcraft.items.fluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.fluids.FluidRegistry;

import java.util.function.Supplier;

public class CondensedMoonlightBucketItem extends BucketItem {
    
    public CondensedMoonlightBucketItem(Supplier<? extends Fluid> supplier, Properties properties) {
        super(supplier, properties.craftRemainder(Items.BUCKET).stacksTo(1));
    }
}
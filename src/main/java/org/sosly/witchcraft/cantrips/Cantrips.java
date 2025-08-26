package org.sosly.witchcraft.cantrips;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.cantrips.CantripRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.sosly.witchcraft.Witchcraft;

import java.util.Objects;

public class Cantrips {
    private static final ResourceLocation BOLT = new ResourceLocation("mna", "manaweave_patterns/bolt");
    private static final ResourceLocation CIRCLE = new ResourceLocation("mna", "manaweave_patterns/circle");
    private static final ResourceLocation INVERSE_TRIANGLE = new ResourceLocation("mna", "manaweave_patterns/inverted_triangle");

    public static void registerCantrips() {
        Objects.requireNonNull(CantripRegistry.INSTANCE.registerCantrip(SummonBroom.ID, SummonBroom.ICON, SummonBroom.TIER, SummonBroom::execute,
                        ItemStack.EMPTY, INVERSE_TRIANGLE, CIRCLE, BOLT))
                .setRequiredAdvancement(SummonBroom.ADVANCEMENT);
    }
}
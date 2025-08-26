package org.sosly.witchcraft.renderers.items;

import com.mna.items.renderers.ItemSpellBookRenderer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import org.sosly.witchcraft.Witchcraft;

public class CovenGrimoireBookRenderer extends ItemSpellBookRenderer {
    public CovenGrimoireBookRenderer(BlockEntityRenderDispatcher berd, EntityModelSet ems) {
        super(berd, ems, 
            new ResourceLocation(Witchcraft.MOD_ID, "item/grimoire_witch_open"), 
            new ResourceLocation(Witchcraft.MOD_ID, "item/grimoire_witch_closed"), 
            true);
    }
}